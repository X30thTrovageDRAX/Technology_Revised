package com.neofast.tech_revised.networking.packet;

import com.neofast.tech_revised.client.VmScreenClientState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class VmScreenshotPacket {
    private static final int MAX_MESSAGE_LENGTH = 512;

    private final BlockPos blockPos;
    private final boolean success;
    private final String message;
    private final byte[] imageBytes;

    public VmScreenshotPacket(BlockPos blockPos, boolean success, String message, byte[] imageBytes) {
        this.blockPos = blockPos;
        this.success = success;
        this.message = message;
        this.imageBytes = imageBytes;
    }

    public static void encode(VmScreenshotPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.blockPos);
        buffer.writeBoolean(packet.success);
        buffer.writeUtf(packet.message, MAX_MESSAGE_LENGTH);
        buffer.writeByteArray(packet.imageBytes);
    }

    public static VmScreenshotPacket decode(FriendlyByteBuf buffer) {
        BlockPos blockPos = buffer.readBlockPos();
        boolean success = buffer.readBoolean();
        String message = buffer.readUtf(MAX_MESSAGE_LENGTH);
        byte[] imageBytes = buffer.readByteArray();
        return new VmScreenshotPacket(blockPos, success, message, imageBytes);
    }

    public static void handle(VmScreenshotPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        VmScreenClientState.update(packet.blockPos, packet.success, packet.message, packet.imageBytes)));
        context.setPacketHandled(true);
    }
}
