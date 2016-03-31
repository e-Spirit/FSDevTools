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

import com.espirit.moddev.cli.api.command.Command;
import com.espirit.moddev.cli.api.result.Result;
import com.espirit.moddev.cli.configuration.GlobalConfig;

/**
 * A simple template for a base command implementation, that can be further extended as needed.
 * Provides convenience access to a FirstSpirit context, that can be initialized in an
 * execution environment.
 *
 * @param <RESULT_TYPE> the result type that is returned when a command of this class is called
 * @author e-Spirit AG
 */
public abstract class SimpleCommand<RESULT_TYPE extends Result> extends GlobalConfig implements Command<RESULT_TYPE> {
    @Override
    public abstract RESULT_TYPE call();


}
