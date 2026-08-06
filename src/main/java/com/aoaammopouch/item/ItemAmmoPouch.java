package com.aoaammopouch.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import baubles.api.BaubleType;
import baubles.api.IBauble;

import com.aoaammopouch.AoAAmmoPouch;
import com.aoaammopouch.Tags;
import com.aoaammopouch.config.AmmoPouchConfig;
import com.aoaammopouch.gui.GuiHandler;
import com.aoaammopouch.util.AmmoPouchHelper;

@Optional.Interface(iface = "baubles.api.IBauble", modid = "baubles")
public class ItemAmmoPouch extends Item implements IBauble {

    public ItemAmmoPouch() {
        setRegistryName(Tags.MODID, "ammo_pouch");
        setTranslationKey(Tags.MODID + ".ammo_pouch");
        setMaxStackSize(1);
        setHasSubtypes(true);
        setCreativeTab(CreativeTabs.TOOLS);
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        int metadata = stack.getMetadata();
        if (metadata <= 0) return super.getTranslationKey(stack);

        return super.getTranslationKey(stack) + "." + metadata;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (!isInCreativeTab(tab)) return;

        for (int meta = 0; meta < AmmoPouchConfig.getTierCount(); meta++) {
            items.add(new ItemStack(this, 1, meta));
        }
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (!worldIn.isRemote) AmmoPouchHelper.ensureInitialized(stack);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);

        if (!worldIn.isRemote) {
            int selectedSlot = handIn == EnumHand.MAIN_HAND ? playerIn.inventory.currentItem : 0;
            int offHandFlag = handIn == EnumHand.OFF_HAND ? 1 : 0;

            playerIn.openGui(AoAAmmoPouch.instance, GuiHandler.GUI_AMMO_POUCH, worldIn, selectedSlot, 0, offHandFlag);
        }

        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    @Optional.Method(modid = "baubles")
    @Override
    public BaubleType getBaubleType(ItemStack itemStack) {
        return BaubleType.TRINKET;
    }

    @Optional.Method(modid = "baubles")
    @Override
    public void onWornTick(ItemStack itemstack, EntityLivingBase player) {}

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        tooltip.add(TextFormatting.AQUA + I18n.format("item.aoaammopouch.ammo_pouch.tip1"));
        tooltip.add("");
        tooltip.add(TextFormatting.GRAY + I18n.format(
            "item.aoaammopouch.ammo_pouch.tip2",
            Integer.toString(AmmoPouchHelper.getFilledSlotCount(stack)),
            Integer.toString(AmmoPouchHelper.getSlotCount(stack))));

        List<AmmoPouchHelper.ContentEntry> contents = AmmoPouchHelper.getAggregatedContents(stack);

        if (!GuiScreen.isShiftKeyDown()) {
            tooltip.add(TextFormatting.DARK_GRAY + I18n.format("item.aoaammopouch.ammo_pouch.tip3"));
            return;
        }

        if (contents.isEmpty()) {
            tooltip.add(TextFormatting.DARK_GRAY + I18n.format("item.aoaammopouch.ammo_pouch.empty"));
            return;
        }

        for (AmmoPouchHelper.ContentEntry entry : contents) {
            tooltip.add(TextFormatting.GRAY + I18n.format(
                "item.aoaammopouch.ammo_pouch.content_line",
                Integer.toString(entry.getCount()),
                Integer.toString(entry.getLimit()),
                entry.getDisplayStack().getDisplayName()));
        }
    }
}