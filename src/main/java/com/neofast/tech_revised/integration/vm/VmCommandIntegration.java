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
import java.util.regex.Matcher;

public final class VmCommandIntegration {
    public enum VmAction {
        START,
        STOP
    }

    public enum VmSpecialKey {
        ENTER(List.of("1c", "9c")),
        BACKSPACE(List.of("0e", "8e")),
        TAB(List.of("0f", "8f")),
        ESCAPE(List.of("01", "81")),
        LEFT(List.of("e0", "4b", "e0", "cb")),
        RIGHT(List.of("e0", "4d", "e0", "cd")),
        UP(List.of("e0", "48", "e0", "c8")),
        DOWN(List.of("e0", "50", "e0", "d0")),
        DELETE(List.of("e0", "53", "e0", "d3")),
        HOME(List.of("e0", "47", "e0", "c7")),
        END(List.of("e0", "4f", "e0", "cf")),
        PAGE_UP(List.of("e0", "49", "e0", "c9")),
        PAGE_DOWN(List.of("e0", "51", "e0", "d1")),
        F1(List.of("3b", "bb")),
        F2(List.of("3c", "bc")),
        F3(List.of("3d", "bd")),
        F4(List.of("3e", "be")),
        F5(List.of("3f", "bf")),
        F6(List.of("40", "c0")),
        F7(List.of("41", "c1")),
        F8(List.of("42", "c2")),
        F9(List.of("43", "c3")),
        F10(List.of("44", "c4")),
        F11(List.of("57", "d7")),
        F12(List.of("58", "d8"));

        private final List<String> scanCodes;

        VmSpecialKey(List<String> scanCodes) {
            this.scanCodes = scanCodes;
        }

        public List<String> getScanCodes() {
            return scanCodes;
        }
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
    private static final String VM_MOUSE_MODE_PROPERTY = "tech_revised.vm.mouseMode";
    private static final String DEFAULT_VM_NAME = "Windows 7";
    private static final String DEFAULT_VM_MOUSE_MODE = "usbtablet";
    private static final String VBOXMANAGE_PLACEHOLDER = "{vboxmanage}";
    private static final String DEFAULT_START_TEMPLATE = "\"" + VBOXMANAGE_PLACEHOLDER + "\" startvm \"{vmName}\" --type headless";
    private static final String DEFAULT_STOP_TEMPLATE = "\"" + VBOXMANAGE_PLACEHOLDER + "\" controlvm \"{vmName}\" acpipowerbutton";
    private static final String VBOX_PATH_PROGRAM_FILES = "C:\\Program Files\\Oracle\\VirtualBox\\VBoxManage.exe";
    private static final String VBOX_PATH_PROGRAM_FILES_X86 = "C:\\Program Files (x86)\\Oracle\\VirtualBox\\VBoxManage.exe";
    private static final int MAX_SCREENSHOT_BYTES = Integer.getInteger("tech_revised.vm.maxScreenshotBytes", 2_000_000);
    private static final ThreadLocal<Path> SCREENSHOT_PATH_CACHE = ThreadLocal.withInitial(() -> {
        try {
            Path path = Files.createTempFile("tech_revised_vm_", ".png");
            path.toFile().deleteOnExit();
            return path;
        } catch (Exception ignored) {
            return null;
        }
    });

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

    public static String getConfiguredMouseMode() {
        String mode = System.getProperty(VM_MOUSE_MODE_PROPERTY, DEFAULT_VM_MOUSE_MODE).trim().toLowerCase(Locale.ROOT);
        return switch (mode) {
            case "ps2", "usb", "usbtablet", "usbmultitouch", "usbmtscreenpluspad" -> mode;
            default -> DEFAULT_VM_MOUSE_MODE;
        };
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

    public static void sendKeyboardTextAsync(String localVmName, String localStartTemplate, String localStopTemplate,
                                             String text) {
        if (text == null || text.isEmpty()) {
            return;
        }

        String vmName = resolveVmName(localVmName);
        String vboxExecutable = resolveVBoxManageExecutable(localStartTemplate, localStopTemplate);

        EXECUTOR.submit(() -> {
            try {
                runVBoxManage(vboxExecutable, List.of("controlvm", vmName, "keyboardputstring", text), 8);
            } catch (Exception exception) {
                LOGGER.debug("Failed to send keyboard text to VM '{}': {}", vmName, exception.getMessage());
            }
        });
    }

    public static void sendKeyboardSpecialKeyAsync(String localVmName, String localStartTemplate, String localStopTemplate,
                                                   VmSpecialKey specialKey) {
        if (specialKey == null) {
            return;
        }

        String vmName = resolveVmName(localVmName);
        String vboxExecutable = resolveVBoxManageExecutable(localStartTemplate, localStopTemplate);

        EXECUTOR.submit(() -> {
            try {
                List<String> command = new ArrayList<>();
                command.add("controlvm");
                command.add(vmName);
                command.add("keyboardputscancode");
                command.addAll(specialKey.getScanCodes());
                runVBoxManage(vboxExecutable, command, 8);
            } catch (Exception exception) {
                LOGGER.debug("Failed to send keyboard special key '{}' to VM '{}': {}",
                        specialKey.name(), vmName, exception.getMessage());
            }
        });
    }

    public static void sendMouseClickAsync(String localVmName, String localStartTemplate, String localStopTemplate,
                                           int absoluteX, int absoluteY, int buttonMask) {
        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (!osName.contains("win")) {
            return;
        }

        if ((buttonMask & 0x07) == 0) {
            return;
        }

        String vmName = resolveVmName(localVmName);
        int clampedX = Math.max(0, Math.min(65535, absoluteX));
        int clampedY = Math.max(0, Math.min(65535, absoluteY));
        int sanitizedMask = buttonMask & 0x07;

        EXECUTOR.submit(() -> {
            try {
                CommandResult result = runPowerShell(mouseClickScript(vmName, clampedX, clampedY, sanitizedMask), 8);
                if (result.exitCode() != 0) {
                    LOGGER.debug("Failed to send mouse click to VM '{}': {}", vmName, result.outputPreview());
                }
            } catch (Exception exception) {
                LOGGER.debug("Failed to send mouse click to VM '{}': {}", vmName, exception.getMessage());
            }
        });
    }

    public static void captureScreenshotAsync(String localVmName, String localStartTemplate, String localStopTemplate,
                                              Consumer<VmScreenshotResult> callback) {
        String vmName = resolveVmName(localVmName);
        String vboxExecutable = resolveVBoxManageExecutable(localStartTemplate, localStopTemplate);

        EXECUTOR.submit(() -> {
            try {
                VmScreenshotResult result = captureScreenshot(vmName, vboxExecutable, true);
                callback.accept(result);
            } catch (Exception exception) {
                LOGGER.error("VM screenshot capture failed.", exception);
                callback.accept(new VmScreenshotResult(false, "Screenshot error: " + exception.getMessage(), new byte[0]));
            }
        });
    }

    public static VmScreenshotResult captureScreenshotNow(String localVmName, String localStartTemplate, String localStopTemplate) {
        String vmName = resolveVmName(localVmName);
        String vboxExecutable = resolveVBoxManageExecutable(localStartTemplate, localStopTemplate);
        try {
            // Live stream path: skip expensive VM state query for lower frame latency.
            return captureScreenshot(vmName, vboxExecutable, false);
        } catch (Exception exception) {
            return new VmScreenshotResult(false, "Screenshot error: " + exception.getMessage(), new byte[0]);
        }
    }

    public static void executeAction(ServerLevel level, BlockPos pos, ServerPlayer player, VmAction action) {
        executeAction(level, pos, player, action, "", "", "");
    }

    public static void executeAction(ServerLevel level, BlockPos pos, ServerPlayer player, VmAction action,
                                     String localVmName, String localStartTemplate, String localStopTemplate) {
        String vmName = resolveVmName(localVmName);
        String vboxExecutable = resolveVBoxManageExecutable(localStartTemplate, localStopTemplate);
        String command = resolveCommand(action, vmName, localStartTemplate, localStopTemplate, vboxExecutable);

        EXECUTOR.submit(() -> {
            try {
                ActionExecution actionExecution = action == VmAction.START
                        ? executeStartWithLockHandling(vmName, command, vboxExecutable)
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

    private static String resolveCommand(VmAction action, String vmName,
                                         String localStartTemplate, String localStopTemplate,
                                         String vboxExecutable) {
        String localTemplate = action == VmAction.START ? localStartTemplate : localStopTemplate;
        String template = resolveCommandTemplate(action, localTemplate);
        String normalized = normalizeLegacyVBoxManageCommand(template, vboxExecutable);
        String quotedVBox = "\"" + vboxExecutable + "\"";
        String quotedVmName = "\"" + vmName + "\"";

        String withVBox = normalized
                .replace("\"" + VBOXMANAGE_PLACEHOLDER + "\"", quotedVBox)
                .replace("'" + VBOXMANAGE_PLACEHOLDER + "'", quotedVBox)
                .replace(VBOXMANAGE_PLACEHOLDER, quotedVBox);
        String withVmName = withVBox
                .replace("\"{vmName}\"", quotedVmName)
                .replace("'{vmName}'", quotedVmName)
                .replace("{vmName}", quotedVmName);

        String fixedKnownPath = quoteKnownVBoxPathIfNeeded(withVmName, vboxExecutable);
        return normalizeSingleQuotedSegments(fixedKnownPath);
    }

    private static String resolveCommandTemplate(VmAction action, String localTemplate) {
        String trimmedLocal = localTemplate == null ? "" : localTemplate.trim();
        if (!trimmedLocal.isEmpty()) {
            return trimmedLocal;
        }
        return action == VmAction.START ? getConfiguredStartTemplate() : getConfiguredStopTemplate();
    }

    private static VmScreenshotResult captureScreenshot(String vmName, String vboxExecutable,
                                                        boolean verifyVmState) throws Exception {
        if (verifyVmState) {
            String vmState = queryVmState(vmName, vboxExecutable);
            if (!isActiveVmState(vmState)) {
                return new VmScreenshotResult(false,
                        "VM is not running (state: " + (vmState.isEmpty() ? "unknown" : vmState) + ").",
                        new byte[0]);
            }
        }

        Path screenshotPath = screenshotPath();
        CommandResult result = runVBoxManage(vboxExecutable,
                List.of("controlvm", vmName, "screenshotpng", screenshotPath.toAbsolutePath().toString()),
                12);
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
        if (imageBytes.length > MAX_SCREENSHOT_BYTES) {
            return new VmScreenshotResult(false, "Screenshot too large (" + imageBytes.length + " bytes).", new byte[0]);
        }

        return new VmScreenshotResult(true, "OK", imageBytes);
    }

    private static Path screenshotPath() throws Exception {
        Path cachedPath = SCREENSHOT_PATH_CACHE.get();
        if (cachedPath != null) {
            return cachedPath;
        }
        Path fallbackPath = Files.createTempFile("tech_revised_vm_", ".png");
        fallbackPath.toFile().deleteOnExit();
        return fallbackPath;
    }

    private static ActionExecution executeStop(String stopCommand) throws Exception {
        CommandResult result = runCommand(stopCommand, 8);
        return new ActionExecution(result.exitCode() == 0, false, result);
    }

    private static ActionExecution executeStartWithLockHandling(String vmName, String startCommand,
                                                                String vboxExecutable) throws Exception {
        String initialState = queryVmState(vmName, vboxExecutable);
        if (isActiveVmState(initialState)) {
            return new ActionExecution(true, true, new CommandResult(0, "VMState=" + initialState));
        }

        applyMouseModeIfPossible(vmName, vboxExecutable);

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

            String state = queryVmState(vmName, vboxExecutable);
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

        String finalState = queryVmState(vmName, vboxExecutable);
        if (isActiveVmState(finalState)) {
            return new ActionExecution(true, true, new CommandResult(0, "VMState=" + finalState));
        }

        return new ActionExecution(false, false, attempt);
    }

    private static void applyMouseModeIfPossible(String vmName, String vboxExecutable) {
        try {
            String mouseMode = getConfiguredMouseMode();
            CommandResult result = runVBoxManage(
                    vboxExecutable,
                    List.of("modifyvm", vmName, "--mouse", mouseMode),
                    8
            );
            if (result.exitCode() != 0) {
                LOGGER.debug("Failed to set VM mouse mode '{}' for '{}': {}",
                        mouseMode, vmName, result.outputPreview());
            }
        } catch (Exception exception) {
            LOGGER.debug("Failed to apply VM mouse mode for '{}': {}", vmName, exception.getMessage());
        }
    }

    private static CommandResult runCommand(String command, int maxOutputLines) throws Exception {
        String normalizedCommand = normalizeSingleQuotedSegments(command);
        ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", normalizedCommand);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        return collectProcessOutput(process, maxOutputLines);
    }

    private static CommandResult runVBoxManage(String vboxExecutable, List<String> arguments, int maxOutputLines) throws Exception {
        List<String> command = new ArrayList<>();
        command.add(sanitizeExecutablePath(vboxExecutable));
        command.addAll(arguments);

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        return collectProcessOutput(process, maxOutputLines);
    }

    private static CommandResult runPowerShell(String script, int maxOutputLines) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "powershell.exe",
                "-NoProfile",
                "-NonInteractive",
                "-ExecutionPolicy",
                "Bypass",
                "-Command",
                script
        );
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        return collectProcessOutput(process, maxOutputLines);
    }

    private static CommandResult collectProcessOutput(Process process, int maxOutputLines) throws Exception {
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

        String quotedVBox = "\"" + vboxExecutable + "\"";
        return template.replaceFirst(
                "(?i)^\\s*(?:\"|')?VBoxManage(?:\\.exe)?(?:\"|')?",
                Matcher.quoteReplacement(quotedVBox)
        );
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

    private static String normalizeSingleQuotedSegments(String command) {
        if (command.indexOf('\'') < 0) {
            return command;
        }
        // cmd.exe does not treat single quotes as argument quoting; convert single-quoted segments.
        return command.replaceAll("'([^']*)'", "\"$1\"");
    }

    private static String resolveVBoxManageExecutable(String... commandTemplates) {
        if (commandTemplates != null) {
            for (String template : commandTemplates) {
                String fromTemplate = extractVBoxExecutableFromTemplate(template);
                if (!fromTemplate.isEmpty()) {
                    return fromTemplate;
                }
            }
        }

        String configured = sanitizeExecutablePath(System.getProperty(VBOX_MANAGE_PATH_PROPERTY, ""));
        if (!configured.isEmpty()) {
            return configured;
        }

        if (Files.isRegularFile(Path.of(VBOX_PATH_PROGRAM_FILES))) {
            return sanitizeExecutablePath(VBOX_PATH_PROGRAM_FILES);
        }
        if (Files.isRegularFile(Path.of(VBOX_PATH_PROGRAM_FILES_X86))) {
            return sanitizeExecutablePath(VBOX_PATH_PROGRAM_FILES_X86);
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

    private static String queryVmState(String vmName, String vboxExecutable) {
        try {
            CommandResult result = runVBoxManage(vboxExecutable,
                    List.of("showvminfo", vmName, "--machinereadable"),
                    300);
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

    private static String extractVBoxExecutableFromTemplate(String template) {
        String trimmed = template == null ? "" : template.trim();
        if (trimmed.isEmpty() || trimmed.contains(VBOXMANAGE_PLACEHOLDER)) {
            return "";
        }

        String normalizedQuotes = normalizeSingleQuotedSegments(trimmed);
        if (normalizedQuotes.startsWith("\"")) {
            int endQuote = normalizedQuotes.indexOf('"', 1);
            if (endQuote > 1) {
                String firstQuoted = normalizedQuotes.substring(1, endQuote).trim();
                if (looksLikeVBoxExecutable(firstQuoted)) {
                    return sanitizeExecutablePath(firstQuoted);
                }
            }
        }

        String lower = normalizedQuotes.toLowerCase(Locale.ROOT);
        int idx = lower.indexOf("vboxmanage");
        if (idx >= 0) {
            int end = idx + "vboxmanage".length();
            if (lower.startsWith(".exe", end)) {
                end += 4;
            }
            String candidate = normalizedQuotes.substring(0, end).trim();
            if (looksLikeVBoxExecutable(candidate)) {
                return sanitizeExecutablePath(candidate);
            }
        }

        int firstSpace = normalizedQuotes.indexOf(' ');
        String firstToken = firstSpace >= 0 ? normalizedQuotes.substring(0, firstSpace).trim() : normalizedQuotes;
        if (looksLikeVBoxExecutable(firstToken)) {
            return sanitizeExecutablePath(firstToken);
        }

        return "";
    }

    private static boolean looksLikeVBoxExecutable(String value) {
        String lower = sanitizeExecutablePath(value).toLowerCase(Locale.ROOT);
        return lower.endsWith("vboxmanage") || lower.endsWith("vboxmanage.exe");
    }

    private static String sanitizeExecutablePath(String value) {
        String sanitized = value == null ? "" : value.trim();
        if (sanitized.isEmpty()) {
            return "";
        }

        while (sanitized.startsWith("\"") || sanitized.startsWith("'")) {
            sanitized = sanitized.substring(1).trim();
        }
        while (sanitized.endsWith("\"") || sanitized.endsWith("'")) {
            sanitized = sanitized.substring(0, sanitized.length() - 1).trim();
        }

        if ("VBoxManage.exe".equalsIgnoreCase(sanitized)) {
            return "VBoxManage";
        }
        return sanitized;
    }

    private static String mouseClickScript(String vmName, int absoluteX, int absoluteY, int buttonMask) {
        String escapedVmName = vmName.replace("'", "''");
        return "$ErrorActionPreference='Stop';"
                + "$vbox=New-Object -ComObject VirtualBox.VirtualBox;"
                + "$session=New-Object -ComObject VirtualBox.Session;"
                + "$machine=$vbox.FindMachine('" + escapedVmName + "');"
                + "$machine.LockMachine($session,1);"
                + "try{"
                + "$mouse=$session.Console.Mouse;"
                + "$mouse.PutMouseEventAbsolute(" + absoluteX + "," + absoluteY + ",0,0," + buttonMask + ")|Out-Null;"
                + "Start-Sleep -Milliseconds 20;"
                + "$mouse.PutMouseEventAbsolute(" + absoluteX + "," + absoluteY + ",0,0,0)|Out-Null;"
                + "}finally{$session.UnlockMachine();}";
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
