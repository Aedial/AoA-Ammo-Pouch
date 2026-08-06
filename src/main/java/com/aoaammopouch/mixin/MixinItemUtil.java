package com.aoaammopouch.mixin;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.aoaammopouch.util.AmmoPouchAccess;
import com.aoaammopouch.util.AmmoPouchHelper;

import net.tslat.aoa3.utils.ItemUtil;


@Mixin(value = ItemUtil.class, remap = false)
public class MixinItemUtil {

    @Inject(method = "findInventoryItem", at = @At("HEAD"), cancellable = true)
    private static void aoaammopouch$checkCachedPouch(EntityPlayer player, ItemStack stack, boolean consumeItem, int amount, CallbackInfoReturnable<Boolean> cir) {
        if (stack.isEmpty() || amount <= 0 || player.isCreative()) return;

        // If we have a cached pouch, try to consume ammo from it first
        ItemStack pouchStack = AmmoPouchAccess.getCachedPouch(player);
        if (pouchStack.isEmpty()) return;

        if (AmmoPouchHelper.tryConsumeAmmo(pouchStack, stack, consumeItem, amount)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "findInventoryItem", at = @At("RETURN"))
    private static void aoaammopouch$cachePouchOnMiss(EntityPlayer player, ItemStack stack, boolean consumeItem, int amount, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() || stack.isEmpty() || amount <= 0 || player.isCreative()) return;
        if (!AmmoPouchAccess.getCachedPouch(player).isEmpty()) return;

        // If we have no cached pouch and the original findInventoryItem failed, try to find a pouch
        AmmoPouchAccess.findAndCacheFirstPouch(player);
    }
}