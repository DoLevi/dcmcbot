package de.untenrechts.dev.dcmcbot;

public class DcMcBotConstants {

    static final String CONFIGURATION_PARAM_NAME = "dcmcbot.configurationFile";
    public static final String BOT_FURL = "https://lmgtfy.com/?s=b&q=%s";

    public static final String COMMAND_PREFIX = "!";
    public static final String PARAMETER_SEPARATOR = " ";
    public static final int HELP_COMMAND_PAD_SIZE = 16;

    public static final String TMUX_BASH_COMMAND = "lib/execute-mc-command.sh";
    public enum IoMode {
        SEND_WITH_READ, SEND_ONLY
    }
    public enum TmuxCommand {
        GET("get"), SEND("send");

        private final String value;
        TmuxCommand(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

}
