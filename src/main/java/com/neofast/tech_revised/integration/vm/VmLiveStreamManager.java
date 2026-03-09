package com.neofast.tech_revised.integration.vm;

import com.neofast.tech_revised.networking.ModNetworking;
import com.neofast.tech_revised.networking.packet.VmScreenshotPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class VmLiveStreamManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int STREAM_INTERVAL_MS =
            Math.max(5, Integer.getInteger("tech_revised.vm.streamIntervalMs", 16));

    private static final ThreadFactory STREAM_THREAD_FACTORY = runnable -> {
        Thread thread = new Thread(runnable, "tech-revised-vm-stream");
        thread.setDaemon(true);
        return thread;
    };
    private static final ScheduledExecutorService STREAM_EXECUTOR =
            Executors.newScheduledThreadPool(1, STREAM_THREAD_FACTORY);
    private static final ConcurrentMap<StreamKey, StreamSession> SESSIONS = new ConcurrentHashMap<>();

    private VmLiveStreamManager() {
    }

    public static void startWatching(ServerPlayer player, BlockPos blockPos, String vmName,
                                     String startCommand, String stopCommand) {
        if (player.server == null) {
            return;
        }

        StreamKey key = new StreamKey(player.level().dimension().location(), blockPos.immutable());
        UUID viewerId = player.getUUID();

        StreamSession session = SESSIONS.compute(key, (k, existing) -> {
            if (existing != null) {
                existing.setVmConfig(vmName, startCommand, stopCommand);
                existing.addViewer(viewerId);
                return existing;
            }

            StreamSession created = new StreamSession(player.server, k, vmName, startCommand, stopCommand);
            created.addViewer(viewerId);
            created.start();
            return created;
        });

        if (session != null) {
            LOGGER.debug("Started VM live stream watch: viewer={}, key={}", viewerId, key);
        }
    }

    public static void stopWatching(ServerPlayer player, BlockPos blockPos) {
        StreamKey key = new StreamKey(player.level().dimension().location(), blockPos.immutable());
        removeViewer(key, player.getUUID());
    }

    private static void removeViewer(StreamKey key, UUID viewerId) {
        SESSIONS.computeIfPresent(key, (k, session) -> {
            session.removeViewer(viewerId);
            if (session.hasViewers()) {
                return session;
            }
            session.stop();
            LOGGER.debug("Stopped VM live stream session with no viewers: key={}", key);
            return null;
        });
    }

    private static final class StreamSession {
        private final MinecraftServer server;
        private final StreamKey key;
        private final Set<UUID> viewers = ConcurrentHashMap.newKeySet();
        private final AtomicBoolean captureInProgress = new AtomicBoolean(false);

        private volatile String vmName;
        private volatile String startCommand;
        private volatile String stopCommand;
        private volatile ScheduledFuture<?> task;

        private StreamSession(MinecraftServer server, StreamKey key, String vmName,
                              String startCommand, String stopCommand) {
            this.server = server;
            this.key = key;
            this.vmName = vmName;
            this.startCommand = startCommand;
            this.stopCommand = stopCommand;
        }

        private void addViewer(UUID viewerId) {
            viewers.add(viewerId);
        }

        private void removeViewer(UUID viewerId) {
            viewers.remove(viewerId);
        }

        private boolean hasViewers() {
            return !viewers.isEmpty();
        }

        private void setVmConfig(String vmName, String startCommand, String stopCommand) {
            this.vmName = vmName;
            this.startCommand = startCommand;
            this.stopCommand = stopCommand;
        }

        private void start() {
            task = STREAM_EXECUTOR.scheduleAtFixedRate(this::captureAndBroadcast, 0, STREAM_INTERVAL_MS, TimeUnit.MILLISECONDS);
        }

        private void stop() {
            ScheduledFuture<?> localTask = task;
            if (localTask != null) {
                localTask.cancel(false);
            }
        }

        private void captureAndBroadcast() {
            if (!captureInProgress.compareAndSet(false, true)) {
                return;
            }

            try {
                VmCommandIntegration.VmScreenshotResult result =
                        VmCommandIntegration.captureScreenshotNow(vmName, startCommand, stopCommand);
                server.execute(() -> broadcast(result));
            } catch (Exception exception) {
                LOGGER.debug("Failed VM live stream frame capture for {}: {}", key, exception.getMessage());
                server.execute(() -> broadcast(new VmCommandIntegration.VmScreenshotResult(
                        false,
                        "Live stream error: " + exception.getMessage(),
                        new byte[0]
                )));
            } finally {
                captureInProgress.set(false);
            }
        }

        private void broadcast(VmCommandIntegration.VmScreenshotResult result) {
            viewers.removeIf(viewerId -> {
                ServerPlayer viewer = server.getPlayerList().getPlayer(viewerId);
                if (viewer == null) {
                    return true;
                }

                if (!viewer.level().dimension().location().equals(key.dimension())) {
                    return true;
                }

                if (viewer.distanceToSqr(
                        key.blockPos().getX() + 0.5D,
                        key.blockPos().getY() + 0.5D,
                        key.blockPos().getZ() + 0.5D) > 256.0D) {
                    return true;
                }

                ModNetworking.CHANNEL.send(
                        net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> viewer),
                        new VmScreenshotPacket(key.blockPos(), result.success(), result.message(), result.imageBytes()));
                return false;
            });

            if (!hasViewers()) {
                stop();
                SESSIONS.remove(key, this);
                LOGGER.debug("Removed VM live stream session after viewer cleanup: key={}", key);
            }
        }
    }

    private record StreamKey(ResourceLocation dimension, BlockPos blockPos) {
    }
}
