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

package com.espirit.moddev.cli.commands.module.utils;

import com.espirit.moddev.cli.commands.module.installCommand.ModuleInstallationParameters;
import com.espirit.moddev.shared.StringUtils;
import com.espirit.moddev.shared.webapp.WebAppIdentifier;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.ServerConfiguration;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.access.store.LockException;
import de.espirit.firstspirit.agency.ModuleAdminAgent;
import de.espirit.firstspirit.agency.ModuleAdminAgent.ModuleResult;
import de.espirit.firstspirit.agency.WebAppId;
import de.espirit.firstspirit.io.FileHandle;
import de.espirit.firstspirit.io.FileSystem;
import de.espirit.firstspirit.module.descriptor.ComponentDescriptor;
import de.espirit.firstspirit.module.descriptor.ModuleDescriptor;
import de.espirit.firstspirit.module.descriptor.ProjectAppDescriptor;
import de.espirit.firstspirit.server.module.WebAppType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.espirit.moddev.shared.webapp.WebAppIdentifier.isFs5RootWebApp;
import static de.espirit.firstspirit.access.ConnectionManager.SOCKET_MODE;
import static de.espirit.firstspirit.module.descriptor.ComponentDescriptor.Type.SERVICE;
import static de.espirit.firstspirit.module.descriptor.ComponentDescriptor.Type.WEBAPP;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

/**
 * This class can install modules and module configurations.
 */
@SuppressWarnings("squid:S1200")
public class ModuleInstaller {

	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ModuleInstaller.class);

	private static boolean setActiveWebServer(final ServerConfiguration serverConfiguration, final WebAppIdentifier webScope, final Project project) {
		if (webScope.isGlobal()) {
			return true;
		}
		final String scopeName = webScope.getScope().name();
		String activeWebServer = project.getActiveWebServer(scopeName);
		if (StringUtils.isNullOrEmpty(project.getActiveWebServer(scopeName))) {
			activeWebServer = serverConfiguration.getActiveWebserverConfiguration(WebAppType.FS5ROOT.getId());
			if (StringUtils.isNullOrEmpty(activeWebServer)) {
				LOGGER.warn("Project has no active web server. Using default webserver of global root.");
			} else {
				LOGGER.warn("Project has no active web server. Using webserver '" + activeWebServer + "' of global root.");
			}
			try {
				project.lock();
				project.setActiveWebServer(scopeName, activeWebServer);
				project.save();
			} catch (LockException e) {
				LOGGER.error("Cannot lock and save project!", e);
				return false;
			} finally {
				LOGGER.debug("Unlocking project");
				project.unlock();
			}
		} else {
			LOGGER.info("'{}' already has an active web server for scope '{}'. Active web server is: {}", project.getName(), scopeName, activeWebServer);
		}
		return true;
	}

	private final Connection _connection;
	private ModuleAdminAgent _moduleAdminAgent;

	/**
	 * Instantiates a {@link ModuleInstaller}. Doesn't do anything else.
	 */
	public ModuleInstaller(final Connection connection) {
		// Nothing to do here
		_connection = connection;
	}

	private synchronized ModuleAdminAgent getModuleAdminAgent() {
		if (_moduleAdminAgent == null) {
			_moduleAdminAgent = _connection.getBroker().requireSpecialist(ModuleAdminAgent.TYPE);
		}
		return _moduleAdminAgent;
	}

	/**
	 * Method for installing a given FirstSpirit module (only the module itself will be installed, no components will be added to any project).
	 *
	 * @param fsm The path to the FirstSpirit module file (fsm) to be installed
	 * @return An InstallModuleResult. Result might be absent when there's an exception with the fsm file stream.
	 * @throws IOException may be thrown server side while installing the module
	 */
	@NotNull
	private ModuleResult installFsm(@NotNull final File fsm, final boolean deploy) throws IOException {
		LOGGER.info("Starting module installation for fsm '{}'...", fsm.getName());
		try (FileInputStream fsmStream = new FileInputStream(fsm)) {
			LOGGER.debug("Installing module in fsm '{}'...", fsm.getName());
			ModuleResult result = getModuleAdminAgent().install(fsmStream, true, deploy);
			final String moduleName = result.getDescriptor().getModuleName();
			LOGGER.debug("Module '{}' installed (fsm was '{}').", moduleName, fsm.getName());
			LOGGER.debug("Setting module '{}' as trusted...", moduleName);
			getModuleAdminAgent().setTrusted(moduleName, true);
			LOGGER.debug("Module '{}' is now trusted.", moduleName);
			LOGGER.info("Module of fsm '{}' successfully installed.", fsm.getName());
			return result;
		}
	}

	/**
	 * Method for activating auto start of services of a given module
	 *
	 * @param descriptor the module descriptor
	 * @param parameters the {@link ModuleInstallationParameters parameters} of the command
	 */
	private void activateServices(@NotNull final ModuleDescriptor descriptor, @NotNull final ModuleInstallationParameters parameters) {
		final String moduleName = descriptor.getModuleName();

		final ComponentDescriptor[] componentDescriptors = descriptor.getComponents();
		if (componentDescriptors == null) {
			LOGGER.error("No components found for module '{}'!", moduleName);
		} else {
			List<ComponentDescriptor> serviceDescriptors = stream(componentDescriptors).filter(it -> it.getType().equals(SERVICE)).collect(toList());
			if (!serviceDescriptors.isEmpty()) {
				LOGGER.info("Configuring services for module '{}'...", moduleName);
				serviceDescriptors.forEach(serviceDescriptor -> {
					LOGGER.info("Configuring service '{}:{}'...", moduleName, serviceDescriptor.getName());
					File configuration = parameters.getServiceConfigurations().get(serviceDescriptor.getName());
					if (configuration != null) {
						createConfigurationFile(SERVICE, serviceDescriptor, configuration, moduleName, parameters.getProjectName(), null);
					} else {
						LOGGER.info("No configuration file for service '{}:{}' found. Nothing to do.", moduleName, serviceDescriptor.getName());
					}
					setAutostartAndRestartService(moduleName, serviceDescriptor);
					LOGGER.info("Service '{}:{}' configured.", moduleName, serviceDescriptor.getName());
				});
			}
		}
	}

	private void setAutostartAndRestartService(@NotNull final String moduleName, @NotNull final ComponentDescriptor descriptor) {
		final ModuleAdminAgent moduleAdminAgent = getModuleAdminAgent();
		String componentDescriptorName = descriptor.getName();
		LOGGER.info("Setting autostart for service '{}:{}'", moduleName, componentDescriptorName);
		moduleAdminAgent.setAutostart(componentDescriptorName, true);
		LOGGER.debug("Autostart for service '{}:{}' set.", moduleName, componentDescriptorName);
		LOGGER.info("Stopping service '{}:{}'", moduleName, componentDescriptorName);
		moduleAdminAgent.stopService(componentDescriptorName);
		LOGGER.debug("Service service '{}:{}' stopped.", moduleName, componentDescriptorName);
		LOGGER.info("Starting service '{}:{}'", moduleName, componentDescriptorName);
		moduleAdminAgent.startService(componentDescriptorName);
		LOGGER.info("Status of service '{}:{}': {}", moduleName, componentDescriptorName, moduleAdminAgent.isRunning(componentDescriptorName) ? "RUNNING" : "STOPPED");
	}

	/**
	 * Convenience method for copying the configuration files forProjectAndScope the module to the server-dirs
	 *
	 * @param type                Type of the module whose configuration should be written e.g. Service, ProjectApp
	 * @param componentDescriptor The component forProjectAndScope the module.xml to use
	 * @param configurationFile   The map forProjectAndScope the pom.xml that includes the configuration files
	 * @param moduleName          The name of the module whose configuration should be written (nullable)
	 * @param projectName         The optional project name applications shall be installed to
	 * @param webAppId            The webAppId to use - only used by webapp configurations
	 */
	private void createConfigurationFile(final ComponentDescriptor.Type type,
										 final ComponentDescriptor componentDescriptor,
										 final File configurationFile,
										 final String moduleName,
										 final String projectName, WebAppId webAppId) {
		final String fileName = configurationFile.getName();
		LOGGER.info("Creating configuration file '{}' ( type = {} ) for '{}:{}'...", fileName, type.name(), moduleName, componentDescriptor.getName());
		FileSystem<?> fileSystem = getFileSystemForConfigurationType(type, componentDescriptor, moduleName, projectName, webAppId);
		try {
			LOGGER.debug("Fetching file '{}'...", fileName);
			FileHandle handle = fileSystem.obtain(fileName);
			LOGGER.info("Saving file to '{}'...", handle.getPath());
			handle.save(new FileInputStream(configurationFile));
			LOGGER.debug("File {} saved.", handle.getPath());
		} catch (IOException e) {
			LOGGER.error("Error uploading file!", e);
		}
		LOGGER.info("Configuration file '{}' ( type = {} ) for '{}:{}' created.", fileName, type.name(), moduleName, componentDescriptor.getName());
	}

	@NotNull
	private FileSystem<?> getFileSystemForConfigurationType(final ComponentDescriptor.Type type,
															final ComponentDescriptor componentDescriptor,
															final String moduleName,
															final String projectName,
															final WebAppId webAppId) {
		ModuleAdminAgent moduleAdminAgent = getModuleAdminAgent();
		final String componentName = componentDescriptor.getName();
		switch (type) {
			case SERVICE: {
				LOGGER.debug("Retrieving filesystem for service '{}:{}'...", moduleName, componentName);
				return moduleAdminAgent.getServiceConfig(componentName);
			}
			case PROJECTAPP: {
				LOGGER.debug("Retrieving filesystem for project app '{}:{}:{}'...", projectName, moduleName, componentName);
				Project project = safelyRetrieveProject(projectName);
				return moduleAdminAgent.getProjectAppConfig(moduleName, componentName, project);
			}
			case WEBAPP: {
				LOGGER.debug("Retrieving filesystem for webapp '{}:{}:{}'...", webAppId, moduleName, componentName);
				return moduleAdminAgent.getWebAppConfig(moduleName, componentName, webAppId);
			}
			default: {
				throw new IllegalStateException(String.format("Unknown component type '%s'!", type.name()));
			}
		}
	}

	private Project safelyRetrieveProject(final String projectName) {
		if (StringUtils.isNullOrEmpty(projectName)) {
			throw new IllegalArgumentException("No project given, can't get a project app configuration!");
		}
		Project project = _connection.getProjectByName(projectName);
		if (project == null) {
			throw new IllegalArgumentException("Cannot find project " + projectName + "!");
		}
		return project;
	}

	/**
	 * Method for installing the project applications of a given module into a given project
	 *
	 * @param descriptor the descriptor of the module whose project applications shall be installed
	 * @param parameters the {@link ModuleInstallationParameters} of the current command
	 */
	private void installProjectApps(@NotNull final ModuleDescriptor descriptor, @NotNull final ModuleInstallationParameters parameters) {
		List<ComponentDescriptor> projectAppDescriptors = stream(descriptor.getComponents()).filter(it -> it instanceof ProjectAppDescriptor).collect(toList());

		final String moduleName = descriptor.getModuleName();
		final String projectName = parameters.getProjectName();
		if (StringUtils.isNullOrEmpty(projectName)) {
			if (!projectAppDescriptors.isEmpty()) {
				LOGGER.info("Found project apps in module '{}', but cannot install project apps without a project name!", moduleName);
			}
		} else {
			if (!projectAppDescriptors.isEmpty()) {
				LOGGER.info("Installing project apps of module '{}' in project '{}'...", moduleName, projectName);
				projectAppDescriptors.forEach(projectAppDescriptor -> {
					LOGGER.info("Creating project app configuration '{}:{}' in project '{}' ...", moduleName, projectAppDescriptor.getName(), projectName);
					createProjectAppConfiguration(projectName, moduleName, projectAppDescriptor);
					LOGGER.debug("Project app configuration '{}:{}' in project '{}' created.", moduleName, projectAppDescriptor.getName(), projectName);
					LOGGER.info("Installing project app '{}:{}' in '{}'...", moduleName, projectAppDescriptor.getName(), projectName);
					Project project = safelyRetrieveProject(projectName);
					getModuleAdminAgent().installProjectApp(moduleName, projectAppDescriptor.getName(), project);
					LOGGER.debug("Project app '{}:{}' installed in '{}'.", moduleName, projectAppDescriptor.getName(), projectName);
					parameters.getProjectAppConfiguration().ifPresent(projectAppFile -> createConfigurationFile(ComponentDescriptor.Type.PROJECTAPP, projectAppDescriptor, projectAppFile, moduleName, projectName, null));
				});
			}
		}
	}

	private void createProjectAppConfiguration(final String projectName, final String moduleName, final ComponentDescriptor projectAppDescriptor) {
		FileSystem<?> projectAppConfig = null;
		try {
			projectAppConfig = getFileSystemForConfigurationType(projectAppDescriptor.getType(), projectAppDescriptor, moduleName, projectName, null);
		} catch (IllegalArgumentException e) {
			LOGGER.info("Project app configuration for '{}:{}' in project '{}' already exists - creating...", moduleName, projectAppDescriptor.getName(), projectName);
			LOGGER.debug("Exception: ", e);
		}
		if (projectAppConfig != null) {
			LOGGER.info("Project app configuration for '{}:{}' in project '{}' already exists - updating...", moduleName, projectAppDescriptor.getName(), projectName);
		}
	}

	private boolean installProjectWebAppsAndCreateConfig(@NotNull final ModuleDescriptor descriptor, @NotNull final ModuleInstallationParameters parameters) {
		final String moduleName = descriptor.getModuleName();
		final List<ComponentDescriptor> webappDescriptors = stream(descriptor.getComponents()).filter(it -> WEBAPP.equals(it.getType())).collect(toList());
		final List<WebAppIdentifier> webAppScopes = parameters.getWebAppScopes();
		final AtomicBoolean result = new AtomicBoolean(true);
		if (!webappDescriptors.isEmpty() && !webAppScopes.isEmpty()) {
			LOGGER.info("Configuring web app components for module '{}'...", moduleName);
			webappDescriptors.forEach(componentDescriptor -> {
				for (WebAppIdentifier scope : webAppScopes) {
					final String componentName = componentDescriptor.getName();
					LOGGER.info("Configuring web app component '{}:{}' in scope {}...", moduleName, componentName, scope.getScope());
					final boolean webAppAndConfigurationsCreated = createWebAppAndConfigurations(descriptor, parameters, componentDescriptor, scope);
					if (webAppAndConfigurationsCreated) {
						LOGGER.debug("Web app component '{}:{}' in scope {} configured.", moduleName, componentName, scope.getScope());
					} else {
						LOGGER.warn("Could not configure web app component '{}:{}' in scope {}...", moduleName, componentName, scope.getScope());
					}
					result.set(result.get() && webAppAndConfigurationsCreated);
				}
			});
		}
		return result.get();
	}

	/**
	 * Method for installing the web applications of a given module into a given project
	 *
	 * @param descriptor A {@link ModuleDescriptor} to describe FirstSpirit module components
	 * @param parameters parameters containing the specific entries forProjectAndScope the config files
	 * @return success indicator
	 */
	private boolean installProjectWebAppsAndDeploy(@NotNull final ModuleDescriptor descriptor, @NotNull final ModuleInstallationParameters parameters) {
		if (!installProjectWebAppsAndCreateConfig(descriptor, parameters)) {
			return false;
		}
		if (parameters.getDeploy()) {
			return deployWebAppsForScopes(descriptor, parameters);
		} else {
			return true;
		}
	}

	private boolean installWebAppAndActivateWebServer(final WebAppIdentifier webScope, final ModuleInstallationParameters parameters) {
		try {
			final String projectName = parameters.getProjectName();
			Project projectOrNull = null;
			if (!StringUtils.isNullOrEmpty(projectName)) {
				projectOrNull = _connection.getProjectByName(projectName);
			}

			LOGGER.info("Setting active webserver for project scope: {}", webScope);
			boolean activeServerForProjectSet = setActiveWebServer(_connection.getServerConfiguration(), webScope, projectOrNull);
			LOGGER.info(activeServerForProjectSet ? "Setting active webserver was successful." : "Setting active webserver wasn't successful.");
			if (!activeServerForProjectSet) {
				return false;
			}
			return deployWebApp(webScope, projectOrNull);
		} catch (IllegalStateException ise) {
			LOGGER.error("Cannot deploy war file!", ise);
			return false;
		}
	}

	private boolean createWebAppAndConfigurations(final ModuleDescriptor moduleDescriptor,
												  final ModuleInstallationParameters parameters,
												  final ComponentDescriptor componentDescriptor,
												  final WebAppIdentifier scope) {
		final String projectName = parameters.getProjectName();
		final String moduleName = moduleDescriptor.getModuleName();
		final Map<WebAppIdentifier, File> webAppConfigurations = parameters.getWebAppConfigurations();

		Project projectOrNull = StringUtils.isNullOrEmpty(projectName) ? null : _connection.getProjectByName(projectName);
		try {
			WebAppId id = scope.createWebAppId(projectOrNull);
			getModuleAdminAgent().installWebApp(moduleName, componentDescriptor.getName(), id, parameters.getDeploy());
			if (webAppConfigurations.containsKey(scope)) {
				createConfigurationFile(WEBAPP,
						componentDescriptor,
						webAppConfigurations.get(scope),
						moduleName, projectName,
						id);
			}
			LOGGER.info("WebAppScope: {}", scope);
			return true;
		} catch (IllegalArgumentException e) {
			LOGGER.error("Invalid Scope " + scope, e);
			return false;
		}
	}

	private boolean deployWebAppsForScopes(@NotNull final ModuleDescriptor descriptor, @NotNull final ModuleInstallationParameters parameters) {
		final ComponentDescriptor[] componentDescriptors = descriptor.getComponents();
		final List<WebAppIdentifier> webAppScopes = parameters.getWebAppScopes();
		List<ComponentDescriptor> webAppDescriptors = asList(componentDescriptors).stream().filter(componentDescriptor -> WEBAPP.equals(componentDescriptor.getType())).collect(toList());
		if (!webAppDescriptors.isEmpty() && !webAppScopes.isEmpty()) {
			LOGGER.info("Installing Project WebApps");
		}
		for (ComponentDescriptor componentDescriptor : webAppDescriptors) {
			if (!deployWebApps(componentDescriptor, parameters)) {
				return false;
			}
		}
		return true;
	}

	private boolean deployWebApps(final ComponentDescriptor descriptor, final ModuleInstallationParameters parameters) {
		final List<WebAppIdentifier> webAppScopes = parameters.getWebAppScopes();
		LOGGER.info("Going to install and activate component {} with webAppIds: {}", descriptor.getName(), webAppScopes);
		Optional<Boolean> failed = webAppScopes
				.stream()
				.map(it -> installWebAppAndActivateWebServer(it, parameters))
				.filter(it -> !it)
				.findAny();

		if (failed.isPresent()) {
			LOGGER.error("Cannot install WebApp for specific scope!  IDs: {}", webAppScopes);
			return false;
		}
		return true;
	}

	private boolean deployWebApp(final WebAppIdentifier webScope, final Project projectOrNull) {
		final WebAppId webAppId = webScope.createWebAppId(projectOrNull);
		final boolean isRootWebAppAndNonSocketConnection = isFs5RootWebApp(webAppId) && SOCKET_MODE != _connection.getMode();
		boolean successfullyDeployed = false;
		if (isRootWebAppAndNonSocketConnection) {
			LOGGER.error("Cannot use a non socket connection to deploy a web component to the FirstSpirit root WebApp. Use SOCKET as connection mode!");
		} else {
			LOGGER.info("Deploying WebApp {}", projectOrNull == null ? webScope.toString() : (projectOrNull.getName() + '/' + webScope.getScope()));
			successfullyDeployed = getModuleAdminAgent().deployWebApp(webAppId);
			LOGGER.info("Successfully deployed: {}", successfullyDeployed);
		}
		return successfullyDeployed;
	}

	/**
	 * Installs a module on a FirstSpirit server. Uses the given connection.
	 * If any of the configured components is already installed, it is updated.
	 *
	 * @param parameters a parameter bean that defines how the module should be installed
	 * @return the optional {@link ModuleResult}, which is empty on failure
	 * @throws IOException may be thrown server side while installing the module
	 */
	@NotNull
	public ModuleResult install(@NotNull final ModuleInstallationParameters parameters, final boolean deploy) throws IOException {
		if (_connection == null || !_connection.isConnected()) {
			throw new IllegalStateException("Connection is null or not connected!");
		}

		final ModuleResult moduleResult = installFsm(parameters.getFsm(), parameters.getDeploy());
		final ModuleDescriptor moduleDescriptor = moduleResult.getDescriptor();
		final String moduleName = moduleDescriptor.getName();

		// activate services
		activateServices(moduleDescriptor, parameters);

		// install project apps
		installProjectApps(moduleDescriptor, parameters);

		if (deploy) {
			// install and deploy project web apps
			final boolean webAppsSuccessfullyInstalledAndDeployed = installProjectWebAppsAndDeploy(moduleDescriptor, parameters);
			if (!webAppsSuccessfullyInstalledAndDeployed) {
				LOGGER.error("WebApp installation and activation for module '{}' failed!", moduleName);
			}
		} else {
			// only create configurations
			final boolean webAppsConfigured = installProjectWebAppsAndCreateConfig(moduleDescriptor, parameters);
			if (!webAppsConfigured) {
				LOGGER.error("WebApp configuration for module '{}' failed!", moduleName);
			}
		}
		return moduleResult;
	}

}
