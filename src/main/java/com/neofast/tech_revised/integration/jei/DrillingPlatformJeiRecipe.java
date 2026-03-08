package com.neofast.tech_revised.integration.jei;

import net.minecraftforge.fluids.FluidStack;

public class DrillingPlatformJeiRecipe {
    private final FluidStack outputFluid;
    private final int processTicks;
    private final int energyPerTick;

    public DrillingPlatformJeiRecipe(FluidStack outputFluid, int processTicks, int energyPerTick) {
        this.outputFluid = outputFluid;
        this.processTicks = processTicks;
        this.energyPerTick = energyPerTick;
    }

    public FluidStack getOutputFluid() {
        return outputFluid;
    }

    public int getProcessTicks() {
        return processTicks;
    }

    public int getEnergyPerTick() {
        return energyPerTick;
    }
}
