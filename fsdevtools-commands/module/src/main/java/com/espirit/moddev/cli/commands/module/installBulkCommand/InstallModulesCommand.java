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

package com.espirit.moddev.cli.commands.module.installBulkCommand;

import com.espirit.moddev.cli.ConnectionBuilder;
import com.espirit.moddev.cli.api.annotations.ParameterExamples;
import com.espirit.moddev.cli.api.result.AbstractCommandResult;
import com.espirit.moddev.cli.api.result.ExecutionResults;
import com.espirit.moddev.cli.commands.SimpleCommand;
import com.espirit.moddev.cli.commands.module.ModuleCommandGroup;
import com.espirit.moddev.cli.commands.module.ModuleCommandNames;
import com.espirit.moddev.cli.commands.module.common.ModuleInstallationConfiguration;
import com.espirit.moddev.cli.commands.module.common.ModuleInstallationParameters;
import com.espirit.moddev.cli.commands.module.installCommand.InstallModuleCommandResult;
import com.espirit.moddev.cli.commands.module.utils.ModuleInstallationResult;
import com.espirit.moddev.cli.commands.module.utils.ModuleInstaller;
import com.espirit.moddev.cli.commands.module.utils.WebAppUtil;
import com.espirit.moddev.shared.exception.MultiException;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.help.Examples;
import com.github.rvesse.airline.annotations.restrictions.Once;
import com.github.rvesse.airline.annotations.restrictions.PathKind;
import com.github.rvesse.airline.annotations.restrictions.Required;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.agency.ModuleAdminAgent;
import de.espirit.firstspirit.agency.WebAppId;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Installs a set of modules on a FirstSpirit server. The configuration of this command is done by a config file in
 * json format.
 * Already installed modules will be updated (with its usages).
 */
@Command(name = ModuleCommandNames.INSTALL_BULK, groupNames = ModuleCommandGroup.NAME, description = "Installs a FirstSpirit module into a FirstSpirit Server. If a given component is already installed, it is updated.")
@Examples(
		examples = {
				"module installBulk -mcf \"folder/configFile.json\"",
				"module installBulk -mcf \"myConfigFile.json\" --deployWebApps false",
				"[\n" +
						"\t{\n" +
						"\t\t\"fsm\": \"H:\\\\path\\\\fs-saml-login-1.1.fsm\"\n" +
						"\t},\n" +
						"\t{\n" +
						"\t\t\"fsm\": \"C:\\\\path\\\\fs-tpp-api-1.2.11-SNAPSHOT.fsm\",\n" +
						"\t\t\"moduleProjectName\": \"Mithras\",\n" +
						"\t\t\"webAppScopes\" : [ \"webedit\", \"global(fs5root)\" ]\n" +
						"\t}\n" +
						"]"
		},
		descriptions = {
				"Installs the modules of the given configuration file and deploys all related webapps.",
				"Installs the modules of the given configuration file but does not deploy the related webapps.",
				"Example configFile.json:"
		}
)
public class InstallModulesCommand extends SimpleCommand<InstallModulesCommandResult> {

	protected static final Logger LOGGER = LoggerFactory.getLogger(InstallModulesCommand.class);

	@Option(type = OptionType.COMMAND, name = {"-mcf", "--moduleConfigFile"}, description = "Path to the configuration json file", title = "configFile")
	@com.github.rvesse.airline.annotations.restrictions.Path(mustExist = true, kind = PathKind.FILE, writable = false)
	@Required
	@ParameterExamples(
			examples = {
					"-mcf \"path/to/file.json\"",
					"--moduleConfigFile \"C:/path/to/file.json\"",
			},
			descriptions = {
					"Sets the config file to `path/to/file.json`.",
					"Sets the config file to `C:/path/to/file.json`.",
			}
	)
	private String _configFile;

	@Option(arity = 1, type = OptionType.COMMAND, name = {"-dwa", "--deployWebApps"}, description = "Define whether all related web apps of the modules should be immediately deployed after the installation or not [true = deploy (default) | false = no deploy]", title = "deployWebApps")
	@Once
	@ParameterExamples(
			examples = {
					"-dwa true",
					"--deployWebApps false"
			},
			descriptions = {
					"Sets the flag to `true`.",
					"Sets the flag to `false`.",
			}
	)
	private boolean _deploy = true;

	@Override
	public InstallModulesCommandResult call() {
		try (final Connection connection = ConnectionBuilder.with(this).build()) {
			connection.connect();
			return bulkInstall(connection);
		} catch (final Throwable throwable) {
			return new InstallModulesCommandResult(new MultiException(throwable.getMessage(), Collections.singletonList(throwable)));
		}
	}

	@NotNull
	private InstallModulesCommandResult bulkInstall(@NotNull final Connection connection) throws IOException {
		final List<ModuleInstallationConfiguration> configurations = ModuleInstallationConfiguration.fromFile(_configFile);
		final List<ModuleInstallationParameters> installationParameters = new ArrayList<>();

		if (configurations.isEmpty()) {
			throw new IllegalArgumentException("File '" + _configFile + "' must contain at least one configuration.");
		}

		// verify the config file and build parameters for all configurations
		final ArrayList<Throwable> parameterErrorResults = verifyAndCreateParameters(connection, configurations, installationParameters);

		// we got at least one error --> return here without installing anything
		if (!parameterErrorResults.isEmpty()) {
			return new InstallModulesCommandResult(new MultiException("Error verifying module configurations.", parameterErrorResults));
		}

		// finally: install modules, deploy and return the result
		return installModulesAndDeploy(connection, installationParameters);
	}

	@NotNull
	private ArrayList<Throwable> verifyAndCreateParameters(@NotNull final Connection connection,
														   @NotNull final List<ModuleInstallationConfiguration> configurations,
														   @NotNull final List<ModuleInstallationParameters> installationParameters) {
		final ArrayList<Throwable> results = new ArrayList<>();
		for (final ModuleInstallationConfiguration config : configurations) {
			try {
				// override deploy parameter for a single module, because we don't want to deploy all web apps for each
				// installed module.
				config.setDeploy(String.valueOf(false));
				// verify the configuration
				config.verify(connection);
				// create parameters object
				installationParameters.add(ModuleInstallationParameters.forConfiguration(config));
			} catch (final Throwable throwable) {
				results.add(throwable);
			}
		}
		return results;
	}

	/**
	 * Installs the modules for all given {@link ModuleInstallationParameters parameters}.
	 * <p>
	 * What happens in this method:
	 * - iterate over each parameter-set
	 * - install the module, based on the parameter-set
	 * --> fetch the result:
	 * * if the installation was successful: simply add the result of the installation to the list of results
	 * * if the installation failed: add an error-result to the list of results
	 * - return the list of results
	 * <p>
	 * This way we make sure that we collect all installation results, independent of the installation-result of a single module
	 *
	 * @param connection the connection to use for the installation
	 * @param parameters the list of {@link ModuleInstallationParameters module parameters} used for the installation
	 * @return a {@link List list} of {@link InstallModulesCommandResult results} containing results for {@code all} installation processes.
	 */
	@NotNull
	private InstallModulesCommandResult installModulesAndDeploy(@NotNull final Connection connection, @NotNull final List<ModuleInstallationParameters> parameters) {
		final ArrayList<InstallModuleCommandResult> results = new ArrayList<>();
		final Set<WebAppId> overallUpdatedWebApps = new HashSet<>();
		final ModuleInstaller moduleInstaller = new ModuleInstaller(connection);
		for (final ModuleInstallationParameters singleParameter : parameters) {
			try {
				if (!connection.isConnected()) {
					throw new IllegalStateException("Connection is null or not connected!");
				}
				LOGGER.info(AbstractCommandResult.LINE_SEPARATOR);
				final InstallModuleCommandResult singleInstallResult = moduleInstaller.installModule(singleParameter);
				final ModuleInstallationResult installationResult = singleInstallResult.getInstallationResult();
				if (installationResult != null) {
					final ModuleAdminAgent.ModuleResult moduleResult = installationResult.getModuleResult();
					final ArrayList<WebAppId> updatedWebApps = new ArrayList<>(moduleResult.getUpdatedWebApps());
					overallUpdatedWebApps.addAll(updatedWebApps);
					results.add(singleInstallResult);
				} else {
					results.add(singleInstallResult);
				}
			} catch (final Throwable throwable) {
				results.add(new InstallModuleCommandResult(singleParameter.getFsm().getAbsolutePath(), throwable));
			}
		}

		// deploy related web apps
		final ExecutionResults executionResults = new ExecutionResults();
		results.forEach(installModuleCommandResult -> executionResults.add(installModuleCommandResult.get()));
		if (_deploy) {
			final ExecutionResults deployExecutionResults = WebAppUtil.deployWebApps(connection, overallUpdatedWebApps);
			deployExecutionResults.stream().forEach(executionResults::add);
		}
		return new InstallModulesCommandResult(executionResults);
	}

	@Override
	public boolean needsContext() {
		return false;
	}

}
