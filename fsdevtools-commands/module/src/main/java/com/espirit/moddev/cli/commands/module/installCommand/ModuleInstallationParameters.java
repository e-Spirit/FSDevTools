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

import com.espirit.moddev.shared.StringUtils;
import com.espirit.moddev.shared.annotation.VisibleForTesting;
import com.espirit.moddev.shared.webapp.WebAppIdentifier;
import com.espirit.moddev.shared.webapp.WebAppIdentifierParser;
import de.espirit.common.tools.Strings;

import java.io.File;
import java.util.*;

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
	 * Instantiates a parameters object and uses empty configurations for services, project apps and webapps.
	 *
	 * @param projectName the name of the FirstSpirit project the module's components should be installed to
	 * @param fsm         the module file (fsm)
	 * @deprecated please use the {@link ModuleInstallationParametersBuilder} instead.
	 */
	@Deprecated
	public ModuleInstallationParameters(String projectName, File fsm) {
		this(projectName, fsm, new HashMap(), null, new ArrayList<>(), new HashMap<>(), true);
	}

	/**
	 * @param projectName             the optional name of the FirstSpirit project the module's components should be installed to
	 * @param fsm                     the module file (fsm)
	 * @param serviceConfigurations   configurations for the module's services
	 * @param projectAppConfiguration configuration file for the module's project app
	 * @param webAppScopeDefinitions  scope configurations for the module's webapp
	 * @param webAppConfigurations    configurations for the module's webapps per scope
	 */
	private ModuleInstallationParameters(String projectName, File fsm, Map<String, File> serviceConfigurations, File projectAppConfiguration, List<WebAppIdentifier> webAppScopeDefinitions, Map<WebAppIdentifier, File> webAppConfigurations, boolean deploy) {
		_projectName = projectName;
		_fsm = fsm;
		_serviceConfigurations = serviceConfigurations != null ? serviceConfigurations : new HashMap<>();
		_projectAppConfiguration = projectAppConfiguration;
		_webAppScopes = webAppScopeDefinitions != null ? webAppScopeDefinitions : new ArrayList<>();
		_webAppConfigurations = webAppConfigurations != null ? webAppConfigurations : new HashMap<>();
		_deploy = deploy;
	}

	public static ModuleInstallationParametersBuilder builder() {
		return new ModuleInstallationParametersBuilder();
	}

	public String getProjectName() {
		return _projectName;
	}

	public File getFsm() {
		return _fsm;
	}

	public Map<String, File> getServiceConfigurations() {
		return _serviceConfigurations;
	}

	public Optional<File> getProjectAppConfiguration() {
		return Optional.ofNullable(_projectAppConfiguration);
	}

	public List<WebAppIdentifier> getWebAppScopes() {
		return Collections.unmodifiableList(_webAppScopes);
	}

	public Map<WebAppIdentifier, File> getWebAppConfigurations() {
		return _webAppConfigurations;
	}

	public boolean getDeploy() {
		return _deploy;
	}

	public static ModuleInstallationParameters forConfiguration(final ModuleInstallationConfiguration configuration) {
		return ModuleInstallationParameters.builder()
				.fsm(configuration.getFsm())
				.projectName(configuration.getModuleProjectName())
				.webAppScopes(Strings.implode(configuration.getWebAppScopes(), ","))
				.deploy(configuration.getDeploy())
				.projectAppConfigurationFile(configuration.getProjectAppConfigurationFile())
				.webAppConfigurationFiles(Strings.implode(configuration.getWebAppConfigurationFiles(), ","))
				.serviceConfigurationFiles(Strings.implode(configuration.getServiceConfigurationFiles(), ","))
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
		 * This method creates a new instance of @see com.espirit.moddev.moduleinstaller.ModuleInstallationParameters
		 * based on all set parameters.
		 *
		 * @return an instance of @see com.espirit.moddev.moduleinstaller.ModuleInstallationParameters
		 */
		public ModuleInstallationParameters build() {
			File firstSpiritModule = new File(_fsm);
			if (!firstSpiritModule.isFile() || !firstSpiritModule.exists()) {
				throw new IllegalArgumentException("Could not open .fsm file: " + firstSpiritModule.getPath());
			}

			List<WebAppIdentifier> splittedWebAppScopes = new WebAppIdentifierParser().extractWebScopes(_webAppScopes);
			File projectAppConfigFile = createAndValidateOptionalProjectAppConfigurationFile(_projectAppConfigurationFile);
			Map<String, File> configurationFileForServiceName = getAndValidateStringFilesMap(_serviceConfigurationFiles);
			Map<WebAppIdentifier, File> webAppConfigurationFilesForWebScopes = getAndValidateWebScopeFileMap(_webAppConfigurationFiles);
			return new ModuleInstallationParameters(_projectName, firstSpiritModule, configurationFileForServiceName, projectAppConfigFile, splittedWebAppScopes, webAppConfigurationFilesForWebScopes, shouldDeploy());
		}

		public boolean shouldDeploy() {
			// we need to check against "false" because the web apps should be deployed by default
			return !"false".equalsIgnoreCase(_deploy);
		}

		@VisibleForTesting
		File createAndValidateOptionalProjectAppConfigurationFile(String projectAppConfigurationFile) {
			File result = createOptionalProjectAppConfigurationFile(projectAppConfigurationFile);
			if (result != null && (!result.isFile() || !result.exists())) {
				throw new IllegalArgumentException("Project app configuration file doesn't exist or is not a file!");
			}
			return result;
		}

		public File createOptionalProjectAppConfigurationFile(String projectAppConfigurationFile) {
			return Optional.ofNullable(projectAppConfigurationFile)
					.map(File::new)
					.orElse(null);
		}

		private Map<WebAppIdentifier, File> getAndValidateWebScopeFileMap(String webAppConfigurationFiles) {
			Map<WebAppIdentifier, File> webScopeFileMap = getWebScopeFileMap(webAppConfigurationFiles);
			validateWebScopeFileMap(webScopeFileMap);
			return webScopeFileMap;
		}

		private void validateWebScopeFileMap(Map<WebAppIdentifier, File> webScopeFileMap) {
			for (Map.Entry<WebAppIdentifier, File> entry : webScopeFileMap.entrySet()) {
				if (!entry.getValue().isFile() || !entry.getValue().exists()) {
					throw new IllegalArgumentException("File for webapp configuration with scope " + entry.getKey() + " doesn't exist or is not a file.");
				}
			}
		}

		public Map<WebAppIdentifier, File> getWebScopeFileMap(String webAppConfigurationFiles) {
			Set<Map.Entry<String, File>> entries = getStringFilesMap(webAppConfigurationFiles).entrySet();
			return entries.stream().collect(
					toMap(entry -> new WebAppIdentifierParser().parseSingle(entry.getKey()), Map.Entry::getValue));
		}

		public Map<String, File> getStringFilesMap(String webAppConfigurations) {
			if (StringUtils.isNullOrEmpty(webAppConfigurations)) {
				return new HashMap<>();
			}
			return Arrays.stream(webAppConfigurations.split(","))
					.map(propertyString -> propertyString.split("="))
					.collect(toMap(entry -> entry[0], entry -> new File(entry[1])));
		}

		private Map<String, File> getAndValidateStringFilesMap(String configurations) {
			Map<String, File> result = getStringFilesMap(configurations);
			for (Map.Entry<String, File> entry : result.entrySet()) {
				if (!entry.getValue().exists() || !entry.getValue().isFile()) {
					throw new IllegalArgumentException("File doesn't exist for key " + entry.getKey());
				}
			}
			return result;
		}

		public ModuleInstallationParametersBuilder webAppConfigurationFiles(String webAppConfigurationFiles) {
			_webAppConfigurationFiles = webAppConfigurationFiles;
			return this;
		}

		public ModuleInstallationParametersBuilder webAppScopes(String webAppScopes) {
			_webAppScopes = webAppScopes;
			return this;
		}

		public ModuleInstallationParametersBuilder projectAppConfigurationFile(String projectAppConfigurationFile) {
			_projectAppConfigurationFile = projectAppConfigurationFile;
			return this;
		}

		public ModuleInstallationParametersBuilder serviceConfigurationFiles(String serviceConfigurationFile) {
			_serviceConfigurationFiles = serviceConfigurationFile;
			return this;
		}

		public ModuleInstallationParametersBuilder fsm(String fsm) {
			_fsm = fsm;
			return this;
		}

		public ModuleInstallationParametersBuilder projectName(String projectName) {
			_projectName = projectName;
			return this;
		}

		public ModuleInstallationParametersBuilder deploy(String deploy) {
			_deploy = deploy;
			return this;
		}

		public String toString() {
			return "ModuleInstallationRawParameters.ModuleInstallationRawParametersBuilder(webAppConfigurationFiles=" + _webAppConfigurationFiles + ", webAppScopes=" + _webAppScopes + ", projectAppConfigurationFile=" + _projectAppConfigurationFile + ", serviceConfigurationFiles=" + _serviceConfigurationFiles + ", fsm=" + _fsm + ", projectName=" + _projectName + ")";
		}
	}
}
