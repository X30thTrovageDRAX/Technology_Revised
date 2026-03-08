package com.neofast.tech_revised.block.custom;

import com.neofast.tech_revised.block.entity.custom.ElectricArcFurnaceFluidOutputBusBlockEntity;
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
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public class ElectricArcFurnaceFluidOutputBusBlock extends BaseEntityBlock {
    public ElectricArcFurnaceFluidOutputBusBlock(Properties properties) {
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

        if (FluidUtil.interactWithFluidHandler(player, hand, level, pos, hit.getDirection())) {
            return InteractionResult.CONSUME;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ElectricArcFurnaceFluidOutputBusBlockEntity fluidOutputBus) {
            FluidStack storedFluid = fluidOutputBus.getStoredFluid();
            String fluidName = storedFluid.isEmpty() ? "Empty" : storedFluid.getDisplayName().getString();
            player.displayClientMessage(Component.literal("Stored Fluid: " + fluidName + " (" +
                    fluidOutputBus.getFluidAmount() + " / " + fluidOutputBus.getCapacity() + " mB)"), true);
        }
        return InteractionResult.CONSUME;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ElectricArcFurnaceFluidOutputBusBlockEntity(pos, state);
    }
}
