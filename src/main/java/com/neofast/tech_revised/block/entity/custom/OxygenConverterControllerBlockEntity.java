package com.neofast.tech_revised.block.entity.custom;

import com.neofast.tech_revised.block.custom.OxygenConverterControllerBlock;
import com.neofast.tech_revised.block.entity.ModBlockEntities;
import com.neofast.tech_revised.fluid.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class OxygenConverterControllerBlockEntity extends BlockEntity {
    private static final int DEFAULT_PROCESS_TICKS = 100;
    private static final int ENERGY_CAPACITY = 50000;
    private static final int ENERGY_TRANSFER_PER_TICK = 1000;
    private static final int ENERGY_PER_TICK = 60;
    private static final int WATER_PER_OPERATION = 1000;
    private static final int OXYGEN_PER_OPERATION = 500;
    private static final int HYDROGEN_PER_OPERATION = 1000;

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

    public OxygenConverterControllerBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(ModBlockEntities.OXYGEN_CONVERTER_CONTROLLER.get(), worldPosition, blockState);
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
        tag.putInt("oxygen_converter_progress", progress);
        tag.put("oxygen_converter_energy", energyStorage.serializeNBT());
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        progress = tag.getInt("oxygen_converter_progress");
        if (tag.contains("oxygen_converter_energy")) {
            energyStorage.deserializeNBT(tag.get("oxygen_converter_energy"));
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, OxygenConverterControllerBlockEntity blockEntity) {
        if (level.isClientSide()) {
            return;
        }

        Direction front = state.getValue(OxygenConverterControllerBlock.FACING);
        boolean formed = OxygenConverterControllerBlock.isStructureValid(level, pos, front);
        OxygenConverterControllerBlock.updateFrameFormedStates(level, pos, front, formed);
        if (!formed) {
            blockEntity.resetProgress();
            return;
        }

        BlockEntity inputEntity = level.getBlockEntity(OxygenConverterControllerBlock.getFluidInputBusPos(pos, front));
        BlockEntity oxygenOutputEntity = level.getBlockEntity(OxygenConverterControllerBlock.getOxygenOutputBusPos(pos, front));
        BlockEntity hydrogenOutputEntity = level.getBlockEntity(OxygenConverterControllerBlock.getHydrogenOutputBusPos(pos, front));
        BlockEntity energyEntity = level.getBlockEntity(OxygenConverterControllerBlock.getEnergyInputHatchPos(pos, front));

        if (!(inputEntity instanceof ElectricArcFurnaceFluidInputBusBlockEntity inputBus)
                || !(oxygenOutputEntity instanceof ElectricArcFurnaceFluidOutputBusBlockEntity oxygenOutputBus)
                || !(hydrogenOutputEntity instanceof ElectricArcFurnaceFluidOutputBusBlockEntity hydrogenOutputBus)
                || !(energyEntity instanceof ElectricArcFurnaceEnergyInputHatchBlockEntity energyInputHatch)) {
            blockEntity.resetProgress();
            return;
        }

        blockEntity.pullEnergyFromHatch(energyInputHatch);

        if (blockEntity.energyStorage.getEnergyStored() < ENERGY_PER_TICK) {
            blockEntity.resetProgress();
            return;
        }

        FluidStack simulatedDrain = inputBus.drain(WATER_PER_OPERATION, IFluidHandler.FluidAction.SIMULATE);
        if (simulatedDrain.getAmount() < WATER_PER_OPERATION || simulatedDrain.getFluid() != Fluids.WATER) {
            blockEntity.resetProgress();
            return;
        }

        FluidStack oxygenOutput = new FluidStack(ModFluids.OXYGEN.get(), OXYGEN_PER_OPERATION);
        if (oxygenOutputBus.fill(oxygenOutput, IFluidHandler.FluidAction.SIMULATE) < OXYGEN_PER_OPERATION) {
            blockEntity.resetProgress();
            return;
        }

        FluidStack hydrogenOutput = new FluidStack(ModFluids.HYDROGEN.get(), HYDROGEN_PER_OPERATION);
        if (hydrogenOutputBus.fill(hydrogenOutput, IFluidHandler.FluidAction.SIMULATE) < HYDROGEN_PER_OPERATION) {
            blockEntity.resetProgress();
            return;
        }

        blockEntity.energyStorage.extractEnergy(ENERGY_PER_TICK, false);
        blockEntity.progress++;
        if (blockEntity.progress < DEFAULT_PROCESS_TICKS) {
            blockEntity.setChanged();
            return;
        }

        inputBus.drain(WATER_PER_OPERATION, IFluidHandler.FluidAction.EXECUTE);
        oxygenOutputBus.fill(oxygenOutput, IFluidHandler.FluidAction.EXECUTE);
        hydrogenOutputBus.fill(hydrogenOutput, IFluidHandler.FluidAction.EXECUTE);

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
