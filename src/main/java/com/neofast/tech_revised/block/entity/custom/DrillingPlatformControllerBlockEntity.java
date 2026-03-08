package com.neofast.tech_revised.block.entity.custom;

import com.neofast.tech_revised.block.custom.DrillingPlatformControllerBlock;
import com.neofast.tech_revised.block.entity.ModBlockEntities;
import com.neofast.tech_revised.recipe.DrillingPlatformRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Optional;

public class DrillingPlatformControllerBlockEntity extends BlockEntity {
    private static final int DEFAULT_PROCESS_TICKS = 80;
    private static final int ENERGY_CAPACITY = 50000;
    private static final int ENERGY_TRANSFER_PER_TICK = 1000;

    private int progress = 0;

    private final EnergyStorage energyStorage = new EnergyStorage(ENERGY_CAPACITY, 1000, 0) {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int received = super.receiveEnergy(maxReceive, simulate);
            if (!simulate && received > 0) {
                setChanged();
            }
            return received;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int extracted = super.extractEnergy(maxExtract, simulate);
            if (!simulate && extracted > 0) {
                setChanged();
            }
            return extracted;
        }
    };

    private LazyOptional<IEnergyStorage> lazyEnergy = LazyOptional.empty();

    public DrillingPlatformControllerBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(ModBlockEntities.DRILLING_PLATFORM_CONTROLLER.get(), worldPosition, blockState);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return lazyEnergy.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyEnergy = LazyOptional.of(() -> energyStorage);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyEnergy.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.putInt("drilling_progress", progress);
        tag.put("drilling_energy", energyStorage.serializeNBT());
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        progress = tag.getInt("drilling_progress");
        if (tag.contains("drilling_energy")) {
            energyStorage.deserializeNBT(tag.get("drilling_energy"));
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DrillingPlatformControllerBlockEntity blockEntity) {
        if (level.isClientSide()) {
            return;
        }

        Direction front = state.getValue(DrillingPlatformControllerBlock.FACING);
        boolean formed = DrillingPlatformControllerBlock.isStructureValid(level, pos, front);
        DrillingPlatformControllerBlock.updateFrameFormedStates(level, pos, front, formed);
        if (!formed) {
            blockEntity.resetProgress();
            return;
        }

        BlockEntity energyInputEntity = level.getBlockEntity(DrillingPlatformControllerBlock.getEnergyInputHatchPos(pos, front));
        BlockEntity fluidOutputEntity = level.getBlockEntity(DrillingPlatformControllerBlock.getFluidOutputBusPos(pos, front));

        if (!(energyInputEntity instanceof ElectricArcFurnaceEnergyInputHatchBlockEntity energyInputHatch) ||
                !(fluidOutputEntity instanceof ElectricArcFurnaceFluidOutputBusBlockEntity fluidOutputBus)) {
            blockEntity.resetProgress();
            return;
        }

        Optional<DrillingPlatformRecipe> recipe = getRecipe(level);
        if (recipe.isEmpty()) {
            blockEntity.resetProgress();
            return;
        }

        DrillingPlatformRecipe currentRecipe = recipe.get();

        blockEntity.pullEnergyFromHatch(energyInputHatch);

        if (!blockEntity.hasRequiredEnergy(currentRecipe.getEnergyPerTick())) {
            blockEntity.resetProgress();
            return;
        }

        FluidStack outputStack = currentRecipe.getOutputFluid();
        if (fluidOutputBus.fill(outputStack, IFluidHandler.FluidAction.SIMULATE) < outputStack.getAmount()) {
            blockEntity.resetProgress();
            return;
        }

        blockEntity.energyStorage.extractEnergy(currentRecipe.getEnergyPerTick(), false);
        blockEntity.progress++;
        if (blockEntity.progress < currentRecipe.getProcessTicks()) {
            blockEntity.setChanged();
            return;
        }

        fluidOutputBus.fill(outputStack, IFluidHandler.FluidAction.EXECUTE);
        blockEntity.progress = 0;
        blockEntity.setChanged();
    }

    private void pullEnergyFromHatch(ElectricArcFurnaceEnergyInputHatchBlockEntity energyInputHatch) {
        int freeSpace = energyStorage.getMaxEnergyStored() - energyStorage.getEnergyStored();
        if (freeSpace <= 0) {
            return;
        }

        int maxTransfer = Math.min(freeSpace, ENERGY_TRANSFER_PER_TICK);
        int simulatedExtract = energyInputHatch.extractEnergy(maxTransfer, true);
        if (simulatedExtract <= 0) {
            return;
        }

        int accepted = energyStorage.receiveEnergy(simulatedExtract, false);
        if (accepted > 0) {
            energyInputHatch.extractEnergy(accepted, false);
        }
    }

    private boolean hasRequiredEnergy(int energyPerTick) {
        return energyStorage.getEnergyStored() >= energyPerTick;
    }

    private static Optional<DrillingPlatformRecipe> getRecipe(Level level) {
        return level.getRecipeManager().getAllRecipesFor(DrillingPlatformRecipe.Type.INSTANCE).stream().findFirst();
    }

    private void resetProgress() {
        if (progress != 0) {
            progress = 0;
            setChanged();
        }
    }

    public int getProgress() {
        return progress;
    }

    public int getProcessTicks() {
        return DEFAULT_PROCESS_TICKS;
    }

    public int getEnergyStored() {
        return energyStorage.getEnergyStored();
    }

    public int getMaxEnergyStored() {
        return energyStorage.getMaxEnergyStored();
    }
}
