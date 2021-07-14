/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2021 e-Spirit AG
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

package com.espirit.moddev.cli.commands.service.startCommand;

import com.espirit.moddev.cli.commands.service.ServiceCommandGroup;
import com.espirit.moddev.cli.commands.service.ServiceCommandNames;
import com.espirit.moddev.cli.commands.service.common.AbstractServiceCommand;
import com.espirit.moddev.cli.commands.service.common.ServiceInfo;
import com.espirit.moddev.cli.commands.service.common.ServiceInfo.ServiceStatus;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.help.Examples;
import de.espirit.firstspirit.agency.ModuleAdminAgent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.espirit.moddev.cli.commands.service.common.ServiceInfo.ServiceStatus.RUNNING;
import static com.espirit.moddev.cli.commands.service.common.ServiceInfo.ServiceStatus.STOPPED;

/**
 * Command class that can start a FirstSpirit services.
 *
 * @author Andreas Straub
 */
@Command(name = ServiceCommandNames.START, groupNames = ServiceCommandGroup.NAME, description = "Starts a FirstSpirit service if it is not running.")
@Examples(
		examples = {
				"service start",
				"service start --serviceNames \"UXBService\"",
				"service start --serviceNames \"UXBService, AnotherService\""
		},
		descriptions = {
				"Start the all services that have auto start enabled and are currently not running.",
				"Start the service 'UXBService', if it is not running.",
				"Start the services 'UXBService' and 'AnotherService', if they are not running."
		}
)
public class ServiceStartCommand extends AbstractServiceCommand {

	@Override
	@NotNull
	protected String getResultLoggingHeaderString(@NotNull List<ServiceInfo> serviceInfos) {
		return "Started " + serviceInfos.size() + " services:";
	}

	@NotNull
	@Override
	protected ServiceInfo processService(@NotNull final ModuleAdminAgent moduleAdminAgent, @NotNull final String serviceName) {
		ServiceStatus previousStatus = STOPPED;
		if (moduleAdminAgent.isRunning(serviceName)) {
			previousStatus = RUNNING;
		} else {
			moduleAdminAgent.startService(serviceName);
		}
		return new ServiceInfo(serviceName, previousStatus, RUNNING);
	}

}
