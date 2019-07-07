package de.untenrechts.dev.dcmcbot.commands;

import de.untenrechts.dev.dcmcbot.exceptions.IllegalCommandException;
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
import java.util.function.Predicate;
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
        try {
            Optional<String> optionalContent = message.getContent();
            if (optionalContent.isPresent()) {
                String content = optionalContent.get();
                if (content.startsWith(COMMAND_PREFIX)) {
                    onNewCommand(message.getChannel(), content.substring(COMMAND_PREFIX.length()));
                } // else: do nothing, the message was not meant for you...
            }
        } catch (IllegalCommandException e) {
            // Keep running if something bad happens
            LOG.error("Processing of new Message failed. {}", e.getMessage());
            respondSafely(message, e.getMessage());
        }catch (RuntimeException e) {
            // Keep running if something bad happens
            LOG.error("Processing of new Message failed. {}", e.getMessage(), e);
            respondSafely(message, "Some programmer(s) messed up :(");
        }
    }

    public static void onUpdatedMessage(MessageUpdateEvent messageUpdateEvent) {
        // TODO: 26.06.2019 implement
    }

    private static void onNewCommand(Mono<MessageChannel> channelMono, String message) {
        // TODO: 02.07.2019 make prettier
        String command = message.split(PARAMETER_SEPARATOR)[0];
        List<ICommandExecutable> commanders = CommandContainer.COMMAND_LIST.stream()
                .filter(canBeInvoked(command))
                .collect(toList());
        if (commanders.size() == 0) {
            LOG.debug("Unknown command: '{}'. I will not respond.", message);
        } else {
            String[] parametersRaw = message.split(PARAMETER_SEPARATOR);
            String[] parameters = Arrays.copyOfRange(parametersRaw, 1, parametersRaw.length);
            if (commanders.size() > 1) {
                LOG.warn("Multiple command mapping detected, command: '{}' / mappings: {}",
                        message,
                        commanders.stream()
                                .map(ICommandExecutable::getInvocation)
                                .collect(Collectors.joining(","))
                );
            }
            commanders.forEach(commandExecutor -> commandExecutor.onCommand(channelMono, parameters));
        }
    }

    private static void respondSafely(Message onMessage, String message) {
        onMessage.getChannel()
                .blockOptional()
                .ifPresent(channel -> sendSimpleResponse(channel, message));
    }

    private static void sendSimpleResponse(MessageChannel channel, String message) {
        Mono<Message> messageMono = channel.createMessage(message);
        messageMono.block();
    }

    private static Predicate<ICommandExecutable> canBeInvoked(String invocationCommand) {
        return executor -> executor.getInvocation().equals(invocationCommand);
    }

}
