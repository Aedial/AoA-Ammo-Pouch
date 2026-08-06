package com.aoaammopouch;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.aoaammopouch.config.AmmoPouchConfig;
import com.aoaammopouch.item.ItemAmmoPouch;
import com.aoaammopouch.recipe.AmmoPouchReloadRecipe;


@Mod.EventBusSubscriber(modid = Tags.MODID)
public class ItemRegistry {

    public static ItemAmmoPouch AMMO_POUCH;

    public static void init() {
        AMMO_POUCH = new ItemAmmoPouch();
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(AMMO_POUCH);
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        event.getRegistry().register(
            new AmmoPouchReloadRecipe().setRegistryName(Tags.MODID, "ammo_pouch_reload"));
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void registerModels(ModelRegistryEvent event) {
        for (int metadata = 0; metadata < AmmoPouchConfig.getTierCount(); metadata++) {
            ModelLoader.setCustomModelResourceLocation(
                AMMO_POUCH,
                metadata,
                new ModelResourceLocation(Tags.MODID + ":" + getAmmoPouchModelName(metadata), "inventory"));
        }
    }

    private static String getAmmoPouchModelName(int metadata) {
        return metadata <= 0 ? "ammo_pouch" : "ammo_pouch_" + metadata;
    }
}