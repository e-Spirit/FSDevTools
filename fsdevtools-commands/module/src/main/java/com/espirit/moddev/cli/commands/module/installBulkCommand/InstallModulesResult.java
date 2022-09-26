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

package com.espirit.moddev.cli.commands.module.installBulkCommand;

import com.espirit.moddev.cli.commands.module.installCommand.InstallModuleResult;
import com.espirit.moddev.cli.results.SimpleResult;
import com.espirit.moddev.shared.exception.MultiException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Specialization of the generic {@link SimpleResult} class that holds a list of {@link InstallModulesResult multiple InstallModulesResults}.
 *
 * @author e-Spirit GmbH
 * @see InstallModuleResult
 */
public class InstallModulesResult extends SimpleResult<List<InstallModuleResult>> {

	public InstallModulesResult(final List<InstallModuleResult> result) {
		this(result, null);
	}

	public InstallModulesResult(final MultiException exception) {
		this(null, exception);
	}

	public InstallModulesResult(final List<InstallModuleResult> result, final MultiException exception) {
		super(result, exception);
	}

	@Override
	public void log() {
		if (isError()) {
			LOGGER.error("There was an error installing the modules.");
		} else {
			final List<InstallModuleResult> installedModules = getModules();
			if (!installedModules.isEmpty()) {
				LOGGER.info("The following modules have been installed successfully:" + buildModulesList(installedModules));
			}
		}
	}

	private List<InstallModuleResult> getModules() {
		return get().stream().filter(installModuleResult -> !installModuleResult.isError()).collect(Collectors.toList());
	}

	private String buildModulesList(final List<InstallModuleResult> modules) {
		final StringBuilder builder = new StringBuilder();
		modules.iterator().forEachRemaining(result -> {
			builder.append("\n - ");
			builder.append(result.getModuleName());
		});
		return builder.toString();
	}

}
