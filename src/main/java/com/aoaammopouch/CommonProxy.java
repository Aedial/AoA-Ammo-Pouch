package com.aoaammopouch;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import com.aoaammopouch.recipe.AmmoPouchReloadHandler;


public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {}

    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new AmmoPouchReloadHandler());
    }
}