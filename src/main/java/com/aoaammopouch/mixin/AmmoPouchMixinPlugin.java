package com.aoaammopouch.mixin;

import java.util.Collections;
import java.util.List;

import net.minecraftforge.fml.common.Optional;

import zone.rong.mixinbooter.ILateMixinLoader;


@Optional.Interface(iface = "zone.rong.mixinbooter.ILateMixinLoader", modid = "mixinbooter")
public class AmmoPouchMixinPlugin implements ILateMixinLoader {

    @Override
    @Optional.Method(modid = "mixinbooter")
    public List<String> getMixinConfigs() {
        return Collections.singletonList("mixins.aoaammopouch.json");
    }

    @Override
    @Optional.Method(modid = "mixinbooter")
    public boolean shouldMixinConfigQueue(String mixinConfig) {
        return true;
    }
}