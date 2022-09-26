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

import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.agency.ModuleAdminAgent;
import de.espirit.firstspirit.agency.SpecialistsBroker;
import de.espirit.firstspirit.agency.WebAppId;
import de.espirit.firstspirit.io.FileSystem;
import de.espirit.firstspirit.io.MemoryFileSystem;
import de.espirit.firstspirit.module.WebEnvironment;
import de.espirit.firstspirit.module.descriptor.ComponentDescriptor;
import de.espirit.firstspirit.module.descriptor.ModuleDescriptor;
import de.espirit.firstspirit.server.module.ModuleException;

import com.espirit.moddev.cli.api.result.ExecutionResult;
import com.espirit.moddev.cli.api.result.ExecutionResults;
import com.espirit.moddev.cli.commands.module.configureCommand.json.JsonTestUtil;
import com.espirit.moddev.cli.commands.module.configureCommand.json.components.common.ComponentNotFoundResult;
import com.espirit.moddev.cli.commands.module.configureCommand.json.components.common.MultipleComponentsFoundResult;
import com.espirit.moddev.cli.commands.module.configureCommand.json.components.common.NoProjectNameDefinedResult;
import com.espirit.moddev.cli.commands.module.configureCommand.json.components.common.ProjectNotFoundResult;
import com.espirit.moddev.cli.commands.module.utils.FileSystemUtil;
import com.espirit.moddev.cli.configuration.GlobalConfig;
import com.espirit.moddev.shared.webapp.WebAppIdentifier;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_COMPONENT_NAME;
import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_FILES;
import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_WEB_APPS;
import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_WEB_APP_NAME;
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

@SuppressWarnings({"unchecked", "rawtypes"})
public class ComponentWebAppsTest {

	private static final String COMPONENT_NAME = "testComponent";
	private static final String PROJECT_NAME_1 = "firstProject";
	private static final String PROJECT_NAME_2 = "secondProject";
	private static final String FILE_NAME_1 = "dir/1.json";
	private static final String FILE_NAME_2 = "dir/subDir/2.json";
	private static final String FILE_NAME_3 = "3.json";
	private static final String MODULE_NAME = "testModule";

	private static final List<ComponentWebApps.WebApp> APP_AMOUNT_0 = new ArrayList<>();
	private static final List<ComponentWebApps.WebApp> GLOBAL_APPS_AMOUNT_1 = Lists.newArrayList(new ComponentWebApps.WebApp("global(first-global-web-app)"));
	private static final List<ComponentWebApps.WebApp> PROJECT_APPS_AMOUNT_1 = Lists.newArrayList(new ComponentWebApps.WebApp("webedit"));

	private ObjectMapper _objectMapper;

	private MemoryFileSystem _fileSystem;
	private ModuleAdminAgent _moduleAdminAgent;
	private Connection _connection;
	private ConfigurationContext _context;
	private ModuleDescriptor _moduleDescriptor;
	private Project _project1;
	private Project _project2;
	private WebAppIdentifier _globalWebApp;
	private WebAppIdentifier _projectWebApp;

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
			when(project.getId()).thenReturn(1L);
			when(project.getName()).thenReturn(PROJECT_NAME_1);
			when(_connection.getProjectByName(PROJECT_NAME_1)).thenReturn(project);
			_project1 = project;
		}
		{
			final Project project = mock(Project.class);
			when(project.getId()).thenReturn(2L);
			when(project.getName()).thenReturn(PROJECT_NAME_2);
			when(_connection.getProjectByName(PROJECT_NAME_2)).thenReturn(project);
			_project2 = project;
		}
		final SpecialistsBroker broker = mock(SpecialistsBroker.class);
		when(_connection.getBroker()).thenReturn(broker);
		when(broker.requireSpecialist(ModuleAdminAgent.TYPE)).thenReturn(_moduleAdminAgent);

		_globalWebApp = WebAppIdentifier.forGlobalWebApp("msm-micro-app");
		_projectWebApp = WebAppIdentifier.forScope(WebEnvironment.WebScope.WEBEDIT);
	}

	@Test
	public void deserialize_componentName_is_undefined() throws IOException {
		// setup
		final String json = JsonTestUtil.toJsonObject(JsonTestUtil.createMap(JsonTestUtil.createEntry(ATTR_WEB_APPS, GLOBAL_APPS_AMOUNT_1)));

		// test
		org.junit.jupiter.api.Assertions.assertThrows(MismatchedInputException.class, () -> {
			_objectMapper.readValue(json, ComponentWebApps.class);
		});
	}

	@Test
	public void deserialize_componentName_is_defined_as_null() throws IOException {
		// setup
		final String json = JsonTestUtil.toJsonObject(JsonTestUtil.createMap(JsonTestUtil.createEntry(ATTR_COMPONENT_NAME, null), JsonTestUtil.createEntry(ATTR_WEB_APPS, GLOBAL_APPS_AMOUNT_1)));

		// test
		try {
			_objectMapper.readValue(json, ComponentWebApps.class);
			failBecauseExceptionWasNotThrown(JsonMappingException.class);
		} catch (final JsonMappingException e) {
			Assertions.assertThat(e.getCause()).isExactlyInstanceOf(NullPointerException.class);
		}
	}

	@Test
	public void deserialize_componentName_is_defined_as_empty_string() throws IOException {
		// setup
		final String json = JsonTestUtil.toJsonObject(JsonTestUtil.createMap(JsonTestUtil.createEntry(ATTR_COMPONENT_NAME, ""), JsonTestUtil.createEntry(ATTR_WEB_APPS, GLOBAL_APPS_AMOUNT_1)));

		// test
		try {
			_objectMapper.readValue(json, ComponentWebApps.class);
			failBecauseExceptionWasNotThrown(JsonMappingException.class);
		} catch (final JsonMappingException e) {
			Assertions.assertThat(e.getCause()).isExactlyInstanceOf(IllegalArgumentException.class);
		}
	}

	@Test
	public void deserialize_componentName_is_defined_as_empty_string_with_whitespaces() throws IOException {
		// setup
		final String json = JsonTestUtil.toJsonObject(JsonTestUtil.createMap(JsonTestUtil.createEntry(ATTR_COMPONENT_NAME, " "), JsonTestUtil.createEntry(ATTR_WEB_APPS, GLOBAL_APPS_AMOUNT_1)));

		// test
		try {
			_objectMapper.readValue(json, ComponentWebApps.class);
			failBecauseExceptionWasNotThrown(JsonMappingException.class);
		} catch (final JsonMappingException e) {
			Assertions.assertThat(e.getCause()).isExactlyInstanceOf(IllegalArgumentException.class);
		}
	}

	@Test
	public void deserialize_webApps_are_undefined() throws IOException {
		// setup
		final String json = JsonTestUtil.toJsonObject(JsonTestUtil.createMap(JsonTestUtil.createEntry(ATTR_COMPONENT_NAME, "myProjectComponent")));

		// test
		org.junit.jupiter.api.Assertions.assertThrows(MismatchedInputException.class, () -> {
			_objectMapper.readValue(json, ComponentWebApps.class);
		});
	}

	@Test
	public void deserialize_webApps_has_length_0() throws IOException {
		// setup
		final String json = JsonTestUtil.toJsonObject(JsonTestUtil.createMap(JsonTestUtil.createEntry(ATTR_COMPONENT_NAME, "myProjectComponent"), JsonTestUtil.createEntry(ATTR_WEB_APPS, APP_AMOUNT_0)));

		// test
		try {
			_objectMapper.readValue(json, ComponentWebApps.class);
			failBecauseExceptionWasNotThrown(JsonMappingException.class);
		} catch (final JsonMappingException e) {
			Assertions.assertThat(e.getCause()).isExactlyInstanceOf(IllegalArgumentException.class);
		}
	}

	@Test
	public void deserialize() throws IOException {
		// setup
		final ComponentWebApps.WebApp[] webApps = new ComponentWebApps.WebApp[2];
		webApps[0] = GLOBAL_APPS_AMOUNT_1.get(0);
		webApps[0]._files = Lists.newArrayList(FILE_NAME_1, FILE_NAME_2);
		webApps[0]._deploy = true;
		webApps[1] = PROJECT_APPS_AMOUNT_1.get(0);
		webApps[1]._projectName = PROJECT_NAME_1;
		webApps[1]._files = Lists.newArrayList(FILE_NAME_3);
		webApps[1]._deploy = false;
		final String json = JsonTestUtil.toJsonObject(JsonTestUtil.createMap(JsonTestUtil.createEntry(ATTR_COMPONENT_NAME, COMPONENT_NAME), JsonTestUtil.createEntry(ATTR_WEB_APPS, webApps)));

		// test
		final ComponentWebApps deserialized = _objectMapper.readValue(json, ComponentWebApps.class);

		// verify
		assertThat(deserialized.getComponentName()).isEqualTo(COMPONENT_NAME);
		assertThat(deserialized.getWebApps()).hasSize(2);
		{
			final ComponentWebApps.WebApp webApp = deserialized.getWebApps().get(0);
			assertThat(webApp.getProjectName(_context)).isNull();
			assertThat(webApp.getFiles()).hasSize(2);
			assertThat(webApp.getFiles().get(0)).isEqualTo(FILE_NAME_1);
			assertThat(webApp.getFiles().get(1)).isEqualTo(FILE_NAME_2);
			assertThat(webApp.getDeploy()).isTrue();
		}
		{
			final ComponentWebApps.WebApp webApp = deserialized.getWebApps().get(1);
			assertThat(webApp.getProjectName(_context)).isEqualTo(PROJECT_NAME_1);
			assertThat(webApp.getFiles()).hasSize(1);
			assertThat(webApp.getFiles().get(0)).isEqualTo(FILE_NAME_3);
			assertThat(webApp.getDeploy()).isFalse();
		}
	}

	@Test
	public void deserialize_webApp_project_is_undefined() throws IOException {
		// setup
		final String json = JsonTestUtil.toJsonObject(JsonTestUtil.createMap(JsonTestUtil.createEntry(ATTR_FILES, new String[0])));

		// test
		org.junit.jupiter.api.Assertions.assertThrows(MismatchedInputException.class, () -> {
			_objectMapper.readValue(json, ComponentWebApps.WebApp.class);
		});
	}

	@Test
	public void deserialize_webApp_files_are_undefined() throws IOException {
		// setup
		final String json = JsonTestUtil.toJsonObject(JsonTestUtil.createMap(JsonTestUtil.createEntry(ATTR_FILES, new String[0])));

		// test
		org.junit.jupiter.api.Assertions.assertThrows(MismatchedInputException.class, () -> {
			_objectMapper.readValue(json, ComponentWebApps.WebApp.class);
		});
	}

	@Test
	public void deserialize_webApp_webAppName_is_null() throws IOException {
		// setup
		final String json = JsonTestUtil.toJsonObject(JsonTestUtil.createMap(JsonTestUtil.createEntry(ATTR_WEB_APP_NAME, null)));

		// test
		try {
			_objectMapper.readValue(json, ComponentWebApps.WebApp.class);
			failBecauseExceptionWasNotThrown(JsonMappingException.class);
		} catch (final JsonMappingException e) {
			Assertions.assertThat(e.getCause()).isExactlyInstanceOf(NullPointerException.class);
		}
	}

	@Test
	public void deserialize_webApp_webAppName_is_empty_string() throws IOException {
		// setup
		final String json = JsonTestUtil.toJsonObject(JsonTestUtil.createMap(JsonTestUtil.createEntry(ATTR_WEB_APP_NAME, "")));

		// test
		try {
			_objectMapper.readValue(json, ComponentWebApps.WebApp.class);
			failBecauseExceptionWasNotThrown(JsonMappingException.class);
		} catch (final JsonMappingException e) {
			Assertions.assertThat(e.getCause()).isExactlyInstanceOf(IllegalArgumentException.class);
		}
	}

	@Test
	public void configure_global_web_app() {
		// setup
		final Connection connection = mock(Connection.class);
		final ConfigurationContext context = new ConfigurationContext(connection, mock(GlobalConfig.class));
		// mock web app #1
		final ComponentWebApps.WebApp webApp1 = spy(mock(ComponentWebApps.WebApp.class));
		final WebAppIdentifier webAppIdentifier1 = WebAppIdentifier.forGlobalWebApp("first-global-web-app");
		when(webApp1.getWebAppName()).thenReturn(webAppIdentifier1);
		final ComponentWebApps.WebComponentResult webComponent1Result = new ComponentWebApps.WebComponentResult(MODULE_NAME, COMPONENT_NAME, webAppIdentifier1.createWebAppId(null), false);
		when(webApp1.configure(context, _moduleDescriptor, COMPONENT_NAME)).thenReturn(webComponent1Result);

		// mock web app #2
		final ComponentWebApps.WebApp webApp2 = spy(mock(ComponentWebApps.WebApp.class));
		final WebAppIdentifier webAppIdentifier2 = WebAppIdentifier.forGlobalWebApp("second-global-web-app");
		when(webApp1.getWebAppName()).thenReturn(webAppIdentifier2);
		final ComponentNotFoundResult webComponent2Result = new ComponentNotFoundResult(MODULE_NAME, ComponentDescriptor.Type.WEBAPP, COMPONENT_NAME);
		when(webApp2.configure(context, _moduleDescriptor, COMPONENT_NAME)).thenReturn(webComponent2Result);
		// mock ComponentWebApps
		final ComponentWebApps ComponentWebApps = mock(ComponentWebApps.class);
		when(ComponentWebApps.getComponentName()).thenReturn(COMPONENT_NAME);
		final List<ComponentWebApps.WebApp> webApps = Lists.newArrayList(webApp1, webApp2);
		when(ComponentWebApps.getWebApps()).thenReturn(webApps);
		when(ComponentWebApps.configure(context, _moduleDescriptor)).thenCallRealMethod();

		// test
		final ExecutionResults result = ComponentWebApps.configure(context, _moduleDescriptor);

		// verify
		verify(webApp1, times(1)).configure(context, _moduleDescriptor, COMPONENT_NAME);
		verify(webApp2, times(1)).configure(context, _moduleDescriptor, COMPONENT_NAME);
		assertThat(result.size()).isEqualTo(2);
		assertThat(result.get(0)).isEqualTo(webComponent1Result);
		assertThat(result.get(1)).isEqualTo(webComponent2Result);
	}

	@Test
	public void configure_project_web_app() {
		// setup
		final Connection connection = mock(Connection.class);
		final ConfigurationContext context = new ConfigurationContext(connection, mock(GlobalConfig.class));
		// mock web app #1
		final ComponentWebApps.WebApp webApp1 = spy(mock(ComponentWebApps.WebApp.class));
		final WebAppIdentifier webAppIdentifier1 = WebAppIdentifier.forScope(WebEnvironment.WebScope.WEBEDIT);
		when(webApp1.getWebAppName()).thenReturn(webAppIdentifier1);
		final ComponentWebApps.WebComponentResult webApp1Result = new ComponentWebApps.WebComponentResult(MODULE_NAME, COMPONENT_NAME, webAppIdentifier1.createWebAppId(_project1), false);
		when(webApp1.configure(context, _moduleDescriptor, COMPONENT_NAME)).thenReturn(webApp1Result);

		// mock web app #2
		final ComponentWebApps.WebApp webApp2 = spy(mock(ComponentWebApps.WebApp.class));
		final WebAppIdentifier webAppIdentifier2 = WebAppIdentifier.forScope(WebEnvironment.WebScope.PREVIEW);
		when(webApp2.getWebAppName()).thenReturn(webAppIdentifier2);
		final ComponentNotFoundResult webApp2Result = new ComponentNotFoundResult(MODULE_NAME, ComponentDescriptor.Type.WEBAPP, COMPONENT_NAME);
		when(webApp2.configure(context, _moduleDescriptor, COMPONENT_NAME)).thenReturn(webApp2Result);
		// mock ComponentWebApps
		final ComponentWebApps componentWebApps = mock(ComponentWebApps.class);
		when(componentWebApps.getComponentName()).thenReturn(COMPONENT_NAME);
		final List<ComponentWebApps.WebApp> webApps = Lists.newArrayList(webApp1, webApp2);
		when(componentWebApps.getWebApps()).thenReturn(webApps);
		when(componentWebApps.configure(context, _moduleDescriptor)).thenCallRealMethod();

		// test
		final ExecutionResults result = componentWebApps.configure(context, _moduleDescriptor);

		// verify
		verify(webApp1, times(1)).configure(context, _moduleDescriptor, COMPONENT_NAME);
		verify(webApp2, times(1)).configure(context, _moduleDescriptor, COMPONENT_NAME);
		assertThat(result.size()).isEqualTo(2);
		assertThat(result.get(0)).isEqualTo(webApp1Result);
		assertThat(result.get(1)).isEqualTo(webApp2Result);
	}

	@Test
	public void configure_project_web_app_with_fallback_project_name() {
		// setup
		final Connection connection = mock(Connection.class);
		final GlobalConfig config = mock(GlobalConfig.class);
		when(config.getProject()).thenReturn(PROJECT_NAME_1);
		final ConfigurationContext context = new ConfigurationContext(connection, config);
		// mock web app #1
		final ComponentWebApps.WebApp webApp1 = spy(mock(ComponentWebApps.WebApp.class));
		final WebAppIdentifier webAppIdentifier1 = WebAppIdentifier.forScope(WebEnvironment.WebScope.WEBEDIT);
		when(webApp1.getWebAppName()).thenReturn(webAppIdentifier1);
		final ComponentWebApps.WebComponentResult webApp1Result = new ComponentWebApps.WebComponentResult(MODULE_NAME, COMPONENT_NAME, webAppIdentifier1.createWebAppId(_project1), false);
		when(webApp1.configure(context, _moduleDescriptor, COMPONENT_NAME)).thenReturn(webApp1Result);

		// mock web app #2
		final ComponentWebApps.WebApp webApp2 = spy(mock(ComponentWebApps.WebApp.class));
		final WebAppIdentifier webAppIdentifier2 = WebAppIdentifier.forScope(WebEnvironment.WebScope.PREVIEW);
		when(webApp2.getWebAppName()).thenReturn(webAppIdentifier2);
		final ComponentNotFoundResult webApp2Result = new ComponentNotFoundResult(MODULE_NAME, ComponentDescriptor.Type.WEBAPP, COMPONENT_NAME);
		when(webApp2.configure(context, _moduleDescriptor, COMPONENT_NAME)).thenReturn(webApp2Result);
		// mock ComponentWebApps
		final ComponentWebApps componentWebApps = mock(ComponentWebApps.class);
		when(componentWebApps.getComponentName()).thenReturn(COMPONENT_NAME);
		final List<ComponentWebApps.WebApp> webApps = Lists.newArrayList(webApp1, webApp2);
		when(componentWebApps.getWebApps()).thenReturn(webApps);
		when(componentWebApps.configure(context, _moduleDescriptor)).thenCallRealMethod();

		// test
		final ExecutionResults result = componentWebApps.configure(context, _moduleDescriptor);

		// verify
		verify(webApp1, times(1)).configure(context, _moduleDescriptor, COMPONENT_NAME);
		verify(webApp2, times(1)).configure(context, _moduleDescriptor, COMPONENT_NAME);
		assertThat(result.size()).isEqualTo(2);
		assertThat(result.get(0)).isEqualTo(webApp1Result);
		assertThat(result.get(1)).isEqualTo(webApp2Result);
	}

	@Test
	public void WebApp_default_values_global_web_app() {
		// setup
		final String webAppName = "test-web-app";
		final ComponentWebApps.WebApp webApp = new ComponentWebApps.WebApp("global(" + webAppName + ")");
		// verify
		assertThat(webApp.getProjectName(_context)).isNull();
		assertThat(webApp.getDeploy()).isTrue();
		assertThat(webApp.getFiles()).isSameAs(Collections.EMPTY_LIST);
		assertThat(webApp.getWebAppName().isGlobal()).isTrue();
		assertThat(((WebAppIdentifier.GlobalWebAppIdentifier) (webApp.getWebAppName())).getGlobalWebAppId()).isEqualTo(webAppName);
	}

	@Test
	public void WebApp_default_values_project_web_app() {
		// setup
		final String webAppName = "webedit";
		final ComponentWebApps.WebApp webApp = new ComponentWebApps.WebApp(webAppName);
		webApp._projectName = "myProject";
		// verify
		assertThat(webApp.getProjectName(_context)).isEqualTo(webApp._projectName);
		assertThat(webApp.getDeploy()).isTrue();
		assertThat(webApp.getFiles()).isSameAs(Collections.EMPTY_LIST);
		assertThat(webApp.getWebAppName().isGlobal()).isFalse();
		assertThat(webApp.getWebAppName().getScope().toString()).isEqualTo(webAppName.toUpperCase(Locale.UK));
	}

	@Test
	public void WebApp_configure_project_is_null() {
		// setup
		final ComponentWebApps.WebApp webApp = new ComponentWebApps.WebApp("webedit");
		webApp._projectName = null;

		// test
		final ExecutionResult result = webApp.configure(_context, _moduleDescriptor, COMPONENT_NAME);

		// verify
		assertThat(result).isInstanceOf(NoProjectNameDefinedResult.class);
		assertThat(result.toString()).isEqualTo(String.format(NoProjectNameDefinedResult.MESSAGE, MODULE_NAME, COMPONENT_NAME));
	}

	@Test
	public void WebApp_configure_project_not_found() {
		// setup
		final ComponentWebApps.WebApp webApp = new ComponentWebApps.WebApp("webedit");
		final String nonExistingProjectName = "nonExistingProject";
		webApp._projectName = nonExistingProjectName;

		// test
		final ExecutionResult result = webApp.configure(_context, _moduleDescriptor, COMPONENT_NAME);

		// verify
		assertThat(result).isInstanceOf(ProjectNotFoundResult.class);
		assertThat(result.toString()).isEqualTo(String.format(ProjectNotFoundResult.MESSAGE, nonExistingProjectName));
	}

	@Test
	public void WebApp_configure_component_not_found() {
		// setup
		final ComponentWebApps.WebApp webApp = GLOBAL_APPS_AMOUNT_1.get(0);
		when(_moduleDescriptor.getComponents()).thenReturn(new ComponentDescriptor[0]);

		// test
		final ExecutionResult result = webApp.configure(_context, _moduleDescriptor, COMPONENT_NAME);

		// verify
		assertThat(result).isInstanceOf(ComponentNotFoundResult.class);
	}

	@Test
	public void WebApp_configure_multiple_components_found() {
		// setup
		final ComponentWebApps.WebApp webApp = GLOBAL_APPS_AMOUNT_1.get(0);
		final ComponentDescriptor componentDescriptor = mock(ComponentDescriptor.class);
		when(componentDescriptor.getName()).thenReturn("just a name");
		when(componentDescriptor.getDisplayName()).thenReturn(COMPONENT_NAME);
		when(componentDescriptor.getType()).thenReturn(ComponentDescriptor.Type.WEBAPP);
		when(_moduleDescriptor.getComponents()).thenReturn(new ComponentDescriptor[]{componentDescriptor, componentDescriptor});

		// test
		final ExecutionResult result = webApp.configure(_context, _moduleDescriptor, COMPONENT_NAME);

		// verify
		assertThat(result).isInstanceOf(MultipleComponentsFoundResult.class);
	}

	@Test
	public void WebApp_configure_install_failed() {
		// setup
		final ComponentWebApps.WebApp webApp = GLOBAL_APPS_AMOUNT_1.get(0);
		final ComponentDescriptor componentDescriptor = mock(ComponentDescriptor.class);
		when(componentDescriptor.getName()).thenReturn(COMPONENT_NAME);
		when(componentDescriptor.getType()).thenReturn(ComponentDescriptor.Type.WEBAPP);
		when(_moduleDescriptor.getComponents()).thenReturn(new ComponentDescriptor[]{componentDescriptor});
		when(_moduleAdminAgent.getProjectAppUsages(MODULE_NAME, COMPONENT_NAME)).thenReturn(Collections.emptyList());
		final String errorMessage = "Whoops...";
		doAnswer(inv -> {
			throw new ModuleException(errorMessage);
		}).when(_moduleAdminAgent).installWebApp(MODULE_NAME, COMPONENT_NAME, webApp._webAppName.createWebAppId(null), false);

		// test
		final ExecutionResult result = webApp.configure(_context, _moduleDescriptor, COMPONENT_NAME);

		// verify
		assertThat(result).isInstanceOf(ComponentWebApps.WebComponentInstallFailedResult.class);
		assertThat(result.toString()).isEqualTo(String.format(ComponentWebApps.WebComponentInstallFailedResult.MESSAGE_INSTALL, MODULE_NAME, COMPONENT_NAME, webApp._webAppName, ModuleException.class.getName() + ": " + errorMessage));
	}

	@Test
	public void WebApp_configure_update_failed() {
		// setup
		final ComponentWebApps.WebApp webApp = GLOBAL_APPS_AMOUNT_1.get(0);
		final ComponentDescriptor componentDescriptor = mock(ComponentDescriptor.class);
		when(componentDescriptor.getName()).thenReturn(COMPONENT_NAME);
		when(componentDescriptor.getType()).thenReturn(ComponentDescriptor.Type.WEBAPP);
		when(_moduleDescriptor.getComponents()).thenReturn(new ComponentDescriptor[]{componentDescriptor});
		final WebAppId webAppId = webApp.getWebAppName().createWebAppId(null);
		when(_moduleAdminAgent.getWebAppUsages(MODULE_NAME, COMPONENT_NAME)).thenReturn(Lists.newArrayList(webAppId));
		final String errorMessage = "Whoops...";
		doAnswer(inv -> {
			throw new ModuleException(errorMessage);
		}).when(_moduleAdminAgent).installWebApp(MODULE_NAME, COMPONENT_NAME, webAppId, false);

		// test
		final ExecutionResult result = webApp.configure(_context, _moduleDescriptor, COMPONENT_NAME);

		// verify
		assertThat(result).isInstanceOf(ComponentWebApps.WebComponentInstallFailedResult.class);
		assertThat(result.toString()).isEqualTo(String.format(ComponentWebApps.WebComponentInstallFailedResult.MESSAGE_UPDATE, MODULE_NAME, COMPONENT_NAME, webApp._webAppName, ModuleException.class.getName() + ": " + errorMessage));
	}

	@Test
	public void WebApp_configure_file_upload_partly_failed() {
		// setup
		final ComponentWebApps.WebApp webApp = GLOBAL_APPS_AMOUNT_1.get(0);
		webApp._files = Lists.list("src/test/resources/nonExistingFile.json", "src/test/resources/1.json");
		final ComponentDescriptor componentDescriptor = mock(ComponentDescriptor.class);
		when(componentDescriptor.getName()).thenReturn(COMPONENT_NAME);
		when(componentDescriptor.getType()).thenReturn(ComponentDescriptor.Type.WEBAPP);
		when(_moduleDescriptor.getComponents()).thenReturn(new ComponentDescriptor[]{componentDescriptor});
		when(_moduleAdminAgent.getWebAppUsages(MODULE_NAME, COMPONENT_NAME)).thenReturn(Collections.emptyList());
		when(_moduleAdminAgent.getWebAppConfig(MODULE_NAME, COMPONENT_NAME, webApp._webAppName.createWebAppId(null))).thenReturn((FileSystem) _fileSystem);

		// test
		final ExecutionResult result = webApp.configure(_context, _moduleDescriptor, COMPONENT_NAME);

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
	public void WebApp_configure_global_full_stack_scenario() {
		// setup
		final ComponentWebApps.WebApp webApp = GLOBAL_APPS_AMOUNT_1.get(0);
		webApp._files = Lists.list("src/test/resources/dir/", "src/test/resources/1.json");
		final ComponentDescriptor componentDescriptor = mock(ComponentDescriptor.class);
		when(componentDescriptor.getName()).thenReturn(COMPONENT_NAME);
		when(componentDescriptor.getType()).thenReturn(ComponentDescriptor.Type.WEBAPP);
		when(_moduleDescriptor.getComponents()).thenReturn(new ComponentDescriptor[]{componentDescriptor});
		when(_moduleAdminAgent.getWebAppUsages(MODULE_NAME, COMPONENT_NAME)).thenReturn(Collections.emptyList());
		when(_moduleAdminAgent.getWebAppConfig(MODULE_NAME, COMPONENT_NAME, webApp._webAppName.createWebAppId(null))).thenReturn((FileSystem) _fileSystem);

		// test
		final ExecutionResult result = webApp.configure(_context, _moduleDescriptor, COMPONENT_NAME);

		// verify
		assertThat(result).isInstanceOf(ComponentWebApps.WebComponentResult.class);
		final ComponentWebApps.WebComponentResult webComponentResult = (ComponentWebApps.WebComponentResult) result;
		assertThat(webComponentResult.toString()).isEqualTo(String.format(ComponentWebApps.WebComponentResult.MESSAGE_GLOBAL_WEBAPP, MODULE_NAME, COMPONENT_NAME, ((WebAppIdentifier.GlobalWebAppIdentifier) webApp._webAppName).getGlobalWebAppId()));
		assertThat(webComponentResult.getDeploy()).isTrue();
		assertThat(webComponentResult.getWebAppId()).isEqualTo(webApp._webAppName.createWebAppId(null));
		verifyFile(_fileSystem, "1.json");
		verifyDirDoesNotExist(_fileSystem, "dir");
		verifyFile(_fileSystem, "2.json");
		verifyFile(_fileSystem, "subDir/3.json");
	}

	@Test
	public void WebApp_configure_project_full_stack_scenario() {
		// setup
		final ComponentWebApps.WebApp webApp = PROJECT_APPS_AMOUNT_1.get(0);
		webApp._projectName = _project1.getName();
		webApp._files = Lists.list("src/test/resources/dir/", "src/test/resources/1.json");
		final ComponentDescriptor componentDescriptor = mock(ComponentDescriptor.class);
		when(componentDescriptor.getName()).thenReturn(COMPONENT_NAME);
		when(componentDescriptor.getType()).thenReturn(ComponentDescriptor.Type.WEBAPP);
		when(_moduleDescriptor.getComponents()).thenReturn(new ComponentDescriptor[]{componentDescriptor});
		when(_moduleAdminAgent.getWebAppUsages(MODULE_NAME, COMPONENT_NAME)).thenReturn(Collections.emptyList());
		when(_moduleAdminAgent.getWebAppConfig(MODULE_NAME, COMPONENT_NAME, webApp._webAppName.createWebAppId(_project1))).thenReturn((FileSystem) _fileSystem);

		// test
		final ExecutionResult result = webApp.configure(_context, _moduleDescriptor, COMPONENT_NAME);

		// verify
		assertThat(result).isInstanceOf(ComponentWebApps.WebComponentResult.class);
		final ComponentWebApps.WebComponentResult webComponentResult = (ComponentWebApps.WebComponentResult) result;
		assertThat(webComponentResult.toString()).isEqualTo(String.format(ComponentWebApps.WebComponentResult.MESSAGE_PROJECT_WEBAPP, MODULE_NAME, COMPONENT_NAME, webApp.getWebAppName().toString(), webApp._projectName));
		assertThat(webComponentResult.getDeploy()).isTrue();
		assertThat(webComponentResult.getWebAppId()).isEqualTo(webApp._webAppName.createWebAppId(_project1));
		verifyFile(_fileSystem, "1.json");
		verifyDirDoesNotExist(_fileSystem, "dir");
		verifyFile(_fileSystem, "2.json");
		verifyFile(_fileSystem, "subDir/3.json");
	}

	@Test
	public void WebComponentResult_equals() {
		// setup
		final String globalWebAppId1 = "global-web-app";
		final String globalWebAppId2 = "another-global-web-app";
		final ComponentWebApps.WebComponentResult global_result1 = new ComponentWebApps.WebComponentResult(MODULE_NAME, COMPONENT_NAME, WebAppId.Factory.create(globalWebAppId1), true);
		final ComponentWebApps.WebComponentResult global_result2 = new ComponentWebApps.WebComponentResult(MODULE_NAME, COMPONENT_NAME, WebAppId.Factory.create(globalWebAppId1), false);
		final ComponentWebApps.WebComponentResult project_result1 = new ComponentWebApps.WebComponentResult(MODULE_NAME, COMPONENT_NAME, WebAppId.Factory.create(_project1, WebEnvironment.WebScope.WEBEDIT), true);
		final ComponentWebApps.WebComponentResult project_result2 = new ComponentWebApps.WebComponentResult(MODULE_NAME, COMPONENT_NAME, WebAppId.Factory.create(_project2, WebEnvironment.WebScope.WEBEDIT), false);

		// test & verify
		assertThat(global_result1).isEqualTo(new ComponentWebApps.WebComponentResult(MODULE_NAME, COMPONENT_NAME, WebAppId.Factory.create(globalWebAppId1), true));
		assertThat(global_result1).isNotEqualTo(new ComponentWebApps.WebComponentResult(MODULE_NAME, COMPONENT_NAME, WebAppId.Factory.create(globalWebAppId2), true));
		assertThat(global_result1).isEqualTo(new ComponentWebApps.WebComponentResult("ANOTHER_MODULE_NAME", COMPONENT_NAME, WebAppId.Factory.create(globalWebAppId1), true));
		assertThat(global_result1).isEqualTo(new ComponentWebApps.WebComponentResult(MODULE_NAME, "ANOTHER_COMPONENT_NAME", WebAppId.Factory.create(globalWebAppId1), true));
		assertThat(global_result1).isNotEqualTo(project_result1);
		assertThat(global_result1).isNotEqualTo(project_result2);

		assertThat(global_result2).isEqualTo(new ComponentWebApps.WebComponentResult(MODULE_NAME, COMPONENT_NAME, WebAppId.Factory.create(globalWebAppId1), false));
		assertThat(global_result2).isNotEqualTo(new ComponentWebApps.WebComponentResult(MODULE_NAME, COMPONENT_NAME, WebAppId.Factory.create(globalWebAppId2), false));
		assertThat(global_result2).isEqualTo(new ComponentWebApps.WebComponentResult("ANOTHER_MODULE_NAME", COMPONENT_NAME, WebAppId.Factory.create(globalWebAppId1), false));
		assertThat(global_result2).isEqualTo(new ComponentWebApps.WebComponentResult(MODULE_NAME, "ANOTHER_COMPONENT_NAME", WebAppId.Factory.create(globalWebAppId1), false));
		assertThat(global_result2).isNotEqualTo(project_result1);
		assertThat(global_result2).isNotEqualTo(project_result2);

		assertThat(project_result1).isEqualTo(new ComponentWebApps.WebComponentResult(MODULE_NAME, COMPONENT_NAME, WebAppId.Factory.create(_project1, WebEnvironment.WebScope.WEBEDIT), true));
		assertThat(project_result1).isEqualTo(new ComponentWebApps.WebComponentResult("ANOTHER_MODULE_NAME", COMPONENT_NAME, WebAppId.Factory.create(_project1, WebEnvironment.WebScope.WEBEDIT), true));
		assertThat(project_result1).isEqualTo(new ComponentWebApps.WebComponentResult(MODULE_NAME, "ANOTHER_COMPONENT_NAME", WebAppId.Factory.create(_project1, WebEnvironment.WebScope.WEBEDIT), true));
		assertThat(project_result1).isNotEqualTo(new ComponentWebApps.WebComponentResult(MODULE_NAME, COMPONENT_NAME, WebAppId.Factory.create(_project2, WebEnvironment.WebScope.WEBEDIT), true));
		assertThat(project_result1).isNotEqualTo(new ComponentWebApps.WebComponentResult(MODULE_NAME, COMPONENT_NAME, WebAppId.Factory.create(_project1, WebEnvironment.WebScope.LIVE), true));
		assertThat(project_result1).isNotEqualTo(project_result2);
		assertThat(project_result1).isNotEqualTo(global_result1);
		assertThat(project_result1).isNotEqualTo(global_result2);

		assertThat(project_result2).isEqualTo(new ComponentWebApps.WebComponentResult(MODULE_NAME, COMPONENT_NAME, WebAppId.Factory.create(_project2, WebEnvironment.WebScope.WEBEDIT), false));
		assertThat(project_result2).isEqualTo(new ComponentWebApps.WebComponentResult("ANOTHER_MODULE_NAME", COMPONENT_NAME, WebAppId.Factory.create(_project2, WebEnvironment.WebScope.WEBEDIT), false));
		assertThat(project_result2).isEqualTo(new ComponentWebApps.WebComponentResult(MODULE_NAME, "ANOTHER_COMPONENT_NAME", WebAppId.Factory.create(_project2, WebEnvironment.WebScope.WEBEDIT), false));
		assertThat(project_result2).isNotEqualTo(new ComponentWebApps.WebComponentResult(MODULE_NAME, COMPONENT_NAME, WebAppId.Factory.create(_project1, WebEnvironment.WebScope.WEBEDIT), false));
		assertThat(project_result2).isNotEqualTo(new ComponentWebApps.WebComponentResult(MODULE_NAME, COMPONENT_NAME, WebAppId.Factory.create(_project2, WebEnvironment.WebScope.LIVE), false));
		assertThat(project_result2).isNotEqualTo(project_result1);
		assertThat(project_result2).isNotEqualTo(global_result1);
		assertThat(project_result2).isNotEqualTo(global_result2);
	}

	@Test
	public void WebComponentResult_hashCode() {
		// setup
		final String globalWebAppId1 = "global-web-app";
		final String globalWebAppId2 = "another-global-web-app";
		final ComponentWebApps.WebComponentResult global_result1 = new ComponentWebApps.WebComponentResult(MODULE_NAME, COMPONENT_NAME, WebAppId.Factory.create(globalWebAppId1), true);
		final ComponentWebApps.WebComponentResult global_result2 = new ComponentWebApps.WebComponentResult(MODULE_NAME, COMPONENT_NAME, WebAppId.Factory.create(globalWebAppId1), false);
		final ComponentWebApps.WebComponentResult project_result1 = new ComponentWebApps.WebComponentResult(MODULE_NAME, COMPONENT_NAME, WebAppId.Factory.create(_project1, WebEnvironment.WebScope.WEBEDIT), true);
		final ComponentWebApps.WebComponentResult project_result2 = new ComponentWebApps.WebComponentResult(MODULE_NAME, COMPONENT_NAME, WebAppId.Factory.create(_project2, WebEnvironment.WebScope.WEBEDIT), false);

		// test & verify
		assertThat(global_result1.hashCode()).isEqualTo(new ComponentWebApps.WebComponentResult(MODULE_NAME, COMPONENT_NAME, WebAppId.Factory.create(globalWebAppId1), true).hashCode());
		assertThat(global_result1.hashCode()).isEqualTo(new ComponentWebApps.WebComponentResult("ANOTHER_MODULE_NAME", COMPONENT_NAME, WebAppId.Factory.create(globalWebAppId1), true).hashCode());
		assertThat(global_result1.hashCode()).isEqualTo(new ComponentWebApps.WebComponentResult(MODULE_NAME, "ANOTHER_COMPONENT_NAME", WebAppId.Factory.create(globalWebAppId1), true).hashCode());
		assertThat(global_result1.hashCode()).isNotEqualTo(new ComponentWebApps.WebComponentResult(MODULE_NAME, COMPONENT_NAME, WebAppId.Factory.create(globalWebAppId2), true).hashCode());
		assertThat(global_result1.hashCode()).isNotEqualTo(global_result2.hashCode());
		assertThat(global_result1.hashCode()).isNotEqualTo(project_result1.hashCode());
		assertThat(global_result1.hashCode()).isNotEqualTo(project_result2.hashCode());

		assertThat(global_result2.hashCode()).isEqualTo(new ComponentWebApps.WebComponentResult(MODULE_NAME, COMPONENT_NAME, WebAppId.Factory.create(globalWebAppId1), false).hashCode());
		assertThat(global_result2.hashCode()).isEqualTo(new ComponentWebApps.WebComponentResult("ANOTHER_MODULE_NAME", COMPONENT_NAME, WebAppId.Factory.create(globalWebAppId1), false).hashCode());
		assertThat(global_result2.hashCode()).isEqualTo(new ComponentWebApps.WebComponentResult(MODULE_NAME, "ANOTHER_COMPONENT_NAME", WebAppId.Factory.create(globalWebAppId1), false).hashCode());
		assertThat(global_result2.hashCode()).isNotEqualTo(new ComponentWebApps.WebComponentResult(MODULE_NAME, COMPONENT_NAME, WebAppId.Factory.create(globalWebAppId2), false).hashCode());
		assertThat(global_result2.hashCode()).isNotEqualTo(global_result1.hashCode());
		assertThat(global_result2.hashCode()).isNotEqualTo(project_result1.hashCode());
		assertThat(global_result2.hashCode()).isNotEqualTo(project_result2.hashCode());

		assertThat(project_result1.hashCode()).isEqualTo(new ComponentWebApps.WebComponentResult(MODULE_NAME, COMPONENT_NAME, WebAppId.Factory.create(_project1, WebEnvironment.WebScope.WEBEDIT), true).hashCode());
		assertThat(project_result1.hashCode()).isEqualTo(new ComponentWebApps.WebComponentResult("ANOTHER_MODULE_NAME", COMPONENT_NAME, WebAppId.Factory.create(_project1, WebEnvironment.WebScope.WEBEDIT), true).hashCode());
		assertThat(project_result1.hashCode()).isEqualTo(new ComponentWebApps.WebComponentResult(MODULE_NAME, "ANOTHER_COMPONENT_NAME", WebAppId.Factory.create(_project1, WebEnvironment.WebScope.WEBEDIT), true).hashCode());
		assertThat(project_result1.hashCode()).isNotEqualTo(new ComponentWebApps.WebComponentResult(MODULE_NAME, COMPONENT_NAME, WebAppId.Factory.create(_project2, WebEnvironment.WebScope.WEBEDIT), true).hashCode());
		assertThat(project_result1.hashCode()).isNotEqualTo(new ComponentWebApps.WebComponentResult(MODULE_NAME, COMPONENT_NAME, WebAppId.Factory.create(_project1, WebEnvironment.WebScope.PREVIEW), true).hashCode());
		assertThat(project_result1.hashCode()).isNotEqualTo(project_result2.hashCode());
		assertThat(project_result1.hashCode()).isNotEqualTo(global_result1.hashCode());
		assertThat(project_result1.hashCode()).isNotEqualTo(global_result2.hashCode());

		assertThat(project_result2.hashCode()).isEqualTo(new ComponentWebApps.WebComponentResult(MODULE_NAME, COMPONENT_NAME, WebAppId.Factory.create(_project2, WebEnvironment.WebScope.WEBEDIT), false).hashCode());
		assertThat(project_result2.hashCode()).isEqualTo(new ComponentWebApps.WebComponentResult("ANOTHER_MODULE_NAME", COMPONENT_NAME, WebAppId.Factory.create(_project2, WebEnvironment.WebScope.WEBEDIT), false).hashCode());
		assertThat(project_result2.hashCode()).isEqualTo(new ComponentWebApps.WebComponentResult(MODULE_NAME, "ANOTHER_COMPONENT_NAME", WebAppId.Factory.create(_project2, WebEnvironment.WebScope.WEBEDIT), false).hashCode());
		assertThat(project_result2.hashCode()).isNotEqualTo(new ComponentWebApps.WebComponentResult(MODULE_NAME, COMPONENT_NAME, WebAppId.Factory.create(_project1, WebEnvironment.WebScope.WEBEDIT), false).hashCode());
		assertThat(project_result2.hashCode()).isNotEqualTo(new ComponentWebApps.WebComponentResult(MODULE_NAME, COMPONENT_NAME, WebAppId.Factory.create(_project2, WebEnvironment.WebScope.PREVIEW), false).hashCode());
		assertThat(project_result2.hashCode()).isNotEqualTo(project_result1.hashCode());
		assertThat(project_result2.hashCode()).isNotEqualTo(global_result1.hashCode());
		assertThat(project_result2.hashCode()).isNotEqualTo(global_result2.hashCode());
	}

	@Test
	public void WebComponentInstallFailedResult_install_global_getException() {
		final String errorMessage = "Whoops...";
		final ModuleException moduleException = new ModuleException(errorMessage);
		final WebAppId webAppId = _globalWebApp.createWebAppId(null);
		final ComponentWebApps.WebComponentInstallFailedResult result = new ComponentWebApps.WebComponentInstallFailedResult(MODULE_NAME, COMPONENT_NAME, webAppId, false, moduleException);
		final IllegalStateException exception = result.getException();
		assertThat(exception.getMessage()).isEqualTo(String.format(ComponentWebApps.WebComponentInstallFailedResult.MESSAGE_INSTALL, MODULE_NAME, COMPONENT_NAME, WebAppIdentifier.getName(webAppId), ModuleException.class.getName() + ": " + errorMessage));
	}

	@Test
	public void WebComponentInstallFailedResult_install_global_toString() {
		final String errorMessage = "Whoops...";
		final ModuleException moduleException = new ModuleException(errorMessage);
		final WebAppId webAppId = _globalWebApp.createWebAppId(null);
		final ComponentWebApps.WebComponentInstallFailedResult result = new ComponentWebApps.WebComponentInstallFailedResult(MODULE_NAME, COMPONENT_NAME, webAppId, false, moduleException);
		assertThat(result.toString()).isEqualTo(String.format(ComponentWebApps.WebComponentInstallFailedResult.MESSAGE_INSTALL, MODULE_NAME, COMPONENT_NAME, WebAppIdentifier.getName(webAppId), ModuleException.class.getName() + ": " + errorMessage));
	}

	@Test
	public void WebComponentInstallFailedResult_update_global_getException() {
		final String errorMessage = "Whoops...";
		final ModuleException moduleException = new ModuleException(errorMessage);
		final WebAppId webAppId = _globalWebApp.createWebAppId(null);
		final ComponentWebApps.WebComponentInstallFailedResult result = new ComponentWebApps.WebComponentInstallFailedResult(MODULE_NAME, COMPONENT_NAME, webAppId, true, moduleException);
		final IllegalStateException exception = result.getException();
		assertThat(exception.getMessage()).isEqualTo(String.format(ComponentWebApps.WebComponentInstallFailedResult.MESSAGE_UPDATE, MODULE_NAME, COMPONENT_NAME, WebAppIdentifier.getName(webAppId), ModuleException.class.getName() + ": " + errorMessage));
	}

	@Test
	public void WebComponentInstallFailedResult_update_global_toString() {
		final String errorMessage = "Whoops...";
		final ModuleException moduleException = new ModuleException(errorMessage);
		final WebAppId webAppId = _globalWebApp.createWebAppId(null);
		final ComponentWebApps.WebComponentInstallFailedResult result = new ComponentWebApps.WebComponentInstallFailedResult(MODULE_NAME, COMPONENT_NAME, webAppId, true, moduleException);
		assertThat(result.toString()).isEqualTo(String.format(ComponentWebApps.WebComponentInstallFailedResult.MESSAGE_UPDATE, MODULE_NAME, COMPONENT_NAME, WebAppIdentifier.getName(webAppId), ModuleException.class.getName() + ": " + errorMessage));
	}

}
