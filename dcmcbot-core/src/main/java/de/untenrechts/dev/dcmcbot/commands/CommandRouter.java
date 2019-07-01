package de.untenrechts.dev.dcmcbot.commands;

import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.MessageUpdateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.untenrechts.dev.dcmcbot.DcMcBotConstants.*;
import static java.util.stream.Collectors.toList;

public class CommandRouter {

    private static final Logger LOG = LoggerFactory.getLogger(CommandRouter.class);


    public static void onReady(ReadyEvent readyEvent) {
        LOG.info("Logged in as user: {} (ID: {}).",
                readyEvent.getSelf().getUsername(), readyEvent.getSelf().getId());
    }

    public static void onNewMessage(MessageCreateEvent messageCreateEvent) {
        Message message = messageCreateEvent.getMessage();
        Optional<String> optionalContent = message.getContent();
        if (optionalContent.isPresent()) {
            String content = optionalContent.get();
            if (content.startsWith(COMMAND_PREFIX)) {
                onNewCommand(message.getChannel(), content.substring(COMMAND_PREFIX.length() - 1));
            } // else: do nothing, the message was not meant for you...
        }
    }

    public static void onUpdatedMessage(MessageUpdateEvent messageUpdateEvent) {
        // TODO: 26.06.2019 implement
    }

    private static void onNewCommand(Mono<MessageChannel> channelMono, String command) {
        List<ICommandExecutable> commanders = CommandContainer.COMMAND_LIST.stream()
                .filter(commanderIt -> commanderIt.getInvocation().equals(command))
                .collect(toList());
        if (commanders.size() == 0) {
            LOG.debug("Unknown command: '{}'. I will not respond.", command);
        } else {
            String[] parametersRaw = command.split(PARAMETER_SEPARATOR);
            String[] parameters = Arrays.copyOfRange(parametersRaw, 1, parametersRaw.length);
            if (commanders.size() > 1) {
                LOG.warn("Multiple command mapping detected, command: '{}' / mappings: {}",
                        command,
                        commanders.stream()
                                .map(ICommandExecutable::getInvocation)
                                .collect(Collectors.joining(","))
                );
            }
            commanders.forEach(commandExecutor -> commandExecutor.onCommand(channelMono, parameters));
        }
    }

}
