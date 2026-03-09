package com.neofast.tech_revised.networking;

import com.neofast.tech_revised.TechRevised;
import com.neofast.tech_revised.networking.packet.RequestVmScreenshotPacket;
import com.neofast.tech_revised.networking.packet.SaveVmConfigPacket;
import com.neofast.tech_revised.networking.packet.VmKeyboardInputPacket;
import com.neofast.tech_revised.networking.packet.VmMouseInputPacket;
import com.neofast.tech_revised.networking.packet.VmScreenshotPacket;
import com.neofast.tech_revised.networking.packet.VmStreamControlPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetworking {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(TechRevised.MOD_ID, "messages"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private ModNetworking() {
    }

    public static void register() {
        CHANNEL.registerMessage(
                packetId++,
                SaveVmConfigPacket.class,
                SaveVmConfigPacket::encode,
                SaveVmConfigPacket::decode,
                SaveVmConfigPacket::handle
        );
        CHANNEL.registerMessage(
                packetId++,
                RequestVmScreenshotPacket.class,
                RequestVmScreenshotPacket::encode,
                RequestVmScreenshotPacket::decode,
                RequestVmScreenshotPacket::handle
        );
        CHANNEL.registerMessage(
                packetId++,
                VmScreenshotPacket.class,
                VmScreenshotPacket::encode,
                VmScreenshotPacket::decode,
                VmScreenshotPacket::handle
        );
        CHANNEL.registerMessage(
                packetId++,
                VmKeyboardInputPacket.class,
                VmKeyboardInputPacket::encode,
                VmKeyboardInputPacket::decode,
                VmKeyboardInputPacket::handle
        );
        CHANNEL.registerMessage(
                packetId++,
                VmStreamControlPacket.class,
                VmStreamControlPacket::encode,
                VmStreamControlPacket::decode,
                VmStreamControlPacket::handle
        );
        CHANNEL.registerMessage(
                packetId++,
                VmMouseInputPacket.class,
                VmMouseInputPacket::encode,
                VmMouseInputPacket::decode,
                VmMouseInputPacket::handle
        );
    }
}
