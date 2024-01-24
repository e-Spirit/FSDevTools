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

package com.espirit.moddev.cli.commands.module.configureCommand.json;

import com.espirit.moddev.cli.api.result.ExecutionErrorResult;
import com.espirit.moddev.cli.api.result.ExecutionResults;
import com.espirit.moddev.cli.commands.module.configureCommand.json.components.Components;
import com.espirit.moddev.cli.commands.module.configureCommand.json.components.ConfigurationContext;
import com.espirit.moddev.cli.commands.module.configureCommand.json.components.common.ComponentNotFoundResult;
import com.espirit.moddev.cli.configuration.GlobalConfig;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.google.common.collect.Lists;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.module.descriptor.ComponentDescriptor;
import de.espirit.firstspirit.module.descriptor.ModuleDescriptor;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_COMPONENTS;
import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_MODULE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("rawtypes")
public class ModuleConfigurationTest {

	private static final String MODULE_NAME = "testModule";

	@TempDir
	public File _temporaryFolder;

	private ObjectMapper _objectMapper;
	private ModuleDescriptor _moduleDescriptor;
	private Connection _connection;
	private ConfigurationContext _context;

	@BeforeEach
	public void setup() {
		_objectMapper = JsonTestUtil.createMapper();
		_moduleDescriptor = mock(ModuleDescriptor.class);
		when(_moduleDescriptor.getModuleName()).thenReturn(MODULE_NAME);
		_connection = mock(Connection.class);
		_context = new ConfigurationContext(_connection, mock(GlobalConfig.class));
	}

	@Test
	public void deserialize_module_name_is_not_defined() throws IOException {
		// setup
		final String json = JsonTestUtil.toJsonObject(
				JsonTestUtil.createMap(
						JsonTestUtil.createEntry(ATTR_COMPONENTS, new Components())
				)
		);

		// test
		Assertions.assertThrows(MismatchedInputException.class, () -> {
			_objectMapper.readValue(json, ModuleConfiguration.class);
		});
	}

	@Test
	public void deserialize_module_name_is_null() throws IOException {
		// setup
		final String json = JsonTestUtil.toJsonObject(
				JsonTestUtil.createMap(
						JsonTestUtil.createEntry(ATTR_MODULE_NAME, null),
						JsonTestUtil.createEntry(ATTR_COMPONENTS, new Components())
				)
		);

		// test
		try {
			_objectMapper.readValue(json, ModuleConfiguration.class);
			failBecauseExceptionWasNotThrown(JsonMappingException.class);
		} catch (final JsonMappingException e) {
			assertThat(e.getCause()).isExactlyInstanceOf(NullPointerException.class);
		}
	}

	@Test
	public void deserialize_module_name_is_empty_string() throws IOException {
		// setup
		final String json = JsonTestUtil.toJsonObject(
				JsonTestUtil.createMap(
						JsonTestUtil.createEntry(ATTR_MODULE_NAME, ""),
						JsonTestUtil.createEntry(ATTR_COMPONENTS, new Components())
				)
		);

		// test
		try {
			_objectMapper.readValue(json, ModuleConfiguration.class);
			failBecauseExceptionWasNotThrown(JsonMappingException.class);
		} catch (final JsonMappingException e) {
			assertThat(e.getCause()).isExactlyInstanceOf(IllegalArgumentException.class);
		}
	}

	@Test
	public void deserialize_components_is_not_defined() throws IOException {
		// setup
		final String json = JsonTestUtil.toJsonObject(
				JsonTestUtil.createMap(
						JsonTestUtil.createEntry(ATTR_MODULE_NAME, MODULE_NAME)
				)
		);

		// test
		Assertions.assertThrows(MismatchedInputException.class, () -> {
			_objectMapper.readValue(json, ModuleConfiguration.class);
		});
	}

	@Test
	public void deserialize_components_is_null() throws IOException {
		// setup
		final String json = JsonTestUtil.toJsonObject(
				JsonTestUtil.createMap(
						JsonTestUtil.createEntry(ATTR_MODULE_NAME, MODULE_NAME),
						JsonTestUtil.createEntry(ATTR_COMPONENTS, null)
				)
		);

		// test
		try {
			_objectMapper.readValue(json, ModuleConfiguration.class);
			failBecauseExceptionWasNotThrown(JsonMappingException.class);
		} catch (final JsonMappingException e) {
			assertThat(e.getCause()).isExactlyInstanceOf(NullPointerException.class);
		}
	}

	@Test
	public void deserialize() throws IOException {
		// setup
		final Components components = new Components();
		final String json = JsonTestUtil.toJsonObject(
				JsonTestUtil.createMap(
						JsonTestUtil.createEntry(ATTR_MODULE_NAME, MODULE_NAME),
						JsonTestUtil.createEntry(ATTR_COMPONENTS, components)
				)
		);

		// test
		final ModuleConfiguration deserialized = _objectMapper.readValue(json, ModuleConfiguration.class);

		// verify
		assertThat(deserialized.getModuleName()).isEqualTo(MODULE_NAME);
		assertThat(equals(deserialized.getComponents(), components)).isTrue();
	}

	@Test
	public void fromBytes() throws IOException {
		// setup
		final String moduleName1 = "module1";
		final String moduleName2 = "module2";
		final String json = _objectMapper.writeValueAsString(Lists.newArrayList(new ModuleConfiguration(moduleName1, new Components()), new ModuleConfiguration(moduleName2, new Components())));

		// test
		final Collection<ModuleConfiguration> deserialized = ModuleConfiguration.fromBytes(json.getBytes());

		// verify
		assertThat(deserialized).isInstanceOf(List.class);
		assertThat(((List<ModuleConfiguration>) deserialized).get(0).getModuleName()).isEqualTo(moduleName1);
		assertThat(((List<ModuleConfiguration>) deserialized).get(1).getModuleName()).isEqualTo(moduleName2);
	}

	@Test
	public void fromFile() throws IOException {
		// setup
		final String moduleName1 = "module1";
		final String moduleName2 = "module2";
		final String json = _objectMapper.writeValueAsString(Lists.newArrayList(new ModuleConfiguration(moduleName1, new Components()), new ModuleConfiguration(moduleName2, new Components())));
		final File file = _temporaryFolder.toPath().resolve("testFile.json").toFile();
		final FileOutputStream fileOutputStream = new FileOutputStream(file, true);
		fileOutputStream.write(json.getBytes());
		fileOutputStream.flush();
		fileOutputStream.close();

		// test
		final Collection<ModuleConfiguration> deserialized = ModuleConfiguration.fromPath(file.getAbsolutePath());

		// verify
		assertThat(deserialized).isInstanceOf(List.class);
		assertThat(((List<ModuleConfiguration>) deserialized).get(0).getModuleName()).isEqualTo(moduleName1);
		assertThat(((List<ModuleConfiguration>) deserialized).get(1).getModuleName()).isEqualTo(moduleName2);
	}

	@Test
	public void fromFile_file_does_not_exist() {
		Assertions.assertThrows(FileNotFoundException.class, () -> {
			ModuleConfiguration.fromPath(new File("fileDoesNotExist.json").getAbsolutePath());
		});
	}

	@Test
	public void configure() {
		// setup
		final Components components = spy(new Components());
		final ModuleConfiguration moduleConfiguration = new ModuleConfiguration(_moduleDescriptor.getModuleName(), components);

		// test
		final ExecutionResults configure = moduleConfiguration.configure(_context, _moduleDescriptor);

		// verify
		verify(components, times(1)).configure(_context, _moduleDescriptor);
		assertThat(configure.hasError()).isFalse();
		assertThat(configure.get(0)).isInstanceOf(ModuleConfiguration.ModuleConfiguredResult.class);
		assertThat(configure.get(0).toString()).isEqualTo(String.format(ModuleConfiguration.ModuleConfiguredResult.MESSAGE, _moduleDescriptor.getModuleName()));
	}

	@Test
	public void configure_failed() {
		// setup
		final Components components = mock(Components.class);
		final ModuleConfiguration moduleConfiguration = new ModuleConfiguration(_moduleDescriptor.getModuleName(), components);
		final ExecutionResults results = new ExecutionResults();
		results.add(new ComponentNotFoundResult(MODULE_NAME, ComponentDescriptor.Type.PROJECTAPP, "componentName"));
		when(components.configure(_context, _moduleDescriptor)).thenReturn(results);

		// test
		final ExecutionResults configure = moduleConfiguration.configure(_context, _moduleDescriptor);

		// verify
		verify(components, times(1)).configure(_context, _moduleDescriptor);
		assertThat(configure.hasError()).isTrue();
		assertThat(configure.get(0)).isInstanceOf(ComponentNotFoundResult.class);
		assertThat(configure.get(1)).isInstanceOf(ModuleConfiguration.ModuleConfigurationFailedResult.class);
		assertThat(((ExecutionErrorResult) configure.get(1)).getThrowable()).isInstanceOf(IllegalStateException.class);
		assertThat(configure.get(1).toString()).isEqualTo(String.format(ModuleConfiguration.ModuleConfigurationFailedResult.MESSAGE, _moduleDescriptor.getModuleName()));
	}

	public static boolean equals(@Nullable final Components first, @Nullable final Components second) {
		if (first == second) return true;
		if (first == null || second == null) {
			return false;
		}
		if (first.getClass() != second.getClass()) return false;
		return Objects.equals(first.getWebComponents(), second.getWebComponents()) &&
				Objects.equals(first.getProjectComponents(), second.getProjectComponents()) &&
				Objects.equals(first.getServices(), second.getServices());
	}

}
