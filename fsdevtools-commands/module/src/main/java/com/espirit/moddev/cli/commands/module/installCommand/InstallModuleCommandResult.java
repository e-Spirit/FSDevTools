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

package com.espirit.moddev.cli.commands.module.installCommand;

import com.espirit.moddev.cli.api.result.AbstractCommandResult;
import com.espirit.moddev.cli.api.result.ExecutionResults;
import com.espirit.moddev.cli.api.result.Result;
import com.espirit.moddev.cli.commands.module.utils.ModuleInstallationResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Implementation of a {@link Result} for the {@link InstallModuleCommand}.
 */
public class InstallModuleCommandResult extends AbstractCommandResult {

	private final String _pathToFsm;
	private final ModuleInstallationResult _installationResult;

	public InstallModuleCommandResult(@NotNull final String pathToFsm, final ModuleInstallationResult installationResult, @NotNull final ExecutionResults results) {
		super("Error installing module!", results);
		_pathToFsm = pathToFsm;
		_installationResult = installationResult;
	}

	public InstallModuleCommandResult(@NotNull final String pathToFsm, @NotNull final Throwable throwable) {
		super(throwable);
		_pathToFsm = pathToFsm;
		_installationResult = null;
	}

	@Nullable
	public ModuleInstallationResult getInstallationResult() {
		return _installationResult;
	}

	@Override
	public void log() {
		_logger.info("");
		_logger.info(LINE_SEPARATOR);
		if (isError()) {
			_logger.error(String.format("Module installation of fsm '%s' completed with errors!", _pathToFsm));
		} else {
			_logger.info(String.format("Module installation of fsm '%s' successfully completed.", _pathToFsm));
		}
		_logger.info(LINE_SEPARATOR);
		logResults();
		_logger.info("");
	}

}
