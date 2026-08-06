package com.aoaammopouch.client;

import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.aoaammopouch.util.AmmoPouchHelper;


@SideOnly(Side.CLIENT)
public class AmmoTooltipHandler {

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();

        if (stack.isEmpty() || !AmmoPouchHelper.isAllowedAmmo(stack)) return;

        List<String> tooltip = event.getToolTip();
        tooltip.add("");
        tooltip.add(TextFormatting.AQUA + I18n.format("tooltip.aoaammopouch.compatible"));
    }
}