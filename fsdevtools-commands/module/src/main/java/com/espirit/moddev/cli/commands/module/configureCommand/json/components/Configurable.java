/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2021 e-Spirit AG
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

package com.espirit.moddev.cli.commands.module.configureCommand.json.components;

import com.espirit.moddev.cli.api.result.ExecutionResult;
import com.espirit.moddev.cli.api.result.ExecutionResults;
import com.espirit.moddev.cli.commands.module.configureCommand.ConfigureModulesCommand;
import de.espirit.firstspirit.module.descriptor.ModuleDescriptor;
import org.jetbrains.annotations.NotNull;

/**
 * Interface representing one single step when configuring a module via {@link ConfigureModulesCommand}.
 */
public interface Configurable {

	/**
	 * Configures a part of the given {@link ModuleDescriptor module} and returns an {@link ExecutionResult}.
	 * <br/>
	 * Implementations of this method should always configure one single part of the module or wrap a set of {@link Configurable configurables}.
	 * It is recommended to return an instance of {@link ExecutionResults} when wrapping a set of configurables.
	 *
	 * @param context the {@link ConfigurationContext} for this Configurable
	 * @param module  the {@link ModuleDescriptor module} to configure
	 * @return a new {@link ExecutionResult}
	 */
	@NotNull
	ExecutionResult configure(@NotNull final ConfigurationContext context, @NotNull final ModuleDescriptor module);

}
