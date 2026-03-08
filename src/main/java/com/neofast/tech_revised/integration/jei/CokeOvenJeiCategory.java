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

import java.util.Collections;

public class CokeOvenJeiCategory implements IRecipeCategory<CokeOvenJeiRecipe> {
    public static final RecipeType<CokeOvenJeiRecipe> RECIPE_TYPE =
            RecipeType.create(TechRevised.MOD_ID, "coke_oven", CokeOvenJeiRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable arrow;

    public CokeOvenJeiCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(146, 62);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.COKE_OVEN_CONTROLLER.get()));
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<CokeOvenJeiRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.tech_revised.category.coke_oven");
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
    public void setRecipe(IRecipeLayoutBuilder builder, CokeOvenJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 8, 22)
                .addIngredients(recipe.getInput());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 84, 22)
                .addItemStack(recipe.getOutputItem());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 116, 22)
                .addIngredients(ForgeTypes.FLUID_STACK, Collections.singletonList(recipe.getOutputFluid()))
                .setFluidRenderer(recipe.getOutputFluid().getAmount(), false, 16, 16);
    }

    @Override
    public void draw(CokeOvenJeiRecipe recipe,
                     mezz.jei.api.gui.ingredient.IRecipeSlotsView recipeSlotsView,
                     GuiGraphics guiGraphics,
                     double mouseX,
                     double mouseY) {
        arrow.draw(guiGraphics, 58, 22);

        guiGraphics.drawString(Minecraft.getInstance().font,
                Component.literal("Time: " + recipe.getProcessTicks() + " t"),
                8, 4, 0x8B8B8B, false);
        guiGraphics.drawString(Minecraft.getInstance().font,
                Component.literal("Creosote: " + recipe.getOutputFluid().getAmount() + " mB"),
                8, 54, 0x8B8B8B, false);
    }
}
