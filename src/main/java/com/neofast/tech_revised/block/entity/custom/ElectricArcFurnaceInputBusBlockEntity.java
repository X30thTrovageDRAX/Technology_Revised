package com.neofast.tech_revised.block.entity.custom;

import com.neofast.tech_revised.block.entity.ModBlockEntities;
import com.neofast.tech_revised.item.ModItems;
import com.neofast.tech_revised.screen.ElectricArcFurnaceInputBusMenu;
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

public class ElectricArcFurnaceInputBusBlockEntity extends BlockEntity implements MenuProvider {
    private static final int PRIMARY_INPUT_SLOT = 0;
    private static final int SECONDARY_INPUT_SLOT = 1;
    private static final int ELECTRODE_SLOT = 2;

    private final ItemStackHandler itemHandler = new ItemStackHandler(3) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == ELECTRODE_SLOT) {
                return stack.is(ModItems.ELECTRODE.get());
            }
            if (slot == PRIMARY_INPUT_SLOT || slot == SECONDARY_INPUT_SLOT) {
                return !stack.is(ModItems.ELECTRODE.get());
            }
            return false;
        }

        @Override
        public int getSlotLimit(int slot) {
            if (slot == ELECTRODE_SLOT) {
                return 1;
            }
            return super.getSlotLimit(slot);
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public ElectricArcFurnaceInputBusBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(ModBlockEntities.ELECTRIC_ARC_FURNACE_INPUT_BUS.get(), worldPosition, blockState);
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
        CompoundTag inventoryTag = tag.getCompound("inventory");
        // Backward-compat: old worlds can have size=2 before electrode slot existed.
        inventoryTag.putInt("Size", 3);
        itemHandler.deserializeNBT(inventoryTag);
    }

    public ItemStack getPrimaryInputStack() {
        return itemHandler.getStackInSlot(PRIMARY_INPUT_SLOT);
    }

    public ItemStack getSecondaryInputStack() {
        return itemHandler.getStackInSlot(SECONDARY_INPUT_SLOT);
    }

    public boolean hasElectrode() {
        return itemHandler.getStackInSlot(ELECTRODE_SLOT).is(ModItems.ELECTRODE.get());
    }

    public void damageElectrode() {
        ItemStack electrode = itemHandler.getStackInSlot(ELECTRODE_SLOT);
        if (!electrode.is(ModItems.ELECTRODE.get())) {
            return;
        }

        if (!electrode.isDamageableItem()) {
            itemHandler.extractItem(ELECTRODE_SLOT, 1, false);
            setChanged();
            return;
        }

        int nextDamage = electrode.getDamageValue() + 1;
        if (nextDamage >= electrode.getMaxDamage()) {
            itemHandler.extractItem(ELECTRODE_SLOT, 1, false);
        } else {
            electrode.setDamageValue(nextDamage);
            itemHandler.setStackInSlot(ELECTRODE_SLOT, electrode);
        }
        setChanged();
    }

    public void extractPrimaryOne() {
        itemHandler.extractItem(PRIMARY_INPUT_SLOT, 1, false);
        setChanged();
    }

    public void extractSecondaryOne() {
        itemHandler.extractItem(SECONDARY_INPUT_SLOT, 1, false);
        setChanged();
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(level, worldPosition, inventory);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ElectricArcFurnaceInputBusBlockEntity blockEntity) {
        // No periodic logic for this bus.
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.tech_revised.electric_arc_furnace_input_bus");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ElectricArcFurnaceInputBusMenu(containerId, inventory, this);
    }
}
