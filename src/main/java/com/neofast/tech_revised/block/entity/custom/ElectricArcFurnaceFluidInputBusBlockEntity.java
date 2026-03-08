package com.neofast.tech_revised.block.entity.custom;

import com.neofast.tech_revised.block.entity.ModBlockEntities;
import com.neofast.tech_revised.fluid.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class ElectricArcFurnaceFluidInputBusBlockEntity extends BlockEntity {
    private static final int CAPACITY = 16000;

    private final FluidTank waterTank = new FluidTank(CAPACITY,
            fluid -> fluid.getFluid() == Fluids.WATER || fluid.getFluid() == ModFluids.OXYGEN.get()) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    private LazyOptional<IFluidHandler> lazyFluidHandler = LazyOptional.empty();

    public ElectricArcFurnaceFluidInputBusBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(ModBlockEntities.ELECTRIC_ARC_FURNACE_FLUID_INPUT_BUS.get(), worldPosition, blockState);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return lazyFluidHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyFluidHandler = LazyOptional.of(() -> waterTank);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyFluidHandler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.put("tank", waterTank.writeToNBT(new CompoundTag()));
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("tank")) {
            waterTank.readFromNBT(tag.getCompound("tank"));
        }
    }

    public FluidStack drain(int amount, IFluidHandler.FluidAction action) {
        return waterTank.drain(amount, action);
    }

    public int getWaterAmount() {
        return waterTank.getFluidAmount();
    }

    public int getCapacity() {
        return waterTank.getCapacity();
    }
}
