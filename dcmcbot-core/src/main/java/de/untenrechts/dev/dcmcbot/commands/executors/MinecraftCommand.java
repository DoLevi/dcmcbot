package de.untenrechts.dev.dcmcbot.commands.executors;

import de.untenrechts.dev.dcmcbot.commands.ICommandExecutable;
import de.untenrechts.dev.dcmcbot.config.DcMcBotConfigHandler;
import de.untenrechts.dev.dcmcbot.exceptions.IllegalCommandException;
import de.untenrechts.dev.dcmcbot.systeminteractions.TmuxInteractor;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static de.untenrechts.dev.dcmcbot.DcMcBotConstants.IoMode;

public class MinecraftCommand implements ICommandExecutable {

    private static final Logger LOG = LoggerFactory.getLogger(MinecraftCommand.class);

    private final String INVOCATION = "mc";
    private final String[] PARAMETERS = new String[]{getIoModes(), "mc-console-command"};
    private final List<String> blackListedCommands = DcMcBotConfigHandler.getConfig()
            .getMinecraftBot().getBlackListedCommands().getCommand();
    private final List<String> whiteListedCommands = DcMcBotConfigHandler.getConfig()
            .getMinecraftBot().getWhiteListedCommands().getCommand();


    @Override
    public String getInvocation() {
        return this.INVOCATION;
    }

    @Override
    public String[] getParameters() {
        return this.PARAMETERS;
    }

    @Override
    public void onCommand(Mono<MessageChannel> channelMono, String[] parameters) {
        LOG.debug("Parameters found: [{}]", String.join(" ", parameters));
        CommandExecutor
                .handleMessageResponse(this::execMinecraftServerCommand, channelMono, parameters);
    }

    private void execMinecraftServerCommand(MessageChannel messageChannel, String[] parameters) {
        if (parameters.length < 2) {
            String error = String.format("Unable to execute command to Minecraft server. " +
                    "Expected at least 2 arguments, was: %d", parameters.length);
            throw new IllegalCommandException(error);
        }
        IoMode ioMode = IoMode.valueOf(parameters[0]);
        String[] commandAsArray = Arrays.copyOfRange(parameters, 1, parameters.length);
        String command = String.join(" ", commandAsArray);
        validateAgainstWhitelist(command);
        validateAgainstBlacklist(command);
        List<String> resultLines = TmuxInteractor.send(ioMode, command);
        messageChannel.createEmbed(embed -> fillExecEmbed(embed, resultLines)).block();
    }

    private void fillExecEmbed(EmbedCreateSpec embedCreateSpec, List<String> lines) {
        final String helpTitle = "Result for command";
        CommandExecutor.fillWithAuthor(embedCreateSpec);
        embedCreateSpec.setTitle(helpTitle);
        embedCreateSpec.setDescription(String.join("\n", lines));
    }

    private String getIoModes() {
        List<String> ioModeList = Arrays.stream(IoMode.values())
                .map(IoMode::name)
                .collect(Collectors.toList());
        String ioModeString = String.join("|", ioModeList);
        return String.format("[%s]", ioModeString);
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

    private Predicate<String> matchesRegex(String proposal) {
        return regex -> Pattern.matches(regex, proposal);
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
