package com.aoaammopouch.inventory;

import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import com.aoaammopouch.util.AmmoPouchHelper;


public class AmmoPouchInventory extends InventoryBasic {

    private final ItemStack hostStack;

    public AmmoPouchInventory(ItemStack hostStack) {
        super("AmmoPouch", false, AmmoPouchHelper.getSlotCount(hostStack));
        this.hostStack = hostStack;

        NonNullList<ItemStack> contents = AmmoPouchHelper.readInventory(hostStack);

        for (int slot = 0; slot < contents.size(); slot++) {
            super.setInventorySlotContents(slot, contents.get(slot));
        }
    }

    @Override
    public int getInventoryStackLimit() {
        return AmmoPouchHelper.getMaxStackSize(this.hostStack);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return AmmoPouchHelper.isAllowedAmmo(stack);
    }

    @Override
    public void markDirty() {
        super.markDirty();
        AmmoPouchHelper.saveInventory(this.hostStack, toContentsList());
    }

    private NonNullList<ItemStack> toContentsList() {
        NonNullList<ItemStack> contents = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);

        for (int slot = 0; slot < contents.size(); slot++) {
            contents.set(slot, getStackInSlot(slot));
        }

        return contents;
    }
}