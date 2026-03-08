package com.neofast.tech_revised;

import com.neofast.tech_revised.block.ModBlocks;
import com.neofast.tech_revised.block.entity.ModBlockEntities;
import com.neofast.tech_revised.fluid.ModFluids;
import com.neofast.tech_revised.item.ModCreativeModeTabs;
import com.neofast.tech_revised.item.ModItems;
import com.neofast.tech_revised.recipe.ModRecipes;
import com.neofast.tech_revised.screen.CrusherScreen;
import com.neofast.tech_revised.screen.ElectricArcFurnaceControllerScreen;
import com.neofast.tech_revised.screen.ElectricArcFurnaceInputBusScreen;
import com.neofast.tech_revised.screen.ElectricArcFurnaceOutputBusScreen;
import com.neofast.tech_revised.screen.ModMenuTypes;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(TechRevised.MOD_ID)
public class TechRevised
{
    public static final String MOD_ID = "tech_revised";
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public TechRevised() {
        // Register the setup method for modloading
        IEventBus eventBus =   FMLJavaModLoadingContext.get().getModEventBus();
        ModCreativeModeTabs.register(eventBus);
        ModItems.register(eventBus);
        ModBlocks.register(eventBus);
        ModBlockEntities.register(eventBus);
        ModFluids.register(eventBus);
        ModMenuTypes.register(eventBus);
        ModRecipes.register(eventBus);

        eventBus.addListener(this::setup);
        eventBus.addListener(this::clientSetup);
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        MenuScreens.register(ModMenuTypes.CRUSHER_MENU.get(), CrusherScreen::new);
        MenuScreens.register(ModMenuTypes.ELECTRIC_ARC_FURNACE_CONTROLLER_MENU.get(), ElectricArcFurnaceControllerScreen::new);
        MenuScreens.register(ModMenuTypes.ELECTRIC_ARC_FURNACE_INPUT_BUS_MENU.get(), ElectricArcFurnaceInputBusScreen::new);
        MenuScreens.register(ModMenuTypes.ELECTRIC_ARC_FURNACE_OUTPUT_BUS_MENU.get(), ElectricArcFurnaceOutputBusScreen::new);
        ItemBlockRenderTypes.setRenderLayer(ModFluids.HEAVY_CRUDE_OIL.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.FLOWING_HEAVY_CRUDE_OIL.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.CREOSOTE.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.FLOWING_CREOSOTE.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.OXYGEN.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.FLOWING_OXYGEN.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.HYDROGEN.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.FLOWING_HYDROGEN.get(), RenderType.translucent());
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        // some preinit code
        LOGGER.info("HELLO FROM PREINIT");
        LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));
    }
}
