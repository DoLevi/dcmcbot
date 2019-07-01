package de.untenrechts.dev.dcmcbot.commands;

import discord4j.core.object.entity.MessageChannel;
import reactor.core.publisher.Mono;

public interface ICommandExecutable {


    String getInvocation();

    // TODO: 27.06.2019 implement and make displayable with parameterized help command
//    String getDescription();

    String[] getParameters();

    void onCommand(Mono<MessageChannel> channelMono, String[] parameters);

}
