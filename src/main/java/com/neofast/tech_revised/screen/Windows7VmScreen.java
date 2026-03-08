package com.neofast.tech_revised.screen;

import com.mojang.blaze3d.platform.NativeImage;
import com.neofast.tech_revised.TechRevised;
import com.neofast.tech_revised.client.VmScreenClientState;
import com.neofast.tech_revised.networking.ModNetworking;
import com.neofast.tech_revised.networking.packet.RequestVmScreenshotPacket;
import com.neofast.tech_revised.networking.packet.SaveVmConfigPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.io.ByteArrayInputStream;
import java.util.UUID;

public class Windows7VmScreen extends AbstractContainerScreen<Windows7VmMenu> {
    private static final int LEFT_PANEL_X = 12;
    private static final int FIELD_WIDTH = 170;
    private static final int PREVIEW_X = 198;
    private static final int PREVIEW_Y = 34;
    private static final int PREVIEW_WIDTH = 160;
    private static final int PREVIEW_HEIGHT = 112;

    private EditBox vmNameBox;
    private EditBox startCommandBox;
    private EditBox stopCommandBox;
    private ResourceLocation previewTextureLocation;
    private int previewTextureWidth = 1;
    private int previewTextureHeight = 1;
    private long lastPreviewUpdateId = -1L;
    private String previewStatus = "";
    private int refreshTicks = 0;

    public Windows7VmScreen(Windows7VmMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 370;
        this.imageHeight = 206;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 12;
        this.titleLabelY = 10;

        int left = this.leftPos + LEFT_PANEL_X;
        int fieldWidth = FIELD_WIDTH;

        vmNameBox = new EditBox(this.font, left, this.topPos + 34, fieldWidth, 20,
                Component.translatable("screen.tech_revised.vm.field.vm_name"));
        vmNameBox.setMaxLength(64);
        vmNameBox.setValue(menu.getVmName());
        addRenderableWidget(vmNameBox);

        startCommandBox = new EditBox(this.font, left, this.topPos + 78, fieldWidth, 20,
                Component.translatable("screen.tech_revised.vm.field.start_command"));
        startCommandBox.setMaxLength(512);
        startCommandBox.setValue(menu.getStartCommand());
        addRenderableWidget(startCommandBox);

        stopCommandBox = new EditBox(this.font, left, this.topPos + 122, fieldWidth, 20,
                Component.translatable("screen.tech_revised.vm.field.stop_command"));
        stopCommandBox.setMaxLength(512);
        stopCommandBox.setValue(menu.getStopCommand());
        addRenderableWidget(stopCommandBox);

        addRenderableWidget(Button.builder(
                        Component.translatable("screen.tech_revised.vm.button.save"),
                        button -> saveConfig())
                .bounds(left, this.topPos + 156, 54, 20)
                .build());

        addRenderableWidget(Button.builder(
                        Component.translatable("screen.tech_revised.vm.button.refresh"),
                        button -> requestScreenshot())
                .bounds(left + 58, this.topPos + 156, 54, 20)
                .build());

        addRenderableWidget(Button.builder(
                        Component.translatable("screen.tech_revised.vm.button.close"),
                        button -> onClose())
                .bounds(left + fieldWidth - 54, this.topPos + 156, 54, 20)
                .build());

        previewStatus = Component.translatable("screen.tech_revised.vm.preview.waiting").getString();
        requestScreenshot();
        setInitialFocus(vmNameBox);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        vmNameBox.tick();
        startCommandBox.tick();
        stopCommandBox.tick();

        pollScreenshotUpdate();

        if (--refreshTicks <= 0) {
            requestScreenshot();
            refreshTicks = 40;
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;
        guiGraphics.fill(left, top, left + this.imageWidth, top + this.imageHeight, 0xFF1E1F23);
        guiGraphics.fill(left + 1, top + 1, left + this.imageWidth - 1, top + this.imageHeight - 1, 0xFF2B2E36);
        guiGraphics.fill(left + 10, top + 28, left + this.imageWidth - 10, top + 30, 0xFF5A6070);

        int previewLeft = left + PREVIEW_X;
        int previewTop = top + PREVIEW_Y;
        guiGraphics.fill(previewLeft, previewTop, previewLeft + PREVIEW_WIDTH, previewTop + PREVIEW_HEIGHT, 0xFF111111);
        guiGraphics.fill(previewLeft + 1, previewTop + 1, previewLeft + PREVIEW_WIDTH - 1, previewTop + PREVIEW_HEIGHT - 1, 0xFF000000);

        if (previewTextureLocation != null) {
            int availableWidth = PREVIEW_WIDTH - 2;
            int availableHeight = PREVIEW_HEIGHT - 2;
            float scale = Math.min(availableWidth / (float) previewTextureWidth, availableHeight / (float) previewTextureHeight);
            int drawWidth = Math.max(1, Math.round(previewTextureWidth * scale));
            int drawHeight = Math.max(1, Math.round(previewTextureHeight * scale));
            int drawX = previewLeft + 1 + (availableWidth - drawWidth) / 2;
            int drawY = previewTop + 1 + (availableHeight - drawHeight) / 2;

            guiGraphics.blit(previewTextureLocation, drawX, drawY, 0, 0, drawWidth, drawHeight, previewTextureWidth, previewTextureHeight);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xE0E0E0, false);
        guiGraphics.drawString(this.font,
                Component.translatable("screen.tech_revised.vm.field.vm_name"),
                12, 23, 0xC6CBD8, false);
        guiGraphics.drawString(this.font,
                Component.translatable("screen.tech_revised.vm.field.start_command"),
                12, 67, 0xC6CBD8, false);
        guiGraphics.drawString(this.font,
                Component.translatable("screen.tech_revised.vm.field.stop_command"),
                12, 111, 0xC6CBD8, false);
        guiGraphics.drawString(this.font,
                Component.translatable("screen.tech_revised.vm.preview"),
                PREVIEW_X, 23, 0xC6CBD8, false);
        guiGraphics.drawString(this.font,
                this.font.plainSubstrByWidth(previewStatus, PREVIEW_WIDTH),
                PREVIEW_X, 150, 0xAAB2C5, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (vmNameBox.mouseClicked(mouseX, mouseY, button)
                || startCommandBox.mouseClicked(mouseX, mouseY, button)
                || stopCommandBox.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (vmNameBox.keyPressed(keyCode, scanCode, modifiers)
                || startCommandBox.keyPressed(keyCode, scanCode, modifiers)
                || stopCommandBox.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (vmNameBox.charTyped(codePoint, modifiers)
                || startCommandBox.charTyped(codePoint, modifiers)
                || stopCommandBox.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public void removed() {
        releasePreviewTexture();
        super.removed();
    }

    private void saveConfig() {
        ModNetworking.CHANNEL.sendToServer(new SaveVmConfigPacket(
                menu.getBlockPos(),
                vmNameBox.getValue(),
                startCommandBox.getValue(),
                stopCommandBox.getValue()));
    }

    private void requestScreenshot() {
        previewStatus = Component.translatable("screen.tech_revised.vm.preview.requesting").getString();
        ModNetworking.CHANNEL.sendToServer(new RequestVmScreenshotPacket(menu.getBlockPos()));
    }

    private void pollScreenshotUpdate() {
        VmScreenClientState.VmFrameData frameData = VmScreenClientState.get(menu.getBlockPos());
        if (frameData == null || frameData.updateId() == lastPreviewUpdateId) {
            return;
        }

        lastPreviewUpdateId = frameData.updateId();
        previewStatus = frameData.message();
        if (frameData.success() && frameData.imageBytes().length > 0) {
            updatePreviewTexture(frameData.imageBytes());
        } else {
            releasePreviewTexture();
        }
    }

    private void updatePreviewTexture(byte[] imageBytes) {
        try (ByteArrayInputStream stream = new ByteArrayInputStream(imageBytes)) {
            NativeImage image = NativeImage.read(stream);
            if (image == null) {
                previewStatus = Component.translatable("screen.tech_revised.vm.preview.decode_failed").getString();
                releasePreviewTexture();
                return;
            }

            if (previewTextureLocation == null) {
                previewTextureLocation = new ResourceLocation(TechRevised.MOD_ID, "vm_preview/" + UUID.randomUUID());
            } else {
                Minecraft.getInstance().getTextureManager().release(previewTextureLocation);
            }

            DynamicTexture texture = new DynamicTexture(image);
            previewTextureWidth = Math.max(1, image.getWidth());
            previewTextureHeight = Math.max(1, image.getHeight());
            Minecraft.getInstance().getTextureManager().register(previewTextureLocation, texture);
        } catch (Exception exception) {
            previewStatus = Component.translatable("screen.tech_revised.vm.preview.decode_failed").getString();
            releasePreviewTexture();
        }
    }

    private void releasePreviewTexture() {
        if (previewTextureLocation != null) {
            Minecraft.getInstance().getTextureManager().release(previewTextureLocation);
            previewTextureLocation = null;
        }
    }
}
