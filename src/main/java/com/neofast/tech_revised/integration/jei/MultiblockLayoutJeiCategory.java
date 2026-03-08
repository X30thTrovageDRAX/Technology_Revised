package com.neofast.tech_revised.integration.jei;

import com.neofast.tech_revised.TechRevised;
import com.neofast.tech_revised.item.ModItems;
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

import java.util.List;

public class MultiblockLayoutJeiCategory implements IRecipeCategory<MultiblockLayoutJeiRecipe> {
    public static final RecipeType<MultiblockLayoutJeiRecipe> RECIPE_TYPE =
            RecipeType.create(TechRevised.MOD_ID, "multiblock_layout", MultiblockLayoutJeiRecipe.class);

    private static final int PARTS_START_X = 30;
    private static final int PARTS_START_Y = 18;
    private static final int PARTS_COLUMNS = 5;

    private final IDrawable background;
    private final IDrawable icon;

    public MultiblockLayoutJeiCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(176, 112);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModItems.CONFIGURATOR.get()));
    }

    @Override
    public RecipeType<MultiblockLayoutJeiRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.tech_revised.category.multiblock_layout");
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
    public void setRecipe(IRecipeLayoutBuilder builder, MultiblockLayoutJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.CATALYST, 8, 18)
                .addItemStack(recipe.getController());

        List<ItemStack> parts = recipe.getRequiredParts();
        for (int i = 0; i < parts.size(); i++) {
            int x = PARTS_START_X + (i % PARTS_COLUMNS) * 18;
            int y = PARTS_START_Y + (i / PARTS_COLUMNS) * 18;

            builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                    .addItemStack(parts.get(i));
        }
    }

    @Override
    public void draw(MultiblockLayoutJeiRecipe recipe,
                     mezz.jei.api.gui.ingredient.IRecipeSlotsView recipeSlotsView,
                     GuiGraphics guiGraphics,
                     double mouseX,
                     double mouseY) {
        var font = Minecraft.getInstance().font;

        guiGraphics.drawString(font, recipe.getMachineName(), 8, 2, 0xFFFFFF, false);
        guiGraphics.drawString(font, Component.translatable("jei.tech_revised.layout.controller"), 8, 38, 0x8B8B8B, false);
        guiGraphics.drawString(font, recipe.getDimensions(), 8, 58, 0x8B8B8B, false);
        guiGraphics.drawString(font, Component.translatable("jei.tech_revised.layout.required_parts"), 8, 70, 0x8B8B8B, false);

        int notesY = 82;
        for (Component note : recipe.getNotes()) {
            guiGraphics.drawString(font, note, 8, notesY, 0x8B8B8B, false);
            notesY += 10;
        }
    }
}
