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

package com.espirit.moddev.cli.testcommands;

import com.espirit.moddev.cli.Cli;
import com.espirit.moddev.cli.api.command.Command;
import com.espirit.moddev.cli.api.result.Result;
import com.espirit.moddev.cli.commands.HelpCommand;
import com.github.rvesse.airline.model.CommandGroupMetadata;
import com.github.rvesse.airline.model.CommandMetadata;
import com.github.rvesse.airline.model.GlobalMetadata;
import junit.framework.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class HelpCommandTest {

    @Test
    public void testCommandIsParsedCorrectly() {
        String[] args = new String[] {"help"};
        Command command = Cli.parseCommandLine(args);
        Assert.assertTrue(command instanceof HelpCommand);
    }

    @Test
    public void testCommandIsExecutedCorrectly() throws Exception {
        String[] args = new String[] {"help"};
        Command<Result> command = Cli.parseCommandLine(args);
        Result result = command.call();
        Assert.assertFalse(result.isError());
    }

    @Test
    public void testCommandIsExecutedCorrectlyWithArgument() throws Exception {
        String[] args = new String[] {"help", "export"};
        Command<Result> command = Cli.parseCommandLine(args);
        Result result = command.call();
        Assert.assertFalse(result.isError());
    }

    /**
     * This is meant as an example of how to use the built-in help class,
     * in case you want to expand the HelpCommand class.
     * @throws Exception
     */
    private void testCommandPrintsCommands() throws Exception {
        String[] args = new String[] {"help"};
        Command<Result> command = Cli.parseCommandLine(args);

        Result result = command.call();

        if(result instanceof HelpCommand.HelpResult) {
            HelpCommand.HelpResult helpResult = (HelpCommand.HelpResult) result;
            List<String> commands = new ArrayList<>();

            GlobalMetadata globalMetadata = helpResult.get();
            for(Object group : globalMetadata.getCommandGroups()) {
                CommandGroupMetadata groupMetadata = (CommandGroupMetadata) group;
                for(CommandMetadata data : groupMetadata.getCommands()) {
                    commands.add(data.getName());
                }
            }
            for(String commandName : commands) {
                System.out.println(commandName);
            }
        }
    }
}
