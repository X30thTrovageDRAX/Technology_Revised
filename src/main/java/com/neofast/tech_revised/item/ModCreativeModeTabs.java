package com.neofast.tech_revised.item;

import com.neofast.tech_revised.TechRevised;
import com.neofast.tech_revised.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TechRevised.MOD_ID);

    public static final RegistryObject<CreativeModeTab> TECH_REVISED_TAB = CREATIVE_MODE_TABS.register("tech_revised_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.tech_revised_tab"))
                    .icon(() -> new ItemStack(ModBlocks.ELECTRIC_ARC_FURNACE_CONTROLLER.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModBlocks.GRAPHITE_ORE.get());
                        output.accept(ModItems.GRAPHITE_DUST.get());
                        output.accept(ModItems.ELECTRODE.get());
                        output.accept(ModBlocks.CHROMITE_ORE.get());
                        output.accept(ModItems.CHROMITE_DUST.get());
                        output.accept(ModBlocks.CRUSHER.get());
                        output.accept(ModBlocks.WINDOWS_7_VM_BLOCK.get());
                        output.accept(ModBlocks.STEEL_BLOCK.get());
                        output.accept(ModItems.STEEL_INGOT.get());
                        output.accept(ModItems.STEEL_NUGGET.get());
                        output.accept(ModBlocks.STAINLESS_STEEL_BLOCK.get());
                        output.accept(ModItems.STAINLESS_STEEL_INGOT.get());
                        output.accept(ModItems.STAINLESS_STEEL_NUGGET.get());
                        output.accept(ModBlocks.CHROMITE_BLOCK.get());
                        output.accept(ModItems.CHROMITE_INGOT.get());
                        output.accept(ModItems.CHROMITE_NUGGET.get());
                        output.accept(ModBlocks.FERROCHROMIUM_BLOCK.get());
                        output.accept(ModItems.FERROCHROMIUM_INGOT.get());
                        output.accept(ModItems.FERROCHROMIUM_NUGGET.get());
                        output.accept(ModItems.CONFIGURATOR.get());
                        output.accept(ModBlocks.ELECTRIC_ARC_FURNACE_CONTROLLER.get());
                        output.accept(ModBlocks.ELECTRIC_ARC_FURNACE_INPUT_BUS.get());
                        output.accept(ModBlocks.ELECTRIC_ARC_FURNACE_OUTPUT_BUS.get());
                        output.accept(ModBlocks.ELECTRIC_ARC_FURNACE_FLUID_INPUT_BUS.get());
                        output.accept(ModBlocks.ELECTRIC_ARC_FURNACE_FLUID_OUTPUT_BUS.get());
                        output.accept(ModBlocks.ELECTRIC_ARC_FURNACE_ENERGY_INPUT_HATCH.get());
                        output.accept(ModBlocks.ELECTRIC_ARC_FURNACE_HEATER.get());
                        output.accept(ModBlocks.ELECTRIC_ARC_FURNACE_FRAME.get());
                        output.accept(ModBlocks.DRILLING_PLATFORM_CONTROLLER.get());
                        output.accept(ModBlocks.DRILLING_PLATFORM_DRILL_HEAD.get());
                        output.accept(ModBlocks.DRILLING_PLATFORM_FRAME.get());
                        output.accept(ModBlocks.COKE_OVEN_CONTROLLER.get());
                        output.accept(ModBlocks.COKE_OVEN_FRAME.get());
                        output.accept(ModBlocks.ARGON_OXYGEN_DECARBURIZATION_CONVERTER_CONTROLLER.get());
                        output.accept(ModBlocks.ARGON_OXYGEN_DECARBURIZATION_CONVERTER_FRAME.get());
                        output.accept(ModBlocks.OXYGEN_CONVERTER_CONTROLLER.get());
                        output.accept(ModBlocks.OXYGEN_CONVERTER_FRAME.get());
                        output.accept(ModItems.COAL_COKE.get());
                        output.accept(ModItems.HEAVY_CRUDE_OIL_BUCKET.get());
                        output.accept(ModItems.CREOSOTE_BUCKET.get());
                        output.accept(ModItems.OXYGEN_BUCKET.get());
                        output.accept(ModItems.HYDROGEN_BUCKET.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
