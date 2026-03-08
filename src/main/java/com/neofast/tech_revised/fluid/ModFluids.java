package com.neofast.tech_revised.fluid;

import com.neofast.tech_revised.TechRevised;
import com.neofast.tech_revised.block.ModBlocks;
import com.neofast.tech_revised.item.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, TechRevised.MOD_ID);

    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(ForgeRegistries.FLUIDS, TechRevised.MOD_ID);

    private static final ResourceLocation HEAVY_CRUDE_STILL_TEXTURE =
            new ResourceLocation("minecraft", "block/water_still");
    private static final ResourceLocation HEAVY_CRUDE_FLOWING_TEXTURE =
            new ResourceLocation("minecraft", "block/water_flow");
    private static final ResourceLocation CREOSOTE_STILL_TEXTURE =
            new ResourceLocation("minecraft", "block/water_still");
    private static final ResourceLocation CREOSOTE_FLOWING_TEXTURE =
            new ResourceLocation("minecraft", "block/water_flow");
    private static final ResourceLocation OXYGEN_STILL_TEXTURE =
            new ResourceLocation("minecraft", "block/water_still");
    private static final ResourceLocation OXYGEN_FLOWING_TEXTURE =
            new ResourceLocation("minecraft", "block/water_flow");
    private static final ResourceLocation HYDROGEN_STILL_TEXTURE =
            new ResourceLocation("minecraft", "block/water_still");
    private static final ResourceLocation HYDROGEN_FLOWING_TEXTURE =
            new ResourceLocation("minecraft", "block/water_flow");

    public static final RegistryObject<FluidType> HEAVY_CRUDE_OIL_TYPE = FLUID_TYPES.register("heavy_crude_oil_type",
            () -> new BaseFluidType(HEAVY_CRUDE_STILL_TEXTURE, HEAVY_CRUDE_FLOWING_TEXTURE, 0xFF2C1F17,
                    FluidType.Properties.create()
                            .density(3200)
                            .viscosity(8000)
                            .temperature(350)
                            .descriptionId("fluid.tech_revised.heavy_crude_oil")));

    public static final RegistryObject<FlowingFluid> HEAVY_CRUDE_OIL = FLUIDS.register("heavy_crude_oil",
            () -> new ForgeFlowingFluid.Source(HeavyCrudeOilPropertiesHolder.PROPERTIES));

    public static final RegistryObject<FlowingFluid> FLOWING_HEAVY_CRUDE_OIL = FLUIDS.register("flowing_heavy_crude_oil",
            () -> new ForgeFlowingFluid.Flowing(HeavyCrudeOilPropertiesHolder.PROPERTIES));

    public static final RegistryObject<FluidType> CREOSOTE_TYPE = FLUID_TYPES.register("creosote_type",
            () -> new BaseFluidType(CREOSOTE_STILL_TEXTURE, CREOSOTE_FLOWING_TEXTURE, 0xFF4B2B16,
                    FluidType.Properties.create()
                            .density(1800)
                            .viscosity(5000)
                            .temperature(420)
                            .descriptionId("fluid.tech_revised.creosote")));

    public static final RegistryObject<FlowingFluid> CREOSOTE = FLUIDS.register("creosote",
            () -> new ForgeFlowingFluid.Source(CreosotePropertiesHolder.PROPERTIES));

    public static final RegistryObject<FlowingFluid> FLOWING_CREOSOTE = FLUIDS.register("flowing_creosote",
            () -> new ForgeFlowingFluid.Flowing(CreosotePropertiesHolder.PROPERTIES));

    public static final RegistryObject<FluidType> OXYGEN_TYPE = FLUID_TYPES.register("oxygen_type",
            () -> new BaseFluidType(OXYGEN_STILL_TEXTURE, OXYGEN_FLOWING_TEXTURE, 0xFF83D8FF,
                    FluidType.Properties.create()
                            .density(300)
                            .viscosity(600)
                            .temperature(295)
                            .descriptionId("fluid.tech_revised.oxygen")));

    public static final RegistryObject<FlowingFluid> OXYGEN = FLUIDS.register("oxygen",
            () -> new ForgeFlowingFluid.Source(OxygenPropertiesHolder.PROPERTIES));

    public static final RegistryObject<FlowingFluid> FLOWING_OXYGEN = FLUIDS.register("flowing_oxygen",
            () -> new ForgeFlowingFluid.Flowing(OxygenPropertiesHolder.PROPERTIES));

    public static final RegistryObject<FluidType> HYDROGEN_TYPE = FLUID_TYPES.register("hydrogen_type",
            () -> new BaseFluidType(HYDROGEN_STILL_TEXTURE, HYDROGEN_FLOWING_TEXTURE, 0xFFEEF9FF,
                    FluidType.Properties.create()
                            .density(100)
                            .viscosity(400)
                            .temperature(295)
                            .descriptionId("fluid.tech_revised.hydrogen")));

    public static final RegistryObject<FlowingFluid> HYDROGEN = FLUIDS.register("hydrogen",
            () -> new ForgeFlowingFluid.Source(HydrogenPropertiesHolder.PROPERTIES));

    public static final RegistryObject<FlowingFluid> FLOWING_HYDROGEN = FLUIDS.register("flowing_hydrogen",
            () -> new ForgeFlowingFluid.Flowing(HydrogenPropertiesHolder.PROPERTIES));

    private static final class HeavyCrudeOilPropertiesHolder {
        private static final ForgeFlowingFluid.Properties PROPERTIES = new ForgeFlowingFluid.Properties(
                HEAVY_CRUDE_OIL_TYPE, HEAVY_CRUDE_OIL, FLOWING_HEAVY_CRUDE_OIL)
                .bucket(ModItems.HEAVY_CRUDE_OIL_BUCKET)
                .block(ModBlocks.HEAVY_CRUDE_OIL_BLOCK)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(2)
                .tickRate(30);
    }

    private static final class CreosotePropertiesHolder {
        private static final ForgeFlowingFluid.Properties PROPERTIES = new ForgeFlowingFluid.Properties(
                CREOSOTE_TYPE, CREOSOTE, FLOWING_CREOSOTE)
                .bucket(ModItems.CREOSOTE_BUCKET)
                .block(ModBlocks.CREOSOTE_BLOCK)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(2)
                .tickRate(20);
    }

    private static final class OxygenPropertiesHolder {
        private static final ForgeFlowingFluid.Properties PROPERTIES = new ForgeFlowingFluid.Properties(
                OXYGEN_TYPE, OXYGEN, FLOWING_OXYGEN)
                .bucket(ModItems.OXYGEN_BUCKET)
                .block(ModBlocks.OXYGEN_BLOCK)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(2)
                .tickRate(10);
    }

    private static final class HydrogenPropertiesHolder {
        private static final ForgeFlowingFluid.Properties PROPERTIES = new ForgeFlowingFluid.Properties(
                HYDROGEN_TYPE, HYDROGEN, FLOWING_HYDROGEN)
                .bucket(ModItems.HYDROGEN_BUCKET)
                .block(ModBlocks.HYDROGEN_BLOCK)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(2)
                .tickRate(10);
    }

    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
        FLUIDS.register(eventBus);
    }
}
