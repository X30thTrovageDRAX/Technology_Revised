package com.neofast.tech_revised.integration.jei;

import com.neofast.tech_revised.TechRevised;
import com.neofast.tech_revised.block.ModBlocks;
import com.neofast.tech_revised.item.ModItems;
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

import java.util.Collections;

public class ElectricArcFurnaceJeiCategory implements IRecipeCategory<ElectricArcFurnaceJeiRecipe> {
    public static final RecipeType<ElectricArcFurnaceJeiRecipe> RECIPE_TYPE =
            RecipeType.create(TechRevised.MOD_ID, "electric_arc_furnace", ElectricArcFurnaceJeiRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable arrow;

    public ElectricArcFurnaceJeiCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(146, 62);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.ELECTRIC_ARC_FURNACE_CONTROLLER.get()));
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<ElectricArcFurnaceJeiRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.tech_revised.category.electric_arc_furnace");
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
    public void setRecipe(IRecipeLayoutBuilder builder, ElectricArcFurnaceJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 6, 22)
                .addIngredients(recipe.getPrimaryInput());

        if (!recipe.getSecondaryInput().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 28, 22)
                    .addIngredients(recipe.getSecondaryInput());
        }

        builder.addSlot(RecipeIngredientRole.CATALYST, 50, 22)
                .addItemStack(new ItemStack(ModItems.ELECTRODE.get()));

        builder.addSlot(RecipeIngredientRole.CATALYST, 72, 22)
                .addIngredients(ForgeTypes.FLUID_STACK, Collections.singletonList(recipe.getCoolingWater()))
                .setFluidRenderer(recipe.getCoolingWater().getAmount(), false, 16, 16);

        builder.addSlot(RecipeIngredientRole.OUTPUT, 122, 22)
                .addItemStack(recipe.getOutput());
    }

    @Override
    public void draw(ElectricArcFurnaceJeiRecipe recipe,
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
    }
}
