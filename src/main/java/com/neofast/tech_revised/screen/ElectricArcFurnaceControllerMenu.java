package com.neofast.tech_revised.screen;

import com.neofast.tech_revised.block.ModBlocks;
import com.neofast.tech_revised.block.entity.custom.ElectricArcFurnaceControllerBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ElectricArcFurnaceControllerMenu extends AbstractContainerMenu {
    private final ElectricArcFurnaceControllerBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;
    private final int[] syncedData = new int[7];

    public ElectricArcFurnaceControllerMenu(int containerId, Inventory inventory, FriendlyByteBuf extraData) {
        this(containerId, inventory, inventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public ElectricArcFurnaceControllerMenu(int containerId, Inventory inventory, BlockEntity entity) {
        super(ModMenuTypes.ELECTRIC_ARC_FURNACE_CONTROLLER_MENU.get(), containerId);
        this.blockEntity = (ElectricArcFurnaceControllerBlockEntity) entity;
        this.level = inventory.player.level();

        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                if (index < 0 || index >= syncedData.length) {
                    return 0;
                }

                if (level.isClientSide()) {
                    return syncedData[index];
                }

                return switch (index) {
                    case 0 -> blockEntity.getProgress();
                    case 1 -> blockEntity.getProcessTicks();
                    case 2 -> blockEntity.isStructureFormed() ? 1 : 0;
                    case 3 -> blockEntity.getEnergyStored();
                    case 4 -> blockEntity.getMaxEnergyStored();
                    case 5 -> blockEntity.getCoolingWaterAmount();
                    case 6 -> blockEntity.getCoolingWaterCapacity();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                if (index >= 0 && index < syncedData.length) {
                    syncedData[index] = value;
                }
            }

            @Override
            public int getCount() {
                return syncedData.length;
            }
        };
        addDataSlots(this.data);
    }

    public int getProgress() {
        return data.get(0);
    }

    public int getProcessTicks() {
        return data.get(1);
    }

    public boolean isStructureFormed() {
        return data.get(2) == 1;
    }

    public int getEnergyStored() {
        return data.get(3);
    }

    public int getMaxEnergyStored() {
        return Math.max(1, data.get(4));
    }

    public int getWaterAmount() {
        return data.get(5);
    }

    public int getWaterCapacity() {
        return Math.max(1, data.get(6));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player, ModBlocks.ELECTRIC_ARC_FURNACE_CONTROLLER.get());
    }

}
