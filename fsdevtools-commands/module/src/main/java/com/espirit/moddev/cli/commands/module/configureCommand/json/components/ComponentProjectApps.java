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

package com.espirit.moddev.cli.commands.module.configureCommand.json.components;

import com.espirit.moddev.cli.api.result.ExecutionErrorResult;
import com.espirit.moddev.cli.api.result.ExecutionResult;
import com.espirit.moddev.cli.api.result.ExecutionResults;
import com.espirit.moddev.cli.commands.module.configureCommand.json.components.common.ComponentNotFoundResult;
import com.espirit.moddev.cli.commands.module.configureCommand.json.components.common.MultipleComponentsFoundResult;
import com.espirit.moddev.cli.commands.module.configureCommand.json.components.common.NoProjectNameDefinedResult;
import com.espirit.moddev.cli.commands.module.configureCommand.json.components.common.ProjectNotFoundResult;
import com.espirit.moddev.cli.commands.module.utils.FileSystemUtil;
import com.espirit.moddev.cli.commands.module.utils.ModuleUtil;
import com.espirit.moddev.shared.StringUtils;
import com.espirit.moddev.shared.annotation.VisibleForTesting;
import com.espirit.moddev.util.Preconditions;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.agency.ModuleAdminAgent;
import de.espirit.firstspirit.module.descriptor.ComponentDescriptor;
import de.espirit.firstspirit.module.descriptor.ModuleDescriptor;
import de.espirit.firstspirit.server.module.ModuleException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_COMPONENT_NAME;
import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_FILES;
import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_PROJECT_APPS;
import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_PROJECT_NAME;
import static com.espirit.moddev.util.Preconditions.getNonNullList;

/**
 * Configures a single {@link ComponentProjectApps project component} for a {@link ProjectApp project app}.
 */
public class ComponentProjectApps implements Configurable {

	private static final Logger LOGGER = LoggerFactory.getLogger(ComponentProjectApps.class);

	private final String _componentName;
	private final List<ProjectApp> _projectApps;

	ComponentProjectApps(@JsonProperty(value = ATTR_COMPONENT_NAME, required = true) @NotNull final String componentName, @JsonProperty(value = ATTR_PROJECT_APPS, required = true) @NotNull List<ProjectApp> projectApps) {
		_componentName = Preconditions.checkNotEmpty(componentName, "Attribute '" + ATTR_COMPONENT_NAME + "' is undefined.");
		_projectApps = Preconditions.checkNotEmpty(projectApps, "Attribute '" + ATTR_PROJECT_APPS + "' is undefined.");
	}

	@NotNull
	public String getComponentName() {
		return _componentName;
	}

	@NotNull
	public List<ProjectApp> getProjectApps() {
		return _projectApps;
	}

	@NotNull
	@Override
	public ExecutionResults configure(@NotNull final ConfigurationContext context, @NotNull final ModuleDescriptor module) {
		LOGGER.debug("Configuring {} project " + StringUtils.toPluralRespectingString(getProjectApps().size(), "app") + " for project component '{}' of module '{}'...", getProjectApps().size(), getComponentName(), module.getModuleName());
		final ExecutionResults results = new ExecutionResults();
		for (final ProjectApp projectApp : getProjectApps()) {
			results.add(projectApp.configure(context, module, getComponentName()));
		}
		return results;
	}

	@VisibleForTesting
	public static class ProjectApp {

		private static final Logger LOGGER = LoggerFactory.getLogger(ProjectApp.class);

		@VisibleForTesting
		@JsonProperty(value = ATTR_PROJECT_NAME)
		String _projectName;

		@VisibleForTesting
		@JsonProperty(value = ATTR_FILES)
		List<String> _files;

		@Nullable
		public String getRawProjectName() {
			return StringUtils.isNullOrEmpty(_projectName) ? null : _projectName.trim();
		}

		@Nullable
		public String getProjectName(@NotNull final ConfigurationContext context) {
			if (getRawProjectName() == null && context.getGlobalProjectName() != null) {
				LOGGER.debug("Using project name from global context: '{}'...", context.getGlobalProjectName());
				return context.getGlobalProjectName();
			}
			if (getRawProjectName() != null && context.getGlobalProjectName() != null) {
				LOGGER.debug("The project defined in the configuration file ('{}') contradicts the project from the call ('{}'). Using project '{}'.", getRawProjectName(), context.getGlobalProjectName(), getRawProjectName());
			}
			return getRawProjectName();
		}

		@NotNull
		public List<String> getFiles() {
			return getNonNullList(_files);
		}

		@NotNull
		public ExecutionResult configure(@NotNull final ConfigurationContext context, @NotNull final ModuleDescriptor module, @NotNull final String componentName) {
			final String moduleName = module.getModuleName();
			final String projectName = getProjectName(context);
			if (projectName == null) {
				LOGGER.debug("Project name for '{}:{}' is undefined.", moduleName, componentName);
				return new NoProjectNameDefinedResult(moduleName, componentName);
			}

			LOGGER.debug("Processing project component configuration '{}:{}' for project '{}'...", moduleName, componentName, projectName);

			// verify project
			final Connection connection = context.getConnection();
			final Project project = connection.getProjectByName(projectName);
			if (project == null) {
				LOGGER.debug("Project '{}' not found.", projectName);
				return new ProjectNotFoundResult(projectName);
			}

			// verify component
			final List<ComponentDescriptor> componentsLookUp = ModuleUtil.findComponentByNameOrDisplayName(module, ComponentDescriptor.Type.PROJECTAPP, componentName);
			if (componentsLookUp.isEmpty()) {
				return new ComponentNotFoundResult(moduleName, ComponentDescriptor.Type.PROJECTAPP, componentName);
			} else if (componentsLookUp.size() > 1) {
				return new MultipleComponentsFoundResult(moduleName, componentName, ComponentDescriptor.Type.PROJECTAPP, componentsLookUp);
			}

			// update the project app component name to the name of the found component
			final ComponentDescriptor componentDescriptor = componentsLookUp.get(0);
			final String projectAppComponentName = componentDescriptor.getName();

			// install project app
			final ModuleAdminAgent moduleAdminAgent = connection.getBroker().requireSpecialist(ModuleAdminAgent.TYPE);
			final boolean updateProjectApp = moduleAdminAgent.getProjectAppUsages(moduleName, projectAppComponentName)
					.stream()
					.anyMatch(p -> p.getName().equals(project.getName()));
			if (updateProjectApp) {
				LOGGER.debug("Updating project app '{}:{}' in project '{}'...", moduleName, projectAppComponentName, projectName);
			} else {
				LOGGER.debug("Installing project app '{}:{}' in project '{}'...", moduleName, projectAppComponentName, projectName);
			}

			try {
				moduleAdminAgent.installProjectApp(moduleName, projectAppComponentName, project);
			} catch (final ModuleException moduleException) {
				return new ProjectAppInstallFailedResult(moduleName, projectName, projectAppComponentName, updateProjectApp, moduleException);
			}

			// upload files to server
			final ExecutionResults fileUploadResult = FileSystemUtil.uploadFiles(getFiles(), moduleAdminAgent.getProjectAppConfig(moduleName, projectAppComponentName, project));
			if (fileUploadResult.hasError()) {
				return fileUploadResult;
			}

			LOGGER.debug("Project app '{}:{}' in project '{}' configured.", moduleName, projectAppComponentName, project.getName());
			return new ProjectAppResult(moduleName, projectName, componentName);
		}
	}

	@VisibleForTesting
	static class ProjectAppResult implements ExecutionResult {

		@VisibleForTesting
		static final String MESSAGE = "Successfully configured project app '%s:%s' in project '%s'.";

		private final String _moduleName;
		private final String _projectName;
		private final String _componentName;

		public ProjectAppResult(@NotNull final String moduleName, @NotNull final String projectName, @NotNull final String componentName) {
			_moduleName = moduleName;
			_projectName = projectName;
			_componentName = componentName;
		}

		@Override
		public String toString() {
			return String.format(MESSAGE, _moduleName, _componentName, _projectName);
		}

	}

	@VisibleForTesting
	static class ProjectAppInstallFailedResult implements ExecutionErrorResult<IllegalStateException> {

		@VisibleForTesting
		static final String MESSAGE_INSTALL = "Error installing project app '%s:%s' in project '%s':\n%s";
		@VisibleForTesting
		static final String MESSAGE_UPDATE = "Error updating project app '%s:%s' in project '%s':\n%s";

		private final String _moduleName;
		private final String _projectName;
		private final String _componentName;
		private final boolean _updateProjectApp;
		private final IllegalStateException _exception;
		private final ModuleException _moduleException;

		public ProjectAppInstallFailedResult(@NotNull final String moduleName, @NotNull final String projectName, @NotNull final String componentName, final boolean updateProjectApp, @NotNull final ModuleException moduleException) {
			_moduleName = moduleName;
			_projectName = projectName;
			_componentName = componentName;
			_updateProjectApp = updateProjectApp;
			_moduleException = moduleException;
			_exception = new IllegalStateException(toString(), _moduleException);
		}

		@NotNull
		@Override
		public IllegalStateException getThrowable() {
			return _exception;
		}

		@Override
		public String toString() {
			return String.format(_updateProjectApp ? MESSAGE_UPDATE : MESSAGE_INSTALL, _moduleName, _componentName, _projectName, _moduleException);
		}

	}

}
