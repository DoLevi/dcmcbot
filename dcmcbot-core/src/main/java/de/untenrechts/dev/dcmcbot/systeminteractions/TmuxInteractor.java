package de.untenrechts.dev.dcmcbot.systeminteractions;

import de.untenrechts.dev.dcmcbot.config.DcMcBotConfigHandler;
import de.untenrechts.dev.dcmcbot.config.MinecraftBotType;
import de.untenrechts.dev.dcmcbot.exceptions.IllegalCommandException;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static de.untenrechts.dev.dcmcbot.DcMcBotConstants.TMUX_BASH_COMMAND;
import static de.untenrechts.dev.dcmcbot.DcMcBotConstants.IoMode;
import static de.untenrechts.dev.dcmcbot.DcMcBotConstants.TmuxCommand;

public class TmuxInteractor {

    private static final Logger LOG = LoggerFactory.getLogger(TmuxInteractor.class);

    private static final TmuxInteractor INSTANCE = new TmuxInteractor();

    private final String tmuxSessionName;
    private final int logContentOffset;
    private final int retrySleepMillis;
    private final int retryCount;
    private final List<String> blackListedCommands;
    private final List<String> whiteListedCommands;

    private TmuxInteractor() {
        MinecraftBotType minecraftBot = DcMcBotConfigHandler.getConfig().getMinecraftBot();
        this.tmuxSessionName = minecraftBot.getTmuxSessionName();
        this.logContentOffset = minecraftBot.getLogContentOffset();
        this.retrySleepMillis = 256; // millis
        this.retryCount = 3; // millis
        this.blackListedCommands = minecraftBot.getBlackListedCommands().getCommand();
        this.whiteListedCommands = minecraftBot.getWhiteListedCommands().getCommand();
    }

    /**
     * Fires a bash command to the TMUX session loaded from the object config
     *
     * @param command   command to be fired
     * @param ioMode    IoMode for which to execute the send command
     * @return a String array of lines considered as responses to the issues command
     */
    public static List<String> send(String command, IoMode ioMode) {
        INSTANCE.validateAgainstWhitelist(command);
        INSTANCE.validateAgainstBlacklist(command);
        List<String> directResponse = INSTANCE.sendCommand(ioMode, command)
                .map(Arrays::asList)
                .orElse(Collections.emptyList());
        if (ioMode == IoMode.SEND_WITH_READ) {
            List<String> onPaneResponse = INSTANCE.getLinesAfterCommandRetry(command);
            onPaneResponse.addAll(directResponse);
            return onPaneResponse;
        }
        return directResponse;
    }

    private List<String> getLinesAfterCommandRetry(String command) {
        for (int i = 0; i < this.retryCount; ++i) {
            List<String> lines = getLinesAfterCommand(command);
            if (lines.size() > 0) {
                return lines;
            }
            try {
                Thread.sleep(this.retrySleepMillis);
            } catch (InterruptedException e) {
                LOG.error("Sleep during retry for command '{}' was interrupted. {}",
                        command, e.getMessage(), e);
            }
        }
        return new ArrayList<>();
    }

    private List<String> getLinesAfterCommand(String command) {
        // TODO: 01.07.2019 implement scroll up if command not found
        List<String> linesAfterCommand = new ArrayList<>();
        String[] allLines = INSTANCE.getPaneAsArray();
        ArrayUtils.reverse(allLines);
        for (String line : allLines) {
            String lineContent = extractLineContent(line);
            if (lineContent.equals(command)) {
                return linesAfterCommand;
            } else if (!lineContent.isEmpty()) {
                linesAfterCommand.add(lineContent);
            } // else skip empty lines
        }
        LOG.error("Unable to get lines after command. Command not found: '{}'", command);
        return new ArrayList<>();
    }

    private String extractLineContent(String logLine) {
        if (logLine.length() >= this.logContentOffset) {
            return logLine.substring(this.logContentOffset);
        } else if (logLine.startsWith("> ")) {
            return logLine.substring(2);
        } else if (logLine.startsWith(">")) {
            return logLine.substring(1);
        }
        return logLine;
    }

    private String[] getPaneAsArray() {
        Optional<String> allLinesOpt = getCommand();
        return allLinesOpt
                .orElse("")
                .split(System.lineSeparator());
    }

    private Optional<String> sendCommand(IoMode ioMode, String command) {
        String[] processArguments = new String[] {
                TMUX_BASH_COMMAND, TmuxCommand.SEND.getValue(), this.tmuxSessionName, command
        };
        return executeProcess(processArguments, ioMode);
    }

    private Optional<String> getCommand() {
        IoMode ioMode = IoMode.SEND_WITH_READ;
        String[] processArguments = new String[] {
                TMUX_BASH_COMMAND, TmuxCommand.GET.getValue(), this.tmuxSessionName
        };
        return executeProcess(processArguments, ioMode);
    }

    private Optional<String> executeProcess(String[] processArguments, IoMode ioMode) {
        try {
            LOG.debug("Starting process for command: '{}'",
                    String.join(" ", processArguments));
            Process process = new ProcessBuilder(processArguments).start();
            process.waitFor();
            switch (ioMode) {
                case SEND_WITH_READ:
                    InputStream resultStream = process.getInputStream();
                    String result = streamToString(resultStream);
                    return Optional.of(result);
                case SEND_ONLY:
                    LOG.debug("SEND_ONLY so returning empty Optional.");
                    return Optional.empty();
                default:
                    throw new IllegalStateException("Undefined enum value: " + ioMode);
            }
        } catch (IOException | InterruptedException e) {
            String command = String.join(" ", processArguments);
            String error = String.format("Unable to bash command '%s' (IoMode: %s). %s",
                    command, ioMode, e.getMessage());
            throw new IllegalStateException(error, e);
        }
    }

    private String streamToString(InputStream inputStream) {
        try {
            final int byteBuffer = 1024;
            final String encoding = "UTF-8";
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[byteBuffer];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            return result.toString(encoding);
        } catch (IOException e) {
            LOG.error("Unable to read input from terminal. {}", e.getMessage(), e);
            throw new IllegalStateException("Unable to read input from terminal.");
        }
    }

    private void validateAgainstWhitelist(String command) {
        Predicate<String> matchesCommand = matchesRegex(command);
        if (whiteListedCommands.size() > 0) {
            whiteListedCommands.stream()
                    .filter(matchesCommand)
                    .findAny()
                    .orElseThrow(() -> handleNotWhitelisted(command));
        }
        LOG.debug("Command '{}' passed the whitelist validation.", command);
    }

    private void validateAgainstBlacklist(String command) {
        Predicate<String> matchesCommand = matchesRegex(command);
        if (blackListedCommands.size() > 0) {
            blackListedCommands.stream()
                    .filter(matchesCommand)
                    .findAny()
                    .ifPresent(this::handleBlacklisted);
        }
        LOG.debug("Command '{}' passed the blacklist validation.", command);
    }

    private Predicate<String> matchesRegex(String listedCommand) {
        return str -> Pattern.matches(listedCommand, str);
    }

    private IllegalCommandException handleNotWhitelisted(String command) {
        String error = String.format("Command '%s' is not allowed: Not whitelisted.", command);
        return new IllegalCommandException(error);
    }

    private void handleBlacklisted(String command) {
        String error = String.format("Commands '%s' is not allowed: Blacklisted.", command);
        throw new IllegalCommandException(error);
    }

}
