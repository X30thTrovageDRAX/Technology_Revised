package com.neofast.tech_revised.block.entity.custom;

import com.neofast.tech_revised.block.entity.ModBlockEntities;
import com.neofast.tech_revised.item.ModItems;
import com.neofast.tech_revised.screen.WorkbenchTransistorsMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;


public class WorkbenchTransistorsBlockEntity extends BlockEntity implements MenuProvider {
    private final ItemStackHandler itemHandler = new ItemStackHandler(8) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public WorkbenchTransistorsBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(ModBlockEntities.WORKBENCH_TRANSISTORS.get(), pWorldPosition, pBlockState);
    }

    @Override
    public Component getDisplayName() {
        return new TextComponent("Transistors Workbench");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory, Player pPlayer) {
        return new WorkbenchTransistorsMenu(pContainerId, pInventory, this);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @javax.annotation.Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return lazyItemHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void invalidateCaps()  {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        tag.put("inventory", itemHandler.serializeNBT());
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }


    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, WorkbenchTransistorsBlockEntity pBlockEntity) {
        if(hasRecipe(pBlockEntity) && hasNotReachedStackLimit(pBlockEntity)) {
            craftItem(pBlockEntity);
        }
    }

    private static void craftItem(WorkbenchTransistorsBlockEntity entity) {
        entity.itemHandler.extractItem(0, 1, false);
        entity.itemHandler.extractItem(1, 1, false);
        entity.itemHandler.extractItem(2, 1, false);
        entity.itemHandler.extractItem(3, 1, false);
        entity.itemHandler.extractItem(4, 1, false);
        entity.itemHandler.extractItem(5, 1, false);

        entity.itemHandler.setStackInSlot(7, new ItemStack(ModItems.TRANSISTOR_1.get(),
                entity.itemHandler.getStackInSlot(7).getCount() + 1));
    }

    private static boolean hasRecipe(WorkbenchTransistorsBlockEntity entity) {
        boolean hasItemInFirstSlot = entity.itemHandler.getStackInSlot(0).getItem() == ModItems.SILICON.get();
        boolean hasItemInSecondSlot = entity.itemHandler.getStackInSlot(1).getItem() == Items.GOLD_INGOT.asItem();
        boolean hasItemInThirdSlot = entity.itemHandler.getStackInSlot(2).getItem() == Items.IRON_NUGGET.asItem();
        boolean hasItemInFourthSlot = entity.itemHandler.getStackInSlot(3).getItem() == ModItems.SILICON.get();
        boolean hasItemInFifthSlot = entity.itemHandler.getStackInSlot(4).getItem() == Items.GOLD_INGOT.asItem();
        boolean hasItemInSixthSlot = entity.itemHandler.getStackInSlot(5).getItem() == Items.IRON_NUGGET.asItem();

        return hasItemInFirstSlot && hasItemInSecondSlot && hasItemInThirdSlot && hasItemInFourthSlot && hasItemInFifthSlot && hasItemInSixthSlot;
    }

    private static boolean hasNotReachedStackLimit(WorkbenchTransistorsBlockEntity entity) {
        return entity.itemHandler.getStackInSlot(5).getCount() < entity.itemHandler.getStackInSlot(5).getMaxStackSize();
    }
}