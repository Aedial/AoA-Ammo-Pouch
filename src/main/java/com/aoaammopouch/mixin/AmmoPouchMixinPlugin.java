package com.aoaammopouch.mixin;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.Loader;

import zone.rong.mixinbooter.ILateMixinLoader;

import com.aoaammopouch.Tags;
import com.aoaammopouch.AoAAmmoPouch;
import com.aoaammopouch.config.AmmoPouchConfig;


@Optional.Interface(iface = "zone.rong.mixinbooter.ILateMixinLoader", modid = "mixinbooter")
public class AmmoPouchMixinPlugin implements ILateMixinLoader {

    private static final String AOA_MIXIN_CONFIG = "mixins.aoaammopouch.aoa.json";
    private static final String DIVINERPG_MIXIN_CONFIG = "mixins.aoaammopouch.drpg.json";

    private static final List<String> queuedMixinConfigs = new ArrayList<>(2);

    @Override
    @Optional.Method(modid = "mixinbooter")
    public List<String> getMixinConfigs() {
        loadMixinConfigs();
        return queuedMixinConfigs;
    }

    @Override
    @Optional.Method(modid = "mixinbooter")
    public boolean shouldMixinConfigQueue(String mixinConfig) {
        loadMixinConfigs();
        return queuedMixinConfigs.contains(mixinConfig);
    }

    static private void loadMixinConfigs() {
        if (!queuedMixinConfigs.isEmpty()) return;

        AmmoPouchConfig.load();

        if (Loader.isModLoaded("aoa3") && AmmoPouchConfig.allowAoAWeapons()) {
            queuedMixinConfigs.add(AOA_MIXIN_CONFIG);
        }
        if (Loader.isModLoaded("divinerpg") && AmmoPouchConfig.allowDivineRPGWeapons()) {
            queuedMixinConfigs.add(DIVINERPG_MIXIN_CONFIG);
        }

        if (queuedMixinConfigs.isEmpty()) {
            AoAAmmoPouch.LOGGER.error("The supported mods are either not loaded or disabled. This mod will do NOTHING!");
        }
    }
}