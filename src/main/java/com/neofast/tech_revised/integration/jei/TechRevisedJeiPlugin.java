package com.neofast.tech_revised.integration.jei;

import com.neofast.tech_revised.TechRevised;
import com.neofast.tech_revised.block.ModBlocks;
import com.neofast.tech_revised.fluid.ModFluids;
import com.neofast.tech_revised.recipe.ArgonOxygenDecarburizationConverterRecipe;
import com.neofast.tech_revised.recipe.CokeOvenRecipe;
import com.neofast.tech_revised.recipe.CrusherRecipe;
import com.neofast.tech_revised.recipe.DrillingPlatformRecipe;
import com.neofast.tech_revised.recipe.ElectricArcFurnaceRecipe;
import com.neofast.tech_revised.screen.CrusherMenu;
import com.neofast.tech_revised.screen.CrusherScreen;
import com.neofast.tech_revised.screen.ElectricArcFurnaceControllerScreen;
import com.neofast.tech_revised.screen.ElectricArcFurnaceInputBusMenu;
import com.neofast.tech_revised.screen.ElectricArcFurnaceInputBusScreen;
import com.neofast.tech_revised.screen.ModMenuTypes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class TechRevisedJeiPlugin implements IModPlugin {
    private static final ResourceLocation PLUGIN_UID = new ResourceLocation(TechRevised.MOD_ID, "jei_plugin");
    private static final int EAF_SMELTING_PROCESS_TICKS = 100;
    private static final int EAF_SMELTING_ENERGY_PER_TICK = 40;
    private static final int EAF_SMELTING_WATER_PER_OPERATION = 250;

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new ElectricArcFurnaceJeiCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new ArgonOxygenDecarburizationConverterJeiCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new CokeOvenJeiCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new DrillingPlatformJeiCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new CrusherJeiCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new MultiblockLayoutJeiCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(MultiblockLayoutJeiCategory.RECIPE_TYPE, createMultiblockLayoutRecipes());

        Level level = Minecraft.getInstance().level;
        List<ElectricArcFurnaceJeiRecipe> recipes = new ArrayList<>();
        if (level != null) {
            for (ElectricArcFurnaceRecipe eafRecipe : level.getRecipeManager().getAllRecipesFor(ElectricArcFurnaceRecipe.Type.INSTANCE)) {
                recipes.add(new ElectricArcFurnaceJeiRecipe(
                        eafRecipe.getPrimaryInput(),
                        eafRecipe.getSecondaryInput(),
                        eafRecipe.getResultItem(level.registryAccess()).copy(),
                        new FluidStack(Fluids.WATER, eafRecipe.getWaterAmount()),
                        eafRecipe.getProcessTicks(),
                        eafRecipe.getEnergyPerTick()
                ));
            }

            for (SmeltingRecipe smeltingRecipe : level.getRecipeManager().getAllRecipesFor(RecipeType.SMELTING)) {
                if (smeltingRecipe.getIngredients().isEmpty()) {
                    continue;
                }

                ItemStack result = smeltingRecipe.getResultItem(level.registryAccess()).copy();
                if (result.isEmpty()) {
                    continue;
                }

                recipes.add(new ElectricArcFurnaceJeiRecipe(
                        smeltingRecipe.getIngredients().get(0),
                        Ingredient.EMPTY,
                        result,
                        new FluidStack(Fluids.WATER, EAF_SMELTING_WATER_PER_OPERATION),
                        EAF_SMELTING_PROCESS_TICKS,
                        EAF_SMELTING_ENERGY_PER_TICK
                ));
            }
        }

        registration.addRecipes(ElectricArcFurnaceJeiCategory.RECIPE_TYPE, recipes);

        List<ArgonOxygenDecarburizationConverterJeiRecipe> aodRecipes = new ArrayList<>();
        if (level != null) {
            for (ArgonOxygenDecarburizationConverterRecipe aodRecipe :
                    level.getRecipeManager().getAllRecipesFor(ArgonOxygenDecarburizationConverterRecipe.Type.INSTANCE)) {
                aodRecipes.add(new ArgonOxygenDecarburizationConverterJeiRecipe(
                        aodRecipe.getPrimaryInput(),
                        aodRecipe.getSecondaryInput(),
                        aodRecipe.getResultItem(level.registryAccess()).copy(),
                        new FluidStack(ModFluids.OXYGEN.get(), aodRecipe.getOxygenAmount()),
                        aodRecipe.getProcessTicks(),
                        aodRecipe.getEnergyPerTick()
                ));
            }
        }
        registration.addRecipes(ArgonOxygenDecarburizationConverterJeiCategory.RECIPE_TYPE, aodRecipes);

        List<CokeOvenJeiRecipe> cokeOvenRecipes = new ArrayList<>();
        if (level != null) {
            for (CokeOvenRecipe cokeRecipe : level.getRecipeManager().getAllRecipesFor(CokeOvenRecipe.Type.INSTANCE)) {
                cokeOvenRecipes.add(new CokeOvenJeiRecipe(
                        cokeRecipe.getInput(),
                        cokeRecipe.getResultItem(level.registryAccess()).copy(),
                        cokeRecipe.getOutputFluid(),
                        cokeRecipe.getProcessTicks()
                ));
            }
        }
        registration.addRecipes(CokeOvenJeiCategory.RECIPE_TYPE, cokeOvenRecipes);

        List<DrillingPlatformJeiRecipe> drillingRecipes = new ArrayList<>();
        if (level != null) {
            for (DrillingPlatformRecipe drillingRecipe : level.getRecipeManager().getAllRecipesFor(DrillingPlatformRecipe.Type.INSTANCE)) {
                drillingRecipes.add(new DrillingPlatformJeiRecipe(
                        drillingRecipe.getOutputFluid(),
                        drillingRecipe.getProcessTicks(),
                        drillingRecipe.getEnergyPerTick()
                ));
            }
        }
        registration.addRecipes(DrillingPlatformJeiCategory.RECIPE_TYPE, drillingRecipes);

        List<CrusherJeiRecipe> crusherRecipes = new ArrayList<>();
        if (level != null) {
            for (CrusherRecipe crusherRecipe : level.getRecipeManager().getAllRecipesFor(CrusherRecipe.Type.INSTANCE)) {
                crusherRecipes.add(new CrusherJeiRecipe(
                        crusherRecipe.getInput(),
                        crusherRecipe.getResultItem(level.registryAccess()).copy(),
                        crusherRecipe.getProcessTicks(),
                        crusherRecipe.getEnergyPerTick()
                ));
            }
        }

        registration.addRecipes(CrusherJeiCategory.RECIPE_TYPE, crusherRecipes);
    }

    private static List<MultiblockLayoutJeiRecipe> createMultiblockLayoutRecipes() {
        List<MultiblockLayoutJeiRecipe> recipes = new ArrayList<>();

        recipes.add(new MultiblockLayoutJeiRecipe(
                Component.translatable("jei.tech_revised.layout.electric_arc_furnace"),
                new ItemStack(ModBlocks.ELECTRIC_ARC_FURNACE_CONTROLLER.get()),
                List.of(
                        new ItemStack(ModBlocks.ELECTRIC_ARC_FURNACE_FRAME.get(), 29),
                        new ItemStack(ModBlocks.ELECTRIC_ARC_FURNACE_HEATER.get()),
                        new ItemStack(ModBlocks.ELECTRIC_ARC_FURNACE_INPUT_BUS.get()),
                        new ItemStack(ModBlocks.ELECTRIC_ARC_FURNACE_OUTPUT_BUS.get()),
                        new ItemStack(ModBlocks.ELECTRIC_ARC_FURNACE_FLUID_INPUT_BUS.get()),
                        new ItemStack(ModBlocks.ELECTRIC_ARC_FURNACE_ENERGY_INPUT_HATCH.get()),
                        new ItemStack(ModBlocks.ELECTRIC_ARC_FURNACE_FLUID_OUTPUT_BUS.get())
                ),
                Component.literal("Size: 3W x 3L x 4H"),
                List.of(
                        Component.translatable("jei.tech_revised.layout.eaf.line1"),
                        Component.translatable("jei.tech_revised.layout.eaf.line2"),
                        Component.translatable("jei.tech_revised.layout.eaf.line3")
                )
        ));

        recipes.add(new MultiblockLayoutJeiRecipe(
                Component.translatable("jei.tech_revised.layout.drilling_platform"),
                new ItemStack(ModBlocks.DRILLING_PLATFORM_CONTROLLER.get()),
                List.of(
                        new ItemStack(ModBlocks.DRILLING_PLATFORM_FRAME.get(), 56),
                        new ItemStack(ModBlocks.DRILLING_PLATFORM_DRILL_HEAD.get()),
                        new ItemStack(ModBlocks.ELECTRIC_ARC_FURNACE_ENERGY_INPUT_HATCH.get()),
                        new ItemStack(ModBlocks.ELECTRIC_ARC_FURNACE_FLUID_OUTPUT_BUS.get())
                ),
                Component.literal("Size: 5W x 4L x 3H"),
                List.of(
                        Component.translatable("jei.tech_revised.layout.drilling.line1"),
                        Component.translatable("jei.tech_revised.layout.drilling.line2"),
                        Component.translatable("jei.tech_revised.layout.drilling.line3")
                )
        ));

        recipes.add(new MultiblockLayoutJeiRecipe(
                Component.translatable("jei.tech_revised.layout.coke_oven"),
                new ItemStack(ModBlocks.COKE_OVEN_CONTROLLER.get()),
                List.of(
                        new ItemStack(ModBlocks.COKE_OVEN_FRAME.get(), 23),
                        new ItemStack(ModBlocks.ELECTRIC_ARC_FURNACE_INPUT_BUS.get()),
                        new ItemStack(ModBlocks.ELECTRIC_ARC_FURNACE_OUTPUT_BUS.get()),
                        new ItemStack(ModBlocks.ELECTRIC_ARC_FURNACE_FLUID_OUTPUT_BUS.get())
                ),
                Component.literal("Size: 3W x 3L x 3H"),
                List.of(
                        Component.translatable("jei.tech_revised.layout.coke.line1"),
                        Component.translatable("jei.tech_revised.layout.coke.line2"),
                        Component.translatable("jei.tech_revised.layout.coke.line3")
                )
        ));

        recipes.add(new MultiblockLayoutJeiRecipe(
                Component.translatable("jei.tech_revised.layout.argon_oxygen_decarburization_converter"),
                new ItemStack(ModBlocks.ARGON_OXYGEN_DECARBURIZATION_CONVERTER_CONTROLLER.get()),
                List.of(
                        new ItemStack(ModBlocks.ARGON_OXYGEN_DECARBURIZATION_CONVERTER_FRAME.get(), 187),
                        new ItemStack(ModBlocks.ELECTRIC_ARC_FURNACE_INPUT_BUS.get()),
                        new ItemStack(ModBlocks.ELECTRIC_ARC_FURNACE_OUTPUT_BUS.get()),
                        new ItemStack(ModBlocks.ELECTRIC_ARC_FURNACE_FLUID_INPUT_BUS.get()),
                        new ItemStack(ModBlocks.ELECTRIC_ARC_FURNACE_ENERGY_INPUT_HATCH.get())
                ),
                Component.literal("Size: 5W x 5L x 12H"),
                List.of(
                        Component.translatable("jei.tech_revised.layout.aod_converter.line1"),
                        Component.translatable("jei.tech_revised.layout.aod_converter.line2"),
                        Component.translatable("jei.tech_revised.layout.aod_converter.line3")
                )
        ));

        recipes.add(new MultiblockLayoutJeiRecipe(
                Component.translatable("jei.tech_revised.layout.oxygen_converter"),
                new ItemStack(ModBlocks.OXYGEN_CONVERTER_CONTROLLER.get()),
                List.of(
                        new ItemStack(ModBlocks.OXYGEN_CONVERTER_FRAME.get(), 107),
                        new ItemStack(ModBlocks.ELECTRIC_ARC_FURNACE_FLUID_INPUT_BUS.get()),
                        new ItemStack(ModBlocks.ELECTRIC_ARC_FURNACE_FLUID_OUTPUT_BUS.get(), 2),
                        new ItemStack(ModBlocks.ELECTRIC_ARC_FURNACE_ENERGY_INPUT_HATCH.get())
                ),
                Component.literal("Size: 5W x 5L x 7H"),
                List.of(
                        Component.translatable("jei.tech_revised.layout.oxygen_converter.line1"),
                        Component.translatable("jei.tech_revised.layout.oxygen_converter.line2"),
                        Component.translatable("jei.tech_revised.layout.oxygen_converter.line3")
                )
        ));

        return recipes;
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ELECTRIC_ARC_FURNACE_CONTROLLER.get()),
                ElectricArcFurnaceJeiCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ARGON_OXYGEN_DECARBURIZATION_CONVERTER_CONTROLLER.get()),
                ArgonOxygenDecarburizationConverterJeiCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.COKE_OVEN_CONTROLLER.get()),
                CokeOvenJeiCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.DRILLING_PLATFORM_CONTROLLER.get()),
                DrillingPlatformJeiCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CRUSHER.get()),
                CrusherJeiCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ELECTRIC_ARC_FURNACE_CONTROLLER.get()),
                MultiblockLayoutJeiCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.DRILLING_PLATFORM_CONTROLLER.get()),
                MultiblockLayoutJeiCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.COKE_OVEN_CONTROLLER.get()),
                MultiblockLayoutJeiCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ARGON_OXYGEN_DECARBURIZATION_CONVERTER_CONTROLLER.get()),
                MultiblockLayoutJeiCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.OXYGEN_CONVERTER_CONTROLLER.get()),
                MultiblockLayoutJeiCategory.RECIPE_TYPE);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(
                ElectricArcFurnaceInputBusMenu.class,
                ModMenuTypes.ELECTRIC_ARC_FURNACE_INPUT_BUS_MENU.get(),
                ElectricArcFurnaceJeiCategory.RECIPE_TYPE,
                36, 2,
                0, 36
        );

        registration.addRecipeTransferHandler(
                CrusherMenu.class,
                ModMenuTypes.CRUSHER_MENU.get(),
                CrusherJeiCategory.RECIPE_TYPE,
                36, 1,
                0, 36
        );
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(ElectricArcFurnaceControllerScreen.class,
                8, 20, 130, 12,
                ElectricArcFurnaceJeiCategory.RECIPE_TYPE,
                MultiblockLayoutJeiCategory.RECIPE_TYPE);
        registration.addRecipeClickArea(ElectricArcFurnaceInputBusScreen.class,
                70, 34, 38, 18,
                ElectricArcFurnaceJeiCategory.RECIPE_TYPE);
        registration.addRecipeClickArea(CrusherScreen.class,
                56, 34, 60, 18,
                CrusherJeiCategory.RECIPE_TYPE);
    }
}
