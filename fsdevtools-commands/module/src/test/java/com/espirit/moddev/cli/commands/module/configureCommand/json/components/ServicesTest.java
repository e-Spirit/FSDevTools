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

import com.espirit.moddev.cli.api.result.ExecutionErrorResult;
import com.espirit.moddev.cli.api.result.ExecutionResult;
import com.espirit.moddev.cli.api.result.ExecutionResults;
import com.espirit.moddev.cli.commands.module.configureCommand.json.components.common.ComponentNotFoundResult;
import com.espirit.moddev.cli.commands.module.utils.FileSystemUtil;
import com.espirit.moddev.cli.configuration.GlobalConfig;
import com.espirit.moddev.util.JacksonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.ServiceNotFoundException;
import de.espirit.firstspirit.agency.ModuleAdminAgent;
import de.espirit.firstspirit.agency.SpecialistsBroker;
import de.espirit.firstspirit.io.FileSystem;
import de.espirit.firstspirit.io.MemoryFileSystem;
import de.espirit.firstspirit.module.descriptor.ComponentDescriptor;
import de.espirit.firstspirit.module.descriptor.ModuleDescriptor;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_AUTO_START;
import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_FILES;
import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_RESTART;
import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_SERVICE_NAME;
import static com.espirit.moddev.cli.commands.module.configureCommand.json.JsonTestUtil.createEntry;
import static com.espirit.moddev.cli.commands.module.configureCommand.json.JsonTestUtil.createMap;
import static com.espirit.moddev.cli.commands.module.configureCommand.json.JsonTestUtil.toJsonObject;
import static com.espirit.moddev.cli.commands.module.utils.FileSystemUtilTest.verifyDirDoesNotExist;
import static com.espirit.moddev.cli.commands.module.utils.FileSystemUtilTest.verifyFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings({"unchecked", "rawtypes"})
public class ServicesTest {

	private ObjectMapper _objectMapper;

	private final String _moduleName = "testModule";
	private final String _serviceName = "testService";
	private final String _serviceDisplayName = "testServiceDisplayName";
	private ModuleAdminAgent _moduleAdminAgent;
	private Connection _connection;
	private ConfigurationContext _context;
	private MemoryFileSystem _fileSystem;
	private ModuleDescriptor _moduleDescriptor;
	private ComponentDescriptor _serviceDescriptor;

	@Before
	public void setup() {
		_objectMapper = JacksonUtil.createInputMapper();

		// integrative tests
		_moduleDescriptor = mock(ModuleDescriptor.class);
		when(_moduleDescriptor.getModuleName()).thenReturn(_moduleName);
		_moduleAdminAgent = spy(mock(ModuleAdminAgent.class));
		_fileSystem = new MemoryFileSystem();
		when(_moduleAdminAgent.getServiceConfig(_serviceName)).thenReturn((FileSystem) _fileSystem);
		_connection = mock(Connection.class);
		_context = new ConfigurationContext(_connection, mock(GlobalConfig.class));
		final SpecialistsBroker broker = mock(SpecialistsBroker.class);
		when(_connection.getBroker()).thenReturn(broker);
		when(broker.requireSpecialist(ModuleAdminAgent.TYPE)).thenReturn(_moduleAdminAgent);
		_serviceDescriptor = mock(ComponentDescriptor.class);
		when(_serviceDescriptor.getType()).thenReturn(ComponentDescriptor.Type.SERVICE);
		when(_serviceDescriptor.getName()).thenReturn(_serviceName);
		when(_serviceDescriptor.getDisplayName()).thenReturn(_serviceDisplayName);
	}

	@Test
	public void deserialize_defaultValues() throws IOException {
		// setup
		final String serviceName = "myServiceName";
		final String json = _objectMapper.writeValueAsString(new Service(serviceName));

		// test
		final Service service = _objectMapper.readValue(json, Service.class);

		// verify
		assertThat(service.getServiceName()).isEqualTo(serviceName);
		assertThat(service.getAutoStart()).isFalse();
		assertThat(service.getRestart()).isFalse();
		assertThat(service.getFiles()).hasSize(0);
	}

	@Test(expected = MismatchedInputException.class)
	public void deserialize_serviceName_is_undefined() throws IOException {
		// setup
		final String json = toJsonObject(createMap(createEntry(ATTR_AUTO_START, true)));

		// test
		_objectMapper.readValue(json, Service.class);
	}

	@Test
	public void deserialize_customizedValues() throws IOException {
		// setup
		final String serviceName = "myServiceName";
		final String json = toJsonObject(createMap(
				createEntry(ATTR_SERVICE_NAME, serviceName),
				createEntry(ATTR_AUTO_START, true),
				createEntry(ATTR_RESTART, true),
				createEntry(ATTR_FILES, Lists.newArrayList("dir/1.json", "dir/subDir/2.json"))
		));

		// test
		final Service deserialized = _objectMapper.readValue(json, Service.class);

		// verify
		assertThat(deserialized.getServiceName()).isEqualTo(serviceName);
		assertThat(deserialized.getAutoStart()).isTrue();
		assertThat(deserialized.getRestart()).isTrue();
		assertThat(deserialized.getFiles()).hasSize(2);
		assertThat(deserialized.getFiles().get(0)).isEqualTo("dir/1.json");
		assertThat(deserialized.getFiles().get(1)).isEqualTo("dir/subDir/2.json");
	}

	@Test
	public void configure_service_component_not_found() {
		// setup
		when(_moduleAdminAgent.isAutostart(anyString())).thenThrow(new ServiceNotFoundException(_serviceName));
		when(_moduleDescriptor.getComponents()).thenReturn(new ComponentDescriptor[0]);

		// test
		final Service service = new Service(_serviceName);
		final ExecutionResult result = service.configure(_context, _moduleDescriptor);

		// verify
		assertThat(result).isInstanceOf(ComponentNotFoundResult.class);
	}

	@Test
	public void configure_service_stop_failed() {
		// setup
		final de.espirit.firstspirit.module.Service<?> fsService = mock(de.espirit.firstspirit.module.Service.class);
		when(_connection.getService(anyString())).thenReturn(fsService);
		when(_moduleDescriptor.getComponents()).thenReturn(new ComponentDescriptor[]{_serviceDescriptor});
		doThrow(new ServiceNotFoundException(_serviceName)).when(_moduleAdminAgent).stopService(_serviceName);
		final Service service = new Service(_serviceName);
		service._restart = true;

		// test
		final ExecutionResult result = service.configure(_context, _moduleDescriptor);

		// verify
		assertThat(result).isInstanceOf(Service.ServiceStopFailedResult.class);
		assertThat(((Service.ServiceStopFailedResult) result).getModuleName()).isEqualTo(_moduleName);
		assertThat(((Service.ServiceStopFailedResult) result).getServiceName()).isEqualTo(_serviceName);
		assertThat(((ExecutionErrorResult<Exception>) result).getException().getMessage()).isEqualTo(String.format(Service.ServiceStopFailedResult.MESSAGE, _moduleName, _serviceName));
		assertThat(result.toString()).isEqualTo(String.format(Service.ServiceStopFailedResult.MESSAGE, _moduleName, _serviceName));
		verify(_moduleAdminAgent, times(1)).stopService(_serviceName);
		verify(_moduleAdminAgent, times(0)).isAutostart(_serviceName);    // make sure the order is kept (stop, configure, start)
		verify(_moduleAdminAgent, times(0)).startService(_serviceName);
	}

	@Test
	public void configure_service_start_failed() {
		// setup
		final de.espirit.firstspirit.module.Service<?> fsService = mock(de.espirit.firstspirit.module.Service.class);
		when(_connection.getService(anyString())).thenReturn(fsService);
		doThrow(new ServiceNotFoundException(_serviceName)).when(_moduleAdminAgent).startService(_serviceName);
		when(_moduleDescriptor.getComponents()).thenReturn(new ComponentDescriptor[]{_serviceDescriptor});
		final Service service = new Service(_serviceName);
		service._restart = true;

		// test
		final ExecutionResult result = service.configure(_context, _moduleDescriptor);

		// verify
		assertThat(result).isInstanceOf(Service.ServiceStartFailedResult.class);
		assertThat(((Service.ServiceStartFailedResult) result).getModuleName()).isEqualTo(_moduleName);
		assertThat(((Service.ServiceStartFailedResult) result).getServiceName()).isEqualTo(_serviceName);
		assertThat(((ExecutionErrorResult<Exception>) result).getException().getMessage()).isEqualTo(String.format(Service.ServiceStartFailedResult.MESSAGE, _moduleName, _serviceName));
		assertThat(result.toString()).isEqualTo(String.format(Service.ServiceStartFailedResult.MESSAGE, _moduleName, _serviceName));
		verify(_moduleAdminAgent, times(1)).stopService(_serviceName);
		verify(_moduleAdminAgent, times(1)).startService(_serviceName);
	}

	@Test
	public void configure_full_stack_scenario() {
		// setup
		final de.espirit.firstspirit.module.Service<?> fsService = mock(de.espirit.firstspirit.module.Service.class);
		when(_connection.getService(anyString())).thenReturn(fsService);
		when(_moduleAdminAgent.isAutostart(_serviceName)).thenReturn(false);
		when(_moduleDescriptor.getComponents()).thenReturn(new ComponentDescriptor[]{_serviceDescriptor});

		final Service service = new Service(_serviceName);
		service._files = Lists.list("src/test/resources/dir/", "src/test/resources/1.json");
		service._autoStart = true;
		service._restart = true;

		// test
		final ExecutionResult result = service.configure(_context, _moduleDescriptor);

		// verify
		assertThat(result).isInstanceOf(Service.ServiceConfiguredResult.class);
		verifyFile(_fileSystem, "1.json");
		verifyDirDoesNotExist(_fileSystem, "dir");
		verifyFile(_fileSystem, "2.json");
		verifyFile(_fileSystem, "subDir/3.json");
		verify(_moduleAdminAgent, times(0)).setAutostart(_serviceName, false);
		verify(_moduleAdminAgent, times(1)).setAutostart(_serviceName, true);
		verify(_moduleAdminAgent, times(1)).startService(_serviceName);
		verify(_moduleAdminAgent, times(1)).stopService(_serviceName);
	}

	@Test
	public void configure_upload_files_completed() {
		// setup
		final de.espirit.firstspirit.module.Service<?> fsService = mock(de.espirit.firstspirit.module.Service.class);
		when(_connection.getService(anyString())).thenReturn(fsService);
		when(_moduleAdminAgent.isAutostart(_serviceName)).thenReturn(false);
		when(_moduleDescriptor.getComponents()).thenReturn(new ComponentDescriptor[]{_serviceDescriptor});

		final Service service = new Service(_serviceName);
		service._files = Lists.list("src/test/resources/dir/", "src/test/resources/1.json");

		// test
		final ExecutionResult result = service.configure(_context, _moduleDescriptor);

		// verify
		assertThat(result).isInstanceOf(Service.ServiceConfiguredResult.class);
		verifyFile(_fileSystem, "1.json");
		verifyDirDoesNotExist(_fileSystem, "dir");
		verifyFile(_fileSystem, "2.json");
		verifyFile(_fileSystem, "subDir/3.json");
	}

	@Test
	public void configure_upload_files_partly_failed() {
		// setup
		final de.espirit.firstspirit.module.Service<?> fsService = mock(de.espirit.firstspirit.module.Service.class);
		when(_connection.getService(anyString())).thenReturn(fsService);
		when(_moduleAdminAgent.isAutostart(_serviceName)).thenReturn(false);
		when(_moduleDescriptor.getComponents()).thenReturn(new ComponentDescriptor[]{_serviceDescriptor});

		final Service service = new Service(_serviceName);
		service._files = Lists.list("src/test/resources/nonExistingFile.json", "src/test/resources/1.json");

		// test
		final ExecutionResult result = service.configure(_context, _moduleDescriptor);

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

		// verify: no more interactions after file upload failed
		verify(_moduleAdminAgent, times(0)).setAutostart(_serviceName, true);
		verify(_moduleAdminAgent, times(0)).setAutostart(_serviceName, false);
		verify(_moduleAdminAgent, times(0)).stopService(_serviceName);
		verify(_moduleAdminAgent, times(0)).startService(_serviceName);
	}

	@Test
	public void configure_autoStart_from_false_to_true() {
		// setup
		final de.espirit.firstspirit.module.Service<?> fsService = mock(de.espirit.firstspirit.module.Service.class);
		when(_connection.getService(anyString())).thenReturn(fsService);
		when(_moduleAdminAgent.isAutostart(_serviceName)).thenReturn(false);
		when(_moduleDescriptor.getComponents()).thenReturn(new ComponentDescriptor[]{_serviceDescriptor});

		final Service service = new Service(_serviceName);
		service._autoStart = true;

		// test
		final ExecutionResult result = service.configure(_context, _moduleDescriptor);

		// verify
		assertThat(result).isInstanceOf(Service.ServiceConfiguredResult.class);
		verify(_moduleAdminAgent, times(1)).setAutostart(_serviceName, true);
		verify(_moduleAdminAgent, times(0)).setAutostart(_serviceName, false);
	}

	@Test
	public void configure_autoStart_does_not_change_true_true() {
		// setup
		final de.espirit.firstspirit.module.Service<?> fsService = mock(de.espirit.firstspirit.module.Service.class);
		when(_connection.getService(anyString())).thenReturn(fsService);
		when(_moduleAdminAgent.isAutostart(_serviceName)).thenReturn(true);
		when(_moduleDescriptor.getComponents()).thenReturn(new ComponentDescriptor[]{_serviceDescriptor});

		final Service service = new Service(_serviceName);
		service._autoStart = true;

		// test
		final ExecutionResult result = service.configure(_context, _moduleDescriptor);

		// verify
		assertThat(result).isInstanceOf(Service.ServiceConfiguredResult.class);
		verify(_moduleAdminAgent, times(0)).setAutostart(_serviceName, true);
		verify(_moduleAdminAgent, times(0)).setAutostart(_serviceName, false);
	}

	@Test
	public void configure_autoStart_from_true_to_false() {
		// setup
		final de.espirit.firstspirit.module.Service<?> fsService = mock(de.espirit.firstspirit.module.Service.class);
		when(_connection.getService(anyString())).thenReturn(fsService);
		when(_moduleAdminAgent.isAutostart(_serviceName)).thenReturn(true);
		when(_moduleDescriptor.getComponents()).thenReturn(new ComponentDescriptor[]{_serviceDescriptor});

		final Service service = new Service(_serviceName);
		service._autoStart = false;

		// test
		final ExecutionResult result = service.configure(_context, _moduleDescriptor);

		// verify
		assertThat(result).isInstanceOf(Service.ServiceConfiguredResult.class);
		verify(_moduleAdminAgent, times(1)).setAutostart(_serviceName, false);
		verify(_moduleAdminAgent, times(0)).setAutostart(_serviceName, true);
	}

	@Test
	public void configure_autoStart_does_not_change_false_false() {
		// setup
		final de.espirit.firstspirit.module.Service<?> fsService = mock(de.espirit.firstspirit.module.Service.class);
		when(_connection.getService(anyString())).thenReturn(fsService);
		when(_moduleAdminAgent.isAutostart(_serviceName)).thenReturn(false);
		when(_moduleDescriptor.getComponents()).thenReturn(new ComponentDescriptor[]{_serviceDescriptor});

		final Service service = new Service(_serviceName);
		service._autoStart = false;

		// test
		final ExecutionResult result = service.configure(_context, _moduleDescriptor);

		// verify
		assertThat(result).isInstanceOf(Service.ServiceConfiguredResult.class);
		verify(_moduleAdminAgent, times(0)).setAutostart(_serviceName, true);
		verify(_moduleAdminAgent, times(0)).setAutostart(_serviceName, false);
	}

	@Test
	public void configure_restart_service_false() {
		// setup
		final de.espirit.firstspirit.module.Service<?> fsService = mock(de.espirit.firstspirit.module.Service.class);
		when(_connection.getService(anyString())).thenReturn(fsService);
		when(_moduleDescriptor.getComponents()).thenReturn(new ComponentDescriptor[]{_serviceDescriptor});

		final Service service = new Service(_serviceName);
		service._restart = false;

		// test
		final ExecutionResult result = service.configure(_context, _moduleDescriptor);

		// verify
		assertThat(result).isInstanceOf(Service.ServiceConfiguredResult.class);
		verify(_moduleAdminAgent, times(0)).startService(_serviceName);
		verify(_moduleAdminAgent, times(0)).stopService(_serviceName);
	}

	@Test
	public void configure_restart_service_true() {
		// setup
		final de.espirit.firstspirit.module.Service<?> fsService = mock(de.espirit.firstspirit.module.Service.class);
		when(_connection.getService(anyString())).thenReturn(fsService);
		when(_moduleDescriptor.getComponents()).thenReturn(new ComponentDescriptor[]{_serviceDescriptor});

		final Service service = new Service(_serviceName);
		service._restart = true;

		// test
		final ExecutionResult result = service.configure(_context, _moduleDescriptor);

		// verify
		assertThat(result).isInstanceOf(Service.ServiceConfiguredResult.class);
		verify(_moduleAdminAgent, times(1)).startService(_serviceName);
		verify(_moduleAdminAgent, times(1)).stopService(_serviceName);
	}

}
