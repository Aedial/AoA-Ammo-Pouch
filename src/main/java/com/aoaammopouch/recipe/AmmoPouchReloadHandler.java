package com.aoaammopouch.recipe;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import com.aoaammopouch.util.AmmoPouchHelper;


public class AmmoPouchReloadHandler {

    @SubscribeEvent
    public void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.craftMatrix instanceof InventoryCrafting)) return;
        if (!AmmoPouchHelper.isAmmoPouch(event.crafting)) return;

        AmmoPouchReloadRecipe.applyCraftedReload(event.crafting, (InventoryCrafting)event.craftMatrix);
    }
}