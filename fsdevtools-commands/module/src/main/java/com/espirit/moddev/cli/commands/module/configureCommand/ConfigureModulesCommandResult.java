/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2024 Crownpeak Technology GmbH
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

package com.espirit.moddev.cli.commands.module.configureCommand;

import com.espirit.moddev.cli.api.result.AbstractCommandResult;
import com.espirit.moddev.cli.api.result.ExecutionResults;
import com.espirit.moddev.cli.api.result.Result;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of a {@link Result} for the {@link ConfigureModulesCommand}.
 */
public class ConfigureModulesCommandResult extends AbstractCommandResult {

	public ConfigureModulesCommandResult(@NotNull final ExecutionResults results) {
		super("Error configuring modules!", results);
	}

	public ConfigureModulesCommandResult(@NotNull final Throwable throwable) {
		super(throwable);
	}

	@Override
	public void log() {
		_logger.info("");
		_logger.info(LINE_SEPARATOR);
		if (isError()) {
			_logger.error("Module configuration completed with errors!");
		} else {
			_logger.info("Module configuration successfully completed.");
		}
		_logger.info(LINE_SEPARATOR);
		logResults();
		_logger.info(LINE_SEPARATOR);
		_logger.info("");
	}

}
