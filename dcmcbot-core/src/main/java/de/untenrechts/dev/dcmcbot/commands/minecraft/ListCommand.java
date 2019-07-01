package de.untenrechts.dev.dcmcbot.commands.minecraft;

import de.untenrechts.dev.dcmcbot.commands.ICommandExecutable;
import discord4j.core.object.entity.MessageChannel;
import reactor.core.publisher.Mono;

public class ListCommand implements ICommandExecutable {

    @Override
    public String getInvocation() {
        return null;
    }

    @Override
    public String[] getParameters() {
        return new String[0];
    }

    @Override
    public void onCommand(Mono<MessageChannel> channelMono, String[] parameters) {

    }

}
