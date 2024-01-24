/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2024 Crownpeak Technology GmbH
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

import de.espirit.firstspirit.agency.ModuleAdminAgent;
import de.espirit.firstspirit.module.descriptor.ComponentDescriptor;
import de.espirit.firstspirit.module.descriptor.ModuleDescriptor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class ModuleUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(ModuleUtil.class);

	@NotNull
	public static Optional<ModuleDescriptor> getModuleByName(@NotNull final ModuleAdminAgent moduleAdminAgent, @NotNull final String moduleName) {
		return moduleAdminAgent.getModules()
				.stream()
				.filter(moduleDescriptor -> moduleName.equalsIgnoreCase(moduleDescriptor.getModuleName()))
				.findFirst();
	}

	@NotNull
	public static List<ModuleDescriptor> getModulesByDisplayName(@NotNull final ModuleAdminAgent moduleAdminAgent, @NotNull final String displayName) {
		return moduleAdminAgent.getModules()
				.stream()
				.filter(moduleDescriptor -> displayName.equalsIgnoreCase(moduleDescriptor.getDisplayName()))
				.collect(Collectors.toList());
	}

	@NotNull
	public static Optional<ComponentDescriptor> getComponentByName(@NotNull final ModuleDescriptor moduleDescriptor, @NotNull final ComponentDescriptor.Type type, @NotNull final String name) {
		return Arrays.stream(moduleDescriptor.getComponents())
				.filter(componentDescriptor -> type.equals(componentDescriptor.getType()) && name.equalsIgnoreCase(componentDescriptor.getName()))
				.findFirst();
	}

	@NotNull
	public static List<ComponentDescriptor> getComponentsByDisplayName(@NotNull final ModuleDescriptor moduleDescriptor, @NotNull final ComponentDescriptor.Type type, @NotNull final String displayName) {
		return Arrays.stream(moduleDescriptor.getComponents())
				.filter(componentDescriptor -> type.equals(componentDescriptor.getType()) && displayName.equalsIgnoreCase(componentDescriptor.getDisplayName()))
				.collect(Collectors.toList());
	}

	@NotNull
	public static List<ComponentDescriptor> findComponentByNameOrDisplayName(@NotNull final ModuleDescriptor moduleDescriptor, @NotNull final ComponentDescriptor.Type type, @NotNull final String nameOrDisplayName) {
		final String moduleName = moduleDescriptor.getModuleName();
		final Optional<ComponentDescriptor> optionalComponent = ModuleUtil.getComponentByName(moduleDescriptor, type, nameOrDisplayName);
		if (optionalComponent.isPresent()) {
			final ComponentDescriptor component = optionalComponent.get();
			LOGGER.debug("Found component '{}' ( type = {} ) by using name '{}'.", component.getName(), type, component.getDisplayName());
			return Collections.singletonList(component);
		}
		LOGGER.debug("Component with type {} and name '{}' not found, using display names for lookup...", type, nameOrDisplayName);
		final List<ComponentDescriptor> componentsByDisplayName = ModuleUtil.getComponentsByDisplayName(moduleDescriptor, type, nameOrDisplayName);
		if (componentsByDisplayName.isEmpty()) {
			LOGGER.debug("Component with type {} and display name '{}' not found!", type, nameOrDisplayName);
			return Collections.emptyList();
		}
		if (componentsByDisplayName.size() > 1) {
			LOGGER.debug("Multiple components of type {} with display name '{}' in module '{}' found!", type, componentsByDisplayName, moduleName);
		} else {
			final ComponentDescriptor firstComponent = componentsByDisplayName.get(0);
			LOGGER.debug("Found component '{}' ( type = {} ) by using display name '{}'.", firstComponent.getName(), type, firstComponent.getDisplayName());
		}
		return componentsByDisplayName;
	}

}
