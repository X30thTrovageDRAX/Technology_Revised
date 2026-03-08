package com.neofast.tech_revised.block;

import com.neofast.tech_revised.TechRevised;
import com.neofast.tech_revised.block.custom.CokeOvenControllerBlock;
import com.neofast.tech_revised.block.custom.CokeOvenFrameBlock;
import com.neofast.tech_revised.block.custom.CrusherBlock;
import com.neofast.tech_revised.block.custom.DrillingPlatformControllerBlock;
import com.neofast.tech_revised.block.custom.DrillingPlatformFrameBlock;
import com.neofast.tech_revised.block.custom.ArgonOxygenDecarburizationConverterControllerBlock;
import com.neofast.tech_revised.block.custom.ArgonOxygenDecarburizationConverterFrameBlock;
import com.neofast.tech_revised.block.custom.ElectricArcFurnaceControllerBlock;
import com.neofast.tech_revised.block.custom.ElectricArcFurnaceEnergyInputHatchBlock;
import com.neofast.tech_revised.block.custom.ElectricArcFurnaceFrameBlock;
import com.neofast.tech_revised.block.custom.ElectricArcFurnaceFluidInputBusBlock;
import com.neofast.tech_revised.block.custom.ElectricArcFurnaceFluidOutputBusBlock;
import com.neofast.tech_revised.block.custom.ElectricArcFurnaceInputBusBlock;
import com.neofast.tech_revised.block.custom.ElectricArcFurnaceOutputBusBlock;
import com.neofast.tech_revised.block.custom.OxygenConverterControllerBlock;
import com.neofast.tech_revised.block.custom.OxygenConverterFrameBlock;
import com.neofast.tech_revised.block.custom.Windows7VmBlock;
import com.neofast.tech_revised.fluid.ModFluids;
import com.neofast.tech_revised.item.ModItems;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, TechRevised.MOD_ID);

    private static final Map<RegistryObject<? extends Block>, Integer> TINTED_BLOCK_COLORS = new LinkedHashMap<>();

    public static final RegistryObject<Block> STEEL_BLOCK = registerTintedBlock("steel_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(8f).requiresCorrectToolForDrops()),
            ModItems.STEEL_TINT);

    public static final RegistryObject<Block> STAINLESS_STEEL_BLOCK = registerTintedBlock("stainless_steel_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(8f).requiresCorrectToolForDrops()),
            ModItems.STAINLESS_STEEL_TINT);

    public static final RegistryObject<Block> CHROMITE_BLOCK = registerTintedBlock("chromite_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(8f).requiresCorrectToolForDrops()),
            ModItems.CHROMITE_TINT);

    public static final RegistryObject<Block> FERROCHROMIUM_BLOCK = registerTintedBlock("ferrochromium_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(8f).requiresCorrectToolForDrops()),
            ModItems.FERROCHROMIUM_TINT);

    public static final RegistryObject<Block> GRAPHITE_ORE = registerBlock("graphite_ore",
            () -> new DropExperienceBlock(BlockBehaviour.Properties.copy(Blocks.COAL_ORE).strength(3f).requiresCorrectToolForDrops(),
                    UniformInt.of(1, 3)));

    public static final RegistryObject<Block> CHROMITE_ORE = registerBlock("chromite_ore",
            () -> new DropExperienceBlock(BlockBehaviour.Properties.copy(Blocks.IRON_ORE).strength(3.5f).requiresCorrectToolForDrops(),
                    UniformInt.of(1, 4)));

    public static final RegistryObject<Block> CRUSHER = registerBlock("crusher",
            () -> new CrusherBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(6f).requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> WINDOWS_7_VM_BLOCK = registerBlock("windows_7_vm_block",
            () -> new Windows7VmBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .strength(8f)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> state.getValue(Windows7VmBlock.ACTIVE) ? 12 : 0)));

    public static final RegistryObject<Block> ELECTRIC_ARC_FURNACE_CONTROLLER = registerBlock("electric_arc_furnace_controller",
            () -> new ElectricArcFurnaceControllerBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion()));

    public static final RegistryObject<Block> ELECTRIC_ARC_FURNACE_INPUT_BUS = registerBlock("electric_arc_furnace_input_bus",
            () -> new ElectricArcFurnaceInputBusBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(8f).requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> ELECTRIC_ARC_FURNACE_OUTPUT_BUS = registerBlock("electric_arc_furnace_output_bus",
            () -> new ElectricArcFurnaceOutputBusBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(8f).requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> ELECTRIC_ARC_FURNACE_FLUID_INPUT_BUS = registerBlock("electric_arc_furnace_fluid_input_bus",
            () -> new ElectricArcFurnaceFluidInputBusBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(8f).requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> ELECTRIC_ARC_FURNACE_FLUID_OUTPUT_BUS = registerBlock("electric_arc_furnace_fluid_output_bus",
            () -> new ElectricArcFurnaceFluidOutputBusBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(8f).requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> ELECTRIC_ARC_FURNACE_ENERGY_INPUT_HATCH = registerBlock("electric_arc_furnace_energy_input_hatch",
            () -> new ElectricArcFurnaceEnergyInputHatchBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(8f).requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> ELECTRIC_ARC_FURNACE_HEATER = registerBlock("electric_arc_furnace_heater",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(8f).requiresCorrectToolForDrops().lightLevel(state -> 12)));

    public static final RegistryObject<Block> ELECTRIC_ARC_FURNACE_FRAME = registerBlock("electric_arc_furnace_frame",
            () -> new ElectricArcFurnaceFrameBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(9f).requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> DRILLING_PLATFORM_CONTROLLER = registerBlock("drilling_platform_controller",
            () -> new DrillingPlatformControllerBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion()));

    public static final RegistryObject<Block> DRILLING_PLATFORM_DRILL_HEAD = registerBlock("drilling_platform_drill_head",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(9f).requiresCorrectToolForDrops().lightLevel(state -> 8)));

    public static final RegistryObject<Block> DRILLING_PLATFORM_FRAME = registerBlock("drilling_platform_frame",
            () -> new DrillingPlatformFrameBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(9f).requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> COKE_OVEN_CONTROLLER = registerBlock("coke_oven_controller",
            () -> new CokeOvenControllerBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion()));

    public static final RegistryObject<Block> COKE_OVEN_FRAME = registerBlock("coke_oven_frame",
            () -> new CokeOvenFrameBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(9f).requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> ARGON_OXYGEN_DECARBURIZATION_CONVERTER_CONTROLLER = registerBlock(
            "argon_oxygen_decarburization_converter_controller",
            () -> new ArgonOxygenDecarburizationConverterControllerBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion()));

    public static final RegistryObject<Block> ARGON_OXYGEN_DECARBURIZATION_CONVERTER_FRAME = registerBlock(
            "argon_oxygen_decarburization_converter_frame",
            () -> new ArgonOxygenDecarburizationConverterFrameBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(9f).requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> OXYGEN_CONVERTER_CONTROLLER = registerBlock("oxygen_converter_controller",
            () -> new OxygenConverterControllerBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion()));

    public static final RegistryObject<Block> OXYGEN_CONVERTER_FRAME = registerBlock("oxygen_converter_frame",
            () -> new OxygenConverterFrameBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(9f).requiresCorrectToolForDrops()));

    public static final RegistryObject<LiquidBlock> HEAVY_CRUDE_OIL_BLOCK = registerBlockWithoutItem("heavy_crude_oil_block",
            () -> new LiquidBlock(ModFluids.HEAVY_CRUDE_OIL, BlockBehaviour.Properties.copy(Blocks.WATER).noLootTable()));

    public static final RegistryObject<LiquidBlock> CREOSOTE_BLOCK = registerBlockWithoutItem("creosote_block",
            () -> new LiquidBlock(ModFluids.CREOSOTE, BlockBehaviour.Properties.copy(Blocks.WATER).noLootTable()));

    public static final RegistryObject<LiquidBlock> OXYGEN_BLOCK = registerBlockWithoutItem("oxygen_block",
            () -> new LiquidBlock(ModFluids.OXYGEN, BlockBehaviour.Properties.copy(Blocks.WATER).noLootTable()));

    public static final RegistryObject<LiquidBlock> HYDROGEN_BLOCK = registerBlockWithoutItem("hydrogen_block",
            () -> new LiquidBlock(ModFluids.HYDROGEN, BlockBehaviour.Properties.copy(Blocks.WATER).noLootTable()));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<T> registerTintedBlock(String name, Supplier<T> block, int color) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        RegistryObject<Item> blockItem = registerBlockItem(name, toReturn);
        TINTED_BLOCK_COLORS.put(toReturn, color);
        ModItems.registerTintedItemColor(blockItem, color);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<T> registerBlockWithoutItem(String name, Supplier<T> block) {
        return BLOCKS.register(name, block);
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static Map<RegistryObject<? extends Block>, Integer> getTintedBlockColors() {
        return Collections.unmodifiableMap(TINTED_BLOCK_COLORS);
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
