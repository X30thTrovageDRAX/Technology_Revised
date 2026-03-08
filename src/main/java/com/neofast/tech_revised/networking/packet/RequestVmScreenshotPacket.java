package com.neofast.tech_revised.networking.packet;

import com.neofast.tech_revised.block.entity.custom.Windows7VmBlockEntity;
import com.neofast.tech_revised.integration.vm.VmCommandIntegration;
import com.neofast.tech_revised.networking.ModNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class RequestVmScreenshotPacket {
    private final BlockPos blockPos;

    public RequestVmScreenshotPacket(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    public static void encode(RequestVmScreenshotPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.blockPos);
    }

    public static RequestVmScreenshotPacket decode(FriendlyByteBuf buffer) {
        return new RequestVmScreenshotPacket(buffer.readBlockPos());
    }

    public static void handle(RequestVmScreenshotPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }

            if (!player.hasPermissions(2)) {
                sendFailure(player, packet.blockPos, Component.translatable("message.tech_revised.vm.permission_denied").getString());
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

            String vmName = vmBlockEntity.getVmName();
            String startCommand = vmBlockEntity.getStartCommand();
            String stopCommand = vmBlockEntity.getStopCommand();

            VmCommandIntegration.captureScreenshotAsync(vmName, startCommand, stopCommand, result -> {
                if (player.server == null) {
                    return;
                }
                player.server.execute(() ->
                        ModNetworking.CHANNEL.send(
                                PacketDistributor.PLAYER.with(() -> player),
                                new VmScreenshotPacket(packet.blockPos, result.success(), result.message(), result.imageBytes()))
                );
            });
        });
        context.setPacketHandled(true);
    }

    private static void sendFailure(ServerPlayer player, BlockPos blockPos, String message) {
        ModNetworking.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new VmScreenshotPacket(blockPos, false, message, new byte[0]));
    }
}
