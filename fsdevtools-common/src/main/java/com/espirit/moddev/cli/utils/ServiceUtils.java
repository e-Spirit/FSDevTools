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

package com.espirit.moddev.cli.utils;

import de.espirit.firstspirit.access.ServiceNotFoundException;
import de.espirit.firstspirit.agency.ModuleAdminAgent;
import de.espirit.firstspirit.module.descriptor.AbstractDescriptor;
import de.espirit.firstspirit.module.descriptor.ServiceDescriptor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.espirit.firstspirit.module.descriptor.ComponentDescriptor.Type.SERVICE;

public class ServiceUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceUtils.class);

	@NotNull
	public static List<String> getAllServiceNamesFromServer(@NotNull final ModuleAdminAgent moduleAdminAgent) {
		return moduleAdminAgent
				.getModules()
				.stream()
				.flatMap(moduleDescriptor -> Stream.of(moduleDescriptor.getComponents()))
				.filter(it -> SERVICE.equals(it.getType()))
				.map(ServiceDescriptor.class::cast)
				.map(AbstractDescriptor::getName)
				.sorted()
				.collect(Collectors.toList());
	}

	public static boolean startService(@NotNull final ModuleAdminAgent moduleAdminAgent, @NotNull final String serviceName) {
		try {
			if (moduleAdminAgent.isRunning(serviceName)) {
				LOGGER.debug("Service '{}' is already running.", serviceName);
				return true;
			}
			LOGGER.debug("Starting service '{}'...", serviceName);
			moduleAdminAgent.startService(serviceName);
			LOGGER.debug("Service '{}' started.", serviceName);
			return true;
		} catch (final ServiceNotFoundException e) {
			LOGGER.error("Service '{}' not found.", serviceName);
			return false;
		}
	}

	public static boolean stopService(@NotNull final ModuleAdminAgent moduleAdminAgent, @NotNull final String serviceName) {
		try {
			if (moduleAdminAgent.isRunning(serviceName)) {
				LOGGER.debug("Service '{}' is not running.", serviceName);
				return true;
			}
			LOGGER.debug("Stopping service '{}'...", serviceName);
			moduleAdminAgent.stopService(serviceName);
			LOGGER.debug("Service '{}' stopped.", serviceName);
			return true;
		} catch (final ServiceNotFoundException e) {
			LOGGER.error("Service '{}' not found.", serviceName);
			return false;
		}
	}

}
