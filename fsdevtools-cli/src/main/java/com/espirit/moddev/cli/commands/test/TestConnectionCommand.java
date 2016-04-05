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

import com.espirit.moddev.cli.CliContextImpl;
import com.espirit.moddev.cli.api.command.Command;
import com.espirit.moddev.cli.api.result.Result;
import com.espirit.moddev.cli.configuration.GlobalConfig;
import com.espirit.moddev.cli.results.TestResult;


/**
 * Command for connection testing. Creating a connection to FirstSpirit leads to a successful or unsuccessful result.
 *
 * @author e-Spirit AG
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
}
