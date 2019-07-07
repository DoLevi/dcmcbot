package de.untenrechts.dev.dcmcbot.commands.executors;

import de.untenrechts.dev.dcmcbot.config.DcMcBotConfigHandler;
import de.untenrechts.dev.dcmcbot.config.DiscordBotType;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static de.untenrechts.dev.dcmcbot.DcMcBotConstants.BOT_FURL;

public class CommandExecutor {
    // TODO: 02.07.2019 rename and refactor, do NOT keep this as trash bin

    private static final Logger LOG = LoggerFactory.getLogger(CommandExecutor.class);


    public static void handleMessageResponse(Consumer<MessageChannel> messageCreator,
                                             Mono<MessageChannel> channelMono) {
        MessageChannel messageChannel = channelMono.block();
        if (messageChannel == null) {
            LOG.error("Unable to unwrap MessageChannel.");
        } else {
            messageCreator.accept(messageChannel);
        }
    }

    public static void handleMessageResponse(BiConsumer<MessageChannel, String[]> messageCreator,
                                             Mono<MessageChannel> channelMono,
                                             String[] parameters) {
        MessageChannel messageChannel = channelMono.block();
        if (messageChannel == null) {
            LOG.error("Unable to unwrap MessageChannel.");
        } else {
            messageCreator.accept(messageChannel, parameters);
        }
    }

    public static void fillWithAuthor(EmbedCreateSpec embedCreateSpec) {
        DiscordBotType botConfig = DcMcBotConfigHandler.getConfig().getDiscordBot();
        embedCreateSpec.setAuthor(
                botConfig.getDisplayBotName(),
                String.format(BOT_FURL, botConfig.getDisplayBotName()),
                botConfig.getDisplayBotIconUrl());
    }

}
