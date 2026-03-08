package com.neofast.tech_revised.block.custom;

import com.neofast.tech_revised.block.ModBlocks;
import com.neofast.tech_revised.block.entity.ModBlockEntities;
import com.neofast.tech_revised.block.entity.custom.CokeOvenControllerBlockEntity;
import com.neofast.tech_revised.block.entity.custom.ElectricArcFurnaceFluidOutputBusBlockEntity;
import com.neofast.tech_revised.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public class CokeOvenControllerBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final int MIN_X = -1;
    private static final int MAX_X = 1;
    private static final int MIN_Y = 0;
    private static final int MAX_Y = 2;
    private static final int MIN_Z = 0;
    private static final int MAX_Z = 2;

    public CokeOvenControllerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        Direction front = state.getValue(FACING);
        ItemStack heldStack = player.getItemInHand(hand);

        // Let offhand configurator interactions pass through when main hand is used first.
        if (hand == InteractionHand.MAIN_HAND
                && !heldStack.is(ModItems.CONFIGURATOR.get())
                && player.getItemInHand(InteractionHand.OFF_HAND).is(ModItems.CONFIGURATOR.get())) {
            return InteractionResult.PASS;
        }

        if (heldStack.is(ModItems.CONFIGURATOR.get())) {
            return tryAutoBuildWithConfigurator(level, pos, front, player);
        }

        if (player.isShiftKeyDown()) {
            showStructureHologram(level, pos, front);
            player.displayClientMessage(Component.translatable("message.tech_revised.coke_oven.hologram"), true);
            return InteractionResult.CONSUME;
        }

        String validationKey = validateMultiblock(level, pos, front);
        boolean formed = "message.tech_revised.coke_oven.formed".equals(validationKey);
        updateFrameFormedStates(level, pos, front, formed);
        player.displayClientMessage(Component.translatable(validationKey), true);

        if (formed) {
            BlockEntity controllerEntity = level.getBlockEntity(pos);
            if (controllerEntity instanceof CokeOvenControllerBlockEntity controller) {
                int progressPercent = controller.getProgress() * 100 / Math.max(1, controller.getProcessTicks());
                player.displayClientMessage(Component.literal(
                        "Coking Progress: " + progressPercent + "%"), false);
            }

            BlockEntity fluidEntity = level.getBlockEntity(getFluidOutputBusPos(pos, front));
            if (fluidEntity instanceof ElectricArcFurnaceFluidOutputBusBlockEntity fluidOutputBus) {
                String fluidName = fluidOutputBus.getStoredFluid().isEmpty()
                        ? "Empty"
                        : fluidOutputBus.getStoredFluid().getDisplayName().getString();
                player.displayClientMessage(Component.literal(
                        "Fluid Output: " + fluidName + " (" + fluidOutputBus.getFluidAmount() + " / " +
                                fluidOutputBus.getCapacity() + " mB)"), false);
            }
        }

        return InteractionResult.CONSUME;
    }

    public static boolean isStructureValid(Level level, BlockPos controllerPos, Direction front) {
        return "message.tech_revised.coke_oven.formed".equals(validateMultiblock(level, controllerPos, front));
    }

    public static void updateFrameFormedStates(Level level, BlockPos controllerPos, Direction front, boolean formed) {
        if (level.isClientSide()) {
            return;
        }

        for (int y = MIN_Y; y <= MAX_Y; y++) {
            for (int z = MIN_Z; z <= MAX_Z; z++) {
                for (int x = MIN_X; x <= MAX_X; x++) {
                    if (isControllerPosition(x, y, z)) {
                        continue;
                    }

                    BlockPos checkPos = localToWorld(controllerPos, front, x, y, z);
                    BlockState checkState = level.getBlockState(checkPos);
                    if (!checkState.hasProperty(CokeOvenFrameBlock.FORMED)) {
                        continue;
                    }

                    if (checkState.getValue(CokeOvenFrameBlock.FORMED) != formed) {
                        level.setBlock(checkPos, checkState.setValue(CokeOvenFrameBlock.FORMED, formed), Block.UPDATE_CLIENTS);
                    }
                }
            }
        }
    }

    public static BlockPos getInputBusPos(BlockPos controllerPos, Direction front) {
        return localToWorld(controllerPos, front, -1, 0, 1);
    }

    public static BlockPos getOutputBusPos(BlockPos controllerPos, Direction front) {
        return localToWorld(controllerPos, front, 1, 0, 1);
    }

    public static BlockPos getFluidOutputBusPos(BlockPos controllerPos, Direction front) {
        return localToWorld(controllerPos, front, 0, 0, 2);
    }

    public static String validateMultiblock(Level level, BlockPos controllerPos, Direction front) {
        for (int y = MIN_Y; y <= MAX_Y; y++) {
            for (int z = MIN_Z; z <= MAX_Z; z++) {
                for (int x = MIN_X; x <= MAX_X; x++) {
                    if (isControllerPosition(x, y, z)) {
                        continue;
                    }

                    Block expectedBlock = getExpectedBlock(x, y, z);
                    if (!matches(level, controllerPos, front, x, y, z, expectedBlock)) {
                        return getMissingMessageForExpectedBlock(expectedBlock);
                    }
                }
            }
        }

        return "message.tech_revised.coke_oven.formed";
    }

    private static void showStructureHologram(Level level, BlockPos controllerPos, Direction front) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        for (int y = MIN_Y; y <= MAX_Y; y++) {
            for (int z = MIN_Z; z <= MAX_Z; z++) {
                for (int x = MIN_X; x <= MAX_X; x++) {
                    if (isControllerPosition(x, y, z)) {
                        continue;
                    }
                    showPartHologram(serverLevel, controllerPos, front, x, y, z, getExpectedBlock(x, y, z));
                }
            }
        }
    }

    private static void showPartHologram(ServerLevel level, BlockPos controllerPos, Direction front,
                                         int localX, int localY, int localZ, Block expectedBlock) {
        BlockPos targetPos = localToWorld(controllerPos, front, localX, localY, localZ);
        if (!level.getBlockState(targetPos).is(expectedBlock)) {
            BlockParticleOption particle = new BlockParticleOption(ParticleTypes.BLOCK_MARKER, expectedBlock.defaultBlockState());
            level.sendParticles(particle, targetPos.getX() + 0.5D, targetPos.getY() + 0.2D, targetPos.getZ() + 0.5D, 1, 0, 0, 0, 0);
            level.sendParticles(particle, targetPos.getX() + 0.5D, targetPos.getY() + 0.5D, targetPos.getZ() + 0.5D, 1, 0, 0, 0, 0);
            level.sendParticles(particle, targetPos.getX() + 0.5D, targetPos.getY() + 0.8D, targetPos.getZ() + 0.5D, 1, 0, 0, 0, 0);
        }
    }

    private static InteractionResult tryAutoBuildWithConfigurator(Level level, BlockPos controllerPos, Direction front, Player player) {
        Map<Item, Integer> requiredItems = new LinkedHashMap<>();

        for (int y = MIN_Y; y <= MAX_Y; y++) {
            for (int z = MIN_Z; z <= MAX_Z; z++) {
                for (int x = MIN_X; x <= MAX_X; x++) {
                    if (isControllerPosition(x, y, z)) {
                        continue;
                    }

                    Block expectedBlock = getExpectedBlock(x, y, z);
                    BlockPos targetPos = localToWorld(controllerPos, front, x, y, z);
                    BlockState currentState = level.getBlockState(targetPos);

                    if (currentState.is(expectedBlock)) {
                        continue;
                    }
                    if (!currentState.canBeReplaced()) {
                        player.displayClientMessage(Component.translatable(
                                "message.tech_revised.multiblock.autoconfig_blocked",
                                targetPos.getX(), targetPos.getY(), targetPos.getZ()), true);
                        return InteractionResult.CONSUME;
                    }

                    requiredItems.merge(expectedBlock.asItem(), 1, Integer::sum);
                }
            }
        }

        if (!player.getAbilities().instabuild) {
            for (Map.Entry<Item, Integer> entry : requiredItems.entrySet()) {
                int available = countItemInInventory(player, entry.getKey());
                if (available < entry.getValue()) {
                    player.displayClientMessage(Component.translatable(
                            "message.tech_revised.multiblock.autoconfig_missing",
                            entry.getValue() - available,
                            entry.getKey().getDescription()), true);
                    return InteractionResult.CONSUME;
                }
            }

            for (Map.Entry<Item, Integer> entry : requiredItems.entrySet()) {
                consumeItemFromInventory(player, entry.getKey(), entry.getValue());
            }
        }

        for (int y = MIN_Y; y <= MAX_Y; y++) {
            for (int z = MIN_Z; z <= MAX_Z; z++) {
                for (int x = MIN_X; x <= MAX_X; x++) {
                    if (isControllerPosition(x, y, z)) {
                        continue;
                    }

                    Block expectedBlock = getExpectedBlock(x, y, z);
                    BlockPos targetPos = localToWorld(controllerPos, front, x, y, z);
                    BlockState currentState = level.getBlockState(targetPos);
                    if (!currentState.is(expectedBlock) && currentState.canBeReplaced()) {
                        level.setBlock(targetPos, expectedBlock.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }
        }

        boolean formed = isStructureValid(level, controllerPos, front);
        updateFrameFormedStates(level, controllerPos, front, formed);
        player.displayClientMessage(Component.translatable(
                formed
                        ? "message.tech_revised.multiblock.autoconfig_success"
                        : "message.tech_revised.multiblock.autoconfig_incomplete"), true);
        return InteractionResult.CONSUME;
    }

    private static int countItemInInventory(Player player, Item item) {
        int count = 0;
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static void consumeItemFromInventory(Player player, Item item, int amount) {
        Inventory inventory = player.getInventory();
        int remaining = amount;
        for (int i = 0; i < inventory.getContainerSize() && remaining > 0; i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.is(item)) {
                continue;
            }

            int extracted = Math.min(remaining, stack.getCount());
            stack.shrink(extracted);
            remaining -= extracted;
            if (stack.isEmpty()) {
                inventory.setItem(i, ItemStack.EMPTY);
            }
        }
        inventory.setChanged();
    }

    private static boolean matches(Level level, BlockPos controllerPos, Direction front,
                                   int localX, int localY, int localZ, Block expectedBlock) {
        BlockPos checkPos = localToWorld(controllerPos, front, localX, localY, localZ);
        return level.getBlockState(checkPos).is(expectedBlock);
    }

    private static boolean isControllerPosition(int localX, int localY, int localZ) {
        return localX == 0 && localY == 0 && localZ == 0;
    }

    private static Block getExpectedBlock(int localX, int localY, int localZ) {
        if (localX == -1 && localY == 0 && localZ == 1) {
            return ModBlocks.ELECTRIC_ARC_FURNACE_INPUT_BUS.get();
        }
        if (localX == 1 && localY == 0 && localZ == 1) {
            return ModBlocks.ELECTRIC_ARC_FURNACE_OUTPUT_BUS.get();
        }
        if (localX == 0 && localY == 0 && localZ == 2) {
            return ModBlocks.ELECTRIC_ARC_FURNACE_FLUID_OUTPUT_BUS.get();
        }
        return ModBlocks.COKE_OVEN_FRAME.get();
    }

    private static String getMissingMessageForExpectedBlock(Block expectedBlock) {
        if (expectedBlock == ModBlocks.ELECTRIC_ARC_FURNACE_INPUT_BUS.get()) {
            return "message.tech_revised.coke_oven.missing_input_bus";
        }
        if (expectedBlock == ModBlocks.ELECTRIC_ARC_FURNACE_OUTPUT_BUS.get()) {
            return "message.tech_revised.coke_oven.missing_output_bus";
        }
        if (expectedBlock == ModBlocks.ELECTRIC_ARC_FURNACE_FLUID_OUTPUT_BUS.get()) {
            return "message.tech_revised.coke_oven.missing_fluid_output_bus";
        }
        return "message.tech_revised.coke_oven.missing_frame";
    }

    // localX is right-left, localY is up-down, localZ is distance behind the controller front
    private static BlockPos localToWorld(BlockPos origin, Direction front, int localX, int localY, int localZ) {
        Direction right = front.getClockWise();
        Direction back = front.getOpposite();
        return origin.relative(right, localX).relative(back, localZ).offset(0, localY, 0);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CokeOvenControllerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.COKE_OVEN_CONTROLLER.get(),
                CokeOvenControllerBlockEntity::tick);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            updateFrameFormedStates(level, pos, state.getValue(FACING), false);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
