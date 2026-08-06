package com.aoaammopouch.recipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

import com.aoaammopouch.ItemRegistry;
import com.aoaammopouch.util.AmmoPouchHelper;


/**
 * Custom recipe for reloading an ammo pouch with ammo from the crafting grid.
 * The allowed ammos are defined in config.
 */
public class AmmoPouchReloadRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    @Override
    public boolean matches(@Nonnull InventoryCrafting inv, @Nonnull World worldIn) {
        return findMatch(inv) != null;
    }

    @Override
    @Nonnull
    public ItemStack getCraftingResult(@Nonnull InventoryCrafting inv) {
        ReloadMatch match = findMatch(inv);
        return match == null ? ItemStack.EMPTY : match.result.copy();
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 2;
    }

    @Override
    @Nonnull
    public ItemStack getRecipeOutput() {
        return ItemRegistry.AMMO_POUCH == null ? ItemStack.EMPTY : new ItemStack(ItemRegistry.AMMO_POUCH);
    }

    @Override
    @Nonnull
    public NonNullList<ItemStack> getRemainingItems(@Nonnull InventoryCrafting inv) {
        return NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Nullable
    private ReloadMatch findMatch(InventoryCrafting inv) {
        return findReloadMatch(inv);
    }

    static boolean applyCraftedReload(ItemStack craftedStack, InventoryCrafting inv) {
        ReloadMatch match = findReloadMatch(inv);
        if (match == null) return false;

        if (match.result.hasTagCompound()) {
            craftedStack.setTagCompound(match.result.getTagCompound().copy());
        } else {
            craftedStack.setTagCompound(null);
        }

        for (int slot = 0; slot < match.remainderCounts.length; slot++) {
            if (slot == match.pouchSlot) continue;

            ItemStack stack = inv.getStackInSlot(slot);
            if (stack.isEmpty()) continue;

            // SlotCrafting consumes one ingredient from each occupied slot after the craft event fires.
            stack.setCount(match.remainderCounts[slot] + 1);
            inv.setInventorySlotContents(slot, stack);
        }

        return true;
    }

    @Nullable
    static ReloadMatch findReloadMatch(InventoryCrafting inv) {
        ItemStack pouchStack = ItemStack.EMPTY;
        int pouchSlot = -1;

        for (int slot = 0; slot < inv.getSizeInventory(); slot++) {
            ItemStack stack = inv.getStackInSlot(slot);
            if (stack.isEmpty()) continue;

            if (AmmoPouchHelper.isAmmoPouch(stack)) {
                if (!pouchStack.isEmpty()) return null;

                pouchStack = stack;
                pouchSlot = slot;
                continue;
            }

            if (!AmmoPouchHelper.isAllowedAmmo(stack)) return null;
        }

        if (pouchStack.isEmpty()) return null;

        ItemStack result = pouchStack.copy();
        result.setCount(1);

        NonNullList<ItemStack> contents = AmmoPouchHelper.readInventory(result);
        int[] remainderCounts = new int[inv.getSizeInventory()];
        boolean insertedAny = false;

        for (int slot = 0; slot < inv.getSizeInventory(); slot++) {
            if (slot == pouchSlot) continue;

            ItemStack stack = inv.getStackInSlot(slot);

            if (stack.isEmpty()) continue;

            ItemStack ammoCopy = stack.copy();
            int remainderCount = AmmoPouchHelper.insertIntoContents(result, contents, ammoCopy);

            remainderCounts[slot] = remainderCount;
            if (remainderCount < stack.getCount()) insertedAny = true;
        }

        if (!insertedAny) return null;

        AmmoPouchHelper.saveInventory(result, contents);

        return new ReloadMatch(result, pouchSlot, remainderCounts);
    }

    private static class ReloadMatch {

        final ItemStack result;
        final int pouchSlot;
        final int[] remainderCounts;

        private ReloadMatch(ItemStack result, int pouchSlot, int[] remainderCounts) {
            this.result = result;
            this.pouchSlot = pouchSlot;
            this.remainderCounts = remainderCounts;
        }
    }
}