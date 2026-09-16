package com.aoaammopouch.config;

import java.io.File;
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
    private static final boolean DEFAULT_ALLOW_AOA_WEAPONS = true;
    private static final boolean DEFAULT_ALLOW_DIVINERPG_WEAPONS = true;

    @Config.LangKey("aoaammopouch.config.server.inventory")
    public static final Inventory inventory = new Inventory();

    @Config.LangKey("aoaammopouch.config.server.integrations")
    public static final Integrations integrations = new Integrations();

    @Config.LangKey("aoaammopouch.config.server.whitelist")
    public static final Whitelist whitelist = new Whitelist();

    private static Set<String> cachedAllowedAoAAmmoIds;
    private static Set<String> cachedAllowedDivineRPGAmmoIds;
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

    public static boolean allowAoAWeapons() {
        return integrations.allowAoAWeapons;
    }

    public static boolean allowDivineRPGWeapons() {
        return integrations.allowDivineRPGWeapons;
    }

    public static Set<String> getAllowedAoAAmmoIds() {
        if (cachedAllowedAoAAmmoIds != null) return cachedAllowedAoAAmmoIds;

        cachedAllowedAoAAmmoIds = normalizeAllowedIds(whitelist.allowedAmmoItems);

        return cachedAllowedAoAAmmoIds;
    }

    public static Set<String> getAllowedDivineRPGAmmoIds() {
        if (cachedAllowedDivineRPGAmmoIds != null) return cachedAllowedDivineRPGAmmoIds;

        cachedAllowedDivineRPGAmmoIds = normalizeAllowedIds(whitelist.allowedDivineRPGAmmoItems);

        return cachedAllowedDivineRPGAmmoIds;
    }

    public static Set<String> getAllowedAmmoIds() {
        if (cachedAllowedAmmoIds != null) return cachedAllowedAmmoIds;

        LinkedHashSet<String> allowedIds = new LinkedHashSet<String>();

        if (allowAoAWeapons()) allowedIds.addAll(getAllowedAoAAmmoIds());
        if (allowDivineRPGWeapons()) allowedIds.addAll(getAllowedDivineRPGAmmoIds());

        cachedAllowedAmmoIds = Collections.unmodifiableSet(allowedIds);

        return cachedAllowedAmmoIds;
    }

    public static void invalidateCaches() {
        cachedAllowedAoAAmmoIds = null;
        cachedAllowedDivineRPGAmmoIds = null;
        cachedAllowedAmmoIds = null;
    }

    private static Set<String> normalizeAllowedIds(String[] values) {
        LinkedHashSet<String> allowedIds = new LinkedHashSet<String>();

        for (String value : values) {
            if (value == null) continue;

            String normalized = value.trim().toLowerCase(Locale.ROOT);

            if (!normalized.isEmpty()) allowedIds.add(normalized);
        }

        return Collections.unmodifiableSet(allowedIds);
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

    public static class Integrations {

        @Config.LangKey("aoaammopouch.config.server.integrations.allowAoAWeapons")
        @Config.Comment({
            "Enables Ammo Pouch support for AoA weapons that consume ammo. Requires a restart."
        })
        public boolean allowAoAWeapons = DEFAULT_ALLOW_AOA_WEAPONS;

        @Config.LangKey("aoaammopouch.config.server.integrations.allowDivineRPGWeapons")
        @Config.Comment({
            "Enables Ammo Pouch support for DivineRPG ranged weapons that consume ammo.",
            "Requires a restart."
        })
        public boolean allowDivineRPGWeapons = DEFAULT_ALLOW_DIVINERPG_WEAPONS;
    }

    public static class Whitelist {

        @Config.LangKey("aoaammopouch.config.server.whitelist.allowedAmmoItems")
        @Config.Comment({
            "Registry names of items that can be stored in the Ammo Pouch.",
            "This default list mirrors ammo-like stacks currently consumed by AoA's ranged weapons."
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

        @Config.LangKey("aoaammopouch.config.server.whitelist.allowedDivineRPGAmmoItems")
        @Config.Comment({
            "Registry names of DivineRPG ammo items that can be stored in the Ammo Pouch.",
            "This default list mirrors ranged-weapon ammo currently consumed by DivineRPG."
        })
        public String[] allowedDivineRPGAmmoItems = {
            "divinerpg:acid",
            "divinerpg:apalachia_dust",
            "divinerpg:corrupted_bullet",
            "divinerpg:eden_dust",
            "divinerpg:grenade",
            "divinerpg:ice_shards",
            "divinerpg:mortum_dust",
            "divinerpg:skythern_dust",
            "divinerpg:wildwood_dust",
            "minecraft:cactus",
            "minecraft:gold_nugget",
            "minecraft:snowball"
        };
    }

    /**
     * Syncs the annotated config.
     *
     * @param configFile The configuration file
     */
    public static void init(File configFile) {
        syncConfig();
    }

    public static void load() {
        syncConfig();
    }

    private static void syncConfig() {
        ConfigManager.sync(Tags.MODID, Config.Type.INSTANCE);
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