package com.neofast.tech_revised.integration.vm;

import com.neofast.tech_revised.block.custom.Windows7VmBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public final class VmCommandIntegration {
    public enum VmAction {
        START,
        STOP
    }

    private static final Logger LOGGER = LogManager.getLogger();
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "tech-revised-vm-integration");
        thread.setDaemon(true);
        return thread;
    });

    private static final String VM_NAME_PROPERTY = "tech_revised.vm.name";
    private static final String START_COMMAND_PROPERTY = "tech_revised.vm.startCommand";
    private static final String STOP_COMMAND_PROPERTY = "tech_revised.vm.stopCommand";
    private static final String VBOX_MANAGE_PATH_PROPERTY = "tech_revised.vm.vboxManagePath";
    private static final String DEFAULT_VM_NAME = "Windows 7";
    private static final String VBOXMANAGE_PLACEHOLDER = "{vboxmanage}";
    private static final String DEFAULT_START_TEMPLATE = "\"" + VBOXMANAGE_PLACEHOLDER + "\" startvm \"{vmName}\" --type headless";
    private static final String DEFAULT_STOP_TEMPLATE = "\"" + VBOXMANAGE_PLACEHOLDER + "\" controlvm \"{vmName}\" acpipowerbutton";
    private static final String VBOX_PATH_PROGRAM_FILES = "C:\\Program Files\\Oracle\\VirtualBox\\VBoxManage.exe";
    private static final String VBOX_PATH_PROGRAM_FILES_X86 = "C:\\Program Files (x86)\\Oracle\\VirtualBox\\VBoxManage.exe";

    private VmCommandIntegration() {
    }

    public static String getConfiguredVmName() {
        String name = System.getProperty(VM_NAME_PROPERTY, DEFAULT_VM_NAME).trim();
        return name.isEmpty() ? DEFAULT_VM_NAME : name;
    }

    public static String getConfiguredStartTemplate() {
        String command = System.getProperty(START_COMMAND_PROPERTY, DEFAULT_START_TEMPLATE).trim();
        return command.isEmpty() ? DEFAULT_START_TEMPLATE : command;
    }

    public static String getConfiguredStopTemplate() {
        String command = System.getProperty(STOP_COMMAND_PROPERTY, DEFAULT_STOP_TEMPLATE).trim();
        return command.isEmpty() ? DEFAULT_STOP_TEMPLATE : command;
    }

    public static String resolveVmName(String localVmName) {
        String trimmed = localVmName == null ? "" : localVmName.trim();
        if (!trimmed.isEmpty()) {
            return trimmed;
        }
        return getConfiguredVmName();
    }

    public static Component getActionStartedMessage(VmAction action, String vmName) {
        return Component.translatable(
                action == VmAction.START
                        ? "message.tech_revised.vm.starting"
                        : "message.tech_revised.vm.stopping",
                vmName
        );
    }

    public static void captureScreenshotAsync(String localVmName, String localStartTemplate, String localStopTemplate,
                                              Consumer<VmScreenshotResult> callback) {
        String vmName = resolveVmName(localVmName);

        EXECUTOR.submit(() -> {
            try {
                VmScreenshotResult result = captureScreenshot(vmName);
                callback.accept(result);
            } catch (Exception exception) {
                LOGGER.error("VM screenshot capture failed.", exception);
                callback.accept(new VmScreenshotResult(false, "Screenshot error: " + exception.getMessage(), new byte[0]));
            }
        });
    }

    public static void executeAction(ServerLevel level, BlockPos pos, ServerPlayer player, VmAction action) {
        executeAction(level, pos, player, action, "", "", "");
    }

    public static void executeAction(ServerLevel level, BlockPos pos, ServerPlayer player, VmAction action,
                                     String localVmName, String localStartTemplate, String localStopTemplate) {
        String vmName = resolveVmName(localVmName);
        String command = resolveCommand(action, vmName, localStartTemplate, localStopTemplate);

        EXECUTOR.submit(() -> {
            try {
                ActionExecution actionExecution = action == VmAction.START
                        ? executeStartWithLockHandling(vmName, command)
                        : executeStop(command);

                final boolean finalSuccess = actionExecution.success();
                final boolean finalAlreadyRunning = actionExecution.alreadyRunning();
                final CommandResult finalCommandResult = actionExecution.commandResult();

                level.getServer().execute(() -> {
                    if (finalSuccess) {
                        setBlockActiveState(level, pos, action == VmAction.START);
                        player.displayClientMessage(
                                Component.translatable(
                                        finalAlreadyRunning
                                                ? "message.tech_revised.vm.start.already_running"
                                                : action == VmAction.START
                                                ? "message.tech_revised.vm.start.success"
                                                : "message.tech_revised.vm.stop.success",
                                        vmName
                                ),
                                false
                        );
                    } else {
                        player.displayClientMessage(
                                Component.translatable("message.tech_revised.vm.command.failed",
                                        finalCommandResult.exitCode(), finalCommandResult.outputPreview()),
                                false
                        );
                        String outputPreview = finalCommandResult.outputPreview();

                        if (isVBoxManageNotFound(outputPreview)) {
                            player.displayClientMessage(
                                    Component.translatable("message.tech_revised.vm.vbox_missing", suggestVBoxManagePath()),
                                    false
                            );
                        }
                        if (action == VmAction.START && isVmLockedBySession(outputPreview.toLowerCase(Locale.ROOT))) {
                            player.displayClientMessage(
                                    Component.translatable("message.tech_revised.vm.start.locked", vmName),
                                    false
                            );
                        }
                    }
                });
            } catch (Exception exception) {
                LOGGER.error("VM integration command failed.", exception);
                level.getServer().execute(() ->
                        player.displayClientMessage(
                                Component.translatable("message.tech_revised.vm.command.error", exception.getMessage()),
                                false
                        )
                );
            }
        });
    }

    private static String resolveCommand(VmAction action, String vmName, String localStartTemplate, String localStopTemplate) {
        String localTemplate = action == VmAction.START ? localStartTemplate : localStopTemplate;
        String template = resolveCommandTemplate(action, localTemplate);
        String vboxExecutable = resolveVBoxManageExecutable();
        String normalized = normalizeLegacyVBoxManageCommand(template, vboxExecutable);
        String quotedVBox = "\"" + vboxExecutable + "\"";
        String quotedVmName = "\"" + vmName + "\"";

        String withVBox = normalized
                .replace("\"" + VBOXMANAGE_PLACEHOLDER + "\"", quotedVBox)
                .replace(VBOXMANAGE_PLACEHOLDER, quotedVBox);
        String withVmName = withVBox
                .replace("\"{vmName}\"", quotedVmName)
                .replace("{vmName}", quotedVmName);

        return quoteKnownVBoxPathIfNeeded(withVmName, vboxExecutable);
    }

    private static String resolveCommandTemplate(VmAction action, String localTemplate) {
        String trimmedLocal = localTemplate == null ? "" : localTemplate.trim();
        if (!trimmedLocal.isEmpty()) {
            return trimmedLocal;
        }
        return action == VmAction.START ? getConfiguredStartTemplate() : getConfiguredStopTemplate();
    }

    private static VmScreenshotResult captureScreenshot(String vmName) throws Exception {
        String vmState = queryVmState(vmName);
        if (!isActiveVmState(vmState)) {
            return new VmScreenshotResult(false,
                    "VM is not running (state: " + (vmState.isEmpty() ? "unknown" : vmState) + ").",
                    new byte[0]);
        }

        String vboxExecutable = resolveVBoxManageExecutable();
        Path screenshotPath = Files.createTempFile("tech_revised_vm_", ".png");

        try {
            String command = "\"" + vboxExecutable + "\" controlvm \"" + vmName + "\" screenshotpng \"" + screenshotPath.toAbsolutePath() + "\"";
            CommandResult result = runCommand(command, 12);
            if (result.exitCode() != 0) {
                return new VmScreenshotResult(false, result.outputPreview(), new byte[0]);
            }

            if (!Files.exists(screenshotPath)) {
                return new VmScreenshotResult(false, "Screenshot file was not created.", new byte[0]);
            }

            byte[] imageBytes = Files.readAllBytes(screenshotPath);
            if (imageBytes.length == 0) {
                return new VmScreenshotResult(false, "Screenshot is empty.", new byte[0]);
            }
            if (imageBytes.length > 2_000_000) {
                return new VmScreenshotResult(false, "Screenshot too large (" + imageBytes.length + " bytes).", new byte[0]);
            }

            return new VmScreenshotResult(true, "OK", imageBytes);
        } finally {
            try {
                Files.deleteIfExists(screenshotPath);
            } catch (Exception ignored) {
            }
        }
    }

    private static ActionExecution executeStop(String stopCommand) throws Exception {
        CommandResult result = runCommand(stopCommand, 8);
        return new ActionExecution(result.exitCode() == 0, false, result);
    }

    private static ActionExecution executeStartWithLockHandling(String vmName, String startCommand) throws Exception {
        String initialState = queryVmState(vmName);
        if (isActiveVmState(initialState)) {
            return new ActionExecution(true, true, new CommandResult(0, "VMState=" + initialState));
        }

        CommandResult attempt = runCommand(startCommand, 8);
        if (attempt.exitCode() == 0) {
            return new ActionExecution(true, false, attempt);
        }

        if (!isVmLockedBySession(attempt.outputPreview().toLowerCase(Locale.ROOT))) {
            return new ActionExecution(false, false, attempt);
        }

        // VM may be transitioning lock state; poll state and retry a few times.
        for (int i = 0; i < 5; i++) {
            Thread.sleep(1500L);

            String state = queryVmState(vmName);
            if (isActiveVmState(state)) {
                return new ActionExecution(true, true, new CommandResult(0, "VMState=" + state));
            }

            CommandResult retry = runCommand(startCommand, 8);
            if (retry.exitCode() == 0) {
                return new ActionExecution(true, false, retry);
            }

            attempt = retry;
            if (!isVmLockedBySession(retry.outputPreview().toLowerCase(Locale.ROOT))) {
                return new ActionExecution(false, false, retry);
            }
        }

        String finalState = queryVmState(vmName);
        if (isActiveVmState(finalState)) {
            return new ActionExecution(true, true, new CommandResult(0, "VMState=" + finalState));
        }

        return new ActionExecution(false, false, attempt);
    }

    private static CommandResult runCommand(String command, int maxOutputLines) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", command);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        List<String> outputLines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null && outputLines.size() < maxOutputLines) {
                if (!line.isBlank()) {
                    outputLines.add(line.trim());
                }
            }
        }

        int exitCode = process.waitFor();
        String outputPreview = outputLines.isEmpty() ? "-" : String.join(" | ", outputLines);
        return new CommandResult(exitCode, outputPreview);
    }

    private static String normalizeLegacyVBoxManageCommand(String template, String vboxExecutable) {
        if (template.contains(VBOXMANAGE_PLACEHOLDER)) {
            return template;
        }

        String normalized = template.replace("VBoxManage.exe", "VBoxManage");
        if (normalized.contains("VBoxManage")) {
            return template.replace("VBoxManage.exe", "\"" + vboxExecutable + "\"")
                    .replace("VBoxManage", "\"" + vboxExecutable + "\"");
        }

        return template;
    }

    private static String quoteKnownVBoxPathIfNeeded(String command, String vboxExecutable) {
        String prefix = vboxExecutable + " ";
        if (command.startsWith(prefix)) {
            return "\"" + vboxExecutable + "\"" + command.substring(vboxExecutable.length());
        }
        if (command.equals(vboxExecutable)) {
            return "\"" + vboxExecutable + "\"";
        }
        return command;
    }

    private static String resolveVBoxManageExecutable() {
        String configured = System.getProperty(VBOX_MANAGE_PATH_PROPERTY, "").trim();
        if (!configured.isEmpty()) {
            return configured;
        }

        if (Files.isRegularFile(Path.of(VBOX_PATH_PROGRAM_FILES))) {
            return VBOX_PATH_PROGRAM_FILES;
        }
        if (Files.isRegularFile(Path.of(VBOX_PATH_PROGRAM_FILES_X86))) {
            return VBOX_PATH_PROGRAM_FILES_X86;
        }

        return "VBoxManage";
    }

    private static String suggestVBoxManagePath() {
        if (Files.isRegularFile(Path.of(VBOX_PATH_PROGRAM_FILES))) {
            return VBOX_PATH_PROGRAM_FILES;
        }
        if (Files.isRegularFile(Path.of(VBOX_PATH_PROGRAM_FILES_X86))) {
            return VBOX_PATH_PROGRAM_FILES_X86;
        }
        return VBOX_PATH_PROGRAM_FILES;
    }

    private static boolean isVBoxManageNotFound(String outputPreview) {
        String lower = outputPreview.toLowerCase(Locale.ROOT);
        return lower.contains("vboxmanage")
                && (lower.contains("not recognized")
                || lower.contains("no such file")
                || lower.contains("cannot find"));
    }

    private static boolean isVmLockedBySession(String lowerOutputPreview) {
        return lowerOutputPreview.contains("already locked by a session")
                || lowerOutputPreview.contains("vbox_e_invalid_object_state");
    }

    private static String queryVmState(String vmName) {
        try {
            String command = "\"" + resolveVBoxManageExecutable() + "\" showvminfo \"" + vmName + "\" --machinereadable";
            CommandResult result = runCommand(command, 300);
            if (result.exitCode() != 0) {
                return "";
            }

            String[] lines = result.outputPreview().split("\\|");
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.startsWith("VMState=")) {
                    String state = trimmed.substring("VMState=".length()).replace("\"", "").trim();
                    return state.toLowerCase(Locale.ROOT);
                }
            }
        } catch (Exception ignored) {
            return "";
        }
        return "";
    }

    private static boolean isActiveVmState(String vmState) {
        return "running".equals(vmState)
                || "paused".equals(vmState)
                || "stuck".equals(vmState)
                || "teleporting".equals(vmState);
    }

    private static void setBlockActiveState(ServerLevel level, BlockPos pos, boolean active) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof Windows7VmBlock)) {
            return;
        }
        if (state.getValue(Windows7VmBlock.ACTIVE) == active) {
            return;
        }
        level.setBlock(pos, state.setValue(Windows7VmBlock.ACTIVE, active), Block.UPDATE_CLIENTS);
    }

    private record CommandResult(int exitCode, String outputPreview) {
    }

    private record ActionExecution(boolean success, boolean alreadyRunning, CommandResult commandResult) {
    }

    public record VmScreenshotResult(boolean success, String message, byte[] imageBytes) {
    }
}
