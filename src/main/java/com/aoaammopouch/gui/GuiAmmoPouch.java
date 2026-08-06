package com.aoaammopouch.gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.aoaammopouch.Tags;


@SideOnly(Side.CLIENT)
public class GuiAmmoPouch extends GuiContainer {

    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(Tags.MODID, "textures/guis/ammo_pouch.png");
    // TODO: Should we have a texture per row count?
    private static final ResourceLocation SLOT_TEXTURE = new ResourceLocation(Tags.MODID, "textures/guis/slot.png");

    private final ContainerAmmoPouch container;

    public GuiAmmoPouch(ContainerAmmoPouch inventorySlotsIn) {
        super(inventorySlotsIn);
        this.container = inventorySlotsIn;
        this.xSize = AmmoPouchLayout.GUI_WIDTH;
        this.ySize = AmmoPouchLayout.GUI_HEIGHT;
    }

    private static String formatAmmoCount(int count) {
        if (count < 1000) {
            return Integer.toString(count);
        } else if (count < 1000000) {
            return String.format("%.1fk", count / 1000.0);
        } else if (count < 1000000000) {
            return String.format("%.1fM", count / 1000000.0);
        }

        return String.format("%.1fB", count / 1000000000.0);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        // Set the counts to 1 to mute the default stack count rendering
        int pouchSlotCount = this.container.getPouchSlotCount();
        int[] actualCounts = new int[pouchSlotCount];

        for (int slotIndex = 0; slotIndex < pouchSlotCount; slotIndex++) {
            ItemStack stack = this.inventorySlots.getSlot(slotIndex).getStack();

            if (stack.isEmpty() || stack.getCount() <= 1) continue;

            actualCounts[slotIndex] = stack.getCount();
            stack.setCount(1);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);

        // ...and render the counts ourself to use a more readable format
        for (int slotIndex = 0; slotIndex < pouchSlotCount; slotIndex++) {
            if (actualCounts[slotIndex] <= 1) continue;

            ItemStack stack = this.inventorySlots.getSlot(slotIndex).getStack();
            stack.setCount(actualCounts[slotIndex]);
        }

        drawPouchCounts(actualCounts);
        renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        // Draw the background texture (pouch GUI)
        GL11.glColor4f(1f, 1f, 1f, 1f);
        this.mc.getTextureManager().bindTexture(BACKGROUND_TEXTURE);
        drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);

        // Draw pouch slot backgrounds at half opacity to blend with the background texture
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);
        GL11.glColor4f(1f, 1f, 1f, 0.5f);
        this.mc.getTextureManager().bindTexture(SLOT_TEXTURE);

        for (int slotIndex = 0; slotIndex < this.container.getPouchSlotCount(); slotIndex++) {
            Slot slot = this.inventorySlots.getSlot(slotIndex);
            drawScaledCustomSizeModalRect(
                this.guiLeft + slot.xPos - 1, this.guiTop + slot.yPos - 1,
                0, 0, 18, 18,
                18, 18, 32, 32);
        }

        GL11.glColor4f(1f, 1f, 1f, 1f);
        GlStateManager.disableBlend();
    }

    private void drawPouchCounts(int[] actualCounts) {
        int pouchSlotCount = this.container.getPouchSlotCount();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, 300);
        GlStateManager.scale(0.5f, 0.5f, 1f);  // 0.5 scale because we can reach max int
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableBlend();
        GL11.glColor4f(1f, 1f, 1f, 1f);

        for (int slotIndex = 0; slotIndex < pouchSlotCount; slotIndex++) {
            int count = actualCounts[slotIndex];

            if (count <= 1) continue;

            Slot slot = this.inventorySlots.getSlot(slotIndex);
            String text = formatAmmoCount(count);
            int scaledX = (this.guiLeft + slot.xPos + 16) * 2 - this.fontRenderer.getStringWidth(text);
            int scaledY = (this.guiTop + slot.yPos + 12) * 2;

            this.fontRenderer.drawStringWithShadow(text, scaledX, scaledY, 0xFFFFFF);
        }

        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        GlStateManager.enableBlend();

        GlStateManager.popMatrix();
    }
}