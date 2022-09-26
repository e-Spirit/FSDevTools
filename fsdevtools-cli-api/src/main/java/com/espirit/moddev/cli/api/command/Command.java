/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2022 Crownpeak Technology GmbH
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

package com.espirit.moddev.cli.api.command;

import com.espirit.moddev.cli.api.configuration.Config;
import com.espirit.moddev.cli.api.result.Result;

import de.espirit.firstspirit.access.project.ProjectScriptContext;

import java.util.concurrent.Callable;

/**
 * A generic command that represents a single action and returns a result.
 * The command's logic is implemented in the {@link Callable#call()} method.
 * Since commands are normally executed in a FirstSpirit client-server environment, support
 * for initialization is given through the implementation of
 * {@link Config#setContext(ProjectScriptContext)}.
 * The execution environment can then decide how to use the marker interface {@link Config} as indicator for
 * state initialization. With no implementation of the {@link Config} interface there is no FirstSpirit
 * connection available to the command.
 *
 * @param <RESULT_TYPE> the custom result type for this command class
 * @author e-Spirit GmbH
 */
public interface Command<RESULT_TYPE extends Result> extends Callable<RESULT_TYPE> {
}
