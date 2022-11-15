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

package com.espirit.moddev.cli.commands.module.common;

import com.espirit.moddev.shared.StringUtils;
import com.espirit.moddev.shared.annotation.VisibleForTesting;
import com.espirit.moddev.shared.webapp.WebAppIdentifier;
import com.espirit.moddev.shared.webapp.WebAppIdentifierParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

import static java.util.stream.Collectors.toMap;

/**
 * Parameter class for module installations, used by "module install" and the command line library.
 *
 * @see ModuleInstallationConfiguration
 * @see ModuleInstallationParametersBuilder
 */
public class ModuleInstallationParameters {

	private final String _projectName;
	private final File _fsm;
	private final Map<String, File> _serviceConfigurations;
	private final File _projectAppConfiguration;
	private final List<WebAppIdentifier> _webAppScopes;
	private final Map<WebAppIdentifier, File> _webAppConfigurations;
	private final boolean _deploy;

	/**
	 * @param projectName             the optional name of the FirstSpirit project the module's components should be installed to
	 * @param fsm                     the module file (fsm)
	 * @param serviceConfigurations   configurations for the module's services
	 * @param projectAppConfiguration configuration file for the module's project app
	 * @param webAppScopeDefinitions  scope configurations for the module's webapp
	 * @param webAppConfigurations    configurations for the module's webapps per scope
	 */
	private ModuleInstallationParameters(@Nullable final String projectName,
										 @NotNull final File fsm,
										 @Nullable final Map<String, File> serviceConfigurations,
										 @Nullable final File projectAppConfiguration,
										 @Nullable final List<WebAppIdentifier> webAppScopeDefinitions,
										 @Nullable final Map<WebAppIdentifier, File> webAppConfigurations,
										 final boolean deploy) {
		_projectName = projectName;
		_fsm = fsm;
		_serviceConfigurations = serviceConfigurations != null ? serviceConfigurations : new HashMap<>();
		_projectAppConfiguration = projectAppConfiguration;
		_webAppScopes = webAppScopeDefinitions != null ? webAppScopeDefinitions : new ArrayList<>();
		_webAppConfigurations = webAppConfigurations != null ? webAppConfigurations : new HashMap<>();
		_deploy = deploy;
	}

	@NotNull
	public static ModuleInstallationParametersBuilder builder() {
		return new ModuleInstallationParametersBuilder();
	}

	@Nullable
	public String getProjectName() {
		return _projectName;
	}

	@NotNull
	public File getFsm() {
		return _fsm;
	}

	@NotNull
	public Map<String, File> getServiceConfigurations() {
		return _serviceConfigurations;
	}

	@NotNull
	public Optional<File> getProjectAppConfiguration() {
		return Optional.ofNullable(_projectAppConfiguration);
	}

	@NotNull
	public List<WebAppIdentifier> getWebAppScopes() {
		return Collections.unmodifiableList(_webAppScopes);
	}

	@NotNull
	public Map<WebAppIdentifier, File> getWebAppConfigurations() {
		return _webAppConfigurations;
	}

	public boolean getDeploy() {
		return _deploy;
	}

	@NotNull
	public static ModuleInstallationParameters forConfiguration(@NotNull final ModuleInstallationConfiguration configuration) {
		return ModuleInstallationParameters.builder()
				.fsm(configuration.getFsm())
				.projectName(configuration.getModuleProjectName())
				.webAppScopes(String.join(",", configuration.getWebAppScopes()))
				.deploy(configuration.getDeploy())
				.projectAppConfigurationFile(configuration.getProjectAppConfigurationFile())
				.webAppConfigurationFiles(String.join(",", configuration.getWebAppConfigurationFiles()))
				.serviceConfigurationFiles(String.join(",", configuration.getServiceConfigurationFiles()))
				.build();
	}

	public static class ModuleInstallationParametersBuilder {
		private String _webAppConfigurationFiles;
		private String _webAppScopes;
		private String _projectAppConfigurationFile;
		private String _serviceConfigurationFiles;
		private String _fsm;
		private String _projectName;
		private String _deploy;

		ModuleInstallationParametersBuilder() {
		}

		/**
		 * This method creates a new instance of {@link ModuleInstallationParameters} based on all set parameters.
		 *
		 * @return an instance of {@link ModuleInstallationParameters}
		 */
		@NotNull
		public ModuleInstallationParameters build() {
			final File firstSpiritModule = new File(_fsm);
			if (!firstSpiritModule.isFile() || !firstSpiritModule.exists()) {
				throw new IllegalArgumentException("Could not find .fsm file: " + firstSpiritModule.getPath());
			}

			final List<WebAppIdentifier> splittedWebAppScopes = new WebAppIdentifierParser().extractWebScopes(_webAppScopes);
			final File projectAppConfigFile = createAndValidateOptionalProjectAppConfigurationFile(_projectAppConfigurationFile);
			final Map<String, File> configurationFileForServiceName = getAndValidateStringFilesMap(_serviceConfigurationFiles);
			final Map<WebAppIdentifier, File> webAppConfigurationFilesForWebScopes = getAndValidateWebScopeFileMap(_webAppConfigurationFiles);
			return new ModuleInstallationParameters(_projectName, firstSpiritModule, configurationFileForServiceName, projectAppConfigFile, splittedWebAppScopes, webAppConfigurationFilesForWebScopes, shouldDeploy());
		}

		public boolean shouldDeploy() {
			// we need to check against "false" because the web apps should be deployed by default
			return !"false".equalsIgnoreCase(_deploy);
		}

		@VisibleForTesting
		@Nullable
		File createAndValidateOptionalProjectAppConfigurationFile(@Nullable final String projectAppConfigurationFile) {
			final File result = createOptionalProjectAppConfigurationFile(projectAppConfigurationFile);
			if (result != null && (!result.isFile() || !result.exists())) {
				throw new IllegalArgumentException("Project app configuration file doesn't exist or is not a file!");
			}
			return result;
		}

		@VisibleForTesting
		@Nullable
		public File createOptionalProjectAppConfigurationFile(@Nullable final String projectAppConfigurationFile) {
			return Optional.ofNullable(projectAppConfigurationFile)
					.map(File::new)
					.orElse(null);
		}

		@NotNull
		private Map<WebAppIdentifier, File> getAndValidateWebScopeFileMap(@Nullable final String webAppConfigurationFiles) {
			final Map<WebAppIdentifier, File> webScopeFileMap = getWebScopeFileMap(webAppConfigurationFiles);
			validateWebScopeFileMap(webScopeFileMap);
			return webScopeFileMap;
		}

		private void validateWebScopeFileMap(@NotNull final Map<WebAppIdentifier, File> webScopeFileMap) {
			for (Map.Entry<WebAppIdentifier, File> entry : webScopeFileMap.entrySet()) {
				if (!entry.getValue().isFile() || !entry.getValue().exists()) {
					throw new IllegalArgumentException("File for webapp configuration with scope " + entry.getKey() + " doesn't exist or is not a file.");
				}
			}
		}

		@NotNull
		public Map<WebAppIdentifier, File> getWebScopeFileMap(@Nullable final String webAppConfigurationFiles) {
			final Set<Map.Entry<String, File>> entries = getStringFilesMap(webAppConfigurationFiles).entrySet();
			return entries.stream().collect(
					toMap(entry -> new WebAppIdentifierParser().parseSingle(entry.getKey()), Map.Entry::getValue));
		}

		@NotNull
		public Map<String, File> getStringFilesMap(@Nullable final String configurations) {
			if (StringUtils.isNullOrEmpty(configurations)) {
				return new HashMap<>();
			}
			return Arrays.stream(configurations.split(","))
					.map(propertyString -> propertyString.split("="))
					.collect(toMap(entry -> entry[0], entry -> new File(entry[1])));
		}

		@NotNull
		private Map<String, File> getAndValidateStringFilesMap(@Nullable final String configurations) {
			final Map<String, File> result = getStringFilesMap(configurations);
			for (Map.Entry<String, File> entry : result.entrySet()) {
				if (!entry.getValue().exists() || !entry.getValue().isFile()) {
					throw new IllegalArgumentException("File doesn't exist for key " + entry.getKey());
				}
			}
			return result;
		}

		@NotNull
		public ModuleInstallationParametersBuilder webAppConfigurationFiles(@NotNull final String webAppConfigurationFiles) {
			_webAppConfigurationFiles = webAppConfigurationFiles;
			return this;
		}

		@NotNull
		public ModuleInstallationParametersBuilder webAppScopes(String webAppScopes) {
			_webAppScopes = webAppScopes;
			return this;
		}

		@NotNull
		public ModuleInstallationParametersBuilder projectAppConfigurationFile(String projectAppConfigurationFile) {
			_projectAppConfigurationFile = projectAppConfigurationFile;
			return this;
		}

		@NotNull
		public ModuleInstallationParametersBuilder serviceConfigurationFiles(String serviceConfigurationFile) {
			_serviceConfigurationFiles = serviceConfigurationFile;
			return this;
		}

		@NotNull
		public ModuleInstallationParametersBuilder fsm(String fsm) {
			_fsm = fsm;
			return this;
		}

		@NotNull
		public ModuleInstallationParametersBuilder projectName(String projectName) {
			_projectName = projectName;
			return this;
		}

		@NotNull
		public ModuleInstallationParametersBuilder deploy(String deploy) {
			_deploy = deploy;
			return this;
		}

		@Override
		public String toString() {
			return new StringJoiner(", ", ModuleInstallationParametersBuilder.class.getSimpleName() + "[", "]")
					.add("webAppConfigurationFiles='" + _webAppConfigurationFiles + "'")
					.add("webAppScopes='" + _webAppScopes + "'")
					.add("projectAppConfigurationFile='" + _projectAppConfigurationFile + "'")
					.add("serviceConfigurationFiles='" + _serviceConfigurationFiles + "'")
					.add("fsm='" + _fsm + "'")
					.add("projectName='" + _projectName + "'")
					.add("deploy='" + _deploy + "'")
					.toString();
		}
	}
}
