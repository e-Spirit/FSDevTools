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

import com.espirit.moddev.cli.api.command.Command;
import com.espirit.moddev.cli.api.result.Result;
import com.espirit.moddev.cli.configuration.GlobalConfig;
import com.espirit.moddev.cli.CliContextImpl;
import com.espirit.moddev.cli.results.SimpleResult;


/**
 * Command for project availability testing. Requiring a project specific broker leads to a successful or unsuccessful result.
 *
 * @author e-Spirit AG
 */
@com.github.rvesse.airline.annotations.Command(name = "project", groupNames = {"test"}, description = "Testing if a FirstSpirit project can be queried successfully")
public class TestProjectCommand extends GlobalConfig implements Command {

    @Override
    public Result call() {
        try(CliContextImpl cliContext = new CliContextImpl(this)) {
            return new TestResult(this);
        } catch (Exception e) {
            return new TestResult(this, e);
        }
    }

    /**
     * Specialization of {@link com.espirit.moddev.cli.results.SimpleResult} that can be used in conjunction with test project commands.
     *
     * @author e-Spirit AG
     */
    public class TestResult extends SimpleResult {

        private final GlobalConfig config;

        /**
         * Creates a new instance using the given config and exception.
         *
         * @param config Config that was used for command execution
         * @param e      Optional exception that was produced by the command
         * @throws java.lang.IllegalArgumentException if config is null
         */
        public TestResult(GlobalConfig config, Exception e) {
            super(e);
            validateConfig(config);
            this.config = config;
        }

        /**
         * Creates a new instance using the given config and exception.
         *
         * @param config Config that was used for config execution
         * @throws java.lang.IllegalArgumentException if config is null
         */
        public TestResult(GlobalConfig config) {
            super();
            validateConfig(config);
            this.config = config;
        }

        @Override
        public void log() {
            LOGGER.info("#########");
            if(isError()) {
                LOGGER.info("Test was not successful");
                LOGGER.error(exception.getMessage());
                if (config.isError()) {
                    LOGGER.error("", exception);
                }
            } else {
                LOGGER.info("Test was successful");
            }
            LOGGER.info("#########");
            LOGGER.info("User: " + config.getUser());
            LOGGER.info("Host: " + config.getHost());
            LOGGER.info("Port: " + config.getPort());
            LOGGER.info("Connection Mode: " + config.getConnectionMode());
            LOGGER.info("Project: " + config.getProject());
        }


        private void validateConfig(GlobalConfig command) {
            if(command == null) {
                throw new IllegalArgumentException("Command shouldn't be null");
            }
        }
    }
}
