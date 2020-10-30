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
import com.espirit.moddev.cli.commands.module.utils.FileSystemUtil;
import com.espirit.moddev.cli.commands.module.utils.ModuleUtil;
import com.espirit.moddev.cli.utils.ServiceUtils;
import com.espirit.moddev.shared.annotation.VisibleForTesting;
import com.espirit.moddev.util.Preconditions;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.ServiceNotFoundException;
import de.espirit.firstspirit.agency.ModuleAdminAgent;
import de.espirit.firstspirit.module.descriptor.ComponentDescriptor;
import de.espirit.firstspirit.module.descriptor.ModuleDescriptor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_AUTO_START;
import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_FILES;
import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_RESTART;
import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_SERVICE_NAME;
import static com.espirit.moddev.util.Preconditions.getNonNullList;

/**
 * Configures a single {@link de.espirit.firstspirit.module.Service FirstSpirit service}.
 */
public class Service implements Configurable {

	private static final Logger LOGGER = LoggerFactory.getLogger(Service.class);

	private final String _serviceName;
	@VisibleForTesting
	@JsonProperty(value = ATTR_AUTO_START)
	boolean _autoStart;
	@VisibleForTesting
	@JsonProperty(value = ATTR_RESTART)
	boolean _restart;
	@VisibleForTesting
	@JsonProperty(value = ATTR_FILES)
	List<String> _files;

	Service(@JsonProperty(value = ATTR_SERVICE_NAME, required = true) @NotNull final String serviceName) {
		_serviceName = Preconditions.checkNotEmpty(serviceName);
	}

	@NotNull
	public String getServiceName() {
		return _serviceName;
	}

	public boolean getAutoStart() {
		return _autoStart;
	}

	public boolean getRestart() {
		return _restart;
	}

	@NotNull
	public List<String> getFiles() {
		return getNonNullList(_files);
	}

	@NotNull
	@Override
	public ExecutionResult configure(@NotNull final ConfigurationContext context, @NotNull final ModuleDescriptor module) {
		final String moduleName = module.getModuleName();
		final String serviceName = getServiceName();
		final Connection connection = context.getConnection();
		final ModuleAdminAgent moduleAdminAgent = connection.getBroker().requireSpecialist(ModuleAdminAgent.TYPE);
		try {
			LOGGER.debug("Processing service configuration '{}:{}'...", moduleName, serviceName);

			final List<ComponentDescriptor> componentsLookUp = ModuleUtil.findComponentByNameOrDisplayName(module, ComponentDescriptor.Type.SERVICE, serviceName);
			if (componentsLookUp.isEmpty()) {
				return new ComponentNotFoundResult(moduleName, ComponentDescriptor.Type.SERVICE, serviceName);
			} else if (componentsLookUp.size() > 1) {
				return new MultipleComponentsFoundResult(moduleName, serviceName, ComponentDescriptor.Type.SERVICE, componentsLookUp);
			}

			// update the service name to the name of the found service
			final ComponentDescriptor componentDescriptor = componentsLookUp.get(0);
			final String serviceComponentName = componentDescriptor.getName();

			// Stop the service if the service is forced to restart.
			// We stop the service, change its configuration and start it again
			if (getRestart()) {
				LOGGER.info("Restart of service '{}' forced by configuration. Stopping service...", serviceComponentName);
				if (!ServiceUtils.stopService(moduleAdminAgent, serviceComponentName)) {
					return new ServiceStopFailedResult(moduleName, serviceComponentName);
				}
			}

			// upload files to server
			final ExecutionResults fileUploadResult = FileSystemUtil.uploadFiles(getFiles(), moduleAdminAgent.getServiceConfig(serviceComponentName));
			if (fileUploadResult.hasError()) {
				return fileUploadResult;
			}

			// update autoStart
			final boolean autoStart = getAutoStart();
			if (moduleAdminAgent.isAutostart(serviceComponentName) != autoStart) {
				LOGGER.info("{} auto start for service '{}:{}'...", (autoStart ? "Enabling" : "Disabling"), moduleName, serviceComponentName);
				moduleAdminAgent.setAutostart(serviceComponentName, autoStart);
				LOGGER.info("Auto start for service '{}:{}' {}.", moduleName, serviceComponentName, (autoStart ? "enabled" : "disabled"));
			}

			// force restart
			if (getRestart()) {
				LOGGER.info("Restarting service '{}'...", serviceComponentName);
				if (!ServiceUtils.startService(moduleAdminAgent, serviceComponentName)) {
					return new ServiceStartFailedResult(moduleName, serviceComponentName);
				}
			}

			LOGGER.debug("Service '{}:{}' configured.", moduleName, serviceComponentName);
			return new ServiceConfiguredResult(moduleName, serviceComponentName);
		} catch (final ServiceNotFoundException e) {
			LOGGER.debug("Service '{}:{}' not found.", moduleName, e.getServiceName());
			return new ServiceNotFoundResult(moduleName, e.getServiceName());
		}
	}

	private static abstract class ServiceResult {

		private final String _moduleName;
		private final String _serviceName;
		private final String _message;

		private ServiceResult(@NotNull final String moduleName, @NotNull final String serviceName, @NotNull final String message) {
			_moduleName = moduleName;
			_serviceName = serviceName;
			_message = message;
		}

		@NotNull
		public String getModuleName() {
			return _moduleName;
		}

		@NotNull
		public String getServiceName() {
			return _serviceName;
		}

		@Override
		public String toString() {
			return _message;
		}

	}

	@VisibleForTesting
	static class ServiceConfiguredResult extends ServiceResult implements ExecutionResult {

		@VisibleForTesting
		static final String MESSAGE = "Successfully configured service '%s:%s'.";

		private ServiceConfiguredResult(@NotNull final String moduleName, @NotNull final String serviceName) {
			super(moduleName, serviceName, String.format(MESSAGE, moduleName, serviceName));
		}

	}

	@VisibleForTesting
	static class ServiceNotFoundResult extends ComponentNotFoundResult implements ExecutionErrorResult<IllegalStateException> {

		private ServiceNotFoundResult(@NotNull final String moduleName, @NotNull final String serviceName) {
			super(moduleName, ComponentDescriptor.Type.SERVICE, serviceName);
		}

	}

	@VisibleForTesting
	static class ServiceStopFailedResult extends ServiceResult implements ExecutionErrorResult<IllegalStateException> {
		@VisibleForTesting
		static final String MESSAGE = "Error stopping service '%s:%s'!";

		private final IllegalStateException _exception;

		private ServiceStopFailedResult(@NotNull final String moduleName, @NotNull final String serviceName) {
			super(moduleName, serviceName, String.format(MESSAGE, moduleName, serviceName));
			_exception = new IllegalStateException(toString());
		}

		@NotNull
		@Override
		public IllegalStateException getException() {
			return _exception;
		}

	}

	@VisibleForTesting
	static class ServiceStartFailedResult extends ServiceResult implements ExecutionErrorResult<IllegalStateException> {

		@VisibleForTesting
		static final String MESSAGE = "Error starting service '%s:%s'!";

		private final IllegalStateException _illegalStateException;

		private ServiceStartFailedResult(@NotNull final String moduleName, @NotNull final String serviceName) {
			super(moduleName, serviceName, String.format(MESSAGE, moduleName, serviceName));
			_illegalStateException = new IllegalStateException(toString());
		}

		@NotNull
		@Override
		public IllegalStateException getException() {
			return _illegalStateException;
		}

	}

}
