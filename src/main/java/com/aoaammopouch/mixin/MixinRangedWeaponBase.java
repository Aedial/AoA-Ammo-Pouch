package com.aoaammopouch.mixin;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.aoaammopouch.util.AmmoPouchAccess;
import com.aoaammopouch.util.AmmoPouchHelper;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;


@Pseudo
@Mixin(targets = "divinerpg.objects.items.base.RangedWeaponBase", remap = false)
public class MixinRangedWeaponBase {

    @Shadow @Final private Supplier<Item> ammoSupplier;

    @Inject(method = "tryFindAmmo", at = @At("HEAD"), cancellable = true)
    private void aoaammopouch$tryFindAmmoFromCachedPouch(EntityPlayer player, CallbackInfoReturnable<ActionResult<ItemStack>> cir) {
        if (player.capabilities.isCreativeMode) return;

        ActionResult<ItemStack> result = aoaammopouch$findPouchAmmoResult(AmmoPouchAccess.getCachedPouch(player));
        if (result != null) cir.setReturnValue(result);
    }

    @Inject(method = "tryFindAmmo", at = @At("RETURN"), cancellable = true)
    private void aoaammopouch$cachePouchOnMiss(EntityPlayer player, CallbackInfoReturnable<ActionResult<ItemStack>> cir) {
        ActionResult<ItemStack> result = cir.getReturnValue();

        if (player.capabilities.isCreativeMode || result == null || result.getType() == EnumActionResult.SUCCESS) return;
        if (!AmmoPouchAccess.getCachedPouch(player).isEmpty()) return;
        if (!AmmoPouchAccess.findAndCacheFirstPouch(player)) return;

        ActionResult<ItemStack> pouchResult = aoaammopouch$findPouchAmmoResult(AmmoPouchAccess.getCachedPouch(player));
        if (pouchResult != null) cir.setReturnValue(pouchResult);
    }

    @Redirect(method = "onItemRightClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;shrink(I)V", remap = true))
    private void aoaammopouch$consumeAmmo(ItemStack ammoStack, int amount, World world, EntityPlayer player, EnumHand hand) {
        if (aoaammopouch$isInventoryStack(player, ammoStack)) {
            ammoStack.shrink(amount);
            return;
        }

        if (world.isRemote) return;

        Item ammoItem = aoaammopouch$getAmmoItem();
        if (ammoItem == null) return;

        ItemStack pouchStack = AmmoPouchAccess.getCachedPouch(player);
        if (AmmoPouchHelper.tryConsumeAmmoByItem(pouchStack, ammoItem, true, amount)) return;

        if (!AmmoPouchAccess.findAndCacheFirstPouch(player)) return;

        AmmoPouchHelper.tryConsumeAmmoByItem(AmmoPouchAccess.getCachedPouch(player), ammoItem, true, amount);
    }

    @Unique
    private ActionResult<ItemStack> aoaammopouch$findPouchAmmoResult(ItemStack pouchStack) {
        Item ammoItem = aoaammopouch$getAmmoItem();

        if (ammoItem == null || !AmmoPouchHelper.tryConsumeAmmoByItem(pouchStack, ammoItem, false, 1)) return null;

        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, new ItemStack(ammoItem));
    }

    @Unique
    private Item aoaammopouch$getAmmoItem() {
        return this.ammoSupplier != null ? this.ammoSupplier.get() : null;
    }

    @Unique
    private boolean aoaammopouch$isInventoryStack(EntityPlayer player, ItemStack ammoStack) {
        if (ammoStack == null || ammoStack.isEmpty()) return false;
        if (player.getHeldItemMainhand() == ammoStack) return true;
        if (player.getHeldItemOffhand() == ammoStack) return true;

        for (ItemStack stack : player.inventory.mainInventory) {
            if (stack == ammoStack) return true;
        }

        return false;
    }
}