package com.neofast.tech_revised.client;

import com.neofast.tech_revised.TechRevised;
import com.neofast.tech_revised.block.ModBlocks;
import com.neofast.tech_revised.item.ModItems;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.model.DynamicFluidContainerModel;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TechRevised.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModItemColorHandlers {
    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        ModBlocks.getTintedBlockColors().forEach((block, color) ->
                event.getBlockColors().register((state, level, pos, tintIndex) -> tintIndex == 0 ? color : 0xFFFFFFFF, block.get()));
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        ModItems.getTintedItemColors().forEach((item, color) ->
                event.getItemColors().register((stack, tintIndex) -> tintIndex == 0 ? color : 0xFFFFFFFF, item.get()));

        event.getItemColors().register(new DynamicFluidContainerModel.Colors(), ModItems.HEAVY_CRUDE_OIL_BUCKET.get());
    }
}
