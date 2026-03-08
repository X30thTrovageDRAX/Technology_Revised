package com.neofast.tech_revised.block.entity.custom;

import com.neofast.tech_revised.block.entity.ModBlockEntities;
import com.neofast.tech_revised.screen.ElectricArcFurnaceOutputBusMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class ElectricArcFurnaceOutputBusBlockEntity extends BlockEntity implements MenuProvider {
    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public ElectricArcFurnaceOutputBusBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(ModBlockEntities.ELECTRIC_ARC_FURNACE_OUTPUT_BUS.get(), worldPosition, blockState);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        tag.put("inventory", itemHandler.serializeNBT());
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
    }

    public boolean canAccept(ItemStack stack) {
        ItemStack inSlot = itemHandler.getStackInSlot(0);
        if (inSlot.isEmpty()) {
            return true;
        }

        if (!ItemStack.isSameItemSameTags(inSlot, stack)) {
            return false;
        }

        return inSlot.getCount() + stack.getCount() <= inSlot.getMaxStackSize();
    }

    public void insert(ItemStack stack) {
        ItemStack inSlot = itemHandler.getStackInSlot(0);
        if (inSlot.isEmpty()) {
            itemHandler.setStackInSlot(0, stack.copy());
        } else {
            inSlot.grow(stack.getCount());
            itemHandler.setStackInSlot(0, inSlot);
        }
        setChanged();
    }

    public ItemStack extractAll() {
        ItemStack inSlot = itemHandler.getStackInSlot(0);
        if (inSlot.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack extracted = inSlot.copy();
        itemHandler.setStackInSlot(0, ItemStack.EMPTY);
        setChanged();
        return extracted;
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(level, worldPosition, inventory);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ElectricArcFurnaceOutputBusBlockEntity blockEntity) {
        // No periodic logic for this bus.
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.tech_revised.electric_arc_furnace_output_bus");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ElectricArcFurnaceOutputBusMenu(containerId, inventory, this);
    }
}
