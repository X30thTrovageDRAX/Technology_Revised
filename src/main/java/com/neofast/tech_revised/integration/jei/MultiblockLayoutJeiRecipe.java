package com.neofast.tech_revised.integration.jei;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

public class MultiblockLayoutJeiRecipe {
    private final Component machineName;
    private final ItemStack controller;
    private final List<ItemStack> requiredParts;
    private final Component dimensions;
    private final List<Component> notes;

    public MultiblockLayoutJeiRecipe(Component machineName,
                                     ItemStack controller,
                                     List<ItemStack> requiredParts,
                                     Component dimensions,
                                     List<Component> notes) {
        this.machineName = machineName;
        this.controller = controller.copy();
        this.requiredParts = requiredParts.stream()
                .map(ItemStack::copy)
                .collect(Collectors.toUnmodifiableList());
        this.dimensions = dimensions;
        this.notes = List.copyOf(notes);
    }

    public Component getMachineName() {
        return machineName;
    }

    public ItemStack getController() {
        return controller.copy();
    }

    public List<ItemStack> getRequiredParts() {
        return requiredParts;
    }

    public Component getDimensions() {
        return dimensions;
    }

    public List<Component> getNotes() {
        return notes;
    }
}
