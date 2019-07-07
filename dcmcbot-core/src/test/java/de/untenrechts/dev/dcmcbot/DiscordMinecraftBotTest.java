package de.untenrechts.dev.dcmcbot;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class DiscordMinecraftBotTest {

    private static final Logger LOG = LoggerFactory.getLogger(DiscordMinecraftBotTest.class);

    // TODO: 30.06.2019 write tests
    @Test
    void launchClientTest() {
        LOG.info("CP: {}", DiscordMinecraftBotTest.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        LOG.info("LOG4J2 config found: {}", new File("src/test/resources/log4j2-test.xml").isFile());
        LOG.info("Test");
    }
}
