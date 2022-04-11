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

package com.espirit.moddev.cli.commands.service.listCommand;

import com.espirit.moddev.cli.commands.SimpleCommand;
import com.espirit.moddev.cli.commands.service.ServiceCommandGroup;
import com.espirit.moddev.cli.commands.service.ServiceCommandNames;
import com.espirit.moddev.cli.utils.ServiceUtils;
import com.espirit.moddev.cli.results.SimpleResult;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.help.Examples;
import de.espirit.firstspirit.agency.ModuleAdminAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Command class that can list FirstSpirit services.
 *
 * @author e-Spirit GmbH
 */
@Command(name = ServiceCommandNames.LIST, groupNames = ServiceCommandGroup.NAME, description = "Lists all services with name that can be found on the FirstSpirit.")
@Examples(
		examples = {
				"service list"
		},
		descriptions = {
				"Simply lists the all services that can be found on the FirstSpirit server."
		}
)
public class ServiceListCommand extends SimpleCommand<SimpleResult<Boolean>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceListCommand.class);

	@Override
	public SimpleResult<Boolean> call() {

		try {
			ModuleAdminAgent moduleAdminAgent = _context.getConnection().getBroker().requestSpecialist(ModuleAdminAgent.TYPE);

			List<String> serviceNames = ServiceUtils.getAllServiceNamesFromServer(moduleAdminAgent);
			printServicesCountAndServiceNames(serviceNames, moduleAdminAgent);

			return new SimpleResult<>(true);
		} catch (Exception e) {
			return new SimpleResult<>(new IllegalStateException("Cannot request specialist ModuleAdminAgent!", e));
		}
	}

	private void printServicesCountAndServiceNames(List<String> serviceNames, ModuleAdminAgent moduleAdminAgent) {
		StringBuilder builder = new StringBuilder("");
		if (!serviceNames.isEmpty()) {
			builder.append(String.format("Found %s services on Firstspirit server:", serviceNames.size()));
		} else {
			builder.append("No services found on Firstspirit server.");
		}
		builder.append("\n");

		for (String serviceName : serviceNames) {
			builder.append("\t");
			builder.append(serviceName);
			builder.append(" (");
			builder.append(moduleAdminAgent.isRunning(serviceName) ? "running" : "stopped");
			builder.append(")");
			builder.append("\n");
		}
		LOGGER.info(builder.toString());
	}
}
