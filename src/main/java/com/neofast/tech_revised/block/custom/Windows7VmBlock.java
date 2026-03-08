package com.neofast.tech_revised.block.custom;

import com.neofast.tech_revised.block.entity.custom.Windows7VmBlockEntity;
import com.neofast.tech_revised.integration.vm.VmCommandIntegration;
import com.neofast.tech_revised.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class Windows7VmBlock extends BaseEntityBlock {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public Windows7VmBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(ACTIVE, false));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.CONSUME;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof Windows7VmBlockEntity vmBlockEntity)) {
            return InteractionResult.PASS;
        }

        if (!serverPlayer.hasPermissions(2)) {
            serverPlayer.displayClientMessage(Component.translatable("message.tech_revised.vm.permission_denied"), true);
            return InteractionResult.CONSUME;
        }

        ItemStack heldStack = player.getItemInHand(hand);

        if (hand == InteractionHand.MAIN_HAND
                && !heldStack.is(ModItems.CONFIGURATOR.get())
                && player.getItemInHand(InteractionHand.OFF_HAND).is(ModItems.CONFIGURATOR.get())) {
            return InteractionResult.PASS;
        }

        if (heldStack.is(ModItems.CONFIGURATOR.get())) {
            NetworkHooks.openScreen(serverPlayer, vmBlockEntity, buffer -> {
                buffer.writeBlockPos(pos);
                buffer.writeUtf(vmBlockEntity.getVmName(), Windows7VmBlockEntity.MAX_VM_NAME_LENGTH);
                buffer.writeUtf(vmBlockEntity.getStartCommand(), Windows7VmBlockEntity.MAX_COMMAND_LENGTH);
                buffer.writeUtf(vmBlockEntity.getStopCommand(), Windows7VmBlockEntity.MAX_COMMAND_LENGTH);
            });
            return InteractionResult.CONSUME;
        }

        VmCommandIntegration.VmAction action = player.isShiftKeyDown()
                ? VmCommandIntegration.VmAction.STOP
                : VmCommandIntegration.VmAction.START;
        String vmName = VmCommandIntegration.resolveVmName(vmBlockEntity.getVmName());

        serverPlayer.displayClientMessage(
                VmCommandIntegration.getActionStartedMessage(action, vmName),
                true
        );
        VmCommandIntegration.executeAction(
                serverLevel,
                pos,
                serverPlayer,
                action,
                vmBlockEntity.getVmName(),
                vmBlockEntity.getStartCommand(),
                vmBlockEntity.getStopCommand()
        );

        return InteractionResult.CONSUME;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new Windows7VmBlockEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }
}
