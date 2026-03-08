
package com.neofast.tech_revised.block.entity;

import com.neofast.tech_revised.TechRevised;
import com.neofast.tech_revised.block.ModBlocks;
import com.neofast.tech_revised.block.entity.custom.ArgonOxygenDecarburizationConverterControllerBlockEntity;
import com.neofast.tech_revised.block.entity.custom.CokeOvenControllerBlockEntity;
import com.neofast.tech_revised.block.entity.custom.CrusherBlockEntity;
import com.neofast.tech_revised.block.entity.custom.DrillingPlatformControllerBlockEntity;
import com.neofast.tech_revised.block.entity.custom.ElectricArcFurnaceControllerBlockEntity;
import com.neofast.tech_revised.block.entity.custom.ElectricArcFurnaceEnergyInputHatchBlockEntity;
import com.neofast.tech_revised.block.entity.custom.ElectricArcFurnaceFluidInputBusBlockEntity;
import com.neofast.tech_revised.block.entity.custom.ElectricArcFurnaceFluidOutputBusBlockEntity;
import com.neofast.tech_revised.block.entity.custom.ElectricArcFurnaceInputBusBlockEntity;
import com.neofast.tech_revised.block.entity.custom.ElectricArcFurnaceOutputBusBlockEntity;
import com.neofast.tech_revised.block.entity.custom.OxygenConverterControllerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, TechRevised.MOD_ID);

    public static final RegistryObject<BlockEntityType<ElectricArcFurnaceControllerBlockEntity>> ELECTRIC_ARC_FURNACE_CONTROLLER =
            BLOCK_ENTITIES.register("electric_arc_furnace_controller_entity", () ->
                    BlockEntityType.Builder.of(ElectricArcFurnaceControllerBlockEntity::new,
                            ModBlocks.ELECTRIC_ARC_FURNACE_CONTROLLER.get()).build(null));

    public static final RegistryObject<BlockEntityType<CokeOvenControllerBlockEntity>> COKE_OVEN_CONTROLLER =
            BLOCK_ENTITIES.register("coke_oven_controller_entity", () ->
                    BlockEntityType.Builder.of(CokeOvenControllerBlockEntity::new,
                            ModBlocks.COKE_OVEN_CONTROLLER.get()).build(null));

    public static final RegistryObject<BlockEntityType<ElectricArcFurnaceInputBusBlockEntity>> ELECTRIC_ARC_FURNACE_INPUT_BUS =
            BLOCK_ENTITIES.register("electric_arc_furnace_input_bus_entity", () ->
                    BlockEntityType.Builder.of(ElectricArcFurnaceInputBusBlockEntity::new,
                            ModBlocks.ELECTRIC_ARC_FURNACE_INPUT_BUS.get()).build(null));

    public static final RegistryObject<BlockEntityType<ElectricArcFurnaceOutputBusBlockEntity>> ELECTRIC_ARC_FURNACE_OUTPUT_BUS =
            BLOCK_ENTITIES.register("electric_arc_furnace_output_bus_entity", () ->
                    BlockEntityType.Builder.of(ElectricArcFurnaceOutputBusBlockEntity::new,
                            ModBlocks.ELECTRIC_ARC_FURNACE_OUTPUT_BUS.get()).build(null));

    public static final RegistryObject<BlockEntityType<ElectricArcFurnaceFluidInputBusBlockEntity>> ELECTRIC_ARC_FURNACE_FLUID_INPUT_BUS =
            BLOCK_ENTITIES.register("electric_arc_furnace_fluid_input_bus_entity", () ->
                    BlockEntityType.Builder.of(ElectricArcFurnaceFluidInputBusBlockEntity::new,
                            ModBlocks.ELECTRIC_ARC_FURNACE_FLUID_INPUT_BUS.get()).build(null));

    public static final RegistryObject<BlockEntityType<ElectricArcFurnaceFluidOutputBusBlockEntity>> ELECTRIC_ARC_FURNACE_FLUID_OUTPUT_BUS =
            BLOCK_ENTITIES.register("electric_arc_furnace_fluid_output_bus_entity", () ->
                    BlockEntityType.Builder.of(ElectricArcFurnaceFluidOutputBusBlockEntity::new,
                            ModBlocks.ELECTRIC_ARC_FURNACE_FLUID_OUTPUT_BUS.get()).build(null));

    public static final RegistryObject<BlockEntityType<ElectricArcFurnaceEnergyInputHatchBlockEntity>> ELECTRIC_ARC_FURNACE_ENERGY_INPUT_HATCH =
            BLOCK_ENTITIES.register("electric_arc_furnace_energy_input_hatch_entity", () ->
                    BlockEntityType.Builder.of(ElectricArcFurnaceEnergyInputHatchBlockEntity::new,
                            ModBlocks.ELECTRIC_ARC_FURNACE_ENERGY_INPUT_HATCH.get()).build(null));

    public static final RegistryObject<BlockEntityType<CrusherBlockEntity>> CRUSHER =
            BLOCK_ENTITIES.register("crusher_entity", () ->
                    BlockEntityType.Builder.of(CrusherBlockEntity::new,
                            ModBlocks.CRUSHER.get()).build(null));

    public static final RegistryObject<BlockEntityType<DrillingPlatformControllerBlockEntity>> DRILLING_PLATFORM_CONTROLLER =
            BLOCK_ENTITIES.register("drilling_platform_controller_entity", () ->
                    BlockEntityType.Builder.of(DrillingPlatformControllerBlockEntity::new,
                            ModBlocks.DRILLING_PLATFORM_CONTROLLER.get()).build(null));

    public static final RegistryObject<BlockEntityType<ArgonOxygenDecarburizationConverterControllerBlockEntity>> ARGON_OXYGEN_DECARBURIZATION_CONVERTER_CONTROLLER =
            BLOCK_ENTITIES.register("argon_oxygen_decarburization_converter_controller_entity", () ->
                    BlockEntityType.Builder.of(ArgonOxygenDecarburizationConverterControllerBlockEntity::new,
                            ModBlocks.ARGON_OXYGEN_DECARBURIZATION_CONVERTER_CONTROLLER.get()).build(null));

    public static final RegistryObject<BlockEntityType<OxygenConverterControllerBlockEntity>> OXYGEN_CONVERTER_CONTROLLER =
            BLOCK_ENTITIES.register("oxygen_converter_controller_entity", () ->
                    BlockEntityType.Builder.of(OxygenConverterControllerBlockEntity::new,
                            ModBlocks.OXYGEN_CONVERTER_CONTROLLER.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
