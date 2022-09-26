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

package com.espirit.moddev.cli.commands.service.stopCommand;

import com.espirit.moddev.cli.commands.service.ServiceCommandGroup;
import com.espirit.moddev.cli.commands.service.ServiceCommandNames;
import com.espirit.moddev.cli.commands.service.common.AbstractServiceCommand;
import com.espirit.moddev.cli.commands.service.common.ServiceInfo;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.help.Examples;
import de.espirit.firstspirit.agency.ModuleAdminAgent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.espirit.moddev.cli.commands.service.common.ServiceInfo.ServiceStatus;
import static com.espirit.moddev.cli.commands.service.common.ServiceInfo.ServiceStatus.RUNNING;
import static com.espirit.moddev.cli.commands.service.common.ServiceInfo.ServiceStatus.STOPPED;

/**
 * Command class that can stop a FirstSpirit services.
 *
 * @author e-Spirit GmbH
 */
@Command(name = ServiceCommandNames.STOP, groupNames = ServiceCommandGroup.NAME, description = "Stops a FirstSpirit service if it is running.")
@Examples(
		examples = {
				"service stop",
				"service stop -n UXBService",
				"service stop --serviceNames UXBService,AnotherService"
		},
		descriptions = {
				"Stop the all services that have auto start enabled and currently running",
				"Stop the service: 'UXBService' if it's currently running",
				"Stop the services: 'UXBService' and 'AnotherService' if they are currently running"
		}
)
public class ServiceStopCommand extends AbstractServiceCommand {

	@Override
	@NotNull
	protected String getResultLoggingHeaderString(@NotNull final List<ServiceInfo> serviceInfos) {
		return "Stopped " + serviceInfos.size() + " services:";
	}

	@NotNull
	@Override
	protected ServiceInfo processService(@NotNull final ModuleAdminAgent moduleAdminAgent, @NotNull final String serviceName) {
		ServiceStatus previousStatus = STOPPED;
		if (moduleAdminAgent.isRunning(serviceName)) {
			moduleAdminAgent.stopService(serviceName);
			previousStatus = RUNNING;
		}
		return new ServiceInfo(serviceName, previousStatus, STOPPED);
	}

}
