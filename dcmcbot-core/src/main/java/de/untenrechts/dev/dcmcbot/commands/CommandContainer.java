package de.untenrechts.dev.dcmcbot.commands;

import de.untenrechts.dev.dcmcbot.commands.executors.HelpCommand;
import de.untenrechts.dev.dcmcbot.commands.executors.MinecraftCommand;

import java.util.Arrays;
import java.util.List;

public class CommandContainer {

    public static final List<ICommandExecutable> COMMAND_LIST = Arrays.asList(
            new HelpCommand(),
            new MinecraftCommand()
    );

}
