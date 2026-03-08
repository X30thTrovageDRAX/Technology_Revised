package com.neofast.tech_revised.block.entity.custom;

import com.neofast.tech_revised.block.entity.ModBlockEntities;
import com.neofast.tech_revised.integration.vm.VmCommandIntegration;
import com.neofast.tech_revised.screen.Windows7VmMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Windows7VmBlockEntity extends BlockEntity implements MenuProvider {
    public static final int MAX_VM_NAME_LENGTH = 64;
    public static final int MAX_COMMAND_LENGTH = 512;

    private static final String VM_NAME_TAG = "vm_name";
    private static final String START_COMMAND_TAG = "start_command";
    private static final String STOP_COMMAND_TAG = "stop_command";

    private String vmName = VmCommandIntegration.getConfiguredVmName();
    private String startCommand = VmCommandIntegration.getConfiguredStartTemplate();
    private String stopCommand = VmCommandIntegration.getConfiguredStopTemplate();

    public Windows7VmBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WINDOWS_7_VM_BLOCK.get(), pos, state);
    }

    public String getVmName() {
        return vmName;
    }

    public String getStartCommand() {
        return startCommand;
    }

    public String getStopCommand() {
        return stopCommand;
    }

    public void applyConfig(String vmName, String startCommand, String stopCommand) {
        this.vmName = sanitizeVmName(vmName);
        this.startCommand = sanitizeCommand(startCommand, VmCommandIntegration.getConfiguredStartTemplate());
        this.stopCommand = sanitizeCommand(stopCommand, VmCommandIntegration.getConfiguredStopTemplate());
        setChanged();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        tag.putString(VM_NAME_TAG, vmName);
        tag.putString(START_COMMAND_TAG, startCommand);
        tag.putString(STOP_COMMAND_TAG, stopCommand);
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        vmName = sanitizeVmName(tag.getString(VM_NAME_TAG));
        startCommand = sanitizeCommand(tag.getString(START_COMMAND_TAG), VmCommandIntegration.getConfiguredStartTemplate());
        stopCommand = sanitizeCommand(tag.getString(STOP_COMMAND_TAG), VmCommandIntegration.getConfiguredStopTemplate());
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("screen.tech_revised.windows_7_vm_config");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new Windows7VmMenu(containerId, inventory, this);
    }

    private static String sanitizeVmName(String value) {
        String trimmed = truncate(value, MAX_VM_NAME_LENGTH).trim();
        if (!trimmed.isEmpty()) {
            return trimmed;
        }
        return VmCommandIntegration.getConfiguredVmName();
    }

    private static String sanitizeCommand(String value, String fallback) {
        String trimmed = truncate(value, MAX_COMMAND_LENGTH).trim();
        return trimmed.isEmpty() ? fallback : trimmed;
    }

    private static String truncate(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
