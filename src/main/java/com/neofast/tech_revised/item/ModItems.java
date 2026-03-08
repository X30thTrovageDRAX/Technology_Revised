package com.neofast.tech_revised.item;

import com.neofast.tech_revised.TechRevised;
import com.neofast.tech_revised.fluid.ModFluids;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, TechRevised.MOD_ID);

    public static final int STEEL_TINT = 0xFF8E949C;
    public static final int STAINLESS_STEEL_TINT = 0xFFC7CCD2;
    public static final int CHROMITE_TINT = 0xFF8D7260;
    public static final int FERROCHROMIUM_TINT = 0xFF6C7364;

    // Shared item tint map (ingots, nuggets, and tinted block items).
    private static final Map<RegistryObject<Item>, Integer> TINTED_ITEM_COLORS = new LinkedHashMap<>();

    public static final RegistryObject<Item> STEEL_INGOT = registerTintedItem("steel_ingot", STEEL_TINT);
    public static final RegistryObject<Item> STEEL_NUGGET = registerTintedItem("steel_nugget", STEEL_TINT);
    public static final RegistryObject<Item> STAINLESS_STEEL_INGOT = registerTintedItem("stainless_steel_ingot", STAINLESS_STEEL_TINT);
    public static final RegistryObject<Item> STAINLESS_STEEL_NUGGET = registerTintedItem("stainless_steel_nugget", STAINLESS_STEEL_TINT);
    public static final RegistryObject<Item> CHROMITE_INGOT = registerTintedItem("chromite_ingot", CHROMITE_TINT);
    public static final RegistryObject<Item> CHROMITE_NUGGET = registerTintedItem("chromite_nugget", CHROMITE_TINT);
    public static final RegistryObject<Item> FERROCHROMIUM_INGOT = registerTintedItem("ferrochromium_ingot", FERROCHROMIUM_TINT);
    public static final RegistryObject<Item> FERROCHROMIUM_NUGGET = registerTintedItem("ferrochromium_nugget", FERROCHROMIUM_TINT);
    public static final RegistryObject<Item> GRAPHITE_DUST = ITEMS.register("graphite_dust",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ELECTRODE = ITEMS.register("electrode",
            () -> new Item(new Item.Properties().durability(256)));
    public static final RegistryObject<Item> CHROMITE_DUST = ITEMS.register("chromite_dust",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> COAL_COKE = ITEMS.register("coal_coke",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CONFIGURATOR = ITEMS.register("configurator",
            () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> HEAVY_CRUDE_OIL_BUCKET = ITEMS.register("heavy_crude_oil_bucket",
            () -> new BucketItem(ModFluids.HEAVY_CRUDE_OIL, new Item.Properties()
                    .craftRemainder(Items.BUCKET)
                    .stacksTo(1)));
    public static final RegistryObject<Item> CREOSOTE_BUCKET = ITEMS.register("creosote_bucket",
            () -> new BucketItem(ModFluids.CREOSOTE, new Item.Properties()
                    .craftRemainder(Items.BUCKET)
                    .stacksTo(1)));
    public static final RegistryObject<Item> OXYGEN_BUCKET = ITEMS.register("oxygen_bucket",
            () -> new BucketItem(ModFluids.OXYGEN, new Item.Properties()
                    .craftRemainder(Items.BUCKET)
                    .stacksTo(1)));
    public static final RegistryObject<Item> HYDROGEN_BUCKET = ITEMS.register("hydrogen_bucket",
            () -> new BucketItem(ModFluids.HYDROGEN, new Item.Properties()
                    .craftRemainder(Items.BUCKET)
                    .stacksTo(1)));

    private static RegistryObject<Item> registerTintedItem(String name, int color) {
        RegistryObject<Item> item = ITEMS.register(name, () -> new Item(new Item.Properties()));
        registerTintedItemColor(item, color);
        return item;
    }

    public static void registerTintedItemColor(RegistryObject<Item> item, int color) {
        TINTED_ITEM_COLORS.put(item, color);
    }

    public static Map<RegistryObject<Item>, Integer> getTintedItemColors() {
        return Collections.unmodifiableMap(TINTED_ITEM_COLORS);
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
