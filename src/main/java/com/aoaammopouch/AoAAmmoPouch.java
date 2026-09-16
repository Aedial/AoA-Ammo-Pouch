package com.aoaammopouch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import com.aoaammopouch.gui.GuiHandler;

@Mod(
    modid = Tags.MODID,
    name = Tags.MODNAME,
    version = Tags.VERSION,
    dependencies = "required-after:mixinbooter;after:baubles;after:aoa3;after:divinerpg",
    acceptedMinecraftVersions = "[1.12.2]"
)
public class AoAAmmoPouch {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MODID);

    @SidedProxy(
        clientSide = "com.aoaammopouch.ClientProxy",
        serverSide = "com.aoaammopouch.CommonProxy"
    )
    public static CommonProxy proxy;

    @Mod.Instance(Tags.MODID)
    public static AoAAmmoPouch instance;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ItemRegistry.init();
        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());
        proxy.init(event);
    }
}