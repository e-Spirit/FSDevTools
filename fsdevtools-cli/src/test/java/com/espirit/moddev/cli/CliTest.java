/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2021 e-Spirit GmbH
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * *********************************************************************
 *
 */

package com.espirit.moddev.cli;

import com.espirit.moddev.cli.api.command.Command;
import com.espirit.moddev.cli.commands.help.DefaultCommand;
import com.espirit.moddev.cli.commands.help.HelpCommand;
import com.espirit.moddev.cli.configuration.GlobalConfig;
import com.github.rvesse.airline.builder.CliBuilder;
import com.github.rvesse.airline.model.CommandGroupMetadata;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;


public class CliTest {

    @Test
    public void testMainCalledProgrammatically() throws Exception {
        assertThrows(Exception.class, () -> new Cli().execute(new String[]{"throwexception"}));
    }

    @Test
    public void defaultCliHasNoUndefinedGroup() {
        assertThrows(IllegalArgumentException.class, () -> {
            CliBuilder<Command> builder = Cli.getDefaultCliBuilder();
            assertNull(builder.getGroup("non-existing-group"));
        });
    }

    @Test
    public void defaultCliHasAllGroups() {
        CliBuilder<Command> builder = Cli.getDefaultCliBuilder();
        assertNotNull(builder.getGroup("test"));
    }

    @Test
    public void defaultCliHasHelpAsDefaultCommand() {
        CliBuilder<Command> builder = Cli.getDefaultCliBuilder();
        assertEquals(builder.build().getMetadata().getDefaultCommand().getType(), DefaultCommand.class);
    }

    @Test
    public void defaultCliHasTestGroupWithCommands() {
        com.github.rvesse.airline.Cli<Command> cli = Cli.getDefaultCliBuilder().build();
        Optional<CommandGroupMetadata> group = cli.getMetadata().getCommandGroups()
                .stream()
                .filter(groupMetadata -> groupMetadata.getName().equals("test"))
                .findFirst();

        assertTrue(group.isPresent());
        assertFalse(group.get().getDescription().isEmpty());

        List<String> commands = group.get().getCommands().stream().map(command -> command.getName()).collect(Collectors.toList());
        boolean hasTestConnectionCommand = commands.stream()
                .anyMatch(commandName -> commandName.equals("connection"));
        assertTrue(hasTestConnectionCommand);
    }

    @Test
    public void getResultFile_non_config_instance() {
        assertNull(Cli.getResultFile((Command) new HelpCommand()));
    }

    @Test
    public void getResultFile() {
        final String resultFileName = "testResultFile.json";
        assertEquals(resultFileName, Cli.getResultFile(new TestCommand(resultFileName)).getName());
    }

    @Test
    public void getCommandIdentifier_no_group_name() {
        assertEquals("testCommand2", Cli.getCommandIdentifier(new TestCommand2()));
    }

    @Test
    public void getCommandIdentifier_with_group_name() {
        assertEquals("testGroup testCommand1", Cli.getCommandIdentifier(new TestCommand("ignore")));
    }

    @Test
    public void getCommandIdentifier_annotation_not_present() {
        assertThrows(IllegalStateException.class, () -> Cli.getCommandIdentifier(new TestCommand_without_annotation()));
    }

    @com.github.rvesse.airline.annotations.Command(groupNames = "testGroup", name = "testCommand1")
    private static class TestCommand extends GlobalConfig implements Command {

        public TestCommand(@NotNull final String resultFileName) {
            getEnvironment().put(CliConstants.KEY_RESULT_FILE.value(), resultFileName);
        }

        @Override
        public Object call() throws Exception {
            return Void.class;
        }

    }

    @com.github.rvesse.airline.annotations.Command(name = "testCommand2")
    private static class TestCommand2 extends GlobalConfig implements Command {

        @Override
        public Object call() throws Exception {
            return Void.class;
        }

    }

    private static class TestCommand_without_annotation extends GlobalConfig implements Command {

        @Override
        public Object call() throws Exception {
            return Void.class;
        }

    }
}
