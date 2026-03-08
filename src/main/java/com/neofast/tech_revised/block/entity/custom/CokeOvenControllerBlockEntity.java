package com.neofast.tech_revised.block.entity.custom;

import com.neofast.tech_revised.block.custom.CokeOvenControllerBlock;
import com.neofast.tech_revised.block.entity.ModBlockEntities;
import com.neofast.tech_revised.recipe.CokeOvenRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.Optional;

public class CokeOvenControllerBlockEntity extends BlockEntity {
    private static final int DEFAULT_PROCESS_TICKS = 200;

    private int progress = 0;

    public CokeOvenControllerBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(ModBlockEntities.COKE_OVEN_CONTROLLER.get(), worldPosition, blockState);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.putInt("coke_oven_progress", progress);
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        progress = tag.getInt("coke_oven_progress");
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CokeOvenControllerBlockEntity blockEntity) {
        if (level.isClientSide()) {
            return;
        }

        Direction front = state.getValue(CokeOvenControllerBlock.FACING);
        boolean formed = CokeOvenControllerBlock.isStructureValid(level, pos, front);
        CokeOvenControllerBlock.updateFrameFormedStates(level, pos, front, formed);
        if (!formed) {
            blockEntity.resetProgress();
            return;
        }

        BlockEntity inputEntity = level.getBlockEntity(CokeOvenControllerBlock.getInputBusPos(pos, front));
        BlockEntity outputEntity = level.getBlockEntity(CokeOvenControllerBlock.getOutputBusPos(pos, front));
        BlockEntity fluidOutputEntity = level.getBlockEntity(CokeOvenControllerBlock.getFluidOutputBusPos(pos, front));

        if (!(inputEntity instanceof ElectricArcFurnaceInputBusBlockEntity inputBus) ||
                !(outputEntity instanceof ElectricArcFurnaceOutputBusBlockEntity outputBus) ||
                !(fluidOutputEntity instanceof ElectricArcFurnaceFluidOutputBusBlockEntity fluidOutputBus)) {
            blockEntity.resetProgress();
            return;
        }

        Optional<CokeOvenRecipe> recipe = getRecipe(level, inputBus);
        if (recipe.isEmpty()) {
            blockEntity.resetProgress();
            return;
        }

        CokeOvenRecipe currentRecipe = recipe.get();
        int matchingSlot = currentRecipe.findMatchingSlot(inputBus.getPrimaryInputStack(), inputBus.getSecondaryInputStack());
        if (matchingSlot < 0) {
            blockEntity.resetProgress();
            return;
        }

        ItemStack cokeOutput = currentRecipe.getResultItem(level.registryAccess()).copy();
        if (!outputBus.canAccept(cokeOutput)) {
            blockEntity.resetProgress();
            return;
        }

        FluidStack creosote = currentRecipe.getOutputFluid();
        if (fluidOutputBus.fill(creosote, IFluidHandler.FluidAction.SIMULATE) < creosote.getAmount()) {
            blockEntity.resetProgress();
            return;
        }

        blockEntity.progress++;
        if (blockEntity.progress < currentRecipe.getProcessTicks()) {
            blockEntity.setChanged();
            return;
        }

        if (matchingSlot == 0) {
            inputBus.extractPrimaryOne();
        } else {
            inputBus.extractSecondaryOne();
        }

        outputBus.insert(cokeOutput);
        fluidOutputBus.fill(creosote, IFluidHandler.FluidAction.EXECUTE);

        blockEntity.progress = 0;
        blockEntity.setChanged();
    }

    private static Optional<CokeOvenRecipe> getRecipe(Level level, ElectricArcFurnaceInputBusBlockEntity inputBus) {
        SimpleContainer inventory = new SimpleContainer(2);
        inventory.setItem(0, inputBus.getPrimaryInputStack().copy());
        inventory.setItem(1, inputBus.getSecondaryInputStack().copy());
        return level.getRecipeManager().getRecipeFor(CokeOvenRecipe.Type.INSTANCE, inventory, level);
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
}
