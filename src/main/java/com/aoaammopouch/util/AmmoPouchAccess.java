package com.aoaammopouch.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;

import baubles.api.BaublesApi;


public class AmmoPouchAccess {

    private static final Map<UUID, CachedLocation> locationCache = new HashMap<UUID, CachedLocation>();

    private enum InventoryType {
        MAIN,
        OFFHAND,
        BAUBLES
    }

    private static class CachedLocation {

        private final InventoryType inventoryType;
        private final int slotIndex;

        private CachedLocation(InventoryType inventoryType, int slotIndex) {
            this.inventoryType = inventoryType;
            this.slotIndex = slotIndex;
        }
    }

    public static ItemStack getCachedPouch(EntityPlayer player) {
        if (player == null) return ItemStack.EMPTY;

        CachedLocation cachedLocation = locationCache.get(player.getUniqueID());
        if (cachedLocation == null) return ItemStack.EMPTY;

        ItemStack stack = resolve(player, cachedLocation);
        if (AmmoPouchHelper.isAmmoPouch(stack)) return stack;

        clearCachedLocation(player.getUniqueID());

        return ItemStack.EMPTY;
    }

    public static boolean findAndCacheFirstPouch(EntityPlayer player) {
        if (player == null) return false;

        int selectedSlot = player.inventory.currentItem;
        ItemStack mainHand = player.getHeldItemMainhand();

        if (AmmoPouchHelper.isAmmoPouch(mainHand)) {
            cacheLocation(player.getUniqueID(), InventoryType.MAIN, selectedSlot);
            return true;
        }

        ItemStack offHand = player.getHeldItemOffhand();

        if (AmmoPouchHelper.isAmmoPouch(offHand)) {
            cacheLocation(player.getUniqueID(), InventoryType.OFFHAND, 0);
            return true;
        }

        if (Loader.isModLoaded("baubles") && findPouchInBaubles(player)) return true;

        for (int slot = 0; slot < player.inventory.mainInventory.size(); slot++) {
            if (slot == selectedSlot) continue;

            ItemStack stack = player.inventory.mainInventory.get(slot);
            if (!AmmoPouchHelper.isAmmoPouch(stack)) continue;

            cacheLocation(player.getUniqueID(), InventoryType.MAIN, slot);
            return true;
        }

        return false;
    }

    public static void clearCachedLocation(UUID playerId) {
        locationCache.remove(playerId);
    }

    private static void cacheLocation(UUID playerId, InventoryType inventoryType, int slotIndex) {
        locationCache.put(playerId, new CachedLocation(inventoryType, slotIndex));
    }

    private static ItemStack resolve(EntityPlayer player, CachedLocation cachedLocation) {
        if (cachedLocation.inventoryType == InventoryType.MAIN) {
            if (cachedLocation.slotIndex < 0 || cachedLocation.slotIndex >= player.inventory.mainInventory.size()) return ItemStack.EMPTY;

            return player.inventory.mainInventory.get(cachedLocation.slotIndex);
        }

        if (cachedLocation.inventoryType == InventoryType.OFFHAND) return player.getHeldItemOffhand();

        if (Loader.isModLoaded("baubles")) return resolveBauble(player, cachedLocation.slotIndex);

        return ItemStack.EMPTY;
    }

    @Optional.Method(modid = "baubles")
    private static boolean findPouchInBaubles(EntityPlayer player) {
        baubles.api.cap.IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(player);

        for (int slot = 0; slot < baubles.getSlots(); slot++) {
            ItemStack stack = baubles.getStackInSlot(slot);

            if (!AmmoPouchHelper.isAmmoPouch(stack)) continue;

            cacheLocation(player.getUniqueID(), InventoryType.BAUBLES, slot);
            return true;
        }

        return false;
    }

    @Optional.Method(modid = "baubles")
    private static ItemStack resolveBauble(EntityPlayer player, int slotIndex) {
        baubles.api.cap.IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(player);
        if (slotIndex < 0 || slotIndex >= baubles.getSlots()) return ItemStack.EMPTY;

        return baubles.getStackInSlot(slotIndex);
    }
}