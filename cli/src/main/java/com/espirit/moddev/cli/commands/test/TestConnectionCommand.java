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

package com.espirit.moddev.cli.commands.test;

import com.espirit.moddev.cli.configuration.GlobalConfig;
import com.espirit.moddev.cli.api.command.Command;
import com.espirit.moddev.cli.api.result.Result;
import com.espirit.moddev.cli.CliContextImpl;
import com.espirit.moddev.cli.results.SimpleResult;


/**
 * Command for connection testing. Creating a connection to FirstSpirit leads to a successful or unsuccessful result.
 */
@com.github.rvesse.airline.annotations.Command(name = "connection", groupNames = {"test"}, description = "Testing a FirstSpirit connection")
public class TestConnectionCommand extends GlobalConfig implements Command {

    @Override
    public Result call() {
        try(CliContextImpl cliContext = new CliContextImpl(this)) {
            return new TestResult(this);
        } catch (Exception e) {
            return new TestResult(this, e);
        }
    }

    public class TestResult extends SimpleResult {

        private final GlobalConfig command;

        public TestResult(GlobalConfig command, Exception e) {
            super(e);
            validateCommand(command);
            this.command = command;
        }

        public TestResult(GlobalConfig command) {
            super();
            validateCommand(command);
            this.command = command;
        }

        @Override
        public void log() {
            LOGGER.info("#########");
            if(isError()) {
                LOGGER.info("Test was not successful");
                LOGGER.error(exception.getMessage());
                if(command.isError()) {
                    LOGGER.error("", exception);
                }
            } else {
                LOGGER.info("Test was successful");
            }
            LOGGER.info("#########");
            LOGGER.info("User: " + command.getUser());
            LOGGER.info("Password: " + command.getPassword());
            LOGGER.info("Host: " + command.getHost());
            LOGGER.info("Port: " + command.getPort());
            LOGGER.info("Connection Mode: " + command.getConnectionMode());
        }


        private void validateCommand(GlobalConfig command) {
            if(command == null) {
                throw new IllegalArgumentException("Command shouldn't be null");
            }
        }

    }
}
