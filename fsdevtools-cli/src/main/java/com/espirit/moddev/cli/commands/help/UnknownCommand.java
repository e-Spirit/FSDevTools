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

package com.espirit.moddev.cli.commands.help;

import com.espirit.moddev.cli.api.command.Command;
import com.github.rvesse.airline.annotations.Arguments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * This command is the default command.
 *
 * @author e-Spirit AG
 */
@com.github.rvesse.airline.annotations.Command(name = "UnknownCommand", hidden = true)
public class UnknownCommand implements Command<HelpResult> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnknownCommand.class);

    @Arguments
    protected List<String> args = new ArrayList<>();

    @Override
    public HelpResult call() throws Exception {
        callStandardHelpCommand();
        // enforce exit code 1 with error
        final String unknownCommand = args.stream().reduce((t, u) -> t + " " + u).orElse("");
        final Exception error = new UnknownCommandException(unknownCommand);
        return new HelpResult(error);
    }

    private static void callStandardHelpCommand() {
        LOGGER.info("\nSee help for more information:\n");
        final HelpCommand helpCommand = new HelpCommand();
        helpCommand.call();
    }
}
