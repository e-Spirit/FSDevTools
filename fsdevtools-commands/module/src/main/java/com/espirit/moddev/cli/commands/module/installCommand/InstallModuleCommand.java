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

package com.espirit.moddev.cli.commands.module.installCommand;

import com.espirit.moddev.cli.ConnectionBuilder;
import com.espirit.moddev.cli.commands.SimpleCommand;
import com.espirit.moddev.cli.commands.module.ModuleCommandNames;
import com.espirit.moddev.cli.commands.module.ModuleCommandGroup;
import com.espirit.moddev.cli.commands.module.utils.ModuleInstaller;
import com.espirit.moddev.shared.annotation.VisibleForTesting;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.help.Examples;
import com.github.rvesse.airline.annotations.restrictions.Path;
import com.github.rvesse.airline.annotations.restrictions.PathKind;
import com.github.rvesse.airline.annotations.restrictions.Required;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.agency.ModuleAdminAgent;
import de.espirit.firstspirit.common.MaximumNumberOfSessionsExceededException;
import de.espirit.firstspirit.server.authentication.AuthenticationException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.espirit.moddev.shared.StringUtils.isNullOrEmpty;

/**
 * Installs a module on a FirstSpirit server. Provides mechanisms to configure project apps, webapps
 * and corresponding scopes. If a given component is already installed, it is updated.
 */
@Command(name = ModuleCommandNames.INSTALL, groupNames = ModuleCommandGroup.NAME, description = "Installs a FirstSpirit module into a FirstSpirit Server. If a given component is already installed, it is updated.")
@Examples(
		examples = {
				"module install -mpn \"Mithras Energy\" -fsm \"folder\\videomanagementpro.fsm\" -pacf \"resources\\projectApp.ini\" -scf\n" +
						"\"VideoManagementProService=folder\\videomanagementpro_service.ini\" -wacf \"preview=resources\\previewAppConfig.ini\""
		},
		descriptions = {"Installs the videomanagementpro module with a given project app configuration and configures the VideoManagementProService with the given ini file."
		}
)
public class InstallModuleCommand extends SimpleCommand<InstallModuleResult> {

	protected static final Logger LOGGER = LoggerFactory.getLogger(InstallModuleCommand.class);

	@Option(type = OptionType.COMMAND, name = {"-fsm", "--fsm"}, description = "Path to the module fsm file that should be installed", title = "moduleFile")
	@Path(mustExist = true, kind = PathKind.FILE, writable = false)
	@Required
	private String _fsm;

	@Option(type = OptionType.COMMAND, name = {"-mpn", "--moduleProjectName"}, description = "Name of the FirstSpirit target project where the application's components should be installed to.", title = "moduleProjectName")
	private String _projectName;

	@Option(type = OptionType.COMMAND, name = {"-scf", "--serviceConfigurationFiles"}, description = "Define a map-like configuration file for services of the given module - comma-separated value pairs with service name and configuration path file.", title = "serviceConfigurationFiles")
	private String _serviceConfigurationsFiles;
	@Option(type = OptionType.COMMAND, name = {"-pacf", "--projectAppConfigurationFile"}, description = "Configuration file path for project app", title = "projectAppConfigurationFile")
	private String _projectAppConfigurationFile;
	@Option(type = OptionType.COMMAND, name = {"-was", "--webAppScopes"}, description = "Define a map-like configuration for webapp scopes of the given module - comma-separated values from the FirstSpirit WebScope enum."
			+ " The FS WebScope enum contains the following keys:\n"
			+ "'GLOBAL',\n"
			+ "'LIVE',\n"
			+ "'PREVIEW',\n"
			+ "'STAGING',\n"
			+ "'WEBEDIT'.\n"
			+ "For global webapps, use 'global(WebAppId)'.")
	private String _webAppScopes;
	@Option(type = OptionType.COMMAND, name = {"-wacf", "--webAppConfigurationFiles"}, description = "Define a map-like configuration for the webapps of the given module - with comma-separated key-values.", title = "webAppConfigurationFiles")
	private String _webAppConfigurationFiles;
	@Option(type = OptionType.COMMAND, name = {"-dwa", "--deployWebApps"}, description = "Define whether all related webapps of the module should be immediately deployed after the installation or not [true = deploy (default) | false = no deploy]", title = "deployWebApps")
	private boolean _deploy = true;

	@Override
	public InstallModuleResult call() {
		try (Connection connection = create()) {
			connection.connect();
			return installModule(connection);
		} catch (IOException | AuthenticationException | MaximumNumberOfSessionsExceededException | IllegalArgumentException e) {
			return new InstallModuleResult(_fsm, e);
		}
	}

	@VisibleForTesting
	public void setFsm(@NotNull final String fsm) {
		_fsm = fsm;
	}

	@NotNull
	private InstallModuleResult installModule(Connection connection) throws IOException {
		String projectName = retrieveProjectNameOrFallback();

		final ModuleInstallationConfiguration configuration = new ModuleInstallationConfiguration();
		configuration.setFsm(_fsm);
		configuration.setModuleProjectName(projectName);
		configuration.setWebAppScopes(splitAndTrim(_webAppScopes));
		configuration.setDeploy(Boolean.toString(_deploy));
		configuration.setProjectAppConfigurationFile(_projectAppConfigurationFile);
		configuration.setServiceConfigurationFiles(splitAndTrim(_serviceConfigurationsFiles));
		configuration.setWebAppConfigurationFiles(splitAndTrim(_webAppConfigurationFiles));
		configuration.verify(connection);
		final ModuleInstallationParameters parameters = ModuleInstallationParameters.forConfiguration(configuration);

		final ModuleAdminAgent.ModuleResult result = new ModuleInstaller(connection).install(parameters, parameters.getDeploy());
		return new InstallModuleResult(result.getDescriptor().getModuleName());
	}

	private List<String> splitAndTrim(final String text) {
		final List<String> result = new ArrayList<>();
		if (text == null) {
			return result;
		}
		final String[] splittedText = text.split(",");
		for (String part : splittedText) {
			result.add(part.trim());
		}
		return result;
	}

	private String retrieveProjectNameOrFallback() {
		String projectName = _projectName;
		if (isNullOrEmpty(projectName)) {
			LOGGER.warn("No --moduleProjectName parameter given for module installation.");
			if (!isNullOrEmpty(getProject())) {
				projectName = getProject();
				LOGGER.warn("Using global --project parameter of value \"" + getProject() + "\"");
			} else {
				LOGGER.debug("No project name given as --moduleProjectName or --project parameter. " +
						"Going on without project, so module installation with project specific components could fail.");
			}
		}
		return projectName;
	}

	protected Connection create() {
		return ConnectionBuilder.with(this).build();
	}

	@Override
	public boolean needsContext() {
		return false;
	}

}
