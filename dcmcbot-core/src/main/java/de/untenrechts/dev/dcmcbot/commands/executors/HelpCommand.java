package de.untenrechts.dev.dcmcbot.commands.executors;

import de.untenrechts.dev.dcmcbot.commands.CommandContainer;
import de.untenrechts.dev.dcmcbot.commands.ICommandExecutable;
import de.untenrechts.dev.dcmcbot.config.DcMcBotConfigHandler;
import de.untenrechts.dev.dcmcbot.config.DiscordBotType;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.stream.Collectors;

import static de.untenrechts.dev.dcmcbot.DcMcBotConstants.*;

public class HelpCommand implements ICommandExecutable {

    private final Logger LOG = LoggerFactory.getLogger(HelpCommand.class);

    private final String INVOCATION = "help";
    private final String[] PARAMETERS = new String[]{"command1", "command2", "..."};


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
        CommandExecutor.handleMessageResponse(this::generateGeneralHelpEmbed, channelMono);
    }

    private void generateGeneralHelpEmbed(MessageChannel messageChannel) {
        messageChannel.createEmbed(this::fillHelpEmbed).block();
    }

    private void fillHelpEmbed(EmbedCreateSpec embedCreateSpec) {
        final String helpTitle = "Available commands";
        CommandExecutor.fillWithAuthor(embedCreateSpec);
        embedCreateSpec.setTitle(helpTitle);
        embedCreateSpec.addField("Command Parameters", generateCommandListHelp(), false);
    }

    private String generateCommandListHelp() {
        return CommandContainer.COMMAND_LIST.stream()
                .map(this::generateCommandHelp)
                .collect(Collectors.joining("\n"));
    }

    private String generateCommandHelp(ICommandExecutable executor) {
        String parameters = Arrays.stream(executor.getParameters())
                .map(param -> String.format("<%s>", param))
                .collect(Collectors.joining(PARAMETER_SEPARATOR));
        String command = StringUtils.rightPad(executor.getInvocation(), HELP_COMMAND_PAD_SIZE);
        return String.format("`%s %s`", command, parameters);
    }

}
