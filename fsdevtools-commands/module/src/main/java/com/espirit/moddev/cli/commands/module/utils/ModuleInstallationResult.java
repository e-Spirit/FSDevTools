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

package com.espirit.moddev.cli.commands.module.utils;

import de.espirit.firstspirit.agency.ModuleAdminAgent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ModuleInstallationResult {

	private ModuleAdminAgent.ModuleResult _moduleResult;
	private List<ProjectAppComponentResult> _installedProjectAppComponentResults;
	private List<ProjectAppComponentResult> _updatedProjectAppComponentResults;
	private List<WebAppComponentResult> _installedWebAppComponentResults;
	private List<WebAppComponentResult> _updatedWebAppComponentResults;
	private List<ServiceComponentResult> _configuredServices;

	ModuleInstallationResult() {
		// package protected constructor
	}

	void setModuleResult(@NotNull final ModuleAdminAgent.ModuleResult moduleResult) {
		_moduleResult = moduleResult;
	}

	@NotNull
	public ModuleAdminAgent.ModuleResult getModuleResult() {
		return _moduleResult;
	}

	void setConfiguredServices(@NotNull final List<ServiceComponentResult> configuredServices) {
		_configuredServices = configuredServices;
	}

	@NotNull
	public List<ServiceComponentResult> getConfiguredServices() {
		return _configuredServices;
	}

	void setInstalledProjectAppComponentResults(@NotNull final List<ProjectAppComponentResult> installedProjectAppComponentResults) {
		_installedProjectAppComponentResults = installedProjectAppComponentResults;
	}

	@NotNull
	public List<ProjectAppComponentResult> getInstalledProjectAppComponentResults() {
		return _installedProjectAppComponentResults;
	}

	void setUpdatedProjectAppComponentResults(@NotNull final List<ProjectAppComponentResult> updatedProjectAppComponentResults) {
		_updatedProjectAppComponentResults = updatedProjectAppComponentResults;
	}

	@NotNull
	public List<ProjectAppComponentResult> getUpdatedProjectAppComponentResults() {
		return _updatedProjectAppComponentResults;
	}

	void setInstalledWebAppComponentResults(@NotNull final List<WebAppComponentResult> installedWebAppComponentResults) {
		_installedWebAppComponentResults = installedWebAppComponentResults;
	}

	@NotNull
	public List<WebAppComponentResult> getInstalledWebAppComponentResults() {
		return _installedWebAppComponentResults;
	}

	void setUpdatedWebAppComponentResults(@NotNull final List<WebAppComponentResult> updatedWebAppComponentResults) {
		_updatedWebAppComponentResults = updatedWebAppComponentResults;
	}

	@NotNull
	public List<WebAppComponentResult> getUpdatedWebAppComponentResults() {
		return _updatedWebAppComponentResults;
	}

}
