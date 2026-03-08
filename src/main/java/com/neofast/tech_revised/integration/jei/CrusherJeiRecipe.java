package com.neofast.tech_revised.integration.jei;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class CrusherJeiRecipe {
    private final Ingredient input;
    private final ItemStack output;
    private final int processTicks;
    private final int energyPerTick;

    public CrusherJeiRecipe(Ingredient input, ItemStack output, int processTicks, int energyPerTick) {
        this.input = input;
        this.output = output;
        this.processTicks = processTicks;
        this.energyPerTick = energyPerTick;
    }

    public Ingredient getInput() {
        return input;
    }

    public ItemStack getOutput() {
        return output;
    }

    public int getProcessTicks() {
        return processTicks;
    }

    public int getEnergyPerTick() {
        return energyPerTick;
    }
}
