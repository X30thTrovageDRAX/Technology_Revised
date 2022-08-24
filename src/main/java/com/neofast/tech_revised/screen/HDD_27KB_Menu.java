package com.neofast.tech_revised.screen;

import com.neofast.tech_revised.block.ModBlocks;
import com.neofast.tech_revised.block.entity.custom.HDD_27KB_BlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class HDD_27KB_Menu extends AbstractContainerMenu {
    private final HDD_27KB_BlockEntity blockEntity;
    private final Level level;
    public HDD_27KB_Menu(int pContainerId, Inventory inv, FriendlyByteBuf extraData) {
        this(pContainerId, inv, inv.player.level.getBlockEntity(extraData.readBlockPos()));
    }

    public HDD_27KB_Menu(int pContainerId, Inventory inv, BlockEntity entity) {
        super(ModMenuTypes.HDD_27KB_MENU.get(), pContainerId);
        checkContainerSize(inv, 27);
        blockEntity = ((HDD_27KB_BlockEntity) entity);
        this.level = inv.player.level;//..

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(handler -> {
            this.addSlot(new SlotItemHandler(handler, 0, 8, 8));
            this.addSlot(new SlotItemHandler(handler, 1, 26, 8));
            this.addSlot(new SlotItemHandler(handler, 2, 44, 8));
            this.addSlot(new SlotItemHandler(handler, 3, 62, 8));
            this.addSlot(new SlotItemHandler(handler, 4, 80, 8));
            this.addSlot(new SlotItemHandler(handler, 5, 98, 8));
            this.addSlot(new SlotItemHandler(handler, 6, 116, 8));
            this.addSlot(new SlotItemHandler(handler, 7, 134, 8));
            this.addSlot(new SlotItemHandler(handler, 8, 152, 8));
            this.addSlot(new SlotItemHandler(handler, 9, 8, 26));
            this.addSlot(new SlotItemHandler(handler, 10, 26, 26));
            this.addSlot(new SlotItemHandler(handler, 11, 44, 26));
            this.addSlot(new SlotItemHandler(handler, 12, 62, 26));
            this.addSlot(new SlotItemHandler(handler, 13, 80, 26));
            this.addSlot(new SlotItemHandler(handler, 14, 98, 26));
            this.addSlot(new SlotItemHandler(handler, 15, 116, 26));
            this.addSlot(new SlotItemHandler(handler, 16, 134, 26));
            this.addSlot(new SlotItemHandler(handler, 17, 152, 26));
            this.addSlot(new SlotItemHandler(handler, 18, 8, 44));
            this.addSlot(new SlotItemHandler(handler, 19, 26, 44));
            this.addSlot(new SlotItemHandler(handler, 20, 44, 44));
            this.addSlot(new SlotItemHandler(handler, 21, 62, 44));
            this.addSlot(new SlotItemHandler(handler, 22, 80, 44));
            this.addSlot(new SlotItemHandler(handler, 23, 98, 44));
            this.addSlot(new SlotItemHandler(handler, 24, 116, 44));
            this.addSlot(new SlotItemHandler(handler, 25, 134, 44));
            this.addSlot(new SlotItemHandler(handler, 26, 152, 44));
        });
    }

    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;

    // THIS YOU HAVE TO DEFINE!
    private static final int TE_INVENTORY_SLOT_COUNT = 27;  // must be the number of slots you have!

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        Slot sourceSlot = slots.get(index);
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // Check if the slot clicked is one of the vanilla container slots
        if (index < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            // This is a vanilla container slot so merge the stack into the tile inventory
            if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX
                    + TE_INVENTORY_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;  // EMPTY_ITEM
            }
        } else if (index < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            // This is a TE slot so merge the stack into the players inventory
            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            System.out.println("Invalid slotIndex:" + index);
            return ItemStack.EMPTY;
        }
        // If stack size == 0 (the entire stack was moved) set slot contents to null
        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                pPlayer, ModBlocks.HDD_27KB_BLOCK.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 86 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 144));
        }
    }
}