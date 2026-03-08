package com.neofast.tech_revised.screen;

import com.neofast.tech_revised.block.ModBlocks;
import com.neofast.tech_revised.block.entity.custom.Windows7VmBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;

public class Windows7VmMenu extends AbstractContainerMenu {
    private final BlockPos blockPos;
    private final String vmName;
    private final String startCommand;
    private final String stopCommand;
    private final net.minecraft.world.level.Level level;

    public Windows7VmMenu(int containerId, Inventory inventory, FriendlyByteBuf extraData) {
        this(
                containerId,
                inventory,
                extraData.readBlockPos(),
                extraData.readUtf(Windows7VmBlockEntity.MAX_VM_NAME_LENGTH),
                extraData.readUtf(Windows7VmBlockEntity.MAX_COMMAND_LENGTH),
                extraData.readUtf(Windows7VmBlockEntity.MAX_COMMAND_LENGTH)
        );
    }

    public Windows7VmMenu(int containerId, Inventory inventory, Windows7VmBlockEntity blockEntity) {
        this(containerId, inventory, blockEntity.getBlockPos(), blockEntity.getVmName(), blockEntity.getStartCommand(), blockEntity.getStopCommand());
    }

    private Windows7VmMenu(int containerId, Inventory inventory, BlockPos blockPos, String vmName, String startCommand, String stopCommand) {
        super(ModMenuTypes.WINDOWS_7_VM_MENU.get(), containerId);
        this.level = inventory.player.level();
        this.blockPos = blockPos;
        this.vmName = vmName;
        this.startCommand = startCommand;
        this.stopCommand = stopCommand;
    }

    public BlockPos getBlockPos() {
        return blockPos;
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

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockPos), player, ModBlocks.WINDOWS_7_VM_BLOCK.get());
    }
}
