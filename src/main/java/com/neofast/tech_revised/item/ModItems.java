package com.neofast.tech_revised.item;

import com.neofast.tech_revised.TechRevised;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, TechRevised.MOD_ID);
    public static final RegistryObject<Item> TRANSISTOR_1 = ITEMS.register("transistor_1",
            () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
    public static final RegistryObject<Item> SILICON = ITEMS.register("silicon",
            () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> PCB = ITEMS.register("pcb",
            () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> TRANSISTOR_2 = ITEMS.register("transistor_2",
            () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> CHIPSET_1 = ITEMS.register("chipset_1",
            () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> CHIPSET_2 = ITEMS.register("chipset_2",
            () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> TRANSISTOR_0 = ITEMS.register("transistor_0",
            () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));



    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
