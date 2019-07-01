package de.untenrechts.dev.dcmcbot.commands;

import de.untenrechts.dev.dcmcbot.commands.bot.HelpCommand;
import de.untenrechts.dev.dcmcbot.commands.minecraft.ListCommand;

import java.util.Arrays;
import java.util.List;

public class CommandContainer {

    public static final List<ICommandExecutable> COMMAND_LIST = Arrays.asList(
            new HelpCommand(),
            new ListCommand()
    );

}
