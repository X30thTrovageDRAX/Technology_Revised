package com.neofast.tech_revised.block.entity.custom;

import com.neofast.tech_revised.block.custom.ArgonOxygenDecarburizationConverterControllerBlock;
import com.neofast.tech_revised.block.entity.ModBlockEntities;
import com.neofast.tech_revised.fluid.ModFluids;
import com.neofast.tech_revised.recipe.ArgonOxygenDecarburizationConverterRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.Optional;

public class ArgonOxygenDecarburizationConverterControllerBlockEntity extends BlockEntity {
    private static final int DEFAULT_PROCESS_TICKS = 120;
    private static final int DEFAULT_ENERGY_PER_TICK = 80;
    private static final int DEFAULT_OXYGEN_PER_OPERATION = 500;
    private static final int ENERGY_CAPACITY = 50000;
    private static final int ENERGY_TRANSFER_PER_TICK = 1000;
    private static final int OXYGEN_CAPACITY = 8000;
    private static final int OXYGEN_TRANSFER_PER_TICK = 250;

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

    private final FluidTank oxygenTank = new FluidTank(
            OXYGEN_CAPACITY,
            fluidStack -> fluidStack.getFluid() == ModFluids.OXYGEN.get()
    ) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    public ArgonOxygenDecarburizationConverterControllerBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(ModBlockEntities.ARGON_OXYGEN_DECARBURIZATION_CONVERTER_CONTROLLER.get(), worldPosition, blockState);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.putInt("aod_progress", progress);
        tag.put("aod_energy", energyStorage.serializeNBT());
        tag.put("aod_oxygen", oxygenTank.writeToNBT(new CompoundTag()));
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        progress = tag.getInt("aod_progress");
        if (tag.contains("aod_energy")) {
            energyStorage.deserializeNBT(tag.get("aod_energy"));
        }
        if (tag.contains("aod_oxygen")) {
            oxygenTank.readFromNBT(tag.getCompound("aod_oxygen"));
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state,
                            ArgonOxygenDecarburizationConverterControllerBlockEntity blockEntity) {
        if (level.isClientSide()) {
            return;
        }

        Direction front = state.getValue(ArgonOxygenDecarburizationConverterControllerBlock.FACING);
        boolean formed = ArgonOxygenDecarburizationConverterControllerBlock.isStructureValid(level, pos, front);
        ArgonOxygenDecarburizationConverterControllerBlock.updateFrameFormedStates(level, pos, front, formed);
        if (!formed) {
            blockEntity.resetProgress();
            return;
        }

        BlockEntity inputEntity = level.getBlockEntity(ArgonOxygenDecarburizationConverterControllerBlock.getInputBusPos(pos, front));
        BlockEntity outputEntity = level.getBlockEntity(ArgonOxygenDecarburizationConverterControllerBlock.getOutputBusPos(pos, front));
        BlockEntity fluidInputEntity = level.getBlockEntity(ArgonOxygenDecarburizationConverterControllerBlock.getFluidInputBusPos(pos, front));
        BlockEntity energyInputEntity = level.getBlockEntity(ArgonOxygenDecarburizationConverterControllerBlock.getEnergyInputHatchPos(pos, front));

        if (!(inputEntity instanceof ElectricArcFurnaceInputBusBlockEntity inputBus)
                || !(outputEntity instanceof ElectricArcFurnaceOutputBusBlockEntity outputBus)
                || !(fluidInputEntity instanceof ElectricArcFurnaceFluidInputBusBlockEntity fluidInputBus)
                || !(energyInputEntity instanceof ElectricArcFurnaceEnergyInputHatchBlockEntity energyInputHatch)) {
            blockEntity.resetProgress();
            return;
        }

        blockEntity.pullEnergyFromHatch(energyInputHatch);
        blockEntity.pullOxygenFromInputBus(fluidInputBus);

        Optional<ArgonOxygenDecarburizationConverterRecipe> recipe =
                getRecipe(level, inputBus.getPrimaryInputStack(), inputBus.getSecondaryInputStack());
        if (recipe.isEmpty()) {
            blockEntity.resetProgress();
            return;
        }

        ArgonOxygenDecarburizationConverterRecipe currentRecipe = recipe.get();
        ItemStack outputStack = currentRecipe.getResultItem(level.registryAccess()).copy();

        int processTicks = currentRecipe.getProcessTicks();
        int energyPerTick = currentRecipe.getEnergyPerTick();
        int oxygenPerOperation = currentRecipe.getOxygenAmount();

        if (!outputBus.canAccept(outputStack)
                || !blockEntity.hasRequiredEnergy(energyPerTick)
                || !blockEntity.hasRequiredOxygen(oxygenPerOperation)) {
            blockEntity.resetProgress();
            return;
        }

        blockEntity.energyStorage.extractEnergy(energyPerTick, false);
        blockEntity.progress++;
        if (blockEntity.progress < processTicks) {
            blockEntity.setChanged();
            return;
        }

        inputBus.extractPrimaryOne();
        if (currentRecipe.hasSecondaryInput()) {
            inputBus.extractSecondaryOne();
        }
        outputBus.insert(outputStack);
        blockEntity.oxygenTank.drain(oxygenPerOperation, IFluidHandler.FluidAction.EXECUTE);

        blockEntity.progress = 0;
        blockEntity.setChanged();
    }

    private static Optional<ArgonOxygenDecarburizationConverterRecipe> getRecipe(Level level, ItemStack primaryInput, ItemStack secondaryInput) {
        SimpleContainer inventory = new SimpleContainer(2);
        inventory.setItem(0, primaryInput.copy());
        inventory.setItem(1, secondaryInput.copy());
        return level.getRecipeManager().getRecipeFor(ArgonOxygenDecarburizationConverterRecipe.Type.INSTANCE, inventory, level);
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

    private void pullOxygenFromInputBus(ElectricArcFurnaceFluidInputBusBlockEntity fluidInputBus) {
        int freeSpace = oxygenTank.getCapacity() - oxygenTank.getFluidAmount();
        if (freeSpace <= 0) {
            return;
        }

        int maxTransfer = Math.min(freeSpace, OXYGEN_TRANSFER_PER_TICK);
        FluidStack simulatedDrain = fluidInputBus.drain(maxTransfer, IFluidHandler.FluidAction.SIMULATE);
        if (simulatedDrain.isEmpty() || simulatedDrain.getFluid() != ModFluids.OXYGEN.get()) {
            return;
        }

        int accepted = oxygenTank.fill(simulatedDrain, IFluidHandler.FluidAction.SIMULATE);
        if (accepted <= 0) {
            return;
        }

        FluidStack drained = fluidInputBus.drain(accepted, IFluidHandler.FluidAction.EXECUTE);
        if (!drained.isEmpty() && drained.getFluid() == ModFluids.OXYGEN.get()) {
            oxygenTank.fill(drained, IFluidHandler.FluidAction.EXECUTE);
        }
    }

    private boolean hasRequiredEnergy(int energyPerTick) {
        return energyStorage.getEnergyStored() >= energyPerTick;
    }

    private boolean hasRequiredOxygen(int oxygenPerOperation) {
        return oxygenTank.getFluidAmount() >= oxygenPerOperation;
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

    public int getDefaultEnergyPerTick() {
        return DEFAULT_ENERGY_PER_TICK;
    }

    public int getDefaultOxygenPerOperation() {
        return DEFAULT_OXYGEN_PER_OPERATION;
    }

    public int getEnergyStored() {
        return energyStorage.getEnergyStored();
    }

    public int getMaxEnergyStored() {
        return energyStorage.getMaxEnergyStored();
    }

    public int getOxygenAmount() {
        return oxygenTank.getFluidAmount();
    }

    public int getOxygenCapacity() {
        return oxygenTank.getCapacity();
    }
}
