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

package com.espirit.moddev.cli.commands.module.configureCommand.json;

import com.espirit.moddev.cli.api.result.ExecutionErrorResult;
import com.espirit.moddev.cli.api.result.ExecutionResult;
import com.espirit.moddev.cli.api.result.ExecutionResults;
import com.espirit.moddev.cli.commands.module.configureCommand.json.components.Components;
import com.espirit.moddev.cli.commands.module.configureCommand.json.components.Configurable;
import com.espirit.moddev.cli.commands.module.configureCommand.json.components.ConfigurationContext;
import com.espirit.moddev.shared.annotation.VisibleForTesting;
import com.espirit.moddev.util.JacksonUtil;
import com.espirit.moddev.util.Preconditions;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.espirit.firstspirit.module.descriptor.ModuleDescriptor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;

import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_COMPONENTS;
import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_MODULE_NAME;

/**
 * Configures a single {@link de.espirit.firstspirit.module.Module FirstSpirit module}.
 */
public class ModuleConfiguration implements Configurable {

	private static final Logger LOGGER = LoggerFactory.getLogger(ModuleConfiguration.class);

	/**
	 * Returns a {@link Collection} of {@link ModuleConfiguration configurations} for the file at the given path.
	 *
	 * @param pathToFile the path of the configuration file
	 * @return a {@link Collection} of configurations
	 * @throws IOException thrown if the file at the given path could not be de-serialized properly
	 */
	@NotNull
	public static Collection<ModuleConfiguration> fromPath(@NotNull final String pathToFile) throws IOException {
		LOGGER.info("Loading module configurations from file '{}'...", pathToFile);
		final Path path = Paths.get(pathToFile);
		if (!path.toFile().exists()) {
			throw new FileNotFoundException("File '" + pathToFile + "' does not exist.");
		}
		return fromBytes(Files.readAllBytes(path));
	}

	/**
	 * Returns a {@link Collection} of {@link ModuleConfiguration configurations} for the byte array.
	 *
	 * @param bytes the byte array
	 * @return a {@link Collection} of configurations
	 * @throws IOException thrown if the file at the given path could not be de-serialized properly
	 */
	@NotNull
	public static Collection<ModuleConfiguration> fromBytes(@NotNull final byte[] bytes) throws IOException {
		final ObjectMapper objectMapper = JacksonUtil.createInputMapper();
		final ModuleConfiguration[] configurations = objectMapper.readValue(bytes, ModuleConfiguration[].class);
		return Arrays.asList(configurations);
	}

	private final String _moduleName;
	private final Components _components;

	public ModuleConfiguration(@JsonProperty(value = ATTR_MODULE_NAME, required = true) @NotNull final String moduleName, @JsonProperty(value = ATTR_COMPONENTS, required = true) @NotNull final Components components) {
		_moduleName = Preconditions.checkNotEmpty(moduleName, "Attribute '" + ATTR_MODULE_NAME + "' is undefined.");
		_components = Preconditions.checkNotNull(components, "Attribute '" + ATTR_COMPONENTS + "' is undefined.");
	}

	@NotNull
	public String getModuleName() {
		return _moduleName;
	}

	@VisibleForTesting
	@NotNull
	public Components getComponents() {
		return _components;
	}

	@NotNull
	@Override
	public ExecutionResults configure(@NotNull final ConfigurationContext context, @NotNull final ModuleDescriptor module) {
		LOGGER.info("Configuring module '{}'...", getModuleName());
		// configure
		final ExecutionResults componentsConfigureResult = _components.configure(context, module);
		// build result
		final ExecutionResults results = new ExecutionResults();
		results.add(componentsConfigureResult);
		if (!componentsConfigureResult.hasError()) {
			results.add(new ModuleConfiguredResult(getModuleName()));
		} else {
			results.add(new ModuleConfigurationFailedResult(getModuleName()));
		}
		return results;
	}

	@VisibleForTesting
	public static class ModuleConfiguredResult implements ExecutionResult {

		@VisibleForTesting
		static final String MESSAGE = "Successfully configured module '%s'.";

		private final String _moduleName;

		@VisibleForTesting
		public ModuleConfiguredResult(@NotNull final String moduleName) {
			_moduleName = moduleName;
		}

		@Override
		public String toString() {
			return String.format(MESSAGE, _moduleName);
		}

	}

	@VisibleForTesting
	public static class ModuleConfigurationFailedResult implements ExecutionErrorResult<IllegalStateException> {

		@VisibleForTesting
		static final String MESSAGE = "Error configuring module '%s'!";

		private final String _moduleName;
		private final IllegalStateException _exception;

		private ModuleConfigurationFailedResult(@NotNull final String moduleName) {
			_moduleName = moduleName;
			_exception = new IllegalStateException(toString());
		}

		@NotNull
		@Override
		public IllegalStateException getException() {
			return _exception;
		}

		@Override
		public String toString() {
			return String.format(MESSAGE, _moduleName);
		}

	}
}
