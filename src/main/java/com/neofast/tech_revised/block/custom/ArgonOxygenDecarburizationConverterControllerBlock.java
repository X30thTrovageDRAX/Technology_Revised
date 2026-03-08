package com.neofast.tech_revised.block.custom;

import com.neofast.tech_revised.block.ModBlocks;
import com.neofast.tech_revised.block.entity.ModBlockEntities;
import com.neofast.tech_revised.block.entity.custom.ArgonOxygenDecarburizationConverterControllerBlockEntity;
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

public class ArgonOxygenDecarburizationConverterControllerBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final int MIN_X = -2;
    private static final int MAX_X = 2;
    private static final int MIN_Y = 0;
    private static final int MAX_Y = 11;
    private static final int MIN_Z = 0;
    private static final int MAX_Z = 4;

    private static final int CYLINDER_CENTER_Z = 2;
    private static final int MIN_RING_DISTANCE_SQUARED = 2;
    private static final int MAX_RING_DISTANCE_SQUARED = 5;

    public ArgonOxygenDecarburizationConverterControllerBlock(Properties properties) {
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
            player.displayClientMessage(Component.translatable(
                    "message.tech_revised.argon_oxygen_decarburization_converter.hologram"), true);
            return InteractionResult.CONSUME;
        }

        String validationKey = validateMultiblock(level, pos, front);
        boolean formed = "message.tech_revised.argon_oxygen_decarburization_converter.formed".equals(validationKey);
        updateFrameFormedStates(level, pos, front, formed);
        player.displayClientMessage(Component.translatable(validationKey), true);
        return InteractionResult.CONSUME;
    }

    public static boolean isStructureValid(Level level, BlockPos controllerPos, Direction front) {
        return "message.tech_revised.argon_oxygen_decarburization_converter.formed".equals(
                validateMultiblock(level, controllerPos, front));
    }

    public static BlockPos getInputBusPos(BlockPos controllerPos, Direction front) {
        return localToWorld(controllerPos, front, -2, 0, 2);
    }

    public static BlockPos getOutputBusPos(BlockPos controllerPos, Direction front) {
        return localToWorld(controllerPos, front, 2, 0, 2);
    }

    public static BlockPos getFluidInputBusPos(BlockPos controllerPos, Direction front) {
        return localToWorld(controllerPos, front, 1, 0, 0);
    }

    public static BlockPos getEnergyInputHatchPos(BlockPos controllerPos, Direction front) {
        return localToWorld(controllerPos, front, -1, 0, 0);
    }

    public static void updateFrameFormedStates(Level level, BlockPos controllerPos, Direction front, boolean formed) {
        if (level.isClientSide()) {
            return;
        }

        for (int y = MIN_Y; y <= MAX_Y; y++) {
            for (int z = MIN_Z; z <= MAX_Z; z++) {
                for (int x = MIN_X; x <= MAX_X; x++) {
                    if (isControllerPosition(x, y, z) || !isCylinderShellPosition(x, z)) {
                        continue;
                    }

                    BlockPos checkPos = localToWorld(controllerPos, front, x, y, z);
                    BlockState state = level.getBlockState(checkPos);
                    if (!state.hasProperty(ArgonOxygenDecarburizationConverterFrameBlock.FORMED)) {
                        continue;
                    }

                    if (state.getValue(ArgonOxygenDecarburizationConverterFrameBlock.FORMED) != formed) {
                        level.setBlock(checkPos, state.setValue(ArgonOxygenDecarburizationConverterFrameBlock.FORMED, formed), Block.UPDATE_CLIENTS);
                    }
                }
            }
        }
    }

    public static String validateMultiblock(Level level, BlockPos controllerPos, Direction front) {
        for (int y = MIN_Y; y <= MAX_Y; y++) {
            for (int z = MIN_Z; z <= MAX_Z; z++) {
                for (int x = MIN_X; x <= MAX_X; x++) {
                    if (isControllerPosition(x, y, z) || !isCylinderShellPosition(x, z)) {
                        continue;
                    }

                    Block expectedBlock = getExpectedBlock(x, y, z);
                    if (!matches(level, controllerPos, front, x, y, z, expectedBlock)) {
                        return getMissingMessageForPosition(x, y, z);
                    }
                }
            }
        }

        return "message.tech_revised.argon_oxygen_decarburization_converter.formed";
    }

    private static void showStructureHologram(Level level, BlockPos controllerPos, Direction front) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        for (int y = MIN_Y; y <= MAX_Y; y++) {
            for (int z = MIN_Z; z <= MAX_Z; z++) {
                for (int x = MIN_X; x <= MAX_X; x++) {
                    if (isControllerPosition(x, y, z) || !isCylinderShellPosition(x, z)) {
                        continue;
                    }

                    showPartHologram(serverLevel, controllerPos, front, x, y, z,
                            getExpectedBlock(x, y, z));
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
                    if (isControllerPosition(x, y, z) || !isCylinderShellPosition(x, z)) {
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
                    if (isControllerPosition(x, y, z) || !isCylinderShellPosition(x, z)) {
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

    private static Block getExpectedBlock(int localX, int localY, int localZ) {
        if (isInputBusPosition(localX, localY, localZ)) {
            return ModBlocks.ELECTRIC_ARC_FURNACE_INPUT_BUS.get();
        }
        if (isOutputBusPosition(localX, localY, localZ)) {
            return ModBlocks.ELECTRIC_ARC_FURNACE_OUTPUT_BUS.get();
        }
        if (isFluidInputBusPosition(localX, localY, localZ)) {
            return ModBlocks.ELECTRIC_ARC_FURNACE_FLUID_INPUT_BUS.get();
        }
        if (isEnergyInputHatchPosition(localX, localY, localZ)) {
            return ModBlocks.ELECTRIC_ARC_FURNACE_ENERGY_INPUT_HATCH.get();
        }
        return ModBlocks.ARGON_OXYGEN_DECARBURIZATION_CONVERTER_FRAME.get();
    }

    private static String getMissingMessageForPosition(int localX, int localY, int localZ) {
        if (isInputBusPosition(localX, localY, localZ)) {
            return "message.tech_revised.argon_oxygen_decarburization_converter.missing_input_bus";
        }
        if (isOutputBusPosition(localX, localY, localZ)) {
            return "message.tech_revised.argon_oxygen_decarburization_converter.missing_output_bus";
        }
        if (isFluidInputBusPosition(localX, localY, localZ)) {
            return "message.tech_revised.argon_oxygen_decarburization_converter.missing_fluid_input_bus";
        }
        if (isEnergyInputHatchPosition(localX, localY, localZ)) {
            return "message.tech_revised.argon_oxygen_decarburization_converter.missing_energy_input_hatch";
        }
        return "message.tech_revised.argon_oxygen_decarburization_converter.missing_frame";
    }

    private static boolean isInputBusPosition(int localX, int localY, int localZ) {
        return localX == -2 && localY == 0 && localZ == 2;
    }

    private static boolean isOutputBusPosition(int localX, int localY, int localZ) {
        return localX == 2 && localY == 0 && localZ == 2;
    }

    private static boolean isFluidInputBusPosition(int localX, int localY, int localZ) {
        return localX == 1 && localY == 0 && localZ == 0;
    }

    private static boolean isEnergyInputHatchPosition(int localX, int localY, int localZ) {
        return localX == -1 && localY == 0 && localZ == 0;
    }

    private static boolean matches(Level level, BlockPos controllerPos, Direction front,
                                   int localX, int localY, int localZ, Block expectedBlock) {
        BlockPos checkPos = localToWorld(controllerPos, front, localX, localY, localZ);
        return level.getBlockState(checkPos).is(expectedBlock);
    }

    private static boolean isControllerPosition(int localX, int localY, int localZ) {
        return localX == 0 && localY == 0 && localZ == 0;
    }

    private static boolean isCylinderShellPosition(int localX, int localZ) {
        int centeredZ = localZ - CYLINDER_CENTER_Z;
        int distanceSquared = localX * localX + centeredZ * centeredZ;
        return distanceSquared >= MIN_RING_DISTANCE_SQUARED && distanceSquared <= MAX_RING_DISTANCE_SQUARED;
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
        return new ArgonOxygenDecarburizationConverterControllerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.ARGON_OXYGEN_DECARBURIZATION_CONVERTER_CONTROLLER.get(),
                ArgonOxygenDecarburizationConverterControllerBlockEntity::tick);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            updateFrameFormedStates(level, pos, state.getValue(FACING), false);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
