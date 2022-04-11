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

package com.espirit.moddev.cli.commands.module.installCommand;

import de.espirit.firstspirit.agency.GlobalWebAppId;
import de.espirit.firstspirit.agency.ModuleAdminAgent;
import de.espirit.firstspirit.server.module.WebAppType;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ModuleInstallationConfigurationTest {

	@Test
	public void fromFile_partly_deserialization_with_additional_parameter() throws Exception {
		final List<ModuleInstallationConfiguration> moduleInstallationConfigurations = ModuleInstallationConfiguration.fromFile("src/test/resources/module_installBulk_partly_additional.json");
		assertEquals(1, moduleInstallationConfigurations.size());
		// verify first configuration
		final ModuleInstallationConfiguration configuration = moduleInstallationConfigurations.get(0);
		assertEquals("path/to/file.fsm", configuration.getFsm());
		assertEquals("myProjectName", configuration.getModuleProjectName());
		assertEquals(0, configuration.getWebAppScopes().size());
		assertEquals(0, configuration.getWebAppConfigurationFiles().size());
		assertEquals(0, configuration.getServiceConfigurationFiles().size());
		assertNull(configuration.getDeploy());
		assertNull(configuration.getProjectAppConfigurationFile());
	}

	@Test
	public void fromFile_complete_deserialization() throws Exception {
		final List<ModuleInstallationConfiguration> moduleInstallationConfigurations = ModuleInstallationConfiguration.fromFile("src/test/resources/module_installBulk_complete.json");
		assertEquals(2, moduleInstallationConfigurations.size());
		// verify first configuration
		ModuleInstallationConfiguration configuration = moduleInstallationConfigurations.get(0);
		assertEquals("path/to/file_1.fsm", configuration.getFsm());
		assertEquals("myProjectName", configuration.getModuleProjectName());
		assertEquals("false", configuration.getDeploy());
		assertEquals("path/to/projectApp/1.cfg", configuration.getProjectAppConfigurationFile());
		assertEquals(2, configuration.getServiceConfigurationFiles().size());
		assertEquals("path/to/service/1.cfg", configuration.getServiceConfigurationFiles().get(0));
		assertEquals("path/to/service/2.cfg", configuration.getServiceConfigurationFiles().get(1));
		assertEquals(2, configuration.getWebAppConfigurationFiles().size());
		assertEquals("path/to/webapp/1.cfg", configuration.getWebAppConfigurationFiles().get(0));
		assertEquals("path/to/webapp/2.cfg", configuration.getWebAppConfigurationFiles().get(1));
		assertEquals(2, configuration.getWebAppScopes().size());
		assertEquals("global(fs5preview)", configuration.getWebAppScopes().get(0));
		assertEquals("global(fs5staging)", configuration.getWebAppScopes().get(1));
		// verify second configuration
		configuration = moduleInstallationConfigurations.get(1);
		assertEquals("path/to/file_2.fsm", configuration.getFsm());
		assertEquals("myProjectName2", configuration.getModuleProjectName());
		assertEquals("true", configuration.getDeploy());
		assertEquals("path/to/projectApp/2.cfg", configuration.getProjectAppConfigurationFile());
		assertEquals(2, configuration.getServiceConfigurationFiles().size());
		assertEquals("path/to/service/3.cfg", configuration.getServiceConfigurationFiles().get(0));
		assertEquals("path/to/service/4.cfg", configuration.getServiceConfigurationFiles().get(1));
		assertEquals(2, configuration.getWebAppConfigurationFiles().size());
		assertEquals("path/to/webapp/3.cfg", configuration.getWebAppConfigurationFiles().get(0));
		assertEquals("path/to/webapp/4.cfg", configuration.getWebAppConfigurationFiles().get(1));
		assertEquals(2, configuration.getWebAppScopes().size());
		assertEquals("myWebAppScope", configuration.getWebAppScopes().get(0));
		assertEquals("global(fs5webmon)", configuration.getWebAppScopes().get(1));
	}

	@Test
	public void fromFile_fileDoesNotExist() throws Exception {
		try {
			ModuleInstallationConfiguration.fromFile("fileDoesNotExist.json");
			fail("IllegalArgumentException expected");
		} catch (final IllegalArgumentException e) {
			assertEquals("File 'fileDoesNotExist.json' does not exist.", e.getMessage());
		}
	}

	@Test
	public void fromFile_verify_global_scopes() throws Exception {
		// mock ModuleAdminAgent
		final ModuleAdminAgent moduleAdminAgent = mock(ModuleAdminAgent.class);
		final Collection<GlobalWebAppId> globalWebApps = new ArrayList<>();
		final GlobalWebAppId webMonWebApp = mock(GlobalWebAppId.class);
		when(webMonWebApp.getGlobalId()).thenReturn(WebAppType.FS5WEBMON.getId());
		globalWebApps.add(webMonWebApp);
		when(moduleAdminAgent.getGlobalWebApps(anyBoolean())).thenReturn(globalWebApps);
		// load configuration from json file
		final List<ModuleInstallationConfiguration> moduleInstallationConfigurations = ModuleInstallationConfiguration.fromFile("src/test/resources/module_verify_global_scopes.json");
		assertEquals(1, moduleInstallationConfigurations.size());
		// verify configuration
		ModuleInstallationConfiguration configuration = moduleInstallationConfigurations.get(0);
		assertEquals("path/to/file.fsm", configuration.getFsm());
		final List<String> webAppScopes = configuration.getWebAppScopes();
		assertEquals(2, webAppScopes.size());
		assertEquals("global(unknownWebApp)", webAppScopes.get(0));
		assertEquals("global(fs5webmon)", webAppScopes.get(1));
		// verify scopes
		final ArrayList<Exception> errors = new ArrayList<>();
		configuration.verifyScopes(moduleAdminAgent, errors);
		// verify errors
		assertEquals(1, errors.size());
		assertEquals(IllegalArgumentException.class, errors.get(0).getClass());
		assertEquals("path/to/file.fsm - Unknown global scope 'unknownWebApp'!", errors.get(0).getMessage());
	}

}
