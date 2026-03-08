package com.neofast.tech_revised.networking.packet;

import com.neofast.tech_revised.block.entity.custom.Windows7VmBlockEntity;
import com.neofast.tech_revised.integration.vm.VmCommandIntegration;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class VmKeyboardInputPacket {
    private static final int MAX_TEXT_LENGTH = 256;
    private static final int MAX_KEY_NAME_LENGTH = 32;

    private final BlockPos blockPos;
    private final String text;
    private final String specialKeyName;

    public VmKeyboardInputPacket(BlockPos blockPos, String text, String specialKeyName) {
        this.blockPos = blockPos;
        this.text = text;
        this.specialKeyName = specialKeyName;
    }

    public static VmKeyboardInputPacket forText(BlockPos blockPos, String text) {
        return new VmKeyboardInputPacket(blockPos, text, "");
    }

    public static VmKeyboardInputPacket forSpecialKey(BlockPos blockPos, VmCommandIntegration.VmSpecialKey specialKey) {
        return new VmKeyboardInputPacket(blockPos, "", specialKey.name());
    }

    public static void encode(VmKeyboardInputPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.blockPos);
        buffer.writeUtf(packet.text, MAX_TEXT_LENGTH);
        buffer.writeUtf(packet.specialKeyName, MAX_KEY_NAME_LENGTH);
    }

    public static VmKeyboardInputPacket decode(FriendlyByteBuf buffer) {
        BlockPos blockPos = buffer.readBlockPos();
        String text = buffer.readUtf(MAX_TEXT_LENGTH);
        String specialKeyName = buffer.readUtf(MAX_KEY_NAME_LENGTH);
        return new VmKeyboardInputPacket(blockPos, text, specialKeyName);
    }

    public static void handle(VmKeyboardInputPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
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

            BlockEntity blockEntity = player.level().getBlockEntity(packet.blockPos);
            if (!(blockEntity instanceof Windows7VmBlockEntity vmBlockEntity)) {
                return;
            }

            if (!packet.text.isBlank()) {
                VmCommandIntegration.sendKeyboardTextAsync(
                        vmBlockEntity.getVmName(),
                        vmBlockEntity.getStartCommand(),
                        vmBlockEntity.getStopCommand(),
                        packet.text
                );
                return;
            }

            if (packet.specialKeyName.isBlank()) {
                return;
            }

            try {
                VmCommandIntegration.VmSpecialKey specialKey =
                        VmCommandIntegration.VmSpecialKey.valueOf(packet.specialKeyName);
                VmCommandIntegration.sendKeyboardSpecialKeyAsync(
                        vmBlockEntity.getVmName(),
                        vmBlockEntity.getStartCommand(),
                        vmBlockEntity.getStopCommand(),
                        specialKey
                );
            } catch (IllegalArgumentException ignored) {
                // Ignore invalid packet key values.
            }
        });
        context.setPacketHandled(true);
    }
}
