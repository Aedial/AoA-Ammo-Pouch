package com.aoaammopouch.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

import com.aoaammopouch.config.AmmoPouchConfig;
import com.aoaammopouch.item.ItemAmmoPouch;


public class AmmoPouchHelper {

    private static final String NBT_INVENTORY = "Inventory";
    private static final String NBT_ITEMS = "Items";
    private static final String NBT_SLOT = "Slot";
    private static final String NBT_COUNT = "Count";
    private static final String NBT_SLOT_COUNT = "SlotCount";

    public static void ensureInitialized(ItemStack pouchStack) {
        if (!isAmmoPouch(pouchStack)) return;

        NBTTagCompound tag = getOrCreateTag(pouchStack);
        if (!tag.hasKey(NBT_SLOT_COUNT)) tag.setInteger(NBT_SLOT_COUNT, AmmoPouchConfig.getSlotCount(pouchStack.getMetadata()));
    }

    public static boolean isAmmoPouch(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemAmmoPouch;
    }

    public static int getSlotCount(ItemStack pouchStack) {
        if (!isAmmoPouch(pouchStack)) return AmmoPouchConfig.getSlotCount();

        NBTTagCompound tag = pouchStack.getTagCompound();
        int configuredCount = AmmoPouchConfig.getSlotCount(pouchStack.getMetadata());

        if (tag == null || !tag.hasKey(NBT_SLOT_COUNT)) return configuredCount;

        int storedCount = Math.max(1, Math.min(36, tag.getInteger(NBT_SLOT_COUNT)));
        return Math.max(configuredCount, storedCount);
    }

    public static int getMaxStackSize() {
        return AmmoPouchConfig.getMaxStackSize();
    }

    public static int getMaxStackSize(ItemStack pouchStack) {
        if (!isAmmoPouch(pouchStack)) return AmmoPouchConfig.getMaxStackSize();

        return AmmoPouchConfig.getMaxStackSize(pouchStack.getMetadata());
    }

    public static NonNullList<ItemStack> readInventory(ItemStack pouchStack) {
        // FIXME: The list size should not be saved in NBT, as going from 5 to 36 in the config
        //        will cause the pouch to PERMANENTLY hold 36 slots, which defeats the purpose
        //        of a config. Instead, it should use the config value, but not eagerly
        //        delete the extra slots, so that if the config is changed back to 5,
        //        the items are still technically there, but just not accessible.

        //        Would also need a way to extract the extra items from the pouch,
        //        maybe shift right-click on a chest?

        //        And there would be a warning on the item tooltip if we have extra items,
        //        with instructions on how to extract them.

        NonNullList<ItemStack> contents = NonNullList.withSize(getSlotCount(pouchStack), ItemStack.EMPTY);

        if (!isAmmoPouch(pouchStack) || !pouchStack.hasTagCompound()) return contents;

        NBTTagCompound inventoryTag = pouchStack.getTagCompound().getCompoundTag(NBT_INVENTORY);
        NBTTagList itemTags = inventoryTag.getTagList(NBT_ITEMS, Constants.NBT.TAG_COMPOUND);

        for (int index = 0; index < itemTags.tagCount(); index++) {
            NBTTagCompound itemTag = itemTags.getCompoundTagAt(index);
            int slot = itemTag.getInteger(NBT_SLOT);
            if (slot < 0 || slot >= contents.size()) continue;

            ItemStack stack = new ItemStack(itemTag);
            if (stack.isEmpty() && !itemTag.hasKey(NBT_COUNT)) continue;
            if (itemTag.hasKey(NBT_COUNT)) stack.setCount(itemTag.getInteger(NBT_COUNT));
            if (stack.isEmpty()) continue;

            contents.set(slot, stack);
        }

        return contents;
    }

    public static void saveInventory(ItemStack pouchStack, NonNullList<ItemStack> contents) {
        if (!isAmmoPouch(pouchStack)) return;

        NBTTagCompound tag = getOrCreateTag(pouchStack);
        NBTTagCompound inventoryTag = new NBTTagCompound();
        NBTTagList itemTags = new NBTTagList();

        for (int slot = 0; slot < contents.size(); slot++) {
            ItemStack stack = contents.get(slot);
            if (stack.isEmpty()) continue;

            ItemStack serializedStack = stack.copy();
            NBTTagCompound itemTag = new NBTTagCompound();

            // Write the count ourself, because vanilla writes count as a byte,
            // which then overflows to negative numbers for stacks larger than 127.
            serializedStack.writeToNBT(itemTag);
            itemTag.setInteger(NBT_SLOT, slot);
            itemTag.setInteger(NBT_COUNT, stack.getCount());
            itemTags.appendTag(itemTag);
        }

        inventoryTag.setTag(NBT_ITEMS, itemTags);
        tag.setTag(NBT_INVENTORY, inventoryTag);
        tag.setInteger(NBT_SLOT_COUNT, Math.max(1, Math.min(36, contents.size())));
    }

    public static boolean isAllowedAmmo(ItemStack stack) {
        if (stack.isEmpty()) return false;

        ResourceLocation registryName = stack.getItem().getRegistryName();
        if (registryName == null) return false;

        return AmmoPouchConfig.getAllowedAmmoIds().contains(registryName.toString());
    }

    public static int getFilledSlotCount(ItemStack pouchStack) {
        int count = 0;
        for (ItemStack stack : readInventory(pouchStack)) {
            if (!stack.isEmpty()) count++;
        }

        return count;
    }

    public static List<ContentEntry> getAggregatedContents(ItemStack pouchStack) {
        LinkedHashMap<ItemStackKey, ContentEntry> entries = new LinkedHashMap<ItemStackKey, ContentEntry>();

        int limit = getMaxStackSize(pouchStack);
        for (ItemStack stack : readInventory(pouchStack)) {
            if (stack.isEmpty()) continue;

            ItemStackKey key = ItemStackKey.of(stack);
            ContentEntry entry = entries.get(key);
            if (entry == null) {
                entries.put(key, new ContentEntry(stack.copy(), stack.getCount(), limit));
            } else {
                entry.addCount(stack.getCount());
            }
        }

        return new ArrayList<ContentEntry>(entries.values());
    }

    public static boolean tryConsumeAmmo(ItemStack pouchStack, ItemStack requestedStack, boolean consumeItem, int amount) {
        if (!isAmmoPouch(pouchStack) || !isAllowedAmmo(requestedStack)) return false;

        NonNullList<ItemStack> contents = readInventory(pouchStack);
        int foundCount = 0;

        for (ItemStack stack : contents) {
            if (stack.isEmpty() || !areStacksFunctionallyEqual(stack, requestedStack)) continue;
            foundCount += stack.getCount();
            if (foundCount >= amount) break;
        }

        if (foundCount < amount) return false;
        if (!consumeItem) return true;

        int remaining = amount;
        for (int slot = 0; slot < contents.size(); slot++) {
            ItemStack stack = contents.get(slot);
            if (stack.isEmpty() || !areStacksFunctionallyEqual(stack, requestedStack)) continue;

            int consumeAmount = Math.min(remaining, stack.getCount());
            stack.shrink(consumeAmount);
            if (stack.isEmpty()) contents.set(slot, ItemStack.EMPTY);

            remaining -= consumeAmount;
            if (remaining <= 0) break;
        }

        saveInventory(pouchStack, contents);

        return true;
    }

    public static int insertIntoContents(ItemStack pouchStack, NonNullList<ItemStack> contents, ItemStack stack) {
        if (stack.isEmpty()) return 0;

        int remaining = stack.getCount();
        int maxStackSize = getMaxStackSize(pouchStack);

        for (int slot = 0; slot < contents.size(); slot++) {
            ItemStack existing = contents.get(slot);

            if (existing.isEmpty() || !areStacksFunctionallyEqual(existing, stack)) continue;
            if (existing.getCount() >= maxStackSize) continue;

            int inserted = Math.min(remaining, maxStackSize - existing.getCount());
            if (inserted <= 0) continue;

            existing.grow(inserted);
            remaining -= inserted;
            if (remaining <= 0) return 0;
        }

        for (int slot = 0; slot < contents.size(); slot++) {
            ItemStack existing = contents.get(slot);
            if (!existing.isEmpty()) continue;

            ItemStack insertedStack = stack.copy();
            int inserted = Math.min(remaining, maxStackSize);

            insertedStack.setCount(inserted);
            contents.set(slot, insertedStack);
            remaining -= inserted;
            if (remaining <= 0) return 0;
        }

        return remaining;
    }

    public static boolean areStacksFunctionallyEqual(ItemStack first, ItemStack second) {
        if (first.getItem() != second.getItem()) return false;
        if (first.isItemStackDamageable() ^ second.isItemStackDamageable()) return false;
        if (!first.isItemStackDamageable() && first.getItemDamage() != second.getItemDamage()) return false;

        return !first.hasTagCompound()
            ? !second.hasTagCompound()
            : second.hasTagCompound() && first.getTagCompound().equals(second.getTagCompound());
    }

    private static NBTTagCompound getOrCreateTag(ItemStack stack) {
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());

        return stack.getTagCompound();
    }

    public static class ContentEntry {

        private final ItemStack displayStack;
        private int count;
        private int limit;

        private ContentEntry(ItemStack displayStack, int count, int limit) {
            this.displayStack = displayStack;
            this.count = count;
            this.limit = limit;
        }

        public ItemStack getDisplayStack() {
            return this.displayStack;
        }

        public int getCount() {
            return this.count;
        }

        private void addCount(int amount) {
            this.count += amount;
        }

        public int getLimit() {
            return this.limit;
        }
    }
}