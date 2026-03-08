package com.neofast.tech_revised.screen;

import com.neofast.tech_revised.TechRevised;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CrusherScreen extends AbstractContainerScreen<CrusherMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(TechRevised.MOD_ID, "textures/gui/crusher_gui.png");
    private static final int ENERGY_BAR_X = 154;
    private static final int ENERGY_BAR_Y = 18;
    private static final int ENERGY_BAR_WIDTH = 10;
    private static final int ENERGY_BAR_HEIGHT = 54;

    public CrusherScreen(CrusherMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        int left = x + ENERGY_BAR_X;
        int top = y + ENERGY_BAR_Y;
        int right = left + ENERGY_BAR_WIDTH;
        int bottom = top + ENERGY_BAR_HEIGHT;

        guiGraphics.fill(left, top, right, bottom, 0xFF3A3A3A);
        guiGraphics.fill(left + 1, top + 1, right - 1, bottom - 1, 0xFF111111);

        int scaledEnergy = getScaledEnergy(ENERGY_BAR_HEIGHT - 2);
        if (scaledEnergy > 0) {
            guiGraphics.fill(left + 1, bottom - 1 - scaledEnergy, right - 1, bottom - 1, 0xFF2ED05A);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        guiGraphics.drawString(this.font,
                "FE: " + menu.getEnergyStored() + " / " + menu.getMaxEnergyStored(),
                8, 20, 0x404040, false);
        guiGraphics.drawString(this.font,
                "Progress: " + menu.getProgress() + " / " + menu.getProcessTicks(),
                8, 34, 0x404040, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private int getScaledEnergy(int pixels) {
        int maxEnergy = menu.getMaxEnergyStored();
        if (maxEnergy <= 0) {
            return 0;
        }

        return Math.min(pixels, menu.getEnergyStored() * pixels / maxEnergy);
    }
}
