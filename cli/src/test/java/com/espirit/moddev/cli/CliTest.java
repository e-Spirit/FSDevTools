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
import com.espirit.moddev.cli.commands.export.ExportCommand;
import com.espirit.moddev.cli.exception.ExceptionHandler;
import com.github.rvesse.airline.builder.CliBuilder;
import com.github.rvesse.airline.model.CommandGroupMetadata;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static junit.framework.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author e-Spirit AG
 */
public class CliTest {

    /**
     * @author e-Spirit AG
     */
    public static class SimpleExecutions {
        @Rule
        public final ExpectedSystemExit exit = ExpectedSystemExit.none();

        private Cli testling;
        private transient Throwable error;

        @Before
        public void setUp() throws Exception {
            testling = new Cli();
            testling.addListener(new ExceptionHandler(testling, null));
            testling.addListener(e -> error = e.getError());
            error = null;
        }

        @Test
        public void testMain() throws Exception {
            testling.execute(new String[0]);
            assertThat("Expect a null value: " + error, error, is(nullValue()));
        }

        @Test
        public void testMainVerbose() throws Exception {
            testling.execute(new String[]{"-v", "help"});
            assertThat("Expect a null value: " + error, error, is(nullValue()));
        }

        @Test
        public void testMainError() throws Exception {
            exit.expectSystemExitWithStatus(1);
            testling.execute(new String[]{"-c bla"});
            assertThat("Expect a non-null value", error, is(notNullValue()));
            assertThat("Expect a specific value: " + error.getMessage(), error.getMessage(),
                    containsString("Value for option 'fsMode' was given as ' bla' which is not in the list of allowed values: [HTTP, SOCKET]"));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void defaultCliHasNoUndefinedGroup() {
        CliBuilder<Command> builder = Cli.getDefaultCliBuilder();
        assertNull(builder.getGroup("non-existing-group"));
    }
    @Test
    public void defaultCliHasAllGroups() {
        CliBuilder<Command> builder = Cli.getDefaultCliBuilder();
        assertNotNull(builder.getGroup("export"));
        assertNotNull(builder.getGroup("test"));
    }
    @Test
    public void defaultCliHasHelpAsDefaultCommand() {
        CliBuilder<Command> builder = Cli.getDefaultCliBuilder();
        assertEquals(builder.build().getMetadata().getDefaultCommand().getType(), HelpCommand.class);
    }

    @Test
    public void defaultCliHasExportGroupWithCommands() {
        com.github.rvesse.airline.Cli<Command> cli = Cli.getDefaultCliBuilder().build();
        Optional<CommandGroupMetadata> group = cli.getMetadata().getCommandGroups()
                .stream()
                .filter(groupMetadata -> groupMetadata.getName().equals("export"))
                .findFirst();

        assertTrue(group.isPresent());
        assertFalse(group.get().getDescription().isEmpty());
        List<String> commands = group.get().getCommands().stream().map(command -> command.getName()).collect(Collectors.toList());

        boolean hasExportCommand = commands.stream()
                .anyMatch(commandName -> commandName.equals("all"));
        assertTrue(hasExportCommand);

        boolean hasTemplateStoreCommand = commands.stream()
                .anyMatch(commandName -> commandName.equals("templatestore"));
        assertTrue(hasTemplateStoreCommand);

        boolean hasMediaStoreCommand = commands.stream()
                .anyMatch(commandName -> commandName.equals("mediastore"));
        assertTrue(hasMediaStoreCommand);

        boolean hasProjectPropertiesCommand = commands.stream()
                .anyMatch(commandName -> commandName.equals("projectproperties"));
        assertTrue(hasProjectPropertiesCommand);

        assertEquals(ExportCommand.class, group.get().getDefaultCommand().getType());
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
