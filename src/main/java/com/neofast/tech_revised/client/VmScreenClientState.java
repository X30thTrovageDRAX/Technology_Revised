package com.neofast.tech_revised.client;

import net.minecraft.core.BlockPos;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public final class VmScreenClientState {
    private static final Map<BlockPos, VmFrameData> FRAMES = new ConcurrentHashMap<>();
    private static final AtomicLong NEXT_UPDATE_ID = new AtomicLong(1L);

    private VmScreenClientState() {
    }

    public static void update(BlockPos blockPos, boolean success, String message, byte[] imageBytes) {
        byte[] copy = imageBytes == null ? new byte[0] : imageBytes.clone();
        FRAMES.put(blockPos.immutable(), new VmFrameData(success, message, copy, NEXT_UPDATE_ID.getAndIncrement()));
    }

    public static VmFrameData get(BlockPos blockPos) {
        return FRAMES.get(blockPos);
    }

    public record VmFrameData(boolean success, String message, byte[] imageBytes, long updateId) {
    }
}
