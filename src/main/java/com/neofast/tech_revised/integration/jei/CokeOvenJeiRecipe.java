package com.neofast.tech_revised.integration.jei;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;

public class CokeOvenJeiRecipe {
    private final Ingredient input;
    private final ItemStack outputItem;
    private final FluidStack outputFluid;
    private final int processTicks;

    public CokeOvenJeiRecipe(Ingredient input, ItemStack outputItem, FluidStack outputFluid, int processTicks) {
        this.input = input;
        this.outputItem = outputItem;
        this.outputFluid = outputFluid;
        this.processTicks = processTicks;
    }

    public Ingredient getInput() {
        return input;
    }

    public ItemStack getOutputItem() {
        return outputItem;
    }

    public FluidStack getOutputFluid() {
        return outputFluid;
    }

    public int getProcessTicks() {
        return processTicks;
    }
}
