package de.untenrechts.dev.dcmcbot.commands.bot;

import de.untenrechts.dev.dcmcbot.commands.CommandContainer;
import de.untenrechts.dev.dcmcbot.commands.ICommandExecutable;
import de.untenrechts.dev.dcmcbot.config.DcMcBotConfigHandler;
import de.untenrechts.dev.dcmcbot.config.DcMcBotConfigType;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.stream.Collectors;

import static de.untenrechts.dev.dcmcbot.DcMcBotConstants.*;

public class HelpCommand implements ICommandExecutable {

    private final Logger LOG = LoggerFactory.getLogger(HelpCommand.class);

    private final String HELP_TITLE = "Available commands";

    private final String INVOCATION = "help";
    private final String[] PARAMETERS = new String[]{"command1", "command2", "..."};


    @Override
    public String getInvocation() {
        return this.INVOCATION;
    }

    @Override
    public void onCommand(Mono<MessageChannel> channelMono, String[] parameters) {
        LOG.debug("Parameters found: [{}]", String.join(" ", parameters));
        MessageChannel messageChannel = channelMono.block();
        if (messageChannel == null) {
            LOG.error("Unable to unwrap MessageChannel.");
        } else {
            messageChannel.createEmbed(this::generateGeneralHelpEmbed).block();
            LOG.info("Responded with general help message.");
        }
    }

    @Override
    public String[] getParameters() {
        return this.PARAMETERS;
    }

    private void generateGeneralHelpEmbed(EmbedCreateSpec embedCreateSpec) {
        DcMcBotConfigType config = DcMcBotConfigHandler.getConfig();
        embedCreateSpec.setAuthor(
                config.getDisplayBotName(),
                String.format(BOT_FURL, config.getDisplayBotName()),
                config.getDisplayBotIconUrl());
        embedCreateSpec.setTitle(HELP_TITLE);
        embedCreateSpec.addField("Command", generateCommandInvocationColumn(), false);
        embedCreateSpec.addField("Parameters", generateCommandParameterColumn(), false);
    }

    private String generateCommandParameterColumn() {
        return CommandContainer.COMMAND_LIST.stream()
                .map(executor -> Arrays.stream(executor.getParameters())
                        .map(parameter -> String.format("<%s>", parameter))
                        .collect(Collectors.joining(" ")))
                .collect(Collectors.joining("\n"));
    }

    private String generateCommandInvocationColumn() {
        return CommandContainer.COMMAND_LIST.stream()
                .map(ICommandExecutable::getInvocation)
                .collect(Collectors.joining("\n"));
    }

}
