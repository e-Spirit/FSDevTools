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

package com.espirit.moddev.cli.testgroups.reflectiontest;

import com.espirit.moddev.cli.commands.help.DefaultCommand;
import com.github.rvesse.airline.annotations.Group;

/**
 * @author e-Spirit AG
 */
@Group(name = "command_with_description_after_convention", defaultCommand = DefaultCommand.class)
public final class GroupWithDescriptionMethod {
    public static String getDescription() {
        return "abc";
    }
}
