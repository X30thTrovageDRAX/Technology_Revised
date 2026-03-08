package com.neofast.tech_revised.integration.jei;

import com.neofast.tech_revised.TechRevised;
import com.neofast.tech_revised.block.ModBlocks;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collections;

public class ArgonOxygenDecarburizationConverterJeiCategory
        implements IRecipeCategory<ArgonOxygenDecarburizationConverterJeiRecipe> {
    public static final RecipeType<ArgonOxygenDecarburizationConverterJeiRecipe> RECIPE_TYPE =
            RecipeType.create(
                    TechRevised.MOD_ID,
                    "argon_oxygen_decarburization_converter",
                    ArgonOxygenDecarburizationConverterJeiRecipe.class
            );

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable arrow;

    public ArgonOxygenDecarburizationConverterJeiCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(146, 62);
        this.icon = guiHelper.createDrawableItemStack(
                new ItemStack(ModBlocks.ARGON_OXYGEN_DECARBURIZATION_CONVERTER_CONTROLLER.get())
        );
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<ArgonOxygenDecarburizationConverterJeiRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.tech_revised.category.argon_oxygen_decarburization_converter");
    }

    @Override
    @SuppressWarnings("removal")
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder,
                          ArgonOxygenDecarburizationConverterJeiRecipe recipe,
                          IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 6, 22)
                .addIngredients(recipe.getPrimaryInput());

        if (!recipe.getSecondaryInput().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 28, 22)
                    .addIngredients(recipe.getSecondaryInput());
        }

        builder.addSlot(RecipeIngredientRole.CATALYST, 72, 22)
                .addIngredients(ForgeTypes.FLUID_STACK, Collections.singletonList(recipe.getOxygenInput()))
                .setFluidRenderer(recipe.getOxygenInput().getAmount(), false, 16, 16);

        builder.addSlot(RecipeIngredientRole.OUTPUT, 122, 22)
                .addItemStack(recipe.getOutput());
    }

    @Override
    public void draw(ArgonOxygenDecarburizationConverterJeiRecipe recipe,
                     mezz.jei.api.gui.ingredient.IRecipeSlotsView recipeSlotsView,
                     GuiGraphics guiGraphics,
                     double mouseX,
                     double mouseY) {
        arrow.draw(guiGraphics, 95, 22);

        int totalEnergy = recipe.getProcessTicks() * recipe.getEnergyPerTick();
        guiGraphics.drawString(Minecraft.getInstance().font,
                Component.literal("Time: " + recipe.getProcessTicks() + " t"),
                6, 4, 0x8B8B8B, false);
        guiGraphics.drawString(Minecraft.getInstance().font,
                Component.literal("Energy: " + recipe.getEnergyPerTick() + " FE/t (" + totalEnergy + " FE)"),
                6, 54, 0x8B8B8B, false);
        guiGraphics.drawString(Minecraft.getInstance().font,
                Component.literal("O2: " + recipe.getOxygenInput().getAmount() + " mB"),
                80, 4, 0x8B8B8B, false);
    }
}
