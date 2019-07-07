package de.untenrechts.dev.dcmcbot.commands;

import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class CommandRouterTest {

    @Test
    void test() {
        String command = "help";
        List<ICommandExecutable> commanders = CommandContainer.COMMAND_LIST.stream()
                .filter(commanderIt -> commanderIt.getInvocation().equals(command))
                .collect(toList());
        assert commanders.size() == 1;
    }
}
