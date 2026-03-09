package com.neofast.tech_revised.networking.packet;

import com.neofast.tech_revised.block.entity.custom.Windows7VmBlockEntity;
import com.neofast.tech_revised.integration.vm.VmCommandIntegration;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class VmMouseInputPacket {
    private static final int MIN_COORD = 0;
    private static final int MAX_COORD = 65535;

    private final BlockPos blockPos;
    private final int absoluteX;
    private final int absoluteY;
    private final int buttonMask;

    public VmMouseInputPacket(BlockPos blockPos, int absoluteX, int absoluteY, int buttonMask) {
        this.blockPos = blockPos;
        this.absoluteX = absoluteX;
        this.absoluteY = absoluteY;
        this.buttonMask = buttonMask;
    }

    public static void encode(VmMouseInputPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.blockPos);
        buffer.writeVarInt(packet.absoluteX);
        buffer.writeVarInt(packet.absoluteY);
        buffer.writeVarInt(packet.buttonMask);
    }

    public static VmMouseInputPacket decode(FriendlyByteBuf buffer) {
        BlockPos blockPos = buffer.readBlockPos();
        int absoluteX = buffer.readVarInt();
        int absoluteY = buffer.readVarInt();
        int buttonMask = buffer.readVarInt();
        return new VmMouseInputPacket(blockPos, absoluteX, absoluteY, buttonMask);
    }

    public static void handle(VmMouseInputPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
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

            if (packet.absoluteX < MIN_COORD || packet.absoluteX > MAX_COORD
                    || packet.absoluteY < MIN_COORD || packet.absoluteY > MAX_COORD) {
                return;
            }

            if (packet.buttonMask != 1 && packet.buttonMask != 2 && packet.buttonMask != 4) {
                return;
            }

            BlockEntity blockEntity = player.level().getBlockEntity(packet.blockPos);
            if (!(blockEntity instanceof Windows7VmBlockEntity vmBlockEntity)) {
                return;
            }

            VmCommandIntegration.sendMouseClickAsync(
                    vmBlockEntity.getVmName(),
                    vmBlockEntity.getStartCommand(),
                    vmBlockEntity.getStopCommand(),
                    packet.absoluteX,
                    packet.absoluteY,
                    packet.buttonMask
            );
        });
        context.setPacketHandled(true);
    }
}
