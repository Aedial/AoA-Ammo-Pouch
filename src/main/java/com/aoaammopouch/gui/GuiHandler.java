package com.aoaammopouch.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import com.aoaammopouch.util.AmmoPouchHelper;


public class GuiHandler implements IGuiHandler {

    public static final int GUI_AMMO_POUCH = 0;

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id != GUI_AMMO_POUCH) return null;

        boolean offHand = z == 1;
        ItemStack pouchStack = resolvePouchStack(player, x, offHand);

        if (!AmmoPouchHelper.isAmmoPouch(pouchStack)) return null;

        return new ContainerAmmoPouch(player.inventory, pouchStack, x, offHand);
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id != GUI_AMMO_POUCH) return null;

        boolean offHand = z == 1;
        ItemStack pouchStack = resolvePouchStack(player, x, offHand);

        if (!AmmoPouchHelper.isAmmoPouch(pouchStack)) return null;

        return new GuiAmmoPouch(new ContainerAmmoPouch(player.inventory, pouchStack, x, offHand));
    }

    private ItemStack resolvePouchStack(EntityPlayer player, int slotIndex, boolean offHand) {
        return offHand ? player.getHeldItemOffhand() : player.inventory.getStackInSlot(slotIndex);
    }
}