package de.untenrechts.dev.dcmcbot.commands.executors;

import de.untenrechts.dev.dcmcbot.commands.ICommandExecutable;
import de.untenrechts.dev.dcmcbot.systeminteractions.TmuxInteractor;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.List;

public class MinecraftCommand implements ICommandExecutable {

    private static final Logger LOG = LoggerFactory.getLogger(MinecraftCommand.class);

    private final String INVOCATION = "list";
    private final String[] PARAMETERS = new String[]{"mc-console-command"};

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
        if (parameters.length == 1) {
            List<String> resultLines = TmuxInteractor.bashToTmux(parameters[0], true);
            messageChannel.createEmbed(embed -> fillExecEmbed(embed, resultLines)).block();
        } else {
            String error = String.format("Unable to execute command to Minecraft server. " +
                    "Expected 1 argument instead of: %d", parameters.length);
            throw new IllegalArgumentException(error);
        }
    }

    private void fillExecEmbed(EmbedCreateSpec embedCreateSpec, List<String> lines) {
        final String helpTitle = "Result for command";
        CommandExecutor.fillWithAuthor(embedCreateSpec);
        embedCreateSpec.setTitle(helpTitle);
        embedCreateSpec.setDescription(String.join("\n", lines));
    }

}
