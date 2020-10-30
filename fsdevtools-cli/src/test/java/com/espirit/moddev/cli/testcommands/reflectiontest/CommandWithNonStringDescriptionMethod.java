/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2020 e-Spirit AG
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

package com.espirit.moddev.cli.testcommands.reflectiontest;

import com.espirit.moddev.cli.api.command.Command;
import com.espirit.moddev.cli.api.annotations.Description;

/**
 * @author e-Spirit AG
 */
@com.github.rvesse.airline.annotations.Command(name = "command_with_non_string_description_method")
public final class CommandWithNonStringDescriptionMethod implements Command {
    @Description
    public static StringBuilder getDescription() { return new StringBuilder("1234"); }

    @Override
    public Object call() throws Exception {
        return null;
    }
}
