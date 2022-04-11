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

package com.espirit.moddev.cli.commands.module.configureCommand;

import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.ConnectionManager;
import de.espirit.firstspirit.agency.GlobalWebAppId;
import de.espirit.firstspirit.agency.ModuleAdminAgent;
import de.espirit.firstspirit.agency.SpecialistsBroker;
import de.espirit.firstspirit.agency.WebAppId;
import de.espirit.firstspirit.module.descriptor.ComponentDescriptor;
import de.espirit.firstspirit.module.descriptor.ModuleDescriptor;

import com.espirit.moddev.cli.api.result.ExecutionResult;
import com.espirit.moddev.cli.api.result.ExecutionResults;
import com.espirit.moddev.cli.commands.module.configureCommand.json.ModuleConfiguration;
import com.espirit.moddev.cli.commands.module.configureCommand.json.components.ComponentWebApps;
import com.espirit.moddev.cli.commands.module.configureCommand.json.components.Components;
import com.espirit.moddev.cli.commands.module.configureCommand.json.components.ConfigurationContext;
import com.espirit.moddev.cli.commands.module.configureCommand.json.components.common.ComponentNotFoundResult;
import com.espirit.moddev.cli.commands.module.utils.WebAppUtil;
import com.espirit.moddev.cli.configuration.GlobalConfig;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConfigureModulesCommandTest {

	private static final String MODULE_NAME = "testModule";
	private static final String COMPONENT_NAME = "testComponent";

	private final String _moduleName = "testModule";
	private ModuleAdminAgent _moduleAdminAgent;
	private ConfigurationContext _context;
	private Connection _connection;
	private ModuleDescriptor _moduleDescriptor;

	@BeforeEach
	public void setup() {
		// integrative tests
		_moduleDescriptor = mock(ModuleDescriptor.class);
		when(_moduleDescriptor.getModuleName()).thenReturn(_moduleName);
		_moduleAdminAgent = spy(mock(ModuleAdminAgent.class));
		_connection = mock(de.espirit.firstspirit.access.Connection.class);
		final SpecialistsBroker broker = mock(SpecialistsBroker.class);
		when(_connection.getBroker()).thenReturn(broker);
		when(broker.requireSpecialist(ModuleAdminAgent.TYPE)).thenReturn(_moduleAdminAgent);
		_context = new ConfigurationContext(_connection, mock(GlobalConfig.class));
	}

	@Test
	public void extractWebAppsToDeploy_only_webComponentResults_with_deploy_true() {
		// setup
		final ExecutionResults results = new ExecutionResults();
		results.add(new ComponentNotFoundResult(MODULE_NAME, ComponentDescriptor.Type.WEBAPP, COMPONENT_NAME));
		final GlobalWebAppId webAppId1 = WebAppId.Factory.create("global-web-app-1");
		final GlobalWebAppId webAppId2 = WebAppId.Factory.create("global-web-app-2");
		results.add(new ComponentWebApps.WebComponentResult(MODULE_NAME, COMPONENT_NAME, webAppId1, true));
		results.add(new ComponentWebApps.WebComponentResult(MODULE_NAME, COMPONENT_NAME, webAppId2, false));

		// test
		final Collection<WebAppId> webAppIds = ConfigureModulesCommand.extractWebAppsToDeploy(results);

		// verify
		assertThat(webAppIds).hasSize(1);
		final WebAppId id = webAppIds.iterator().next();
		assertThat((GlobalWebAppId) id).isEqualTo(webAppId1);
	}

	@Test
	public void extractWebAppsToDeploy_distinct_equal_webApp_from_different_modules_or_components() {
		// setup
		final ExecutionResults results = new ExecutionResults();
		final GlobalWebAppId webAppId1 = WebAppId.Factory.create("global-web-app-1");
		results.add(new ComponentWebApps.WebComponentResult("m1", "c1", webAppId1, true));
		results.add(new ComponentWebApps.WebComponentResult("m1", "c2", webAppId1, true));
		results.add(new ComponentWebApps.WebComponentResult("m2", "c1", webAppId1, true));
		results.add(new ComponentWebApps.WebComponentResult("m2", "c2", webAppId1, true));

		// test
		final Collection<WebAppId> webAppIds = ConfigureModulesCommand.extractWebAppsToDeploy(results);

		// verify
		assertThat(webAppIds).hasSize(1);
		final WebAppId id = webAppIds.iterator().next();
		assertThat((GlobalWebAppId) id).isEqualTo(webAppId1);
	}

	@Test
	public void extractWebAppsToDeploy_different_deploy_states_on_equal_webapp() {
		// setup
		final ExecutionResults results = new ExecutionResults();
		final GlobalWebAppId webAppId1 = WebAppId.Factory.create("global-web-app-1");
		results.add(new ComponentWebApps.WebComponentResult("m1", "c1", webAppId1, true));
		results.add(new ComponentWebApps.WebComponentResult("m1", "c2", webAppId1, false));

		// test
		final Collection<WebAppId> webAppIds = ConfigureModulesCommand.extractWebAppsToDeploy(results);

		// verify
		assertThat(webAppIds).hasSize(1);
		final WebAppId id = webAppIds.iterator().next();
		assertThat((GlobalWebAppId) id).isEqualTo(webAppId1);
	}

	@Test
	public void configureModule_module_not_found() {
		// setup
		final ConfigureModulesCommand command = new ConfigureModulesCommand();
		final ModuleConfiguration moduleConfiguration = spy(new ModuleConfiguration("unknownModule", new Components()));

		// test
		final ExecutionResult result = command.configureModule(_context, moduleConfiguration);

		//verify
		assertThat(result).isInstanceOf(ConfigureModulesCommand.ModuleNotFoundResult.class);
	}

	@Test
	public void configureModule_by_displayName() {
		// setup
		final ConfigureModulesCommand command = new ConfigureModulesCommand();
		final ModuleConfiguration moduleConfiguration = spy(new ModuleConfiguration("testDisplayName", new Components()));
		when(_moduleAdminAgent.getModules()).thenReturn(Lists.newArrayList(_moduleDescriptor));
		when(_moduleDescriptor.getDisplayName()).thenReturn("testDisplayName");

		// test
		command.configureModule(_context, moduleConfiguration);

		//verify
		verify(moduleConfiguration, times(1)).configure(_context, _moduleDescriptor);
	}

	@Test
	public void configureModule_by_displayName_multiple_modules_found() {
		// setup
		final ConfigureModulesCommand command = new ConfigureModulesCommand();
		final ModuleConfiguration moduleConfiguration = spy(new ModuleConfiguration("testDisplayName", new Components()));
		when(_moduleAdminAgent.getModules()).thenReturn(Lists.newArrayList(_moduleDescriptor, _moduleDescriptor));
		when(_moduleDescriptor.getDisplayName()).thenReturn("testDisplayName");

		// test
		final ExecutionResult result = command.configureModule(_context, moduleConfiguration);

		//verify
		assertThat(result).isInstanceOf(ConfigureModulesCommand.MultipleModulesFoundResult.class);
	}

	@Test
	public void configureModule() {
		// setup
		final ConfigureModulesCommand command = new ConfigureModulesCommand();
		final ModuleConfiguration moduleConfiguration = spy(new ModuleConfiguration(_moduleDescriptor.getModuleName(), new Components()));
		when(_moduleAdminAgent.getModules()).thenReturn(Lists.newArrayList(_moduleDescriptor));

		// test
		command.configureModule(_context, moduleConfiguration);

		//verify
		verify(moduleConfiguration, times(1)).configure(_context, _moduleDescriptor);
	}

	@Test
	public void configureModules() {
		// setup
		final ConfigureModulesCommand command = new ConfigureModulesCommand();
		final String moduleName1 = "m1";
		// first module
		final ModuleConfiguration moduleConfiguration1 = spy(new ModuleConfiguration(moduleName1, new Components()));
		final ModuleDescriptor moduleDescriptor1 = mock(ModuleDescriptor.class);
		when(moduleDescriptor1.getModuleName()).thenReturn(moduleName1);
		// second module
		final String moduleName2 = "m2";
		final ModuleConfiguration moduleConfiguration2 = spy(new ModuleConfiguration(moduleName2, new Components()));
		final ModuleDescriptor moduleDescriptor2 = mock(ModuleDescriptor.class);
		when(moduleDescriptor2.getModuleName()).thenReturn(moduleName2);
		// unknown module
		final String moduleName3 = "m3";
		final ModuleConfiguration moduleConfiguration3 = spy(new ModuleConfiguration(moduleName3, new Components()));

		when(_moduleAdminAgent.getModules()).thenReturn(Lists.newArrayList(moduleDescriptor1, moduleDescriptor2));

		// test
		final ExecutionResults results = command.configureModules(_context, Lists.newArrayList(moduleConfiguration1, moduleConfiguration2, moduleConfiguration3));

		//verify
		assertThat(results.size()).isEqualTo(3);
		assertThat(results.get(0)).isInstanceOf(ModuleConfiguration.ModuleConfiguredResult.class);
		assertThat(results.get(1)).isInstanceOf(ModuleConfiguration.ModuleConfiguredResult.class);
		assertThat(results.get(2)).isInstanceOf(ConfigureModulesCommand.ModuleNotFoundResult.class);
		verify(moduleConfiguration1, times(1)).configure(_context, moduleDescriptor1);
		verify(moduleConfiguration2, times(1)).configure(_context, moduleDescriptor2);
	}

	@Test
	public void configureModules_empty_list() {
		final ConfigureModulesCommand command = new ConfigureModulesCommand();
		Assertions.assertThrows(IllegalStateException.class, () -> {
			command.configureModules(_context, Collections.emptyList());
		});
	}

	@Test
	public void execute() {
		// setup
		when(_connection.getMode()).thenReturn(ConnectionManager.SOCKET_MODE);
		final List<ModuleConfiguration> moduleConfigurations = new ArrayList<>();
		final ConfigureModulesCommand command = mock(ConfigureModulesCommand.class);
		when(command.execute(_context, moduleConfigurations)).thenCallRealMethod();
		final ExecutionResults configureResult = new ExecutionResults();
		final GlobalWebAppId webAppId = WebAppId.Factory.create("global-web-app-1");
		configureResult.add(new ModuleConfiguration.ModuleConfiguredResult(MODULE_NAME));
		configureResult.add(new ComponentWebApps.WebComponentResult(MODULE_NAME, COMPONENT_NAME, webAppId, true));
		when(command.configureModules(_context, moduleConfigurations)).thenReturn(configureResult);
		when(_moduleAdminAgent.deployWebApp(webAppId)).thenReturn(true);

		// test
		final ExecutionResults result = command.execute(_context, moduleConfigurations);

		// verify
		assertThat(result.size()).isEqualTo(3);
		assertThat(result.get(0)).isInstanceOf(ModuleConfiguration.ModuleConfiguredResult.class);
		assertThat(result.get(1)).isInstanceOf(ComponentWebApps.WebComponentResult.class);
		assertThat(result.get(2)).isInstanceOf(WebAppUtil.WebAppDeployedResult.class);
	}

}
