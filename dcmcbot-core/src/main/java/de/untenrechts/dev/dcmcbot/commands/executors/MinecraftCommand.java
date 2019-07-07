package de.untenrechts.dev.dcmcbot.commands.executors;

import de.untenrechts.dev.dcmcbot.commands.ICommandExecutable;
import de.untenrechts.dev.dcmcbot.exceptions.IllegalCommandException;
import de.untenrechts.dev.dcmcbot.systeminteractions.TmuxInteractor;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static de.untenrechts.dev.dcmcbot.DcMcBotConstants.IoMode;

public class MinecraftCommand implements ICommandExecutable {

    private static final Logger LOG = LoggerFactory.getLogger(MinecraftCommand.class);

    private final String INVOCATION = "mc";
    private final String[] PARAMETERS = new String[]{"mc-console-command", getIoModes()};

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
        if (parameters.length == 2) {
            String command = parameters[0];
            IoMode ioMode = IoMode.valueOf(parameters[1]);
            List<String> resultLines = TmuxInteractor.send(command, ioMode);
            messageChannel.createEmbed(embed -> fillExecEmbed(embed, resultLines)).block();
        } else {
            String error = String.format("Unable to execute command to Minecraft server. " +
                    "Expected 2 arguments, was: %d", parameters.length);
            throw new IllegalCommandException(error);
        }
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

}
