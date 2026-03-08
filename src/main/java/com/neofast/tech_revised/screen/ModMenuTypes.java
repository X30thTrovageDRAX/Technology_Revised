package com.neofast.tech_revised.screen;

import com.neofast.tech_revised.TechRevised;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, TechRevised.MOD_ID);

    public static final RegistryObject<MenuType<ElectricArcFurnaceControllerMenu>> ELECTRIC_ARC_FURNACE_CONTROLLER_MENU =
            registerMenuType(ElectricArcFurnaceControllerMenu::new, "electric_arc_furnace_controller_menu");

    public static final RegistryObject<MenuType<ElectricArcFurnaceInputBusMenu>> ELECTRIC_ARC_FURNACE_INPUT_BUS_MENU =
            registerMenuType(ElectricArcFurnaceInputBusMenu::new, "electric_arc_furnace_input_bus_menu");

    public static final RegistryObject<MenuType<ElectricArcFurnaceOutputBusMenu>> ELECTRIC_ARC_FURNACE_OUTPUT_BUS_MENU =
            registerMenuType(ElectricArcFurnaceOutputBusMenu::new, "electric_arc_furnace_output_bus_menu");

    public static final RegistryObject<MenuType<CrusherMenu>> CRUSHER_MENU =
            registerMenuType(CrusherMenu::new, "crusher_menu");

    private static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> registerMenuType(IContainerFactory<T> factory,
                                                                                                    String name) {
        return MENUS.register(name, () -> IForgeMenuType.create(factory));
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
