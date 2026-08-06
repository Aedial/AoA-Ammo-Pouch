package com.aoaammopouch.config;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import com.aoaammopouch.Tags;


@Config(modid = Tags.MODID, name = Tags.MODID + "/server", category = "server")
@Config.LangKey("aoaammopouch.config.server")
public class AmmoPouchConfig {

    private static final int DEFAULT_SLOT_COUNT = 5;
    private static final int DEFAULT_MAX_STACK_SIZE = 1024;

    @Config.LangKey("aoaammopouch.config.server.inventory")
    public static final Inventory inventory = new Inventory();

    @Config.LangKey("aoaammopouch.config.server.whitelist")
    public static final Whitelist whitelist = new Whitelist();

    private static Set<String> cachedAllowedAmmoIds;

    public static int getSlotCount() {
        return inventory.getSlotCount(0);
    }

    public static int getMaxStackSize() {
        return inventory.getMaxStackSize(0);
    }

    public static int getTierCount() {
        return inventory.getTierCount();
    }

    public static int getSlotCount(int metadata) {
        return inventory.getSlotCount(metadata);
    }

    public static int getMaxStackSize(int metadata) {
        return inventory.getMaxStackSize(metadata);
    }

    public static Set<String> getAllowedAmmoIds() {
        if (cachedAllowedAmmoIds != null) return cachedAllowedAmmoIds;

        LinkedHashSet<String> allowedIds = new LinkedHashSet<String>();

        for (String value : whitelist.allowedAmmoItems) {
            if (value == null) continue;

            String normalized = value.trim().toLowerCase(Locale.ROOT);

            if (!normalized.isEmpty()) allowedIds.add(normalized);
        }

        cachedAllowedAmmoIds = Collections.unmodifiableSet(allowedIds);

        return cachedAllowedAmmoIds;
    }

    public static void invalidateCaches() {
        cachedAllowedAmmoIds = null;
    }

    public static class Inventory {

        @Config.LangKey("aoaammopouch.config.server.inventory.slotCount")
        @Config.Comment({
            "Per-tier slot counts for the Ammo Pouch.",
            "Entry 0 is the base pouch, entry 1 is metadata 1, and so on.",
            "Additional pouch tiers require matching textures and lang entries to be provided separately."
        })
        @Config.RangeInt(min = 1, max = 36)
        public int[] slotCounts = {DEFAULT_SLOT_COUNT};

        @Config.LangKey("aoaammopouch.config.server.inventory.maxStackSize")
        @Config.Comment({
            "Per-tier stack limits for the Ammo Pouch.",
            "Entry 0 is the base pouch, entry 1 is metadata 1, and so on.",
            "Insertion is clamped to this value, but existing larger stacks are left untouched until modified."
        })
        @Config.RangeInt(min = 1, max = 2147483647)
        public int[] maxStackSizes = {DEFAULT_MAX_STACK_SIZE};

        public int getTierCount() {
            return Math.max(Math.max(slotCounts.length, maxStackSizes.length), 1);
        }

        public int getSlotCount(int metadata) {
            return Math.max(1, Math.min(36, getConfiguredValue(slotCounts, metadata, DEFAULT_SLOT_COUNT)));
        }

        public int getMaxStackSize(int metadata) {
            return Math.max(1, getConfiguredValue(maxStackSizes, metadata, DEFAULT_MAX_STACK_SIZE));
        }

        private int getConfiguredValue(int[] values, int metadata, int defaultValue) {
            if (values.length <= 0) return defaultValue;

            int index = Math.max(0, Math.min(metadata, values.length - 1));
            return values[index];
        }
    }

    public static class Whitelist {

        @Config.LangKey("aoaammopouch.config.server.whitelist.allowedAmmoItems")
        @Config.Comment({
            "Registry names of items that can be stored in the Ammo Pouch.",
            "This default list mirrors ammo-like stack currently consumed by AoA's ranged weapons."
        })
        public String[] allowedAmmoItems = {
            "aoa3:balloon",
            "aoa3:cannonball",
            "aoa3:chakram",
            "aoa3:discharge_capsule",
            "aoa3:goo_ball",
            "aoa3:grenade",
            "aoa3:hellfire",
            "aoa3:holly_arrow",
            "aoa3:limonite_bullet",
            "aoa3:metal_slug",
            "aoa3:runic_bomb",
            "aoa3:slice_star",
            "aoa3:spreadshot",
            "aoa3:vulkram",
            "minecraft:cobblestone"
        };
    }

    @Mod.EventBusSubscriber(modid = Tags.MODID)
    public static class ConfigSyncHandler {

        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (!Tags.MODID.equals(event.getModID())) return;

            ConfigManager.sync(Tags.MODID, Config.Type.INSTANCE);
            AmmoPouchConfig.invalidateCaches();
        }
    }
}