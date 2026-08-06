package com.aoaammopouch.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.aoaammopouch.inventory.AmmoPouchInventory;
import com.aoaammopouch.item.ItemAmmoPouch;
import com.aoaammopouch.util.AmmoPouchHelper;


public class ContainerAmmoPouch extends Container {

    private final AmmoPouchInventory pouchInventory;
    private final ItemStack pouchStack;
    private final int lockedHotbarSlot;
    private final AmmoPouchLayout layout;

    public ContainerAmmoPouch(InventoryPlayer playerInventory, ItemStack pouchStack, int selectedSlot, boolean offHand) {
        this.pouchStack = pouchStack;
        this.pouchInventory = new AmmoPouchInventory(pouchStack);
        this.lockedHotbarSlot = offHand ? -1 : selectedSlot;
        this.layout = AmmoPouchLayout.create(this.pouchInventory.getSizeInventory());

        for (int slot = 0; slot < this.pouchInventory.getSizeInventory(); slot++) {
            addSlotToContainer(new SlotAmmo(this.pouchInventory, this.pouchStack, slot, this.layout.getSlotX(slot), this.layout.getSlotY(slot)));
        }

        int inventoryStartX = AmmoPouchLayout.SIDE_PADDING + 1;
        int inventoryStartY = AmmoPouchLayout.PLAYER_INVENTORY_OFFSET_Y;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotIndex = col + row * 9 + 9;
                addSlotToContainer(new Slot(playerInventory, slotIndex, inventoryStartX + col * 18, inventoryStartY + row * 18));
            }
        }

        int hotbarY = AmmoPouchLayout.HOTBAR_OFFSET_Y;

        for (int col = 0; col < 9; col++) {
            if (col == this.lockedHotbarSlot) {
                addSlotToContainer(new LockedSlot(playerInventory, col, inventoryStartX + col * 18, hotbarY));
            }
            else {
                addSlotToContainer(new Slot(playerInventory, col, inventoryStartX + col * 18, hotbarY));
            }
        }
    }

    public AmmoPouchLayout getLayout() {
        return this.layout;
    }

    public int getPouchSlotCount() {
        return this.pouchInventory.getSizeInventory();
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return !this.pouchStack.isEmpty() && this.pouchStack.getItem() instanceof ItemAmmoPouch;
    }

    // TODO: Should we impose a uniqueness constraint on the content?
    //       This is more in line with the spirit of the pouch, although the
    //       free slots may be maddening to some.
    //       Uniqueness would also allow Map-based indexing, which is more efficient.
    //       Would also prevent ammos to leak into other slots when a slot is full.
    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
        if (isLockedSlot(slotId)) return ItemStack.EMPTY;

        if (clickTypeIn == ClickType.PICKUP && isPouchSlot(slotId)) {
            Slot slot = this.inventorySlots.get(slotId);
            ItemStack slotStack = slot.getStack();
            ItemStack carriedStack = player.inventory.getItemStack();

            if (canMergeIntoPouchSlot(slot, slotStack, carriedStack)) {
                return mergeCarriedStackIntoPouchSlot(slot, slotStack, carriedStack, dragType, player);
            }
        }

        return super.slotClick(slotId, dragType, clickTypeIn, player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        Slot slot = this.inventorySlots.get(index);

        if (slot == null || !slot.getHasStack()) return ItemStack.EMPTY;

        ItemStack stackInSlot = slot.getStack();
        ItemStack copiedStack = stackInSlot.copy();
        int pouchSlotCount = getPouchSlotCount();

        if (index < pouchSlotCount) {
            if (!mergeItemStack(stackInSlot, pouchSlotCount, this.inventorySlots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (!AmmoPouchHelper.isAllowedAmmo(stackInSlot)) return ItemStack.EMPTY;
            if (!mergeItemStackIntoPouch(stackInSlot)) return ItemStack.EMPTY;
        }

        if (stackInSlot.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
        } else {
            slot.onSlotChanged();
        }

        return copiedStack;
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        this.pouchInventory.markDirty();
    }

    private boolean isPouchSlot(int slotId) {
        return slotId >= 0 && slotId < getPouchSlotCount();
    }

    private boolean canMergeIntoPouchSlot(Slot slot, ItemStack slotStack, ItemStack carriedStack) {
        return !slotStack.isEmpty()
            && !carriedStack.isEmpty()
            && slot.isItemValid(carriedStack)
            && AmmoPouchHelper.areStacksFunctionallyEqual(slotStack, carriedStack);
    }

    // Vanilla pickup and merge logic clamps to the ammo item's native max stack size, which breaks pouch-sized stacks.
    private ItemStack mergeCarriedStackIntoPouchSlot(Slot slot, ItemStack slotStack, ItemStack carriedStack, int dragType, EntityPlayer player) {
        ItemStack copiedStack = slotStack.copy();
        int availableSpace = slot.getSlotStackLimit() - slotStack.getCount();

        if (availableSpace <= 0) return copiedStack;

        int inserted = dragType == 0
            ? Math.min(carriedStack.getCount(), availableSpace)
            : Math.min(1, availableSpace);

        if (inserted <= 0) return copiedStack;

        slotStack.grow(inserted);
        carriedStack.shrink(inserted);

        if (carriedStack.isEmpty()) player.inventory.setItemStack(ItemStack.EMPTY);

        slot.onSlotChanged();

        return copiedStack;
    }

    // Vanilla mergeItemStack also clamps to ItemStack#getMaxStackSize, so player-to-pouch shift-click needs a custom path.
    private boolean mergeItemStackIntoPouch(ItemStack stack) {
        int originalCount = stack.getCount();

        for (int slotIndex = 0; slotIndex < getPouchSlotCount() && !stack.isEmpty(); slotIndex++) {
            Slot slot = this.inventorySlots.get(slotIndex);
            ItemStack existing = slot.getStack();

            if (existing.isEmpty() || !AmmoPouchHelper.areStacksFunctionallyEqual(existing, stack)) continue;

            int availableSpace = slot.getSlotStackLimit() - existing.getCount();

            if (availableSpace <= 0) continue;

            int inserted = Math.min(stack.getCount(), availableSpace);

            existing.grow(inserted);
            stack.shrink(inserted);
            slot.onSlotChanged();
        }

        for (int slotIndex = 0; slotIndex < getPouchSlotCount() && !stack.isEmpty(); slotIndex++) {
            Slot slot = this.inventorySlots.get(slotIndex);

            if (slot.getHasStack() || !slot.isItemValid(stack)) continue;

            int inserted = Math.min(stack.getCount(), slot.getSlotStackLimit());
            ItemStack insertedStack = stack.copy();

            insertedStack.setCount(inserted);
            slot.putStack(insertedStack);
            stack.shrink(inserted);
        }

        return stack.getCount() != originalCount;
    }

    private boolean isLockedSlot(int slotId) {
        if (this.lockedHotbarSlot < 0 || slotId < 0 || slotId >= this.inventorySlots.size()) return false;

        Slot slot = this.inventorySlots.get(slotId);
        return slot.inventory instanceof InventoryPlayer && slot.getSlotIndex() == this.lockedHotbarSlot;
    }

    private static class SlotAmmo extends Slot {

        private final ItemStack pouchStack;

        public SlotAmmo(AmmoPouchInventory inventoryIn, ItemStack pouchStack, int index, int xPosition, int yPosition) {
            super(inventoryIn, index, xPosition, yPosition);
            this.pouchStack = pouchStack;
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return AmmoPouchHelper.isAllowedAmmo(stack);
        }

        @Override
        public int getSlotStackLimit() {
            return AmmoPouchHelper.getMaxStackSize(this.pouchStack);
        }
    }

    private static class LockedSlot extends Slot {

        public LockedSlot(InventoryPlayer inventoryIn, int index, int xPosition, int yPosition) {
            super(inventoryIn, index, xPosition, yPosition);
        }

        @Override
        public boolean canTakeStack(EntityPlayer playerIn) {
            return false;
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return false;
        }
    }
}