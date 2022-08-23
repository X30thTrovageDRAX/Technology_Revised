
package com.neofast.tech_revised.block.entity;

import com.neofast.tech_revised.TechRevised;
import com.neofast.tech_revised.block.ModBlocks;
import com.neofast.tech_revised.block.custom.WorkbenchTransistors_Basic;
import com.neofast.tech_revised.block.entity.custom.WorkbenchTransistorsBlockEntity;
import com.neofast.tech_revised.block.entity.custom.WorkbenchTransistors_BasicBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, TechRevised.MOD_ID);

    public static final RegistryObject<BlockEntityType<WorkbenchTransistorsBlockEntity>> WORKBENCH_TRANSISTORS =
            BLOCK_ENTITIES.register("workbench_transistors_entity", () ->
                    BlockEntityType.Builder.of(WorkbenchTransistorsBlockEntity::new,
                            ModBlocks.WORKBENCH_TRANSISTORS.get()).build(null));
    public static final RegistryObject<BlockEntityType<WorkbenchTransistors_BasicBlockEntity>> WORKBENCH_TRANSISTORS_BASIC =
            BLOCK_ENTITIES.register("workbench_transistors_basic_entity", () ->
                    BlockEntityType.Builder.of(WorkbenchTransistors_BasicBlockEntity::new,
                            ModBlocks.WORKBENCH_TRANSISTORS_BASIC.get()).build(null));


    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}