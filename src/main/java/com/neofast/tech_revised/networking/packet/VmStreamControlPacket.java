package com.neofast.tech_revised.networking.packet;

import com.neofast.tech_revised.block.entity.custom.Windows7VmBlockEntity;
import com.neofast.tech_revised.integration.vm.VmLiveStreamManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class VmStreamControlPacket {
    private final BlockPos blockPos;
    private final boolean startStreaming;

    public VmStreamControlPacket(BlockPos blockPos, boolean startStreaming) {
        this.blockPos = blockPos;
        this.startStreaming = startStreaming;
    }

    public static void encode(VmStreamControlPacket packet, net.minecraft.network.FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.blockPos);
        buffer.writeBoolean(packet.startStreaming);
    }

    public static VmStreamControlPacket decode(net.minecraft.network.FriendlyByteBuf buffer) {
        return new VmStreamControlPacket(buffer.readBlockPos(), buffer.readBoolean());
    }

    public static void handle(VmStreamControlPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }

            if (!packet.startStreaming) {
                VmLiveStreamManager.stopWatching(player, packet.blockPos);
                return;
            }

            if (!player.hasPermissions(2)) {
                return;
            }

            if (player.distanceToSqr(
                    packet.blockPos.getX() + 0.5D,
                    packet.blockPos.getY() + 0.5D,
                    packet.blockPos.getZ() + 0.5D) > 256.0D) {
                return;
            }

            BlockEntity blockEntity = player.level().getBlockEntity(packet.blockPos);
            if (!(blockEntity instanceof Windows7VmBlockEntity vmBlockEntity)) {
                return;
            }

            VmLiveStreamManager.startWatching(
                    player,
                    packet.blockPos,
                    vmBlockEntity.getVmName(),
                    vmBlockEntity.getStartCommand(),
                    vmBlockEntity.getStopCommand()
            );
        });
        context.setPacketHandled(true);
    }
}
