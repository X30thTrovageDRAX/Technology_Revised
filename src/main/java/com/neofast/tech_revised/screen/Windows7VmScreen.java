package com.neofast.tech_revised.screen;

import com.mojang.blaze3d.platform.NativeImage;
import com.neofast.tech_revised.TechRevised;
import com.neofast.tech_revised.client.VmScreenClientState;
import com.neofast.tech_revised.integration.vm.VmCommandIntegration;
import com.neofast.tech_revised.networking.ModNetworking;
import com.neofast.tech_revised.networking.packet.RequestVmScreenshotPacket;
import com.neofast.tech_revised.networking.packet.SaveVmConfigPacket;
import com.neofast.tech_revised.networking.packet.VmKeyboardInputPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

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
    private Button saveButton;
    private Button refreshButton;
    private Button closeButton;
    private ResourceLocation previewTextureLocation;
    private int previewTextureWidth = 1;
    private int previewTextureHeight = 1;
    private long lastPreviewUpdateId = -1L;
    private String previewStatus = "";
    private int refreshTicks = 0;
    private boolean vmFullscreen = false;
    private boolean previewFocused = false;
    private int previewAreaX = 0;
    private int previewAreaY = 0;
    private int previewAreaWidth = 0;
    private int previewAreaHeight = 0;
    private int previewDrawX = -1;
    private int previewDrawY = -1;
    private int previewDrawWidth = 0;
    private int previewDrawHeight = 0;

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

        saveButton = Button.builder(
                        Component.translatable("screen.tech_revised.vm.button.save"),
                        button -> saveConfig())
                .bounds(left, this.topPos + 156, 54, 20)
                .build();
        addRenderableWidget(saveButton);

        refreshButton = Button.builder(
                        Component.translatable("screen.tech_revised.vm.button.refresh"),
                        button -> requestScreenshot())
                .bounds(left + 58, this.topPos + 156, 54, 20)
                .build();
        addRenderableWidget(refreshButton);

        closeButton = Button.builder(
                        Component.translatable("screen.tech_revised.vm.button.close"),
                        button -> onClose())
                .bounds(left + fieldWidth - 54, this.topPos + 156, 54, 20)
                .build();
        addRenderableWidget(closeButton);

        previewStatus = Component.translatable("screen.tech_revised.vm.preview.waiting").getString();
        vmFullscreen = false;
        previewFocused = false;
        setConfigWidgetsVisible(true);
        updatePreviewArea();
        requestScreenshot();
        setInitialFocus(vmNameBox);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if (vmNameBox.visible) {
            vmNameBox.tick();
            startCommandBox.tick();
            stopCommandBox.tick();
        }

        pollScreenshotUpdate();

        if (--refreshTicks <= 0) {
            requestScreenshot();
            refreshTicks = vmFullscreen ? 8 : 40;
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        updatePreviewArea();

        if (vmFullscreen) {
            guiGraphics.fill(0, 0, this.width, this.height, 0xFF050607);
        } else {
            int left = this.leftPos;
            int top = this.topPos;
            guiGraphics.fill(left, top, left + this.imageWidth, top + this.imageHeight, 0xFF1E1F23);
            guiGraphics.fill(left + 1, top + 1, left + this.imageWidth - 1, top + this.imageHeight - 1, 0xFF2B2E36);
            guiGraphics.fill(left + 10, top + 28, left + this.imageWidth - 10, top + 30, 0xFF5A6070);
        }

        int previewLeft = previewAreaX;
        int previewTop = previewAreaY;
        int previewWidth = previewAreaWidth;
        int previewHeight = previewAreaHeight;
        guiGraphics.fill(previewLeft, previewTop, previewLeft + previewWidth, previewTop + previewHeight, 0xFF111111);
        guiGraphics.fill(previewLeft + 1, previewTop + 1, previewLeft + previewWidth - 1, previewTop + previewHeight - 1, 0xFF000000);
        if (previewFocused) {
            guiGraphics.fill(previewLeft, previewTop, previewLeft + previewWidth, previewTop + 1, 0xFF6AA9FF);
            guiGraphics.fill(previewLeft, previewTop + previewHeight - 1, previewLeft + previewWidth, previewTop + previewHeight, 0xFF6AA9FF);
            guiGraphics.fill(previewLeft, previewTop, previewLeft + 1, previewTop + previewHeight, 0xFF6AA9FF);
            guiGraphics.fill(previewLeft + previewWidth - 1, previewTop, previewLeft + previewWidth, previewTop + previewHeight, 0xFF6AA9FF);
        }

        previewDrawX = previewLeft + 1;
        previewDrawY = previewTop + 1;
        previewDrawWidth = previewWidth - 2;
        previewDrawHeight = previewHeight - 2;
        if (previewTextureLocation != null) {
            int availableWidth = previewWidth - 2;
            int availableHeight = previewHeight - 2;
            float scale = Math.min(availableWidth / (float) previewTextureWidth, availableHeight / (float) previewTextureHeight);
            int drawWidth = Math.max(1, Math.round(previewTextureWidth * scale));
            int drawHeight = Math.max(1, Math.round(previewTextureHeight * scale));
            int drawX = previewLeft + 1 + (availableWidth - drawWidth) / 2;
            int drawY = previewTop + 1 + (availableHeight - drawHeight) / 2;

            previewDrawX = drawX;
            previewDrawY = drawY;
            previewDrawWidth = drawWidth;
            previewDrawHeight = drawHeight;
            guiGraphics.blit(previewTextureLocation, drawX, drawY, 0, 0, drawWidth, drawHeight, previewTextureWidth, previewTextureHeight);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (vmFullscreen) {
            return;
        }

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
        guiGraphics.drawString(this.font,
                Component.translatable(previewFocused
                        ? "screen.tech_revised.vm.preview.focused"
                        : "screen.tech_revised.vm.preview.click_to_focus"),
                PREVIEW_X, 162, 0x8A93A8, false);
        guiGraphics.drawString(this.font,
                Component.translatable("screen.tech_revised.vm.preview.f11_hint"),
                PREVIEW_X, 174, 0x8A93A8, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!vmFullscreen) {
            if (vmNameBox.mouseClicked(mouseX, mouseY, button)
                    || startCommandBox.mouseClicked(mouseX, mouseY, button)
                    || stopCommandBox.mouseClicked(mouseX, mouseY, button)) {
                previewFocused = false;
                return true;
            }
        }

        if (isInPreview(mouseX, mouseY)) {
            previewFocused = true;
            vmNameBox.setFocused(false);
            startCommandBox.setFocused(false);
            stopCommandBox.setFocused(false);
            setFocused(null);
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                previewStatus = Component.translatable("screen.tech_revised.vm.preview.mouse_not_supported").getString();
            }
            return true;
        }

        previewFocused = false;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_F11) {
            toggleVmFullscreen();
            return true;
        }

        if (previewFocused) {
            if ((modifiers & GLFW.GLFW_MOD_CONTROL) != 0 && keyCode == GLFW.GLFW_KEY_V) {
                sendTextToVm(Minecraft.getInstance().keyboardHandler.getClipboard());
                return true;
            }

            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                previewFocused = false;
                return true;
            }

            VmCommandIntegration.VmSpecialKey specialKey = mapSpecialKey(keyCode);
            if (specialKey != null) {
                ModNetworking.CHANNEL.sendToServer(VmKeyboardInputPacket.forSpecialKey(menu.getBlockPos(), specialKey));
            }
            return true;
        }

        if (vmNameBox.keyPressed(keyCode, scanCode, modifiers)
                || startCommandBox.keyPressed(keyCode, scanCode, modifiers)
                || stopCommandBox.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (previewFocused) {
            if (!Character.isISOControl(codePoint)) {
                sendTextToVm(String.valueOf(codePoint));
            }
            return true;
        }

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
        if (vmFullscreen) {
            guiGraphics.drawString(this.font,
                    Component.translatable("screen.tech_revised.vm.preview.fullscreen_hint"),
                    8, 8, 0xC6CBD8, false);
            guiGraphics.drawString(this.font,
                    this.font.plainSubstrByWidth(previewStatus, Math.max(1, this.width - 16)),
                    8, this.height - 22, 0xAAB2C5, false);
            guiGraphics.drawString(this.font,
                    Component.translatable(previewFocused
                            ? "screen.tech_revised.vm.preview.focused"
                            : "screen.tech_revised.vm.preview.click_to_focus"),
                    8, this.height - 12, 0x8A93A8, false);
        }
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

    private boolean isInPreview(double mouseX, double mouseY) {
        return mouseX >= previewAreaX + 1
                && mouseX < previewAreaX + previewAreaWidth - 1
                && mouseY >= previewAreaY + 1
                && mouseY < previewAreaY + previewAreaHeight - 1;
    }

    private void sendTextToVm(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }

        int maxChunk = 200;
        for (int i = 0; i < text.length(); i += maxChunk) {
            String chunk = text.substring(i, Math.min(text.length(), i + maxChunk));
            ModNetworking.CHANNEL.sendToServer(VmKeyboardInputPacket.forText(menu.getBlockPos(), chunk));
        }
    }

    private void toggleVmFullscreen() {
        vmFullscreen = !vmFullscreen;
        setConfigWidgetsVisible(!vmFullscreen);
        updatePreviewArea();
        refreshTicks = 0;

        if (vmFullscreen) {
            previewFocused = true;
            vmNameBox.setFocused(false);
            startCommandBox.setFocused(false);
            stopCommandBox.setFocused(false);
            setFocused(null);
        } else {
            previewFocused = false;
            setInitialFocus(vmNameBox);
        }
    }

    private void setConfigWidgetsVisible(boolean visible) {
        vmNameBox.visible = visible;
        vmNameBox.setEditable(visible);
        startCommandBox.visible = visible;
        startCommandBox.setEditable(visible);
        stopCommandBox.visible = visible;
        stopCommandBox.setEditable(visible);

        saveButton.visible = visible;
        saveButton.active = visible;
        refreshButton.visible = visible;
        refreshButton.active = visible;
        closeButton.visible = visible;
        closeButton.active = visible;
    }

    private void updatePreviewArea() {
        if (vmFullscreen) {
            previewAreaX = 4;
            previewAreaY = 22;
            previewAreaWidth = Math.max(40, this.width - 8);
            previewAreaHeight = Math.max(40, this.height - 48);
        } else {
            previewAreaX = this.leftPos + PREVIEW_X;
            previewAreaY = this.topPos + PREVIEW_Y;
            previewAreaWidth = PREVIEW_WIDTH;
            previewAreaHeight = PREVIEW_HEIGHT;
        }
    }

    private VmCommandIntegration.VmSpecialKey mapSpecialKey(int keyCode) {
        return switch (keyCode) {
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> VmCommandIntegration.VmSpecialKey.ENTER;
            case GLFW.GLFW_KEY_BACKSPACE -> VmCommandIntegration.VmSpecialKey.BACKSPACE;
            case GLFW.GLFW_KEY_TAB -> VmCommandIntegration.VmSpecialKey.TAB;
            case GLFW.GLFW_KEY_ESCAPE -> VmCommandIntegration.VmSpecialKey.ESCAPE;
            case GLFW.GLFW_KEY_LEFT -> VmCommandIntegration.VmSpecialKey.LEFT;
            case GLFW.GLFW_KEY_RIGHT -> VmCommandIntegration.VmSpecialKey.RIGHT;
            case GLFW.GLFW_KEY_UP -> VmCommandIntegration.VmSpecialKey.UP;
            case GLFW.GLFW_KEY_DOWN -> VmCommandIntegration.VmSpecialKey.DOWN;
            case GLFW.GLFW_KEY_DELETE -> VmCommandIntegration.VmSpecialKey.DELETE;
            case GLFW.GLFW_KEY_HOME -> VmCommandIntegration.VmSpecialKey.HOME;
            case GLFW.GLFW_KEY_END -> VmCommandIntegration.VmSpecialKey.END;
            case GLFW.GLFW_KEY_PAGE_UP -> VmCommandIntegration.VmSpecialKey.PAGE_UP;
            case GLFW.GLFW_KEY_PAGE_DOWN -> VmCommandIntegration.VmSpecialKey.PAGE_DOWN;
            case GLFW.GLFW_KEY_F1 -> VmCommandIntegration.VmSpecialKey.F1;
            case GLFW.GLFW_KEY_F2 -> VmCommandIntegration.VmSpecialKey.F2;
            case GLFW.GLFW_KEY_F3 -> VmCommandIntegration.VmSpecialKey.F3;
            case GLFW.GLFW_KEY_F4 -> VmCommandIntegration.VmSpecialKey.F4;
            case GLFW.GLFW_KEY_F5 -> VmCommandIntegration.VmSpecialKey.F5;
            case GLFW.GLFW_KEY_F6 -> VmCommandIntegration.VmSpecialKey.F6;
            case GLFW.GLFW_KEY_F7 -> VmCommandIntegration.VmSpecialKey.F7;
            case GLFW.GLFW_KEY_F8 -> VmCommandIntegration.VmSpecialKey.F8;
            case GLFW.GLFW_KEY_F9 -> VmCommandIntegration.VmSpecialKey.F9;
            case GLFW.GLFW_KEY_F10 -> VmCommandIntegration.VmSpecialKey.F10;
            case GLFW.GLFW_KEY_F12 -> VmCommandIntegration.VmSpecialKey.F12;
            default -> null;
        };
    }
}
