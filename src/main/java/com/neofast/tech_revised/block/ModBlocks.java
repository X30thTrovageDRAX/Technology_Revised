 package com.neofast.tech_revised.block;

 import com.neofast.tech_revised.TechRevised;
 import com.neofast.tech_revised.block.custom.WorkbenchTransistors;
 import com.neofast.tech_revised.item.ModItems;
 import net.minecraft.network.chat.Component;
 import net.minecraft.network.chat.TranslatableComponent;
 import net.minecraft.world.item.*;
 import net.minecraft.world.level.Level;
 import net.minecraft.world.level.block.*;
 import net.minecraft.world.level.block.state.BlockBehaviour;
 import net.minecraft.world.level.material.Material;
 import net.minecraftforge.eventbus.api.IEventBus;
 import net.minecraftforge.registries.DeferredRegister;
 import net.minecraftforge.registries.ForgeRegistries;
 import net.minecraftforge.registries.RegistryObject;
 import org.jetbrains.annotations.Nullable;

 import java.util.List;
 import java.util.function.Supplier;


 public class ModBlocks {
     public static final DeferredRegister<Block> BLOCKS =
             DeferredRegister.create(ForgeRegistries.BLOCKS, TechRevised.MOD_ID);

     public static final RegistryObject<Block> COMPRESSED_SILICON = registerBlock("compressed_silicon",
             () -> new Block(BlockBehaviour.Properties.of(Material.METAL)
                     .strength(9f).requiresCorrectToolForDrops()), CreativeModeTab.TAB_MISC);

     public static final RegistryObject<Block> WORKBENCH_TRANSISTORS = registerBlock("workbench_transistors",
             () -> new WorkbenchTransistors(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion()),
             CreativeModeTab.TAB_MISC);

     private static <T extends Block> RegistryObject<T> registerBlockWithoutBlockItem(String name, Supplier<T> block) {
         return BLOCKS.register(name, block);
     }

     private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block,
                                                                      CreativeModeTab tab, String tooltipKey) {
         RegistryObject<T> toReturn = BLOCKS.register(name, block);
         registerBlockItem(name, toReturn, tab, tooltipKey);
         return toReturn;
     }

     private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block,
                                                                             CreativeModeTab tab, String tooltipKey) {
         return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(),
                 new Item.Properties().tab(tab)) {
             @Override
             public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
                 pTooltip.add(new TranslatableComponent(tooltipKey));
             }
         });
     }

     private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block, CreativeModeTab tab) {
         RegistryObject<T> toReturn = BLOCKS.register(name, block);
         registerBlockItem(name, toReturn, tab);
         return toReturn;
     }

     private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block,
                                                                             CreativeModeTab tab) {
         return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(),
                 new Item.Properties().tab(tab)));
     }

     public static void register(IEventBus eventBus) {
         BLOCKS.register(eventBus);
     }
 }