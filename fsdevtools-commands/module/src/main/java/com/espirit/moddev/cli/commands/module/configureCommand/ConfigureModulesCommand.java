/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2020 e-Spirit AG
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

import com.espirit.moddev.cli.ConnectionBuilder;
import com.espirit.moddev.cli.api.result.ExecutionErrorResult;
import com.espirit.moddev.cli.api.result.ExecutionResult;
import com.espirit.moddev.cli.api.result.ExecutionResults;
import com.espirit.moddev.cli.commands.SimpleCommand;
import com.espirit.moddev.cli.commands.module.ModuleCommandGroup;
import com.espirit.moddev.cli.commands.module.ModuleCommandNames;
import com.espirit.moddev.cli.commands.module.configureCommand.json.ModuleConfiguration;
import com.espirit.moddev.cli.commands.module.configureCommand.json.components.ComponentWebApps;
import com.espirit.moddev.cli.commands.module.configureCommand.json.components.ConfigurationContext;
import com.espirit.moddev.cli.commands.module.utils.ModuleUtil;
import com.espirit.moddev.cli.commands.module.utils.WebAppUtil;
import com.espirit.moddev.shared.StringUtils;
import com.espirit.moddev.shared.annotation.VisibleForTesting;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.help.Examples;
import com.github.rvesse.airline.annotations.restrictions.PathKind;
import com.github.rvesse.airline.annotations.restrictions.Required;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.agency.ModuleAdminAgent;
import de.espirit.firstspirit.agency.WebAppId;
import de.espirit.firstspirit.common.MaximumNumberOfSessionsExceededException;
import de.espirit.firstspirit.module.descriptor.AbstractDescriptor;
import de.espirit.firstspirit.module.descriptor.ModuleDescriptor;
import de.espirit.firstspirit.server.authentication.AuthenticationException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Command(groupNames = ModuleCommandGroup.NAME, name = ModuleCommandNames.CONFIGURE, description = "Configures a FirstSpirit module on a FirstSpirit Server.")
@Examples(
		examples = {
				"module configure -mcf \"folder/configFile.json\"",
				"simple example configFile.json:",
				"extended/complex example configFile.json"
		},
		descriptions = {
				"Configures the modules with the given configuration file.",
				"[\n" +
						"\t{\n" +
						"\t\t\"moduleName\": \"myModuleName\",\n" +
						"\t\t\"components\": {\n" +
						"\t\t\t\"webComponents\": [\n" +
						"\t\t\t\t{\n" +
						"\t\t\t\t\t\"componentName\": \"myProjectWebComponentName\",\n" +
						"\t\t\t\t\t\"webApps\": [\n" +
						"\t\t\t\t\t\t{\n" +
						"\t\t\t\t\t\t\t\"webAppName\": \"WEBEDIT\",\n" +
						"\t\t\t\t\t\t}\n" +
						"\t\t\t\t\t]\n" +
						"\t\t\t\t}\n" +
						"\t\t\t]\n" +
						"\t\t}\n" +
						"\t}\n" +
						"]",
				"[\n" +
						"\t{\n" +
						"\t\t\"moduleName\": \"myFirstModule\",\n" +
						"\t\t\"components\": {\n" +
						"\t\t\t\"webComponents\": [\n" +
						"\t\t\t\t{\n" +
						"\t\t\t\t\t\"componentName\": \"myGlobalWebComponentName\",\n" +
						"\t\t\t\t\t\"webApps\": [\n" +
						"\t\t\t\t\t\t{\n" +
						"\t\t\t\t\t\t\t\"webAppName\": \"global(my_global_web_app_name)\",\n" +
						"\t\t\t\t\t\t\t\"deploy\": false\n" +
						"\t\t\t\t\t\t}\n" +
						"\t\t\t\t\t]\n" +
						"\t\t\t\t}\n" +
						"\t\t\t],\n" +
						"\t\t\t\"services\": [\n" +
						"\t\t\t\t{\n" +
						"\t\t\t\t\t\"serviceName\": \"myServiceName\",\n" +
						"\t\t\t\t\t\"autoStart\": true,\n" +
						"\t\t\t\t}\n" +
						"\t\t\t]\n" +
						"\t\t}\n" +
						"\t},\n" +
						"\t{\n" +
						"\t\t\"moduleName\": \"mySecondModuleName\",\n" +
						"\t\t\"components\": {\n" +
						"\t\t\t\"projectComponents\": [\n" +
						"\t\t\t\t{\n" +
						"\t\t\t\t\t\"componentName\": \"myProjectComponentName\",\n" +
						"\t\t\t\t\t\"projectApps\": [\n" +
						"\t\t\t\t\t\t{\n" +
						"\t\t\t\t\t\t\t\"projectName\": \"myProjectName\",\n" +
						"\t\t\t\t\t\t\t\"files\": [\"path/to/file01.json\", \"path/to/directory\"],\n" +
						"\t\t\t\t\t\t}\n" +
						"\t\t\t\t\t]\n" +
						"\t\t\t\t}\n" +
						"\t\t\t]\n" +
						"\t\t}\n" +
						"\t}\n" +
						"]"
		}
)
public class ConfigureModulesCommand extends SimpleCommand<ConfigureModulesCommandResult> {

	protected static final Logger LOGGER = LoggerFactory.getLogger(ConfigureModulesCommand.class);

	@Option(type = OptionType.COMMAND, name = {"-mcf", "--moduleConfigFile"}, description = "Path to the configuration json file", title = "configFile")
	@com.github.rvesse.airline.annotations.restrictions.Path(mustExist = true, kind = PathKind.FILE, writable = false)
	@Required
	private String _configFile;

	@Override
	public ConfigureModulesCommandResult call() {
		try (final Connection connection = ConnectionBuilder.with(this).build()) {
			connection.connect();
			return new ConfigureModulesCommandResult(execute(new ConfigurationContext(connection, this), ModuleConfiguration.fromPath(_configFile)));
		} catch (final FileNotFoundException e) {
			return new ConfigureModulesCommandResult(e);
		} catch (final IOException | AuthenticationException | MaximumNumberOfSessionsExceededException e) {
			return new ConfigureModulesCommandResult(new IllegalStateException("Unable to connect to FirstSpirit server.", e));
		} catch (final Exception exception) {
			return new ConfigureModulesCommandResult(exception);
		}
	}

	@VisibleForTesting
	@NotNull
	ExecutionResults execute(@NotNull final ConfigurationContext context, @NotNull final Collection<ModuleConfiguration> moduleConfigurations) {
		// configure modules
		final ExecutionResults configureResults = configureModules(context, moduleConfigurations);
		// deploy web apps
		final ExecutionResults deployResults = WebAppUtil.deployWebApps(context.getConnection(), extractWebAppsToDeploy(configureResults));
		// create final result list
		final ExecutionResults results = new ExecutionResults();
		results.add(configureResults);
		results.add(deployResults);
		return results;
	}

	@VisibleForTesting
	@NotNull
	ExecutionResults configureModules(@NotNull final ConfigurationContext context, @NotNull final Collection<ModuleConfiguration> moduleConfigurations) {
		if (moduleConfigurations.isEmpty()) {
			throw new IllegalStateException("No module configurations defined.");
		}

		LOGGER.info("Found {} module " + StringUtils.toPluralRespectingString(moduleConfigurations.size(), "configuration") + ". Processing...", moduleConfigurations.size());
		final ExecutionResults results = new ExecutionResults();
		for (final ModuleConfiguration moduleConfiguration : moduleConfigurations) {
			results.add(configureModule(context, moduleConfiguration));
		}
		return results;
	}

	@VisibleForTesting
	@NotNull
	ExecutionResult configureModule(@NotNull final ConfigurationContext context, @NotNull final ModuleConfiguration moduleConfiguration) {
		final String moduleName = moduleConfiguration.getModuleName();
		LOGGER.debug("Processing module '{}'...", moduleName);
		final ModuleAdminAgent moduleAdminAgent = context.getConnection().getBroker().requireSpecialist(ModuleAdminAgent.TYPE);
		Optional<ModuleDescriptor> optionalModule = ModuleUtil.getModuleByName(moduleAdminAgent, moduleName);
		if (!optionalModule.isPresent()) {
			LOGGER.debug("Module with name '{}' not found, using display names for lookup...", moduleName);
			final List<ModuleDescriptor> modulesByDisplayName = ModuleUtil.getModulesByDisplayName(moduleAdminAgent, moduleName);
			if (modulesByDisplayName.size() == 1) {
				final ModuleDescriptor firstModule = modulesByDisplayName.get(0);
				LOGGER.debug("Found module '{}' by using display name '{}'.", firstModule.getModuleName(), firstModule.getDisplayName());
				optionalModule = Optional.of(firstModule);
			} else if (modulesByDisplayName.size() > 1) {
				LOGGER.debug("Multiple modules with display name '{}' found!", moduleName);
				return new MultipleModulesFoundResult(moduleName, modulesByDisplayName);
			}
		}
		if (!optionalModule.isPresent()) {
			LOGGER.debug("Module '{}' not found!", moduleName);
			return new ModuleNotFoundResult(moduleName);
		}
		return moduleConfiguration.configure(context, optionalModule.get());
	}

	/**
	 * Extracts all {@link WebAppId web apps} that should be deployed from the given {@link ExecutionResults}.
	 * All {@link ComponentWebApps.WebComponentResult} in the result list will be collected if {@link ComponentWebApps.WebComponentResult#getDeploy()} returns <code>true</code>.
	 * Each {@link WebAppId web app} is only contained <b>once</b> in the returned {@link Collection collection}.
	 *
	 * @param results the {@link ExecutionResults} to extract the {@link WebAppId web apps} from
	 * @return a {@link Collection} containing {@link WebAppId web apps}
	 */
	@VisibleForTesting
	@NotNull
	static Collection<WebAppId> extractWebAppsToDeploy(@NotNull final ExecutionResults results) {
		//@formatter:off
		return results
				.stream()																		// stream
				.filter(result -> {        														// only WebComponentResults with #getDeploy == true
					if (!(result instanceof ComponentWebApps.WebComponentResult)) {
						return false;
					}
					return ((ComponentWebApps.WebComponentResult) result).getDeploy();
				})
				.distinct()                    													// distinct WebComponentResults (each WebApp is only included once, because WebApp#equals is correctly implemented)
				.map(result -> ((ComponentWebApps.WebComponentResult) result).getWebAppId())    // map to WebAppId
				.collect(Collectors.toList()); 													// map to list
		//@formatter:on
	}

	@VisibleForTesting
	static class ModuleNotFoundResult implements ExecutionErrorResult<IllegalStateException> {

		private static final String MESSAGE = "Module '%s' not found!";

		private final String _moduleName;

		public ModuleNotFoundResult(@NotNull final String moduleName) {
			_moduleName = moduleName;
		}

		@NotNull
		@Override
		public IllegalStateException getException() {
			return new IllegalStateException(toString());
		}

		@Override
		public String toString() {
			return String.format(MESSAGE, _moduleName);
		}

	}

	@VisibleForTesting
	static class MultipleModulesFoundResult implements ExecutionErrorResult<IllegalStateException> {

		private static final String MESSAGE = "Multiple modules with display name '%s' found: %s";

		private final String _moduleName;
		private final String _moduleNames;

		public MultipleModulesFoundResult(@NotNull final String moduleName, @NotNull final List<ModuleDescriptor> modules) {
			_moduleName = moduleName;
			_moduleNames = modules
					.stream()
					.map(AbstractDescriptor::getModuleName)
					.collect(Collectors.joining(", ", "[", "]"));
		}

		@NotNull
		@Override
		public IllegalStateException getException() {
			return new IllegalStateException(toString());
		}

		@Override
		public String toString() {
			return String.format(MESSAGE, _moduleName, _moduleNames);
		}

	}

}
