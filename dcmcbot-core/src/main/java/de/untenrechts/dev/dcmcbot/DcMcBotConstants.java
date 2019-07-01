package de.untenrechts.dev.dcmcbot;

import de.untenrechts.dev.dcmcbot.commands.ICommandExecutable;
import de.untenrechts.dev.dcmcbot.commands.bot.HelpCommand;
import de.untenrechts.dev.dcmcbot.commands.minecraft.ListCommand;
import de.untenrechts.dev.dcmcbot.config.DcMcBotConfigHandler;

import java.util.HashMap;
import java.util.Map;

public class DcMcBotConstants {

    public static final String CONFIGURATION_PARAM_NAME = "dcmcbot.configurationFile";
    public static final String BOT_FURL = "https://lmgtfy.com/?s=b&q=%s";


    public static final String COMMAND_PREFIX = "!";
    public static final String PARAMETER_SEPARATOR = " ";
    public static final String HELP_COMMAND_PREFIX = "help";
    public static final String MINECRAFT_COMMAND_PREFIX = "mc";
    public static final String MC_LIST_COMMAND = "list";

    public static final Map<String, ICommandExecutable> COMMAND_MAP
            = new HashMap<String, ICommandExecutable>() {{
                put(HELP_COMMAND_PREFIX, new HelpCommand());
                put(MINECRAFT_COMMAND_PREFIX + MC_LIST_COMMAND, new ListCommand());
        }};

}
