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

package com.espirit.moddev.cli.commands;

import com.espirit.moddev.cli.Cli;
import com.espirit.moddev.cli.results.SimpleResult;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.builder.CliBuilder;
import com.github.rvesse.airline.help.Help;
import com.github.rvesse.airline.model.GlobalMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * This command uses airline's builtin help function to retrieve information for all
 * known commands. This implementation uses the default commands and groups via the
 * Cli class.
 */
@Command(
        name = HelpCommand.COMMAND_NAME,
        description = "Display help information"
)
public class HelpCommand implements com.espirit.moddev.cli.api.command.Command<HelpCommand.HelpResult> {
    private static final Logger LOGGER = LoggerFactory.getLogger(HelpCommand.class);

    public static final String COMMAND_NAME = "help";
    final CliBuilder<Callable> builder = com.github.rvesse.airline.Cli.<Callable>builder("fs-cli");

    @Arguments
    List<String> args = new ArrayList<>();

    @Override
    public HelpResult call() {
        Help help;
        builder.withDefaultCommand(Help.class);
        Cli.buildCallableCommandGroups(builder);
        com.github.rvesse.airline.Cli<Callable> cli = builder.build();

        ArrayList<String> argsCopy = new ArrayList<>();
        argsCopy.add(COMMAND_NAME);
        argsCopy.addAll(args);
        String[] argumentArray = argsCopy.toArray(new String[0]);
        help = (Help) cli.parse(argumentArray);
        try {
            help.help(help.global, args);
            return new HelpResult(help.global);
        } catch (IOException e) {
            LOGGER.debug("Help command execution caused an exception", e);
            return new HelpResult(e);
        }
    }


    public class HelpResult extends SimpleResult<GlobalMetadata<Object>> {
        public HelpResult(GlobalMetadata metadata) {
            super(metadata);
        }
        public HelpResult(Exception exception) {
            super(exception);
        }
    }
}
