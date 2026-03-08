package com.neofast.tech_revised.networking.packet;

import com.neofast.tech_revised.block.entity.custom.Windows7VmBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SaveVmConfigPacket {
    private final BlockPos blockPos;
    private final String vmName;
    private final String startCommand;
    private final String stopCommand;

    public SaveVmConfigPacket(BlockPos blockPos, String vmName, String startCommand, String stopCommand) {
        this.blockPos = blockPos;
        this.vmName = vmName;
        this.startCommand = startCommand;
        this.stopCommand = stopCommand;
    }

    public static void encode(SaveVmConfigPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.blockPos);
        buffer.writeUtf(packet.vmName, Windows7VmBlockEntity.MAX_VM_NAME_LENGTH);
        buffer.writeUtf(packet.startCommand, Windows7VmBlockEntity.MAX_COMMAND_LENGTH);
        buffer.writeUtf(packet.stopCommand, Windows7VmBlockEntity.MAX_COMMAND_LENGTH);
    }

    public static SaveVmConfigPacket decode(FriendlyByteBuf buffer) {
        BlockPos blockPos = buffer.readBlockPos();
        String vmName = buffer.readUtf(Windows7VmBlockEntity.MAX_VM_NAME_LENGTH);
        String startCommand = buffer.readUtf(Windows7VmBlockEntity.MAX_COMMAND_LENGTH);
        String stopCommand = buffer.readUtf(Windows7VmBlockEntity.MAX_COMMAND_LENGTH);
        return new SaveVmConfigPacket(blockPos, vmName, startCommand, stopCommand);
    }

    public static void handle(SaveVmConfigPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }

            if (!player.hasPermissions(2)) {
                player.displayClientMessage(Component.translatable("message.tech_revised.vm.permission_denied"), true);
                return;
            }

            if (player.distanceToSqr(
                    packet.blockPos.getX() + 0.5D,
                    packet.blockPos.getY() + 0.5D,
                    packet.blockPos.getZ() + 0.5D) > 64.0D) {
                return;
            }

            BlockEntity blockEntity = player.level().getBlockEntity(packet.blockPos);
            if (!(blockEntity instanceof Windows7VmBlockEntity vmBlockEntity)) {
                return;
            }

            vmBlockEntity.applyConfig(packet.vmName, packet.startCommand, packet.stopCommand);
            player.displayClientMessage(Component.translatable("message.tech_revised.vm.config.saved"), true);
        });
        context.setPacketHandled(true);
    }
}
