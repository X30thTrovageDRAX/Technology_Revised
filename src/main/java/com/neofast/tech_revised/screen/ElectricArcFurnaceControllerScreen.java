package com.neofast.tech_revised.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ElectricArcFurnaceControllerScreen extends AbstractContainerScreen<ElectricArcFurnaceControllerMenu> {
    public ElectricArcFurnaceControllerScreen(ElectricArcFurnaceControllerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 86;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // Intentionally blank: controller GUI is text-only.
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int processTicks = Math.max(1, menu.getProcessTicks());
        int progressPercent = menu.getProgress() * 100 / processTicks;
        guiGraphics.drawString(this.font, this.title, 8, 8, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, Component.literal("Progress: " + progressPercent + "%"), 8, 24, 0xFFFFFF, false);
        guiGraphics.drawString(this.font,
                Component.literal("Structure: " + (menu.isStructureFormed() ? "Formed" : "Incomplete")),
                8, 36, menu.isStructureFormed() ? 0x55FF55 : 0xFF5555, false);

        int energyPercent = menu.getEnergyStored() * 100 / menu.getMaxEnergyStored();
        int waterPercent = menu.getWaterAmount() * 100 / menu.getWaterCapacity();
        guiGraphics.drawString(this.font,
                Component.literal("Energy: " + menu.getEnergyStored() + " FE (" + energyPercent + "%)"),
                8, 48, 0xFFFFFF, false);
        guiGraphics.drawString(this.font,
                Component.literal("Cooling Water: " + menu.getWaterAmount() + " mB (" + waterPercent + "%)"),
                8, 60, 0xFFFFFF, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
