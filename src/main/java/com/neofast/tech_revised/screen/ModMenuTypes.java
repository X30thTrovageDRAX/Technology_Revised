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
            DeferredRegister.create(ForgeRegistries.CONTAINERS, TechRevised.MOD_ID);

    public static final RegistryObject<MenuType<WorkbenchTransistorsMenu>> WORKBENCH_TRANSISTORS_MENU =
            registerMenuType(WorkbenchTransistorsMenu::new, "workbench_transistors_menu");

    public static final RegistryObject<MenuType<WorkbenchTransistors_BasicMenu>> WORKBENCH_TRANSISTORS_BASIC_MENU =
            registerMenuType(WorkbenchTransistors_BasicMenu::new, "workbench_transistors_basic_menu");



    private static <T extends AbstractContainerMenu>RegistryObject<MenuType<T>> registerMenuType(IContainerFactory<T> factory,
                                                                                                 String name) {
        return MENUS.register(name, () -> IForgeMenuType.create(factory));
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}