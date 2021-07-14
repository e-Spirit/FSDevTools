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

package com.espirit.moddev.cli.commands.module.configureCommand.json.components;

import com.espirit.moddev.cli.api.result.ExecutionResults;
import com.espirit.moddev.shared.StringUtils;
import com.espirit.moddev.shared.annotation.VisibleForTesting;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.espirit.firstspirit.module.descriptor.ModuleDescriptor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_PROJECT_COMPONENTS;
import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_SERVICES;
import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_WEB_COMPONENTS;
import static com.espirit.moddev.util.Preconditions.getNonNullList;

/**
 * Configures (and installs) a set of {@link ComponentWebApps web components}, {@link ComponentProjectApps project components} and {@link Service services}.
 */
public class Components implements Configurable {

	private static final Logger LOGGER = LoggerFactory.getLogger(Components.class);

	@VisibleForTesting
	@JsonProperty(value = ATTR_WEB_COMPONENTS)
	List<ComponentWebApps> _webComponents;

	@VisibleForTesting
	@JsonProperty(value = ATTR_PROJECT_COMPONENTS)
	List<ComponentProjectApps> _projectComponents;

	@VisibleForTesting
	@JsonProperty(value = ATTR_SERVICES)
	List<Service> _services;

	@NotNull
	@Override
	public ExecutionResults configure(@NotNull final ConfigurationContext context, @NotNull final ModuleDescriptor module) {
		LOGGER.debug("Processing components of module '{}'...", module.getModuleName());
		final ExecutionResults results = new ExecutionResults();
		results.add(configure(context, module, getServices(), "service"));
		results.add(configure(context, module, getProjectComponents(), "project component"));
		results.add(configure(context, module, getWebComponents(), "web component"));
		return results;
	}

	@VisibleForTesting
	@NotNull
	ExecutionResults configure(@NotNull final ConfigurationContext context, @NotNull final ModuleDescriptor module, @NotNull final List<? extends Configurable> configurables, @NotNull final String title) {
		if (configurables.isEmpty()) {
			LOGGER.debug("No {} configurations found.", title);
		} else {
			LOGGER.debug("Processing {} {} " + StringUtils.toPluralRespectingString(configurables.size(), "configuration") + "...", configurables.size(), title);
		}
		final ExecutionResults results = new ExecutionResults();
		configurables
				.stream()
				.map(configurable -> configurable.configure(context, module))
				.forEach(results::add);
		return results;
	}

	@VisibleForTesting
	@NotNull
	public List<ComponentWebApps> getWebComponents() {
		return getNonNullList(_webComponents);
	}

	@VisibleForTesting
	@NotNull
	public List<ComponentProjectApps> getProjectComponents() {
		return getNonNullList(_projectComponents);
	}

	@VisibleForTesting
	@NotNull
	public List<Service> getServices() {
		return getNonNullList(_services);
	}

}
