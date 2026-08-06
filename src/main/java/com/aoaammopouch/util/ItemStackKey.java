package com.aoaammopouch.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;


public class ItemStackKey {

    private final Item item;
    private final boolean damageable;
    private final int metadata;
    private final String tagString;
    private Integer cachedHashCode;

    private ItemStackKey(Item item, boolean damageable, int metadata, String tagString) {
        this.item = item;
        this.damageable = damageable;
        this.metadata = metadata;
        this.tagString = tagString;
    }

    public static ItemStackKey of(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        boolean damageable = stack.isItemStackDamageable();
        int metadata = damageable ? 0 : stack.getItemDamage();
        String tagString = tag == null ? null : tag.toString();

        return new ItemStackKey(stack.getItem(), damageable, metadata, tagString);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ItemStackKey)) return false;

        ItemStackKey other = (ItemStackKey)obj;

        if (this.item != other.item) return false;
        if (this.damageable != other.damageable) return false;
        if (this.metadata != other.metadata) return false;

        return this.tagString == null ? other.tagString == null : this.tagString.equals(other.tagString);
    }

    @Override
    public int hashCode() {
        if (this.cachedHashCode != null) return this.cachedHashCode;

        int result = this.item.hashCode();
        result = 31 * result + (this.damageable ? 1 : 0);
        result = 31 * result + this.metadata;
        result = 31 * result + (this.tagString == null ? 0 : this.tagString.hashCode());
        this.cachedHashCode = result;

        return result;
    }
}