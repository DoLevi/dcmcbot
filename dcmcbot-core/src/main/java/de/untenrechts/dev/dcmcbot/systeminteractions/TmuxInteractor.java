package de.untenrechts.dev.dcmcbot.systeminteractions;

import de.untenrechts.dev.dcmcbot.config.DcMcBotConfigHandler;
import de.untenrechts.dev.dcmcbot.config.MinecraftBotType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class TmuxInteractor {

    private static final Logger LOG = LoggerFactory.getLogger(TmuxInteractor.class);

    private static final TmuxInteractor INSTANCE = new TmuxInteractor();

    private final Runtime runtime;
    private final String commandWrapper;
    private final String getPaneCommand;
    private final List<String> blackListedCommands;
    private final List<String> whiteListedCommands;

    private TmuxInteractor() {
        this.runtime = Runtime.getRuntime();
        MinecraftBotType minecraftBot = DcMcBotConfigHandler.getConfig().getMinecraftBot();
        String tmuxSessionName = minecraftBot.getTmuxMinecraftSessionName();
        this.commandWrapper = String.format("tmux send-keys -t \"%s\" C-z %s Enter",
                tmuxSessionName, "%s");
        this.getPaneCommand = String.format("tmux capture-pane -t \"%s\" -p", tmuxSessionName);
        this.blackListedCommands = minecraftBot.getBlackListedCommands();
        this.whiteListedCommands = minecraftBot.getWhiteListedCommands();
    }

    /**
     * Fires a bash command to the TMUX session loaded from the object config
     *
     * @param command command to be fired
     * @param readOutput whether to read output, false will be faster
     * @return a String array of lines considered as responses to the issues command
     */
    public static List<String> bashToTmux(String command, boolean readOutput) {
        INSTANCE.validateAgainstWhitelist(command);
        INSTANCE.validateAgainstBlacklist(command);
        final String tmuxCommand = String.format(INSTANCE.commandWrapper, command);
        INSTANCE.executeCommand(tmuxCommand, false);
        if (readOutput) {
            return INSTANCE.getLinesAfterCommand(command);
        } else {
            return Collections.emptyList();
        }
    }

    private List<String> getLinesAfterCommand(String command) {
        // TODO: 01.07.2019 implement scroll up if command not found
        List<String> linesAfterCommand = new ArrayList<>();
        for (String line : getPaneAsArray()) {
            if (line.equals(command) || linesAfterCommand.size() > 0) {
                linesAfterCommand.add(line);
            }
        }
        return linesAfterCommand;
    }

    private String[] getPaneAsArray() {
        Optional<String> allLinesOpt = executeCommand(getPaneCommand, true);
        return allLinesOpt
                .orElse("")
                .split(System.lineSeparator());
    }

    private Optional<String> executeCommand(String command, boolean readOutput) {
        try {
            Process process = runtime.exec(command);
            process.waitFor();
            if (readOutput) {
                InputStream resultStream = process.getInputStream();
                return Optional.of(streamToString(resultStream));
            } else {
                return Optional.empty();
            }
        } catch (IOException | InterruptedException e) {
            LOG.error("Unable to bash command '{}'. {}", command, e.getMessage(), e);
            throw new IllegalStateException("Unable to bash command to tmux.");
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
    }

    private void validateAgainstBlacklist(String command) {
        Predicate<String> matchesCommand = matchesRegex(command);
        if (blackListedCommands.size() > 0) {
            blackListedCommands.stream()
                    .filter(matchesCommand)
                    .findAny()
                    .ifPresent(this::handleBlacklisted);
        }
    }

    private Predicate<String> matchesRegex(String listedCommand) {
        return str -> Pattern.matches(listedCommand, str);
    }

    private IllegalArgumentException handleNotWhitelisted(String command) {
        String error = String.format("Command '%s' is not allowed: Not whitelisted.", command);
        LOG.info(error);
        return new IllegalArgumentException(error);
    }

    private void handleBlacklisted(String command) {
        String error = String.format("Commands '%s' is not allowed: Blacklisted.", command);
        LOG.info(error);
        throw new IllegalArgumentException(error);
    }

}
