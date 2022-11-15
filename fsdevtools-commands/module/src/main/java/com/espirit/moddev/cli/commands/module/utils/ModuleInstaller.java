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

package com.espirit.moddev.cli.commands.module.utils;

import com.espirit.moddev.cli.api.result.DefaultExecutionErrorResult;
import com.espirit.moddev.cli.api.result.DefaultExecutionResult;
import com.espirit.moddev.cli.api.result.ExecutionResults;
import com.espirit.moddev.cli.commands.module.common.ModuleInstallationParameters;
import com.espirit.moddev.cli.commands.module.installCommand.InstallModuleCommandResult;
import com.espirit.moddev.shared.StringUtils;
import com.espirit.moddev.shared.annotation.VisibleForTesting;
import com.espirit.moddev.shared.webapp.WebAppIdentifier;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.ServiceNotFoundException;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.agency.ModuleAdminAgent;
import de.espirit.firstspirit.agency.ModuleAdminAgent.ModuleResult;
import de.espirit.firstspirit.agency.ProjectWebAppId;
import de.espirit.firstspirit.agency.WebAppId;
import de.espirit.firstspirit.io.FileHandle;
import de.espirit.firstspirit.io.FileSystem;
import de.espirit.firstspirit.module.descriptor.ComponentDescriptor;
import de.espirit.firstspirit.module.descriptor.ModuleDescriptor;
import de.espirit.firstspirit.module.descriptor.ProjectAppDescriptor;
import de.espirit.firstspirit.module.descriptor.ServiceDescriptor;
import de.espirit.firstspirit.module.descriptor.WebAppDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static de.espirit.firstspirit.module.descriptor.ComponentDescriptor.Type.SERVICE;
import static de.espirit.firstspirit.module.descriptor.ComponentDescriptor.Type.WEBAPP;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

/**
 * This class can install modules and module configurations.
 */
public class ModuleInstaller {

	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ModuleInstaller.class);

	private final Connection _connection;
	private ModuleAdminAgent _moduleAdminAgent;

	/**
	 * Instantiates a {@link ModuleInstaller}. Doesn't do anything else.
	 */
	public ModuleInstaller(final Connection connection) {
		// Nothing to do here
		_connection = connection;
	}

	@VisibleForTesting
	ModuleAdminAgent getModuleAdminAgent() {
		if (_moduleAdminAgent == null) {
			_moduleAdminAgent = _connection.getBroker().requireSpecialist(ModuleAdminAgent.TYPE);
		}
		return _moduleAdminAgent;
	}

	@NotNull
	public InstallModuleCommandResult installModule(@NotNull final ModuleInstallationParameters parameters) throws IOException {
		final ModuleInstallationResult installationResult = doInstallModule(parameters);
		final ExecutionResults executionResults = new ExecutionResults();
		final ModuleDescriptor moduleDescriptor = installationResult.getModuleResult().getDescriptor();
		executionResults.add(new DefaultExecutionResult(String.format("Module '%s' successfully installed in version '%s'.", moduleDescriptor.getModuleName(), moduleDescriptor.getVersion())));
		installationResult.getConfiguredServices().forEach(result -> executionResults.add(result.getException() == null ? new DefaultExecutionResult(result.toString()) : new DefaultExecutionErrorResult<>(result.toString(), result.getException())));
		installationResult.getInstalledProjectAppComponentResults().forEach(result -> executionResults.add(result.getException() == null ? new DefaultExecutionResult(result.toString()) : new DefaultExecutionErrorResult<>(result.toString(), result.getException())));
		installationResult.getUpdatedProjectAppComponentResults().forEach(result -> executionResults.add(result.getException() == null ? new DefaultExecutionResult(result.toString()) : new DefaultExecutionErrorResult<>(result.toString(), result.getException())));
		installationResult.getInstalledWebAppComponentResults().forEach(result -> executionResults.add(result.getException() == null ? new DefaultExecutionResult(result.toString()) : new DefaultExecutionErrorResult<>(result.toString(), result.getException())));
		installationResult.getUpdatedWebAppComponentResults().forEach(result -> executionResults.add(result.getException() == null ? new DefaultExecutionResult(result.toString()) : new DefaultExecutionErrorResult<>(result.toString(), result.getException())));
		if (parameters.getDeploy()) {
			final ExecutionResults deployExecutionResults = WebAppUtil.deployWebApps(_connection, installationResult.getModuleResult().getUpdatedWebApps());
			deployExecutionResults.stream().forEach(executionResults::add);
		}
		return new InstallModuleCommandResult(parameters.getFsm().getAbsolutePath(), installationResult, executionResults);
	}

	/**
	 * Method for installing a given FirstSpirit module (only the module itself will be installed, no components will be added to any project).
	 *
	 * @param fsm The path to the FirstSpirit module file (fsm) to be installed
	 * @return An InstallModuleResult. Result might be absent when there's an exception with the fsm file stream.
	 * @throws IOException may be thrown server side while installing the module
	 */
	@VisibleForTesting
	@NotNull
	ModuleResult installFsm(@NotNull final File fsm) throws IOException {
		LOGGER.info("Starting module installation for fsm '{}'...", fsm.getName());
		try (final FileInputStream stream = new FileInputStream(fsm)) {
			LOGGER.debug("Installing module in fsm '{}'...", fsm.getName());
			ModuleResult result = getModuleAdminAgent().install(stream, false, false);
			final ModuleDescriptor moduleDescriptor = result.getDescriptor();
			final String moduleName = moduleDescriptor.getModuleName();
			final String nameAndVersionText = moduleName + '@' + moduleDescriptor.getVersion();
			LOGGER.debug("Module '{}' installed (fsm was '{}') in version '{}'.", moduleName, fsm.getName(), moduleDescriptor.getVersion());
			LOGGER.debug("Setting module '{}' as trusted...", nameAndVersionText);
			getModuleAdminAgent().setTrusted(moduleName, true);
			LOGGER.debug("Module '{}' is now trusted.", nameAndVersionText);
			LOGGER.info("Module '{}' successfully installed in version '{}'.", moduleDescriptor.getModuleName(), moduleDescriptor.getVersion());
			return result;
		}
	}

	/**
	 * Method for activating auto start of services of a given module
	 *
	 * @param moduleDescriptor the module descriptor
	 * @param parameters       the {@link ModuleInstallationParameters parameters} of the command
	 */
	@VisibleForTesting
	@NotNull
	List<ServiceComponentResult> configureServices(@NotNull final ModuleDescriptor moduleDescriptor, @NotNull final ModuleInstallationParameters parameters) {
		final ArrayList<ServiceComponentResult> results = new ArrayList<>();
		final String moduleName = moduleDescriptor.getModuleName();
		final ComponentDescriptor[] componentDescriptors = moduleDescriptor.getComponents();
		if (componentDescriptors == null) {
			LOGGER.info("No components found for module '{}'!", moduleName);
		} else {
			final List<ServiceDescriptor> serviceDescriptors = stream(componentDescriptors).filter(it -> it instanceof ServiceDescriptor).map(ServiceDescriptor.class::cast).collect(toList());
			if (!serviceDescriptors.isEmpty()) {
				LOGGER.info("Configuring services for module '{}'...", moduleName);
				serviceDescriptors.forEach(serviceDescriptor -> {
					try {
						LOGGER.info("Configuring and starting service '{}:{}' (version='{}')...", moduleName, serviceDescriptor.getName(), serviceDescriptor.getVersion());
						final File configuration = parameters.getServiceConfigurations().get(serviceDescriptor.getName());
						if (configuration != null) {
							createConfigurationFile(SERVICE, serviceDescriptor, configuration, moduleName, parameters.getProjectName(), null);
						} else {
							LOGGER.info("No configuration file for service '{}:{}' found. Nothing to do.", moduleName, serviceDescriptor.getName());
						}
						final boolean serviceRunning = setAutostartAndRestartService(moduleName, serviceDescriptor);
						if (!serviceRunning) {
							final String message = String.format("Service '%s:%s' (version='%s') configured but could not be started!", moduleName, serviceDescriptor.getName(), serviceDescriptor.getVersion());
							LOGGER.warn(message);
							results.add(new ServiceComponentResult(serviceDescriptor, new IllegalStateException(message)));
						} else {
							LOGGER.info("Service '{}:{}' (version='{}') configured and started.", moduleName, serviceDescriptor.getName(), serviceDescriptor.getVersion());
							results.add(new ServiceComponentResult(serviceDescriptor, null));
						}
					} catch (final Exception exception) {
						LOGGER.warn("Error configuring and starting service '{}:{}' (version='{}')!", moduleName, serviceDescriptor.getName(), serviceDescriptor.getVersion());
						results.add(new ServiceComponentResult(serviceDescriptor, exception));
					}
				});
			}
		}
		return results;
	}

	private boolean setAutostartAndRestartService(@NotNull final String moduleName, @NotNull final ServiceDescriptor descriptor) {
		final ModuleAdminAgent moduleAdminAgent = getModuleAdminAgent();
		final String serviceName = descriptor.getName();
		final String serviceString = String.format("'%s:%s' (version='%s')", moduleName, serviceName, descriptor.getVersion());
		try {
			if (moduleAdminAgent.isRunning(serviceName)) {
				LOGGER.info("Stopping service '{}:{}'...", moduleName, serviceName);
				moduleAdminAgent.stopService(serviceName);
				LOGGER.debug("Service '{}:{}' stopped.", moduleName, serviceName);
			}
		} catch (final ServiceNotFoundException ignore) {
			LOGGER.debug("Service '{}:{}' was not stopped because it was not running.", moduleName, serviceName);
		}
		LOGGER.info("Setting autostart for service {}...", serviceString);
		moduleAdminAgent.setAutostart(serviceName, true);
		LOGGER.debug("Autostart for service {} set.", serviceString);
		LOGGER.info("Starting service {}...", serviceString);
		moduleAdminAgent.startService(serviceName);
		final boolean running = moduleAdminAgent.isRunning(serviceName);
		LOGGER.info("Status of service {}: {}", serviceString, running ? "RUNNING" : "STOPPED");
		return running;
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

	/**
	 * Creates a new {@link ModuleInstallationResult}. This method only exists for testing purposes.
	 *
	 * @return a new {@link ModuleInstallationResult}.
	 */
	@VisibleForTesting
	@NotNull
	ModuleInstallationResult createModuleInstallationResult() {
		return new ModuleInstallationResult();
	}

	@NotNull
	private Project safelyRetrieveProject(@Nullable final String projectName) {
		if (StringUtils.isNullOrEmpty(projectName)) {
			throw new IllegalArgumentException("No project given, can't get a project app configuration!");
		}
		Project project = _connection.getProjectByName(projectName);
		if (project == null) {
			throw new IllegalArgumentException("Cannot find project " + projectName + "!");
		}
		return project;
	}

	@VisibleForTesting
	@NotNull
	List<ProjectAppComponentResult> updateProjectAppComponents(@NotNull final ModuleDescriptor moduleDescriptor, @NotNull final List<ProjectAppComponentResult> projectAppsToIgnore, @Nullable final String projectName) {
		final List<ProjectAppComponentResult> results = new ArrayList<>();
		final List<ProjectAppDescriptor> projectAppDescriptors = stream(moduleDescriptor.getComponents()).filter(it -> it instanceof ProjectAppDescriptor).map(ProjectAppDescriptor.class::cast).collect(toList());
		final ModuleAdminAgent moduleAdminAgent = getModuleAdminAgent();
		projectAppDescriptors.forEach(descriptor -> {
			final Collection<Project> projects = moduleAdminAgent.getProjectAppUsages(descriptor.getModuleName(), descriptor.getName());
			for (final Project project : projects) {
				try {
					if (projectName != null && !projectName.equals(project.getName())) {
						LOGGER.debug("Ignoring update of project app '{}:{}' in project '{}'. Project was not configured to receive updates (project is configured as '{}').", descriptor.getModuleName(), descriptor.getName(), project.getName(), projectName);
						continue;
					}
					if (!project.isActive()) {
						LOGGER.debug("Ignoring update of project app '{}:{}' in '{}'. Project is inactive.", descriptor.getModuleName(), descriptor.getName(), project.getName());
						continue;
					}
					final boolean alreadyInstalled = projectAppsToIgnore.stream().anyMatch(result -> result.getProject().equals(project) && result.getDescriptor().getModuleName().equals(descriptor.getModuleName()) && result.getDescriptor().getName().equals(descriptor.getName()));
					if (alreadyInstalled) {
						LOGGER.debug("Ignoring update of project app '{}:{}' in project '{}'. Project app was already updated during installation phase.", descriptor.getModuleName(), descriptor.getName(), project.getName());
						continue;
					}
					LOGGER.info("Updating project app '{}:{}' in '{}'...", descriptor.getModuleName(), descriptor.getName(), project.getName());
					moduleAdminAgent.installProjectApp(descriptor.getModuleName(), descriptor.getName(), project);
					LOGGER.info("Updated project app '{}:{} to version {}' in '{}'...", descriptor.getModuleName(), descriptor.getName(), descriptor.getVersion(), project.getName());
					results.add(new ProjectAppComponentResult(descriptor, null, project));
				} catch (final Exception exception) {
					results.add(new ProjectAppComponentResult(descriptor, exception, project));
				}
			}
		});
		return results;
	}

	@VisibleForTesting
	@NotNull
	List<WebAppComponentResult> updateWebAppComponents(@NotNull final ModuleDescriptor moduleDescriptor, @NotNull final List<WebAppComponentResult> webAppComponentsToIgnore) {
		final List<WebAppComponentResult> result = new ArrayList<>();
		final ModuleAdminAgent moduleAdminAgent = getModuleAdminAgent();
		final List<WebAppDescriptor> webAppDescriptors = stream(moduleDescriptor.getComponents()).filter(descriptor -> descriptor instanceof WebAppDescriptor).map(WebAppDescriptor.class::cast).collect(toList());
		webAppDescriptors.forEach(descriptor -> {
			final Collection<WebAppId> webAppIdsToUpdate = moduleAdminAgent.getWebAppUsages(descriptor.getModuleName(), descriptor.getName())
					.stream()
					.filter(webAppId -> {
						final WebAppIdentifier webAppIdentifier = WebAppIdentifier.fromWebAppId(webAppId);
						return webAppComponentsToIgnore.stream().noneMatch(componentResult -> webAppIdentifier.equals(componentResult.getWebAppIdentifier()) && componentResult.getDescriptor().getModuleName().equals(descriptor.getModuleName()) && componentResult.getDescriptor().getName().equals(descriptor.getName()));
					}).filter(webAppId -> {
						// filter inactive projects
						final boolean isProjectWebApp = webAppId instanceof ProjectWebAppId;
						if (isProjectWebApp) {
							final ProjectWebAppId projectWebApp = (ProjectWebAppId) webAppId;
							final boolean active = projectWebApp.getProject().isActive();
							if (!active) {
								LOGGER.debug("Ignoring update of web app component '{}:{}' in webapp '{}'. Project is inactive.", descriptor.getModuleName(), descriptor.getName(), WebAppUtil.getReadableWebAppName(webAppId));
								return false;
							}
						}
						return true;
					})
					.collect(toList());
			for (final WebAppId webAppId : webAppIdsToUpdate) {
				final String webAppName = WebAppUtil.getReadableWebAppName(webAppId);
				try {
					LOGGER.info("Updating web app component '{}:{}' in web app '{}'...", descriptor.getModuleName(), descriptor.getName(), webAppName);
					moduleAdminAgent.installWebApp(descriptor.getModuleName(), descriptor.getName(), webAppId, false);
					LOGGER.info("Updated web app component '{}:{}' to version {} in web app '{}'.", descriptor.getModuleName(), descriptor.getName(), descriptor.getVersion(), webAppName);
					result.add(new WebAppComponentResult(descriptor, null, webAppId));
				} catch (final Exception exception) {
					LOGGER.error("Error updating web app component '{}:{}' in web app '{}'.", descriptor.getModuleName(), descriptor.getName(), webAppName);
					result.add(new WebAppComponentResult(descriptor, exception, webAppId));
				}
			}
		});
		return result;
	}

	/**
	 * Method for installing the project applications of a given module into a given project
	 *
	 * @param moduleDescriptor the descriptor of the module whose project applications shall be installed
	 * @param parameters       the {@link ModuleInstallationParameters} of the current command
	 */
	@VisibleForTesting
	@NotNull
	List<ProjectAppComponentResult> installProjectAppComponents(@NotNull final ModuleDescriptor moduleDescriptor,
																@NotNull final ModuleInstallationParameters parameters) {
		final ArrayList<ProjectAppComponentResult> result = new ArrayList<>();
		final List<ProjectAppDescriptor> projectAppDescriptors = stream(moduleDescriptor.getComponents()).filter(it -> it instanceof ProjectAppDescriptor).map(ProjectAppDescriptor.class::cast).collect(toList());
		final String moduleName = moduleDescriptor.getModuleName();
		final String projectName = parameters.getProjectName();
		if (StringUtils.isNullOrEmpty(projectName)) {
			if (!projectAppDescriptors.isEmpty()) {
				LOGGER.debug("Found project apps in module '{}', but cannot install project apps without a project name!", moduleName);
			}
		} else {
			if (!projectAppDescriptors.isEmpty()) {
				LOGGER.info("Installing project apps of module '{}' in project '{}'...", moduleName, projectName);
				projectAppDescriptors.forEach(descriptor -> {
					LOGGER.info("Creating project app configuration '{}:{}' in project '{}' ...", moduleName, descriptor.getName(), projectName);
					createProjectAppConfiguration(projectName, moduleName, descriptor);
					LOGGER.debug("Project app configuration '{}:{}' in project '{}' created.", moduleName, descriptor.getName(), projectName);

					final Project project = safelyRetrieveProject(projectName);
					if (!project.isActive()) {
						LOGGER.info("Ignoring installation/update of project app '{}:{}' in '{}'. Project is inactive.", moduleName, descriptor.getName(), projectName);
						return;
					}

					final ModuleAdminAgent moduleAdminAgent = getModuleAdminAgent();
					final boolean projectAppComponentExisted = moduleAdminAgent.getProjectAppUsages(moduleName, descriptor.getName()).contains(project);
					if (projectAppComponentExisted) {
						LOGGER.info("Updating project app '{}:{}' in '{}'...", moduleName, descriptor.getName(), projectName);
					} else {
						LOGGER.info("Installing project app '{}:{}' in '{}'...", moduleName, descriptor.getName(), projectName);
					}
					try {
						moduleAdminAgent.installProjectApp(moduleName, descriptor.getName(), project);
						result.add(new ProjectAppComponentResult(descriptor, null, project));
						if (projectAppComponentExisted) {
							LOGGER.info("Project app '{}:{}@{}' updated in '{}'.", moduleName, descriptor.getName(), descriptor.getVersion(), projectName);
						} else {
							LOGGER.info("Project app '{}:{}@{}' installed in '{}'.", moduleName, descriptor.getName(), descriptor.getVersion(), projectName);
						}
					} catch (final Exception exception) {
						LOGGER.error("Error installing/updating project app component '{}:{}' in project '{}'.", moduleName, descriptor.getName(), projectName);
						result.add(new ProjectAppComponentResult(descriptor, exception, project));
					}

					parameters.getProjectAppConfiguration().ifPresent(projectAppFile -> createConfigurationFile(ComponentDescriptor.Type.PROJECTAPP, descriptor, projectAppFile, moduleName, projectName, null));
				});
			}
		}
		return result;
	}

	private void createProjectAppConfiguration(final String projectName, final String moduleName,
											   final ComponentDescriptor projectAppDescriptor) {
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

	@VisibleForTesting
	@NotNull
	List<WebAppComponentResult> installWebAppComponents(@NotNull final ModuleDescriptor descriptor, @NotNull final ModuleInstallationParameters parameters) {
		final List<WebAppComponentResult> result = new ArrayList<>();
		final String moduleName = descriptor.getModuleName();
		final List<WebAppDescriptor> webappDescriptors = stream(descriptor.getComponents())
				.filter(componentDescriptor -> componentDescriptor instanceof WebAppDescriptor)
				.map(WebAppDescriptor.class::cast)
				.collect(toList());
		final List<WebAppIdentifier> webAppScopes = parameters.getWebAppScopes();
		if (!webappDescriptors.isEmpty() && !webAppScopes.isEmpty()) {
			LOGGER.info("Configuring web app components for module '{}'...", moduleName);
			webappDescriptors.forEach(webAppDescriptor -> {
				for (final WebAppIdentifier scope : webAppScopes) {
					final String componentName = webAppDescriptor.getName();
					LOGGER.info("Configuring web app component '{}:{}' in scope {}...", moduleName, componentName, scope.getScope());
					final WebAppComponentResult componentResult = createWebAppAndConfigurations(descriptor, parameters, webAppDescriptor, scope);
					if (componentResult != null) {
						if (componentResult.getException() == null) {
							LOGGER.debug("Web app component '{}:{}' in scope '{}' configured.", moduleName, componentName, scope.getScope());
						} else {
							LOGGER.warn("Could not configure web app component '{}:{}' in scope '{}'...", moduleName, componentName, scope.getScope());
						}
						result.add(componentResult);
					}
				}
			});
		}
		return result;
	}

	@Nullable
	private WebAppComponentResult createWebAppAndConfigurations(@NotNull final ModuleDescriptor moduleDescriptor,
																@NotNull final ModuleInstallationParameters parameters,
																@NotNull final WebAppDescriptor webAppDescriptor,
																@NotNull final WebAppIdentifier scope) {
		final String moduleName = moduleDescriptor.getModuleName();
		final String webAppDescriptorName = webAppDescriptor.getName();
		final String projectName = parameters.getProjectName();
		final Project project;
		if (StringUtils.isNullOrEmpty(projectName)) {
			project = null;
		} else {
			project = _connection.getProjectByName(projectName);
			if (project == null) {
				return new WebAppComponentResult(webAppDescriptor, new IllegalArgumentException(String.format("Error installing web app component '%s:%s'. Project '%s' not found!", moduleName, webAppDescriptorName, projectName)), scope, projectName);
			}
		}
		final WebAppId webAppId = scope.createWebAppId(project);
		// fast return for inactive projects
		if (project != null && !project.isActive()) {
			LOGGER.debug("Ignoring installation/update of project app '{}:{}' in '{}'. Project is inactive.", moduleName, webAppDescriptorName, project.getName());
			return null;
		}
		final String webAppName = WebAppUtil.getReadableWebAppName(webAppId);
		try {
			final Map<WebAppIdentifier, File> webAppConfigurations = parameters.getWebAppConfigurations();
			final ModuleAdminAgent moduleAdminAgent = getModuleAdminAgent();
			final boolean webAppComponentExisted = moduleAdminAgent.getWebAppUsages(moduleName, webAppDescriptorName).contains(webAppId);
			if (webAppComponentExisted) {
				LOGGER.info("Updating web app component '{}:{}' in web app '{}'...", moduleName, webAppDescriptorName, webAppName);
			} else {
				LOGGER.info("Installing web app component '{}:{}' in web app '{}'...", moduleName, webAppDescriptorName, webAppName);
			}
			moduleAdminAgent.installWebApp(moduleName, webAppDescriptorName, webAppId, false);
			if (webAppConfigurations.containsKey(scope)) {
				createConfigurationFile(WEBAPP,
						webAppDescriptor,
						webAppConfigurations.get(scope),
						moduleName, projectName,
						webAppId);
			}
			if (webAppComponentExisted) {
				LOGGER.info("Updated web app component '{}:{}' in web app '{}'.", moduleName, webAppDescriptorName, webAppName);
			} else {
				LOGGER.info("Installed web app component '{}:{}' in web app '{}'.", moduleName, webAppDescriptorName, webAppName);
			}
			return new WebAppComponentResult(webAppDescriptor, null, webAppId);
		} catch (final Exception exception) {
			LOGGER.error("Error installing/updating web app component '{}:{}' in web app '{}'.", moduleName, webAppDescriptorName, webAppName);
			return new WebAppComponentResult(webAppDescriptor, exception, webAppId);
		}
	}

	@VisibleForTesting
	@NotNull
	ModuleInstallationResult doInstallModule(@NotNull final ModuleInstallationParameters parameters) throws IOException {
		if (_connection == null || !_connection.isConnected()) {
			throw new IOException("Connection is <null> or not connected!");
		}

		final ModuleInstallationResult moduleInstallationResult = createModuleInstallationResult();
		final ModuleResult moduleResult = installFsm(parameters.getFsm());
		final ModuleDescriptor moduleDescriptor = moduleResult.getDescriptor();

		// configure services
		final List<ServiceComponentResult> configuredServices = configureServices(moduleDescriptor, parameters);
		moduleInstallationResult.setConfiguredServices(configuredServices);

		// install project app components
		final List<ProjectAppComponentResult> installedProjectAppComponents = installProjectAppComponents(moduleDescriptor, parameters);
		moduleInstallationResult.setInstalledProjectAppComponentResults(installedProjectAppComponents);

		// update already existing project app components
		final List<ProjectAppComponentResult> updatedProjectAppComponents = updateProjectAppComponents(moduleDescriptor, installedProjectAppComponents, parameters.getProjectName());
		moduleInstallationResult.setUpdatedProjectAppComponentResults(updatedProjectAppComponents);

		// combine project app results
		final List<Project> installedOrUpdatedProjectAppProjects = Stream.concat(installedProjectAppComponents.stream(), updatedProjectAppComponents.stream())
				.map(ProjectAppComponentResult::getProject)
				.collect(toList());

		// install web app components
		final List<WebAppComponentResult> installedWebAppComponents = installWebAppComponents(moduleDescriptor, parameters);
		moduleInstallationResult.setInstalledWebAppComponentResults(installedWebAppComponents);

		// update already existing web app components
		final List<WebAppComponentResult> updatedWebAppComponents = updateWebAppComponents(moduleDescriptor, installedWebAppComponents);
		moduleInstallationResult.setUpdatedWebAppComponentResults(updatedWebAppComponents);

		// combine web app results
		final List<WebAppComponentResult> installedOrUpdatedWebAppComponents = new ArrayList<>(installedWebAppComponents);
		installedOrUpdatedWebAppComponents.addAll(updatedWebAppComponents);
		final List<WebAppId> installedOrUpdatedWebAppIds = installedOrUpdatedWebAppComponents.stream()
				.filter(webAppComponentResult -> webAppComponentResult.getException() == null)
				.map(componentResult -> componentResult.getWebAppId(_connection))
				.filter(Objects::nonNull)
				.distinct()
				.collect(toList());
		moduleInstallationResult.setModuleResult(new ModuleResult() {
			@Override
			public ModuleDescriptor getDescriptor() {
				return moduleResult.getDescriptor();
			}

			@Override
			public Collection<WebAppId> getUpdatedWebApps() {
				return installedOrUpdatedWebAppIds;
			}

			@Override
			public Collection<Project> getUpdatedProjectApps() {
				return installedOrUpdatedProjectAppProjects;
			}

			@Override
			public String getLogMessages() {
				return moduleResult.getLogMessages();
			}
		});
		return moduleInstallationResult;
	}

}
