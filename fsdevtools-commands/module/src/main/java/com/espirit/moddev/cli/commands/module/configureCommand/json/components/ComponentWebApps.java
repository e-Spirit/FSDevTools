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
import com.espirit.moddev.shared.webapp.WebAppIdentifier;
import com.espirit.moddev.shared.webapp.WebAppIdentifierParser;
import com.espirit.moddev.util.Preconditions;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.agency.GlobalWebAppId;
import de.espirit.firstspirit.agency.ModuleAdminAgent;
import de.espirit.firstspirit.agency.ProjectWebAppId;
import de.espirit.firstspirit.agency.WebAppId;
import de.espirit.firstspirit.module.descriptor.ComponentDescriptor;
import de.espirit.firstspirit.module.descriptor.ModuleDescriptor;
import de.espirit.firstspirit.server.module.ModuleException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_COMPONENT_NAME;
import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_DEPLOY;
import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_FILES;
import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_PROJECT_NAME;
import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_WEB_APPS;
import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_WEB_APP_NAME;
import static com.espirit.moddev.util.Preconditions.getNonNullList;

/**
 * Configures a single {@link ComponentWebApps web component} for a {@link ComponentWebApps.WebApp web app}.
 */
public class ComponentWebApps implements Configurable {

	private static final Logger LOGGER = LoggerFactory.getLogger(ComponentWebApps.class);

	private final String _componentName;
	private final List<WebApp> _webApps;

	ComponentWebApps(@JsonProperty(value = ATTR_COMPONENT_NAME, required = true) @NotNull final String componentName, @JsonProperty(value = ATTR_WEB_APPS, required = true) @NotNull List<WebApp> webApps) {
		_componentName = Preconditions.checkNotEmpty(componentName, "Attribute '" + ATTR_COMPONENT_NAME + "' is undefined.");
		_webApps = Preconditions.checkNotEmpty(webApps, "Attribute '" + ATTR_WEB_APPS + "' is undefined.");
	}

	@NotNull
	public String getComponentName() {
		return _componentName;
	}

	@NotNull
	public List<WebApp> getWebApps() {
		return _webApps;
	}

	@NotNull
	@Override
	public ExecutionResults configure(@NotNull final ConfigurationContext context, @NotNull final ModuleDescriptor module) {
		LOGGER.debug("Configuring {} web " + StringUtils.toPluralRespectingString(getWebApps().size(), "app") + " for web component '{}' of module '{}'...", getWebApps().size(), getComponentName(), module.getModuleName());
		final ExecutionResults results = new ExecutionResults();
		for (final WebApp webApp : getWebApps()) {
			results.add(webApp.configure(context, module, getComponentName()));
		}
		return results;
	}

	@VisibleForTesting
	public static class WebApp {

		private static final Logger LOGGER = LoggerFactory.getLogger(WebApp.class);

		@VisibleForTesting
		@JsonProperty(value = ATTR_WEB_APP_NAME)
		final WebAppIdentifier _webAppName;

		@VisibleForTesting
		@JsonProperty(value = ATTR_PROJECT_NAME)
		String _projectName;

		@VisibleForTesting
		@JsonProperty(value = ATTR_FILES)
		List<String> _files;

		@VisibleForTesting
		@JsonProperty(value = ATTR_DEPLOY)
		boolean _deploy = true;

		public WebApp(@JsonProperty(value = ATTR_WEB_APP_NAME, required = true) @NotNull final String webAppName) {
			final WebAppIdentifierParser parser = new WebAppIdentifierParser();
			_webAppName = parser.parseSingle(Preconditions.checkNotEmpty(webAppName, "Attribute '" + ATTR_WEB_APP_NAME + "' is undefined."));
		}

		@NotNull
		public WebAppIdentifier getWebAppName() {
			return _webAppName;
		}

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

		public boolean getDeploy() {
			return _deploy;
		}

		@NotNull
		public ExecutionResult configure(@NotNull final ConfigurationContext context, @NotNull final ModuleDescriptor module, @NotNull final String componentName) {
			final String moduleName = module.getModuleName();
			final String projectName;
			if (_webAppName.isGlobal()) {
				projectName = null;
				LOGGER.debug("Processing web component configuration '{}:{}' for global wep app '{}'...", moduleName, componentName, _webAppName);
			} else {
				projectName = getProjectName(context);
				LOGGER.debug("Processing web component configuration '{}:{}' for project specific webapp '{}:{}'...", moduleName, componentName, projectName, _webAppName);
			}
			// verify web app id
			final WebAppId webAppId;
			final Connection connection = context.getConnection();
			if (!_webAppName.isGlobal()) {
				if (projectName == null) {
					LOGGER.debug("Project name for '{}:{}' is undefined.", moduleName, componentName);
					return new NoProjectNameDefinedResult(moduleName, componentName);
				}
				final Project project = connection.getProjectByName(projectName);
				if (project == null) {
					LOGGER.debug("Project '{}' not found.", projectName);
					return new ProjectNotFoundResult(projectName);
				}
				webAppId = _webAppName.createWebAppId(project);
			} else {
				webAppId = _webAppName.createWebAppId(null);
			}

			// verify component
			final List<ComponentDescriptor> componentsLookUp = ModuleUtil.findComponentByNameOrDisplayName(module, ComponentDescriptor.Type.WEBAPP, componentName);
			if (componentsLookUp.isEmpty()) {
				return new ComponentNotFoundResult(moduleName, ComponentDescriptor.Type.WEBAPP, componentName);
			} else if (componentsLookUp.size() > 1) {
				return new MultipleComponentsFoundResult(moduleName, componentName, ComponentDescriptor.Type.WEBAPP, componentsLookUp);
			}

			// update the project app component name to the name of the found component
			final ComponentDescriptor componentDescriptor = componentsLookUp.get(0);
			final String webComponentName = componentDescriptor.getName();

			// install web component in web app (without deploying the web-app)
			final ModuleAdminAgent moduleAdminAgent = connection.getBroker().requireSpecialist(ModuleAdminAgent.TYPE);
			final boolean updateWebComponent = moduleAdminAgent.getWebAppUsages(moduleName, webComponentName)
					.stream()
					.anyMatch(webAppId::equals);
			if (!updateWebComponent) {
				LOGGER.debug("Installing web component '{}:{}' in web app '{}'...", moduleName, webComponentName, _webAppName.toString());
			} else {
				LOGGER.debug("Updating web component '{}:{}' in web app '{}'...", moduleName, webComponentName, _webAppName.toString());
			}

			try {
				moduleAdminAgent.installWebApp(moduleName, webComponentName, webAppId, false);
			} catch (final ModuleException moduleException) {
				return new WebComponentInstallFailedResult(moduleName, webComponentName, webAppId, updateWebComponent, moduleException);
			}

			// upload files to server
			final ExecutionResults fileUploadResult = FileSystemUtil.uploadFiles(getFiles(), moduleAdminAgent.getWebAppConfig(moduleName, webComponentName, webAppId));
			if (fileUploadResult.hasError()) {
				return fileUploadResult;
			}

			if (_webAppName.isGlobal()) {
				LOGGER.debug("Web component '{}:{}' in global web app '{}' configured.", moduleName, webComponentName, _webAppName.toString());
			} else {
				LOGGER.debug("Web component '{}:{}' in project specific web app '{}:{}' configured.", moduleName, webComponentName, projectName, _webAppName.toString());
			}
			return new WebComponentResult(moduleName, webComponentName, webAppId, getDeploy());
		}

	}

	public static class WebComponentResult implements ExecutionResult {

		@VisibleForTesting
		static final String MESSAGE_GLOBAL_WEBAPP = "Successfully configured web component '%s:%s' in global web app '%s'.";
		@VisibleForTesting
		static final String MESSAGE_PROJECT_WEBAPP = "Successfully configured web component '%s:%s' in web app '%s' of project '%s'.";

		private final String _moduleName;
		private final String _componentName;
		private final WebAppId _webAppId;
		private final boolean _deploy;

		public WebComponentResult(@NotNull final String moduleName, @NotNull final String componentName, @NotNull final WebAppId webAppId, final boolean deploy) {
			_moduleName = moduleName;
			_componentName = componentName;
			_webAppId = webAppId;
			_deploy = deploy;
		}

		@NotNull
		public WebAppId getWebAppId() {
			return _webAppId;
		}

		public boolean getDeploy() {
			return _deploy;
		}

		@Override
		public boolean equals(final Object o) {
			// We implement #equals because this method is used in Stream#distinct.
			// Be careful when changing this, because we use Stream#distinct to remove duplicate web apps.
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			final WebComponentResult that = (WebComponentResult) o;
			return getDeploy() == that.getDeploy() && getWebAppId().equals(that.getWebAppId());
		}

		@Override
		public int hashCode() {
			return Objects.hash(getWebAppId(), getDeploy());
		}

		@Override
		public String toString() {
			if (_webAppId instanceof GlobalWebAppId) {
				final GlobalWebAppId globalWebAppId = (GlobalWebAppId) _webAppId;
				return String.format(MESSAGE_GLOBAL_WEBAPP, _moduleName, _componentName, globalWebAppId.getGlobalId());
			} else {
				final ProjectWebAppId projectWebAppId = (ProjectWebAppId) _webAppId;
				return String.format(MESSAGE_PROJECT_WEBAPP, _moduleName, _componentName, projectWebAppId.getWebScope().toString().toLowerCase(Locale.UK), projectWebAppId.getProject().getName());
			}
		}

	}

	@VisibleForTesting
	static class WebComponentInstallFailedResult implements ExecutionErrorResult<IllegalStateException> {

		@VisibleForTesting
		static final String MESSAGE_INSTALL = "Error installing web component '%s:%s' in web app '%s':\n%s";
		@VisibleForTesting
		static final String MESSAGE_UPDATE = "Error updating web component '%s:%s' in web app '%s':\n%s";

		private final String _moduleName;
		private final String _componentName;
		private final WebAppId _webAppId;
		private final boolean _updateWebComponent;
		private final IllegalStateException _exception;
		private final ModuleException _moduleException;

		public WebComponentInstallFailedResult(@NotNull final String moduleName, @NotNull final String componentName, @NotNull final WebAppId webAppId, final boolean updateWebComponent, @NotNull final ModuleException moduleException) {
			_moduleName = moduleName;
			_componentName = componentName;
			_webAppId = webAppId;
			_updateWebComponent = updateWebComponent;
			_moduleException = moduleException;
			_exception = new IllegalStateException(toString(), _moduleException);
		}

		@NotNull
		@Override
		public IllegalStateException getException() {
			return _exception;
		}

		@Override
		public String toString() {
			return String.format(_updateWebComponent ? MESSAGE_UPDATE : MESSAGE_INSTALL, _moduleName, _componentName, WebAppIdentifier.getName(_webAppId), _moduleException);
		}

	}

}
