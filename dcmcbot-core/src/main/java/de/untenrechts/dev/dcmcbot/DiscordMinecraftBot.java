package de.untenrechts.dev.dcmcbot;

import de.untenrechts.dev.dcmcbot.commands.CommandRouter;
import de.untenrechts.dev.dcmcbot.config.DcMcBotConfigHandler;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.MessageUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Optional;

public class DiscordMinecraftBot {

    private static final Logger LOG = LoggerFactory.getLogger(DiscordMinecraftBot.class);

    private final DiscordClient discordClient;


    public static void main(String[] args) {
        LOG.info("Starting application ...");
        String configPath = System.getProperty(DcMcBotConstants.CONFIGURATION_PARAM_NAME);
        Optional<Reader> configOptional = getConfigFileReader(configPath);
        if (configOptional.isPresent()) {
            DcMcBotConfigHandler.initialize(configOptional.get());
            DiscordMinecraftBot bot = new DiscordMinecraftBot();
            bot.discordClient.login().block();
        }
        LOG.info("Exiting application ...");
    }

    private static Optional<Reader> getConfigFileReader(String configPath) {
        try {
            LOG.debug("Reading in configuration file: {}", configPath);
            return Optional.of(new FileReader(new File(configPath)));
        } catch (FileNotFoundException e) {
            LOG.error("Config file: {} could not be found.", configPath);
            return Optional.empty();
        }
    }

    private DiscordMinecraftBot() {
        LOG.debug("Constructing {} ...", DiscordMinecraftBot.class.getSimpleName());
        String token = DcMcBotConfigHandler.getConfig().getToken();
        discordClient = new DiscordClientBuilder(token).build();
        discordClient.getEventDispatcher().on(ReadyEvent.class)
                .subscribe(CommandRouter::onReady);
        discordClient.getEventDispatcher().on(MessageCreateEvent.class)
                .subscribe(CommandRouter::onNewMessage);
        discordClient.getEventDispatcher().on(MessageUpdateEvent.class)
                .subscribe(CommandRouter::onUpdatedMessage);
    }

}
