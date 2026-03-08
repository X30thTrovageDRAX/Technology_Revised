package com.neofast.tech_revised.block.entity.custom;

import com.neofast.tech_revised.block.custom.CrusherBlock;
import com.neofast.tech_revised.block.entity.ModBlockEntities;
import com.neofast.tech_revised.recipe.CrusherRecipe;
import com.neofast.tech_revised.screen.CrusherMenu;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Optional;

public class CrusherBlockEntity extends BlockEntity implements MenuProvider {
    private static final int DEFAULT_PROCESS_TICKS = 100;
    private static final int ENERGY_CAPACITY = 30000;
    private static final int MAX_ENERGY_RECEIVE = 1000;
    private static final int DEFAULT_ENERGY_PER_TICK = 20;
    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;

    private int progress = 0;

    private final ItemStackHandler itemHandler = new ItemStackHandler(2) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == INPUT_SLOT) {
                return isValidInput(stack);
            }
            return false;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final EnergyStorage energyStorage = new EnergyStorage(ENERGY_CAPACITY, MAX_ENERGY_RECEIVE, ENERGY_CAPACITY) {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int received = super.receiveEnergy(maxReceive, simulate);
            if (!simulate && received > 0) {
                setChanged();
            }
            return received;
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private LazyOptional<IEnergyStorage> lazyEnergy = LazyOptional.empty();

    public CrusherBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(ModBlockEntities.CRUSHER.get(), worldPosition, blockState);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        if (cap == ForgeCapabilities.ENERGY) {
            return lazyEnergy.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
        lazyEnergy = LazyOptional.of(() -> energyStorage);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
        lazyEnergy.invalidate();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        tag.put("inventory", itemHandler.serializeNBT());
        tag.put("energy", energyStorage.serializeNBT());
        tag.putInt("crusher_progress", progress);
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
        if (tag.contains("energy")) {
            energyStorage.deserializeNBT(tag.get("energy"));
        }
        progress = tag.getInt("crusher_progress");
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CrusherBlockEntity blockEntity) {
        if (level.isClientSide()) {
            return;
        }

        CrusherRecipe recipe = blockEntity.getCurrentRecipe();
        boolean canProcess = recipe != null && blockEntity.canProcess(recipe);
        int energyPerTick = recipe != null ? recipe.getEnergyPerTick() : DEFAULT_ENERGY_PER_TICK;
        boolean hasEnergy = blockEntity.energyStorage.getEnergyStored() >= energyPerTick;

        if (!canProcess || !hasEnergy) {
            setActive(level, pos, false);
            blockEntity.resetProgress();
            return;
        }

        setActive(level, pos, true);

        blockEntity.energyStorage.extractEnergy(energyPerTick, false);
        blockEntity.progress++;
        if (blockEntity.progress < recipe.getProcessTicks()) {
            blockEntity.setChanged();
            return;
        }

        blockEntity.finishProcessing(recipe);
        blockEntity.progress = 0;
        blockEntity.setChanged();

        CrusherRecipe nextRecipe = blockEntity.getCurrentRecipe();
        boolean canContinue = nextRecipe != null &&
                blockEntity.canProcess(nextRecipe) &&
                blockEntity.energyStorage.getEnergyStored() >= nextRecipe.getEnergyPerTick();
        setActive(level, pos, canContinue);
    }

    private static void setActive(Level level, BlockPos pos, boolean active) {
        BlockState currentState = level.getBlockState(pos);
        if (!(currentState.getBlock() instanceof CrusherBlock)) {
            return;
        }

        if (currentState.getValue(CrusherBlock.ACTIVE) != active) {
            level.setBlock(pos, currentState.setValue(CrusherBlock.ACTIVE, active), Block.UPDATE_CLIENTS);
        }
    }

    private boolean canProcess(CrusherRecipe recipe) {
        ItemStack input = itemHandler.getStackInSlot(INPUT_SLOT);
        if (input.isEmpty()) {
            return false;
        }

        ItemStack recipeOutput = recipe.getResultItem(level.registryAccess()).copy();
        if (recipeOutput.isEmpty()) {
            return false;
        }

        ItemStack output = itemHandler.getStackInSlot(OUTPUT_SLOT);
        if (output.isEmpty()) {
            return true;
        }

        if (!ItemStack.isSameItemSameTags(output, recipeOutput)) {
            return false;
        }

        return output.getCount() + recipeOutput.getCount() <= output.getMaxStackSize();
    }

    private void finishProcessing(CrusherRecipe recipe) {
        ItemStack recipeOutput = recipe.getResultItem(level.registryAccess()).copy();
        if (recipeOutput.isEmpty()) {
            return;
        }

        itemHandler.extractItem(INPUT_SLOT, 1, false);

        ItemStack output = itemHandler.getStackInSlot(OUTPUT_SLOT);
        if (output.isEmpty()) {
            itemHandler.setStackInSlot(OUTPUT_SLOT, recipeOutput);
            return;
        }

        output.grow(recipeOutput.getCount());
        itemHandler.setStackInSlot(OUTPUT_SLOT, output);
    }

    private void resetProgress() {
        if (this.progress != 0) {
            this.progress = 0;
            setChanged();
        }
    }

    public int getProgress() {
        return progress;
    }

    public int getProcessTicks() {
        CrusherRecipe recipe = getCurrentRecipe();
        return recipe != null ? recipe.getProcessTicks() : DEFAULT_PROCESS_TICKS;
    }

    public int getEnergyStored() {
        return energyStorage.getEnergyStored();
    }

    public int getMaxEnergyStored() {
        return energyStorage.getMaxEnergyStored();
    }

    public void drops() {
        if (level == null) {
            return;
        }
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(level, worldPosition, inventory);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.tech_revised.crusher");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new CrusherMenu(containerId, inventory, this);
    }

    @Nullable
    private CrusherRecipe getCurrentRecipe() {
        if (level == null) {
            return null;
        }

        SimpleContainer inventory = new SimpleContainer(1);
        inventory.setItem(0, itemHandler.getStackInSlot(INPUT_SLOT).copy());
        Optional<CrusherRecipe> recipe = level.getRecipeManager().getRecipeFor(CrusherRecipe.Type.INSTANCE, inventory, level);
        return recipe.orElse(null);
    }

    public boolean isValidInput(ItemStack stack) {
        if (level == null) {
            return true;
        }

        SimpleContainer inventory = new SimpleContainer(1);
        inventory.setItem(0, stack.copy());
        return level.getRecipeManager().getRecipeFor(CrusherRecipe.Type.INSTANCE, inventory, level).isPresent();
    }
}
