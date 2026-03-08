package com.neofast.tech_revised.recipe;

import com.neofast.tech_revised.TechRevised;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, TechRevised.MOD_ID);

    public static final RegistryObject<RecipeSerializer<CrusherRecipe>> CRUSHER_SERIALIZER =
            SERIALIZERS.register("crusher", CrusherRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<ElectricArcFurnaceRecipe>> ELECTRIC_ARC_FURNACE_SERIALIZER =
            SERIALIZERS.register("electric_arc_furnace", ElectricArcFurnaceRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<ArgonOxygenDecarburizationConverterRecipe>> ARGON_OXYGEN_DECARBURIZATION_CONVERTER_SERIALIZER =
            SERIALIZERS.register("argon_oxygen_decarburization_converter",
                    ArgonOxygenDecarburizationConverterRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<CokeOvenRecipe>> COKE_OVEN_SERIALIZER =
            SERIALIZERS.register("coke_oven", CokeOvenRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<DrillingPlatformRecipe>> DRILLING_PLATFORM_SERIALIZER =
            SERIALIZERS.register("drilling_platform", DrillingPlatformRecipe.Serializer::new);

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }
}
