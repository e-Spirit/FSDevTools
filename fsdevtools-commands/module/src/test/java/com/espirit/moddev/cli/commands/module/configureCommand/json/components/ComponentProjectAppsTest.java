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

package com.espirit.moddev.cli.commands.module.configureCommand.json.components;

import com.espirit.moddev.cli.api.result.ExecutionResult;
import com.espirit.moddev.cli.api.result.ExecutionResults;
import com.espirit.moddev.cli.commands.module.configureCommand.json.JsonTestUtil;
import com.espirit.moddev.cli.commands.module.configureCommand.json.components.common.ComponentNotFoundResult;
import com.espirit.moddev.cli.commands.module.configureCommand.json.components.common.MultipleComponentsFoundResult;
import com.espirit.moddev.cli.commands.module.configureCommand.json.components.common.ProjectNotFoundResult;
import com.espirit.moddev.cli.commands.module.utils.FileSystemUtil;
import com.espirit.moddev.cli.configuration.GlobalConfig;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.agency.ModuleAdminAgent;
import de.espirit.firstspirit.agency.SpecialistsBroker;
import de.espirit.firstspirit.io.FileSystem;
import de.espirit.firstspirit.io.MemoryFileSystem;
import de.espirit.firstspirit.module.descriptor.ComponentDescriptor;
import de.espirit.firstspirit.module.descriptor.ModuleDescriptor;
import de.espirit.firstspirit.server.module.ModuleException;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_COMPONENT_NAME;
import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_FILES;
import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_PROJECT_APPS;
import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_PROJECT_NAME;
import static com.espirit.moddev.cli.commands.module.utils.FileSystemUtilTest.verifyDirDoesNotExist;
import static com.espirit.moddev.cli.commands.module.utils.FileSystemUtilTest.verifyFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
public class ComponentProjectAppsTest {

	private static final List<ComponentProjectApps.ProjectApp> APP_AMOUNT_0 = new ArrayList<>();
	private static final List<ComponentProjectApps.ProjectApp> APP_AMOUNT_1;

	static {
		final ComponentProjectApps.ProjectApp projectApp = new ComponentProjectApps.ProjectApp();
		projectApp._projectName = "myProject";
		APP_AMOUNT_1 = Lists.newArrayList(projectApp);
	}

	private static final String COMPONENT_NAME = "testComponent";
	private static final String PROJECT_NAME_1 = "firstProject";
	private static final String PROJECT_NAME_2 = "secondProject";
	private static final String FILE_NAME_1 = "dir/1.json";
	private static final String FILE_NAME_2 = "dir/subDir/2.json";
	private static final String FILE_NAME_3 = "3.json";
	private static final String MODULE_NAME = "testModule";

	private MemoryFileSystem _fileSystem;
	private ModuleAdminAgent _moduleAdminAgent;
	private Connection _connection;
	private ConfigurationContext _context;
	private ModuleDescriptor _moduleDescriptor;
	private Project _project1;
	private Project _project2;

	private ObjectMapper _objectMapper;

	@BeforeEach
	public void setup() {
		_objectMapper = JsonTestUtil.createMapper();

		// integrative tests
		_fileSystem = new MemoryFileSystem();
		_moduleDescriptor = mock(ModuleDescriptor.class);
		when(_moduleDescriptor.getModuleName()).thenReturn(MODULE_NAME);
		_moduleAdminAgent = spy(mock(ModuleAdminAgent.class));
		_connection = mock(Connection.class);
		_context = new ConfigurationContext(_connection, mock(GlobalConfig.class));
		{
			final Project project = mock(Project.class);
			when(project.getName()).thenReturn(PROJECT_NAME_1);
			when(_connection.getProjectByName(PROJECT_NAME_1)).thenReturn(project);
			_project1 = project;
		}
		{
			final Project project = mock(Project.class);
			when(project.getName()).thenReturn(PROJECT_NAME_2);
			when(_connection.getProjectByName(PROJECT_NAME_2)).thenReturn(project);
			_project2 = project;
		}
		final SpecialistsBroker broker = mock(SpecialistsBroker.class);
		when(_connection.getBroker()).thenReturn(broker);
		when(broker.requireSpecialist(ModuleAdminAgent.TYPE)).thenReturn(_moduleAdminAgent);
	}

	@Test
	public void deserialize_componentName_is_undefined() {
		// setup
		final String json = JsonTestUtil.toJsonObject(JsonTestUtil.createMap(JsonTestUtil.createEntry(ATTR_PROJECT_APPS, APP_AMOUNT_1)));

		// test
		org.junit.jupiter.api.Assertions.assertThrows(MismatchedInputException.class, () -> _objectMapper.readValue(json, ComponentProjectApps.class));
	}

	@Test
	public void deserialize_componentName_is_defined_as_null() throws IOException {
		// setup
		final String json = JsonTestUtil.toJsonObject(JsonTestUtil.createMap(JsonTestUtil.createEntry(ATTR_COMPONENT_NAME, null), JsonTestUtil.createEntry(ATTR_PROJECT_APPS, APP_AMOUNT_1)));

		// test
		try {
			_objectMapper.readValue(json, ComponentProjectApps.class);
			failBecauseExceptionWasNotThrown(JsonMappingException.class);
		} catch (final JsonMappingException e) {
			Assertions.assertThat(e.getCause()).isExactlyInstanceOf(NullPointerException.class);
		}
	}

	@Test
	public void deserialize_componentName_is_defined_as_empty_string() throws IOException {
		// setup
		final String json = JsonTestUtil.toJsonObject(JsonTestUtil.createMap(JsonTestUtil.createEntry(ATTR_COMPONENT_NAME, ""), JsonTestUtil.createEntry(ATTR_PROJECT_APPS, APP_AMOUNT_1)));

		// test
		try {
			_objectMapper.readValue(json, ComponentProjectApps.class);
			failBecauseExceptionWasNotThrown(JsonMappingException.class);
		} catch (final JsonMappingException e) {
			Assertions.assertThat(e.getCause()).isExactlyInstanceOf(IllegalArgumentException.class);
		}
	}

	@Test
	public void deserialize_componentName_is_defined_as_empty_string_with_whitespaces() throws IOException {
		// setup
		final String json = JsonTestUtil.toJsonObject(JsonTestUtil.createMap(JsonTestUtil.createEntry(ATTR_COMPONENT_NAME, " "), JsonTestUtil.createEntry(ATTR_PROJECT_APPS, APP_AMOUNT_1)));

		// test
		try {
			_objectMapper.readValue(json, ComponentProjectApps.class);
			failBecauseExceptionWasNotThrown(JsonMappingException.class);
		} catch (final JsonMappingException e) {
			Assertions.assertThat(e.getCause()).isExactlyInstanceOf(IllegalArgumentException.class);
		}
	}

	@Test
	public void deserialize_projectApps_are_undefined() {
		// setup
		final String json = JsonTestUtil.toJsonObject(JsonTestUtil.createMap(JsonTestUtil.createEntry(ATTR_COMPONENT_NAME, "myProjectComponent")));

		// test
		org.junit.jupiter.api.Assertions.assertThrows(MismatchedInputException.class, () -> _objectMapper.readValue(json, ComponentProjectApps.class));
	}

	@Test
	public void deserialize_projectApps_has_length_0() throws IOException {
		// setup
		final String json = JsonTestUtil.toJsonObject(JsonTestUtil.createMap(JsonTestUtil.createEntry(ATTR_COMPONENT_NAME, "myProjectComponent"), JsonTestUtil.createEntry(ATTR_PROJECT_APPS, APP_AMOUNT_0)));

		// test
		try {
			_objectMapper.readValue(json, ComponentProjectApps.class);
			failBecauseExceptionWasNotThrown(JsonMappingException.class);
		} catch (final JsonMappingException e) {
			Assertions.assertThat(e.getCause()).isExactlyInstanceOf(IllegalArgumentException.class);
		}
	}

	@Test
	public void deserialize() throws IOException {
		// setup
		final ComponentProjectApps.ProjectApp[] projectApps = new ComponentProjectApps.ProjectApp[2];
		projectApps[0] = new ComponentProjectApps.ProjectApp();
		projectApps[0]._projectName = PROJECT_NAME_1;
		projectApps[0]._files = Lists.newArrayList(FILE_NAME_1, FILE_NAME_2);
		projectApps[1] = new ComponentProjectApps.ProjectApp();
		projectApps[1]._projectName = PROJECT_NAME_2;
		projectApps[1]._files = Lists.newArrayList(FILE_NAME_3);
		final String json = JsonTestUtil.toJsonObject(JsonTestUtil.createMap(JsonTestUtil.createEntry(ATTR_COMPONENT_NAME, COMPONENT_NAME), JsonTestUtil.createEntry(ATTR_PROJECT_APPS, projectApps)));

		// test
		final ComponentProjectApps deserialized = _objectMapper.readValue(json, ComponentProjectApps.class);

		// verify
		assertThat(deserialized.getComponentName()).isEqualTo(COMPONENT_NAME);
		assertThat(deserialized.getProjectApps()).hasSize(2);
		{
			final ComponentProjectApps.ProjectApp projectApp = deserialized.getProjectApps().get(0);
			assertThat(projectApp.getProjectName(_context)).isEqualTo(PROJECT_NAME_1);
			assertThat(projectApp.getFiles()).hasSize(2);
			assertThat(projectApp.getFiles().get(0)).isEqualTo(FILE_NAME_1);
			assertThat(projectApp.getFiles().get(1)).isEqualTo(FILE_NAME_2);
		}
		{
			final ComponentProjectApps.ProjectApp projectApp = deserialized.getProjectApps().get(1);
			assertThat(projectApp.getProjectName(_context)).isEqualTo(PROJECT_NAME_2);
			assertThat(projectApp.getFiles()).hasSize(1);
			assertThat(projectApp.getFiles().get(0)).isEqualTo(FILE_NAME_3);
		}
	}

	@Test
	public void deserialize_projectApp_project_is_undefined() throws IOException {
		// setup
		final String json = JsonTestUtil.toJsonObject(JsonTestUtil.createMap(JsonTestUtil.createEntry(ATTR_FILES, new String[0])));

		// test
		final ComponentProjectApps.ProjectApp result = _objectMapper.readValue(json, ComponentProjectApps.ProjectApp.class);

		// verify
		assertThat(result.getRawProjectName()).isNull();
	}

	@Test
	public void deserialize_projectApp_projectName_is_null() throws IOException {
		// setup
		final String json = JsonTestUtil.toJsonObject(JsonTestUtil.createMap(JsonTestUtil.createEntry(ATTR_PROJECT_NAME, null)));

		// test
		final ComponentProjectApps.ProjectApp result = _objectMapper.readValue(json, ComponentProjectApps.ProjectApp.class);

		// verify
		assertThat(result.getRawProjectName()).isNull();
	}

	@Test
	public void deserialize_projectApp_projectName_is_empty_string() throws IOException {
		// setup
		final String json = JsonTestUtil.toJsonObject(JsonTestUtil.createMap(JsonTestUtil.createEntry(ATTR_PROJECT_NAME, "")));

		// test
		final ComponentProjectApps.ProjectApp result = _objectMapper.readValue(json, ComponentProjectApps.ProjectApp.class);

		// verify
		assertThat(result.getRawProjectName()).isNull();
	}

	@Test
	public void deserialize_projectApp_files_are_undefined() throws IOException {
		// setup
		final String json = JsonTestUtil.toJsonObject(JsonTestUtil.createMap(JsonTestUtil.createEntry(ATTR_FILES, new String[0])));

		// test
		final ComponentProjectApps.ProjectApp result = _objectMapper.readValue(json, ComponentProjectApps.ProjectApp.class);

		// verify
		assertThat(result.getRawProjectName()).isNull();
		assertThat(result.getFiles()).isEmpty();
	}

	@Test
	public void configure() {
		// setup
		final String projectName = "testProject";
		final Connection connection = mock(Connection.class);
		final ConfigurationContext context = new ConfigurationContext(connection, mock(GlobalConfig.class));
		// mock project app #1
		final ComponentProjectApps.ProjectApp projectApp1 = spy(mock(ComponentProjectApps.ProjectApp.class));
		final ComponentProjectApps.ProjectAppResult projectApp1Result = new ComponentProjectApps.ProjectAppResult(MODULE_NAME, projectName, COMPONENT_NAME);
		when(projectApp1.configure(context, _moduleDescriptor, COMPONENT_NAME)).thenReturn(projectApp1Result);

		// mock project app #2
		final ComponentProjectApps.ProjectApp projectApp2 = spy(mock(ComponentProjectApps.ProjectApp.class));
		final ComponentNotFoundResult projectApp2Result = new ComponentNotFoundResult(MODULE_NAME, ComponentDescriptor.Type.PROJECTAPP, COMPONENT_NAME);
		when(projectApp2.configure(context, _moduleDescriptor, COMPONENT_NAME)).thenReturn(projectApp2Result);
		// mock ComponentProjectApps
		final ComponentProjectApps componentProjectApps = mock(ComponentProjectApps.class);
		when(componentProjectApps.getComponentName()).thenReturn(COMPONENT_NAME);
		final List<ComponentProjectApps.ProjectApp> projectApps = Lists.newArrayList(projectApp1, projectApp2);
		when(componentProjectApps.getProjectApps()).thenReturn(projectApps);
		when(componentProjectApps.configure(context, _moduleDescriptor)).thenCallRealMethod();

		// test
		final ExecutionResults result = componentProjectApps.configure(context, _moduleDescriptor);

		// verify
		verify(projectApp1, times(1)).configure(context, _moduleDescriptor, COMPONENT_NAME);
		verify(projectApp2, times(1)).configure(context, _moduleDescriptor, COMPONENT_NAME);
		assertThat(result.size()).isEqualTo(2);
		assertThat(result.get(0)).isEqualTo(projectApp1Result);
		assertThat(result.get(1)).isEqualTo(projectApp2Result);
	}

	@Test
	public void ProjectApp_configure_project_not_found() {
		// setup
		final ComponentProjectApps.ProjectApp projectApp = new ComponentProjectApps.ProjectApp();
		projectApp._projectName = "unknownProject";

		// test
		final ExecutionResult result = projectApp.configure(_context, _moduleDescriptor, COMPONENT_NAME);

		// verify
		assertThat(result).isInstanceOf(ProjectNotFoundResult.class);
	}

	@Test
	public void ProjectApp_configure_component_not_found() {
		// setup
		final ComponentProjectApps.ProjectApp projectApp = new ComponentProjectApps.ProjectApp();
		projectApp._projectName = PROJECT_NAME_1;
		when(_moduleDescriptor.getComponents()).thenReturn(new ComponentDescriptor[0]);

		// test
		final ExecutionResult result = projectApp.configure(_context, _moduleDescriptor, COMPONENT_NAME);

		// verify
		assertThat(result).isInstanceOf(ComponentNotFoundResult.class);
	}

	@Test
	public void ProjectApp_configure_multiple_components_found() {
		// setup
		final ComponentProjectApps.ProjectApp projectApp = new ComponentProjectApps.ProjectApp();
		projectApp._projectName = PROJECT_NAME_1;
		final ComponentDescriptor componentDescriptor = mock(ComponentDescriptor.class);
		when(componentDescriptor.getName()).thenReturn("just a name");
		when(componentDescriptor.getDisplayName()).thenReturn(COMPONENT_NAME);
		when(componentDescriptor.getType()).thenReturn(ComponentDescriptor.Type.PROJECTAPP);
		when(_moduleDescriptor.getComponents()).thenReturn(new ComponentDescriptor[]{componentDescriptor, componentDescriptor});

		// test
		final ExecutionResult result = projectApp.configure(_context, _moduleDescriptor, COMPONENT_NAME);

		// verify
		assertThat(result).isInstanceOf(MultipleComponentsFoundResult.class);
	}

	@Test
	public void ProjectApp_configure_install_failed() {
		// setup
		final ComponentProjectApps.ProjectApp projectApp = new ComponentProjectApps.ProjectApp();
		projectApp._projectName = PROJECT_NAME_1;
		final ComponentDescriptor componentDescriptor = mock(ComponentDescriptor.class);
		when(componentDescriptor.getName()).thenReturn(COMPONENT_NAME);
		when(componentDescriptor.getType()).thenReturn(ComponentDescriptor.Type.PROJECTAPP);
		when(_moduleDescriptor.getComponents()).thenReturn(new ComponentDescriptor[]{componentDescriptor});
		when(_moduleAdminAgent.getProjectAppUsages(MODULE_NAME, COMPONENT_NAME)).thenReturn(Collections.emptyList());
		final String errorMessage = "Whoops...";
		doAnswer(inv -> {
			throw new ModuleException(errorMessage);
		}).when(_moduleAdminAgent).installProjectApp(MODULE_NAME, COMPONENT_NAME, _project1);

		// test
		final ExecutionResult result = projectApp.configure(_context, _moduleDescriptor, COMPONENT_NAME);

		// verify
		assertThat(result).isInstanceOf(ComponentProjectApps.ProjectAppInstallFailedResult.class);
		assertThat(result.toString()).isEqualTo(String.format(ComponentProjectApps.ProjectAppInstallFailedResult.MESSAGE_INSTALL, MODULE_NAME, COMPONENT_NAME, PROJECT_NAME_1, ModuleException.class.getName() + ": " + errorMessage));
	}

	@Test
	public void ProjectApp_configure_update_failed() {
		// setup
		final ComponentProjectApps.ProjectApp projectApp = new ComponentProjectApps.ProjectApp();
		projectApp._projectName = PROJECT_NAME_1;
		final ComponentDescriptor componentDescriptor = mock(ComponentDescriptor.class);
		when(componentDescriptor.getName()).thenReturn(COMPONENT_NAME);
		when(componentDescriptor.getType()).thenReturn(ComponentDescriptor.Type.PROJECTAPP);
		when(_moduleDescriptor.getComponents()).thenReturn(new ComponentDescriptor[]{componentDescriptor});
		when(_moduleAdminAgent.getProjectAppUsages(MODULE_NAME, COMPONENT_NAME)).thenReturn(Lists.newArrayList(_project1));
		final String errorMessage = "Whoops...";
		doAnswer(inv -> {
			throw new ModuleException(errorMessage);
		}).when(_moduleAdminAgent).installProjectApp(MODULE_NAME, COMPONENT_NAME, _project1);

		// test
		final ExecutionResult result = projectApp.configure(_context, _moduleDescriptor, COMPONENT_NAME);

		// verify
		assertThat(result).isInstanceOf(ComponentProjectApps.ProjectAppInstallFailedResult.class);
		assertThat(result.toString()).isEqualTo(String.format(ComponentProjectApps.ProjectAppInstallFailedResult.MESSAGE_UPDATE, MODULE_NAME, COMPONENT_NAME, PROJECT_NAME_1, ModuleException.class.getName() + ": " + errorMessage));
	}

	@Test
	public void ProjectApp_configure_file_upload_partly_failed() {
		// setup
		final ComponentProjectApps.ProjectApp projectApp = new ComponentProjectApps.ProjectApp();
		projectApp._projectName = PROJECT_NAME_1;
		projectApp._files = Lists.list("src/test/resources/nonExistingFile.json", "src/test/resources/1.json");
		final ComponentDescriptor componentDescriptor = mock(ComponentDescriptor.class);
		when(componentDescriptor.getName()).thenReturn(COMPONENT_NAME);
		when(componentDescriptor.getType()).thenReturn(ComponentDescriptor.Type.PROJECTAPP);
		when(_moduleDescriptor.getComponents()).thenReturn(new ComponentDescriptor[]{componentDescriptor});
		when(_moduleAdminAgent.getProjectAppUsages(MODULE_NAME, COMPONENT_NAME)).thenReturn(Collections.emptyList());
		when(_moduleAdminAgent.getProjectAppConfig(MODULE_NAME, COMPONENT_NAME, _project1)).thenReturn((FileSystem) _fileSystem);

		// test
		final ExecutionResult result = projectApp.configure(_context, _moduleDescriptor, COMPONENT_NAME);

		// verify
		assertThat(result).isInstanceOf(ExecutionResults.class);
		final ExecutionResults results = (ExecutionResults) result;
		assertThat(results.hasError()).isTrue();
		assertThat(results.size()).isEqualTo(2);
		assertThat(results.get(0)).isInstanceOf(FileSystemUtil.FileResult.class);
		assertThat(((FileSystemUtil.FileResult) results.get(0)).getFileName()).isEqualTo("src/test/resources/nonExistingFile.json");
		assertThat(results.get(1)).isInstanceOf(FileSystemUtil.FileResult.class);
		assertThat(((FileSystemUtil.FileResult) results.get(1)).getFileName()).isEqualTo("src/test/resources/1.json");
		verifyFile(_fileSystem, "1.json");
	}

	@Test
	public void ProjectApp_configure_full_stack_scenario() {
		// setup
		final ComponentProjectApps.ProjectApp projectApp = new ComponentProjectApps.ProjectApp();
		projectApp._projectName = PROJECT_NAME_1;
		projectApp._files = Lists.list("src/test/resources/dir/", "src/test/resources/1.json");
		final ComponentDescriptor componentDescriptor = mock(ComponentDescriptor.class);
		when(componentDescriptor.getName()).thenReturn(COMPONENT_NAME);
		when(componentDescriptor.getType()).thenReturn(ComponentDescriptor.Type.PROJECTAPP);
		when(_moduleDescriptor.getComponents()).thenReturn(new ComponentDescriptor[]{componentDescriptor});
		when(_moduleAdminAgent.getProjectAppUsages(MODULE_NAME, COMPONENT_NAME)).thenReturn(Collections.emptyList());
		when(_moduleAdminAgent.getProjectAppConfig(MODULE_NAME, COMPONENT_NAME, _project1)).thenReturn((FileSystem) _fileSystem);

		// test
		final ExecutionResult result = projectApp.configure(_context, _moduleDescriptor, COMPONENT_NAME);

		// verify
		assertThat(result).isInstanceOf(ComponentProjectApps.ProjectAppResult.class);
		assertThat(result.toString()).isEqualTo(String.format(ComponentProjectApps.ProjectAppResult.MESSAGE, MODULE_NAME, COMPONENT_NAME, PROJECT_NAME_1));
		verifyFile(_fileSystem, "1.json");
		verifyDirDoesNotExist(_fileSystem, "dir");
		verifyFile(_fileSystem, "2.json");
		verifyFile(_fileSystem, "subDir/3.json");
	}

	@Test
	public void ProjectApp_configure_full_stack_scenario_with_fallback_projectName() {
		// setup
		final ComponentProjectApps.ProjectApp projectApp = new ComponentProjectApps.ProjectApp();
		projectApp._files = Lists.list("src/test/resources/dir/", "src/test/resources/1.json");
		final ComponentDescriptor componentDescriptor = mock(ComponentDescriptor.class);
		when(componentDescriptor.getName()).thenReturn(COMPONENT_NAME);
		when(componentDescriptor.getType()).thenReturn(ComponentDescriptor.Type.PROJECTAPP);
		when(_moduleDescriptor.getComponents()).thenReturn(new ComponentDescriptor[]{componentDescriptor});
		when(_moduleAdminAgent.getProjectAppUsages(MODULE_NAME, COMPONENT_NAME)).thenReturn(Collections.emptyList());
		when(_moduleAdminAgent.getProjectAppConfig(MODULE_NAME, COMPONENT_NAME, _project1)).thenReturn((FileSystem) _fileSystem);

		// test
		final GlobalConfig config = mock(GlobalConfig.class);
		when(config.getProject()).thenReturn(PROJECT_NAME_1);
		final ExecutionResult result = projectApp.configure(new ConfigurationContext(_connection, config), _moduleDescriptor, COMPONENT_NAME);

		// verify
		assertThat(result).isInstanceOf(ComponentProjectApps.ProjectAppResult.class);
		assertThat(result.toString()).isEqualTo(String.format(ComponentProjectApps.ProjectAppResult.MESSAGE, MODULE_NAME, COMPONENT_NAME, PROJECT_NAME_1));
		verifyFile(_fileSystem, "1.json");
		verifyDirDoesNotExist(_fileSystem, "dir");
		verifyFile(_fileSystem, "2.json");
		verifyFile(_fileSystem, "subDir/3.json");
	}

	@Test
	public void ProjectAppInstallFailedResult_install_getException() {
		final String errorMessage = "Whoops...";
		final ModuleException moduleException = new ModuleException(errorMessage);
		final ComponentProjectApps.ProjectAppInstallFailedResult result = new ComponentProjectApps.ProjectAppInstallFailedResult(MODULE_NAME, PROJECT_NAME_1, COMPONENT_NAME, false, moduleException);
		final IllegalStateException exception = result.getException();
		assertThat(exception.getMessage()).isEqualTo(String.format(ComponentProjectApps.ProjectAppInstallFailedResult.MESSAGE_INSTALL, MODULE_NAME, COMPONENT_NAME, PROJECT_NAME_1, ModuleException.class.getName() + ": " + errorMessage));
	}

	@Test
	public void ProjectAppInstallFailedResult_install_toString() {
		final String errorMessage = "Whoops...";
		final ModuleException moduleException = new ModuleException(errorMessage);
		final ComponentProjectApps.ProjectAppInstallFailedResult result = new ComponentProjectApps.ProjectAppInstallFailedResult(MODULE_NAME, PROJECT_NAME_1, COMPONENT_NAME, false, moduleException);
		assertThat(result.toString()).isEqualTo(String.format(ComponentProjectApps.ProjectAppInstallFailedResult.MESSAGE_INSTALL, MODULE_NAME, COMPONENT_NAME, PROJECT_NAME_1, ModuleException.class.getName() + ": " + errorMessage));
	}

	@Test
	public void ProjectAppInstallFailedResult_update_getException() {
		final String errorMessage = "Whoops...";
		final ModuleException moduleException = new ModuleException(errorMessage);
		final ComponentProjectApps.ProjectAppInstallFailedResult result = new ComponentProjectApps.ProjectAppInstallFailedResult(MODULE_NAME, PROJECT_NAME_1, COMPONENT_NAME, true, moduleException);
		final IllegalStateException exception = result.getException();
		assertThat(exception.getMessage()).isEqualTo(String.format(ComponentProjectApps.ProjectAppInstallFailedResult.MESSAGE_UPDATE, MODULE_NAME, COMPONENT_NAME, PROJECT_NAME_1, ModuleException.class.getName() + ": " + errorMessage));
	}

	@Test
	public void ProjectAppInstallFailedResult_update_toString() {
		final String errorMessage = "Whoops...";
		final ModuleException moduleException = new ModuleException(errorMessage);
		final ComponentProjectApps.ProjectAppInstallFailedResult result = new ComponentProjectApps.ProjectAppInstallFailedResult(MODULE_NAME, PROJECT_NAME_1, COMPONENT_NAME, true, moduleException);
		assertThat(result.toString()).isEqualTo(String.format(ComponentProjectApps.ProjectAppInstallFailedResult.MESSAGE_UPDATE, MODULE_NAME, COMPONENT_NAME, PROJECT_NAME_1, ModuleException.class.getName() + ": " + errorMessage));
	}

}
