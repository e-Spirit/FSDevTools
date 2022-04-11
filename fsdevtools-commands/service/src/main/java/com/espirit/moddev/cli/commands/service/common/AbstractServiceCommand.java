/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2021 e-Spirit GmbH
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

package com.espirit.moddev.cli.commands.service.common;

import com.espirit.moddev.cli.api.annotations.ParameterExamples;
import com.espirit.moddev.cli.api.annotations.ParameterType;
import com.espirit.moddev.cli.commands.SimpleCommand;
import com.espirit.moddev.cli.utils.ServiceUtils;
import com.espirit.moddev.shared.annotation.VisibleForTesting;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import de.espirit.firstspirit.access.ServiceNotFoundException;
import de.espirit.firstspirit.agency.ModuleAdminAgent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Base class to process the FirstSpirit services. Can be used to implement restart, start and stop Commands.
 */
public abstract class AbstractServiceCommand extends SimpleCommand<ServiceProcessResult> {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractServiceCommand.class);

	@Option(type = OptionType.COMMAND, name = {"-n", "--serviceNames"}, description = "Comma separated list of names of the FirstSpirit services to be processed. If not provided, all services with auto start enabled will be processed.")
	@ParameterExamples(
			examples = {
					"--serviceNames firstService,secondService",
					"-n myService",
			},
			descriptions = {
					"Sets the given services to 'firstService' and 'secondService'.",
					"Sets the given services to 'myService'.",
			}
	)
	@ParameterType(name = "List<String>")
	private String _serviceNames;

	@Override
	public ServiceProcessResult call() {
		try {
			final ModuleAdminAgent moduleAdminAgent = _context.getConnection().getBroker().requestSpecialist(ModuleAdminAgent.TYPE);
			return new ServiceProcessResult(processServices(moduleAdminAgent));
		} catch (ServiceNotFoundException s) {
			return new ServiceProcessResult(new ServiceNotFoundException(s.getLocalizedMessage(), s));
		} catch (Exception e) {
			return new ServiceProcessResult(new IllegalStateException("Cannot request specialist ModuleAdminAgent!", e));
		}
	}

	/**
	 * Set service names as a comma separated string.
	 *
	 * @param serviceNames the services that should be processed as comma separated string
	 */
	@VisibleForTesting
	public void setServiceNames(@NotNull final String serviceNames) {
		_serviceNames = serviceNames;
	}

	/**
	 * Calls processService for a bunch of services and returns a list of processing results.
	 * If no service names are configured, all services available on the FirstSpirit server are handled.
	 *
	 * @param moduleAdminAgent which is required to retrieve service names
	 * @return the list of configured services or all services available on the FirstSpirit server
	 */
	@NotNull
	protected List<ServiceInfo> processServices(final ModuleAdminAgent moduleAdminAgent) {
		final List<String> splitServiceNames = getOptionalServiceNames();

		if (splitServiceNames.isEmpty()) {
			LOGGER.info("No --serviceNames parameter given for processing. All services will be processed!");
		}

		final List<String> serviceNamesToProcess = splitServiceNames.isEmpty() ? ServiceUtils.getAllServiceNamesFromServer(moduleAdminAgent) : splitServiceNames;
		final List<ServiceInfo> results = serviceNamesToProcess
				.stream()
				.map(serviceName -> processService(moduleAdminAgent, serviceName))
				.collect(Collectors.toList());

		logProcessResults(results);
		return results;
	}

	@NotNull
	protected abstract String getResultLoggingHeaderString(@NotNull final List<ServiceInfo> serviceInfos);

	protected void logProcessResults(@NotNull final List<ServiceInfo> serviceInfos) {
		final StringBuilder builder = new StringBuilder();
		builder.append(getResultLoggingHeaderString(serviceInfos));
		builder.append("\n");

		for (ServiceInfo info : serviceInfos) {
			builder.append("\t");
			builder.append(info.getServiceName());
			builder.append("\t");
			builder.append("(");
			builder.append(info.getPreviousStatus().toString().toLowerCase());
			builder.append(" -> ");
			builder.append(info.getCurrentStatus().toString().toLowerCase());
			builder.append(")");
			builder.append("\n");
		}
		LOGGER.info(builder.toString());
	}

	/**
	 * Processed the service with given name
	 *
	 * @param moduleAdminAgent ServiceManager instance
	 * @param serviceName      name of service to process
	 */
	@NotNull
	protected abstract ServiceInfo processService(@NotNull final ModuleAdminAgent moduleAdminAgent, @NotNull final String serviceName) throws ServiceNotFoundException;

	/**
	 * Get configured service names as a {@link List}.
	 *
	 * @return configured service names as a {@link List} or empty {@link List} if no names where configured.
	 */
	@NotNull
	protected List<String> getOptionalServiceNames() {
		if (StringUtils.isBlank(_serviceNames)) {
			return Collections.emptyList();
		}
		return Arrays.stream(StringUtils.split(_serviceNames, ","))
				.filter(StringUtils::isNotBlank)
				.map(StringUtils::trim)
				.collect(Collectors.toList());
	}

}
