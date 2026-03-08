package com.neofast.tech_revised.block.custom;

import com.neofast.tech_revised.block.entity.custom.ElectricArcFurnaceEnergyInputHatchBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class ElectricArcFurnaceEnergyInputHatchBlock extends BaseEntityBlock {
    public ElectricArcFurnaceEnergyInputHatchBlock(Properties properties) {
        super(properties);
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

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ElectricArcFurnaceEnergyInputHatchBlockEntity energyHatch) {
            player.displayClientMessage(Component.literal("Energy: " + energyHatch.getEnergyStored() + " / " +
                    energyHatch.getMaxEnergyStored() + " FE"), true);
        }
        return InteractionResult.CONSUME;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ElectricArcFurnaceEnergyInputHatchBlockEntity(pos, state);
    }
}
