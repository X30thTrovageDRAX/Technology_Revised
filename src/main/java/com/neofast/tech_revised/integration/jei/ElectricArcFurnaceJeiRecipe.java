package com.neofast.tech_revised.integration.jei;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;

public class ElectricArcFurnaceJeiRecipe {
    private final Ingredient primaryInput;
    private final Ingredient secondaryInput;
    private final ItemStack output;
    private final FluidStack coolingWater;
    private final int processTicks;
    private final int energyPerTick;

    public ElectricArcFurnaceJeiRecipe(Ingredient primaryInput,
                                       Ingredient secondaryInput,
                                       ItemStack output,
                                       FluidStack coolingWater,
                                       int processTicks,
                                       int energyPerTick) {
        this.primaryInput = primaryInput;
        this.secondaryInput = secondaryInput;
        this.output = output;
        this.coolingWater = coolingWater;
        this.processTicks = processTicks;
        this.energyPerTick = energyPerTick;
    }

    public Ingredient getPrimaryInput() {
        return primaryInput;
    }

    public Ingredient getSecondaryInput() {
        return secondaryInput;
    }

    public ItemStack getOutput() {
        return output;
    }

    public FluidStack getCoolingWater() {
        return coolingWater;
    }

    public int getProcessTicks() {
        return processTicks;
    }

    public int getEnergyPerTick() {
        return energyPerTick;
    }
}
