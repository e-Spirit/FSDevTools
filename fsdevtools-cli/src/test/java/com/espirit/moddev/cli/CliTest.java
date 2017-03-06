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
import com.github.rvesse.airline.builder.CliBuilder;
import com.github.rvesse.airline.model.CommandGroupMetadata;

import org.junit.Test;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author e-Spirit AG
 */
public class CliTest {

    @Test
    public void testMain() throws Exception {
        Cli.main(new String[]{"fs-cli"});
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
