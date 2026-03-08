package com.neofast.tech_revised.integration.jei;

import com.neofast.tech_revised.TechRevised;
import com.neofast.tech_revised.block.ModBlocks;
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

public class CrusherJeiCategory implements IRecipeCategory<CrusherJeiRecipe> {
    public static final RecipeType<CrusherJeiRecipe> RECIPE_TYPE =
            RecipeType.create(TechRevised.MOD_ID, "crusher", CrusherJeiRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable arrow;

    public CrusherJeiCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(126, 52);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.CRUSHER.get()));
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<CrusherJeiRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.tech_revised.category.crusher");
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
    public void setRecipe(IRecipeLayoutBuilder builder, CrusherJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 8, 18)
                .addIngredients(recipe.getInput());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 104, 18)
                .addItemStack(recipe.getOutput());
    }

    @Override
    public void draw(CrusherJeiRecipe recipe,
                     mezz.jei.api.gui.ingredient.IRecipeSlotsView recipeSlotsView,
                     GuiGraphics guiGraphics,
                     double mouseX,
                     double mouseY) {
        arrow.draw(guiGraphics, 58, 18);

        int totalEnergy = recipe.getProcessTicks() * recipe.getEnergyPerTick();
        guiGraphics.drawString(Minecraft.getInstance().font,
                Component.literal("Time: " + recipe.getProcessTicks() + " t"),
                8, 2, 0x8B8B8B, false);
        guiGraphics.drawString(Minecraft.getInstance().font,
                Component.literal("Energy: " + recipe.getEnergyPerTick() + " FE/t (" + totalEnergy + " FE)"),
                8, 42, 0x8B8B8B, false);
    }
}
