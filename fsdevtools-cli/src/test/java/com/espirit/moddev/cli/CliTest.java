/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2016 e-Spirit AG
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
import com.espirit.moddev.cli.commands.HelpCommand;
import com.github.rvesse.airline.builder.CliBuilder;
import com.github.rvesse.airline.model.CommandGroupMetadata;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class CliTest {

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    @Test
    public void testDefaultConstructor() {
        new Cli();
    }

    @Test
    public void testMainFromCommandLine() throws Exception {
        exit.expectSystemExitWithStatus(0);
        Cli.main(new String[]{"help"});
    }

    @Test
    public void testMainFromCommandLineWithException() throws Exception {
        exit.expectSystemExitWithStatus(1);
        Cli.main(new String[]{"throwexception"});
    }

    @Test(expected = Exception.class)
    public void testMainCalledProgrammatically() throws Exception {
        new Cli().execute(new String[]{"throwexception"});
    }

    @Test(expected = IllegalArgumentException.class)
    public void defaultCliHasNoUndefinedGroup() {
        CliBuilder<Command> builder = Cli.getDefaultCliBuilder();
        assertNull(builder.getGroup("non-existing-group"));
    }
    @Test
    public void defaultCliHasAllGroups() {
        CliBuilder<Command> builder = Cli.getDefaultCliBuilder();
        assertNotNull(builder.getGroup("test"));
    }
    @Test
    public void defaultCliHasHelpAsDefaultCommand() {
        CliBuilder<Command> builder = Cli.getDefaultCliBuilder();
        assertEquals(builder.build().getMetadata().getDefaultCommand().getType(), HelpCommand.class);
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

}
