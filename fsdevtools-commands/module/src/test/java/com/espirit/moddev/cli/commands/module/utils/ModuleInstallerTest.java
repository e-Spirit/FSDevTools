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

package com.espirit.moddev.cli.commands.module.utils;

import com.espirit.moddev.cli.commands.module.common.ModuleInstallationParameters;
import com.espirit.moddev.shared.webapp.WebAppIdentifier;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.agency.ModuleAdminAgent;
import de.espirit.firstspirit.agency.WebAppId;
import de.espirit.firstspirit.module.descriptor.ModuleDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ModuleInstallerTest {


	private static final String MODULE_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<module>\n" +
			// general
			"\t<name>TestModule</name>\n" +
			"\t<version>0.0.1-SNAPSHOT</version>\n" +
			"\t<displayname>TestModule</displayname>\n" +
			"\t<description>TestModule</description>\n" +
			"\t<vendor>Crownpeak Technology GmbH</vendor>\n" +
			"\t<licenses>META-INF/licenses.csv</licenses>\n" +
			"\t<dependencies/>\n" +
			"\t<class>de.espirit.test.TestModule</class>\n" +
			"\t<components>\n" +
			"\t\t<service>\n" +
			// services
			"\t\t\t<name>TestService#1</name>\n" +
			"\t\t\t<displayname/>\n" +
			"\t\t\t<description/>\n" +
			"\t\t\t<class>de.espirit.test.TestService</class>\n" +
			"\t\t</service>\n" +
			"\t\t<service>\n" +
			"\t\t\t<name>TestService#2</name>\n" +
			"\t\t\t<displayname/>\n" +
			"\t\t\t<description/>\n" +
			"\t\t\t<class>de.espirit.test.TestService</class>\n" +
			"\t\t</service>\n" +
			// project app components
			"\t\t<project-app>\n" +
			"\t\t\t<name>TestProjectApp#1</name>\n" +
			"\t\t\t<displayname/>\n" +
			"\t\t\t<description/>\n" +
			"\t\t\t<class>de.espirit.test.TestProjectApp#1</class>\n" +
			"\t\t</project-app>\n" +
			"\t\t<project-app>\n" +
			"\t\t\t<name>TestProjectApp#2</name>\n" +
			"\t\t\t<displayname/>\n" +
			"\t\t\t<description/>\n" +
			"\t\t\t<class>de.espirit.test.TestProjectApp#2</class>\n" +
			"\t\t</project-app>\n" +
			// web app components
			"\t\t<web-app scopes=\"PROJECT,GLOBAL\">\n" +
			"\t\t\t<name>TestWebApp#1</name>\n" +
			"\t\t\t<displayname/>\n" +
			"\t\t\t<description/>\n" +
			"\t\t\t<class>de.espirit.test.TestWebApp</class>\n" +
			"\t\t\t<web-xml>web.xml</web-xml>\n" +
			"\t\t\t<web-resources>\n" +
			"\t\t\t\t<resource name=\"de.espirit.test:TestModule\" version=\"0.0.1-SNAPSHOT\">lib/TestModule-0.0.1-SNAPSHOT.jar</resource>\n" +
			"\t\t\t</web-resources>\n" +
			"\t\t</web-app>\n" +
			"\t\t<web-app scopes=\"PROJECT,GLOBAL\">\n" +
			"\t\t\t<name>TestWebApp#2</name>\n" +
			"\t\t\t<displayname/>\n" +
			"\t\t\t<description/>\n" +
			"\t\t\t<class>de.espirit.test.TestWebApp</class>\n" +
			"\t\t\t<web-xml>web.xml</web-xml>\n" +
			"\t\t\t<web-resources>\n" +
			"\t\t\t\t<resource name=\"de.espirit.test:TestModule\" version=\"0.0.1-SNAPSHOT\">lib/TestModule-0.0.1-SNAPSHOT.jar</resource>\n" +
			"\t\t\t</web-resources>\n" +
			"\t\t</web-app>\n" +
			"\t</components>\n" +
			// resources
			"\t<resources>\n" +
			"\t\t<resource name=\"de.espirit.test:TestModule\" version=\"0.0.1-SNAPSHOT\" scope=\"module\" mode=\"isolated\">lib/TestModule-0.0.1-SNAPSHOT.jar</resource>\n" +
			"\t</resources>\n" +
			"</module>";
	public static final String PROJECT_NAME = "TestProject#1";
	public static final String PROJECT_NAME_2 = "TestProject#2";

	private Project _project;
	private Project _project2;
	private ModuleInstaller _moduleInstaller;
	private ModuleDescriptor _moduleDescriptor;

	@BeforeEach
	public void beforeEach() throws IOException {
		_project = mock(Project.class);
		doReturn(PROJECT_NAME).when(_project).getName();
		doReturn(true).when(_project).isActive();
		doReturn(1L).when(_project).getId();
		doReturn(UUID.randomUUID()).when(_project).getUuid();
		_project2 = mock(Project.class);
		doReturn(PROJECT_NAME_2).when(_project2).getName();
		doReturn(true).when(_project2).isActive();
		doReturn(2L).when(_project2).getId();
		doReturn(UUID.randomUUID()).when(_project2).getUuid();
		final Connection connection = mock(Connection.class);
		doReturn(true).when(connection).isConnected();
		doReturn(_project).when(connection).getProjectByName(PROJECT_NAME);
		doReturn(_project2).when(connection).getProjectByName(PROJECT_NAME_2);
		final ModuleAdminAgent moduleAdminAgent = mock(ModuleAdminAgent.class);
		_moduleInstaller = spy(new ModuleInstaller(connection));
		doReturn(moduleAdminAgent).when(_moduleInstaller).getModuleAdminAgent();
		_moduleDescriptor = ModuleDescriptor.create("myModule.fsm", new ByteArrayInputStream(MODULE_XML.getBytes(StandardCharsets.UTF_8)));
	}

	@Test
	public void doInstallModule() throws IOException {
		// setup module installation parameters
		final ModuleAdminAgent.ModuleResult moduleInstallResult = mock(ModuleAdminAgent.ModuleResult.class);
		when(moduleInstallResult.getDescriptor()).thenReturn(_moduleDescriptor);
		doReturn(moduleInstallResult).when(_moduleInstaller).installFsm(any(File.class));
		final ModuleInstallationParameters parameters = mock(ModuleInstallationParameters.class);
		doReturn(new File(_moduleDescriptor.getFileName())).when(parameters).getFsm();
		doAnswer(invocation -> spy(new ModuleInstallationResult())).when(_moduleInstaller).createModuleInstallationResult();

		// mock answers of configuration
		final WebAppId webedit = WebAppIdentifier.forGlobalWebApp("fs5webedit").createWebAppId(null);
		final WebAppId staging = WebAppIdentifier.forGlobalWebApp("fs5staging").createWebAppId(null);
		doAnswer(invocation -> List.of(new ServiceComponentResult(null, null))).when(_moduleInstaller).configureServices(any(), any());
		doAnswer(invocation -> List.of(new ProjectAppComponentResult(null, null, _project))).when(_moduleInstaller).installProjectAppComponents(any(), any());
		doAnswer(invocation -> List.of(new ProjectAppComponentResult(null, null, _project2))).when(_moduleInstaller).updateProjectAppComponents(any(), any(), any());
		doAnswer(invocation -> List.of(new WebAppComponentResult(null, null, webedit))).when(_moduleInstaller).installWebAppComponents(any(), any());
		doAnswer(invocation -> List.of(new WebAppComponentResult(null, null, staging))).when(_moduleInstaller).updateWebAppComponents(any(), any());

		// test
		final ModuleInstallationResult installationResult = _moduleInstaller.doInstallModule(parameters);

		// verify
		verify(installationResult, times(1)).setModuleResult(any(ModuleAdminAgent.ModuleResult.class));
		verify(installationResult, times(1)).setConfiguredServices(any());
		verify(installationResult, times(1)).setInstalledProjectAppComponentResults(any());
		verify(installationResult, times(1)).setUpdatedProjectAppComponentResults(any());
		verify(installationResult, times(1)).setInstalledWebAppComponentResults(any());
		verify(installationResult, times(1)).setUpdatedProjectAppComponentResults(any());
		final ModuleAdminAgent.ModuleResult moduleResult = installationResult.getModuleResult();
		assertThat(moduleResult.getDescriptor()).isEqualTo(moduleInstallResult.getDescriptor());
		assertThat(moduleResult.getUpdatedProjectApps()).hasSize(2);
		assertThat(moduleResult.getUpdatedProjectApps()).contains(_project, _project2);
		assertThat(moduleResult.getUpdatedWebApps()).hasSize(2);
		assertThat(moduleResult.getUpdatedWebApps()).contains(webedit, staging);
	}

	@Test
	public void configureServices() throws IOException {
		// setup module installation parameters
		final ModuleAdminAgent moduleAdminAgent = _moduleInstaller.getModuleAdminAgent();
		final ModuleAdminAgent.ModuleResult moduleInstallResult = mock(ModuleAdminAgent.ModuleResult.class);
		when(moduleInstallResult.getDescriptor()).thenReturn(_moduleDescriptor);
		doReturn(moduleInstallResult).when(_moduleInstaller).installFsm(any(File.class));
		final ModuleInstallationParameters parameters = mock(ModuleInstallationParameters.class);
		final RuntimeException customRuntimeException = new RuntimeException("Error during moduleAdminAgent.isRunning('TestService#1')");
		doThrow(customRuntimeException).when(moduleAdminAgent).isRunning("TestService#1");
		doReturn(true).when(moduleAdminAgent).isRunning("TestService#2");

		// test
		final List<ServiceComponentResult> configuredServiceResults = _moduleInstaller.configureServices(_moduleDescriptor, parameters);

		// verify
		assertThat(configuredServiceResults).hasSize(2);
		assertThat(configuredServiceResults.get(0).getThrowable()).describedAs("Configuration and restart service 'TestService#1' should fail").isSameAs(customRuntimeException);
		assertThat(configuredServiceResults.get(1).getThrowable()).describedAs("Configuration and restart service 'TestService#2' should be successful").isNull();
	}

	@Test
	public void installProjectAppComponents() throws IOException {
		// setup module installation parameters
		final ModuleAdminAgent moduleAdminAgent = _moduleInstaller.getModuleAdminAgent();
		final ModuleAdminAgent.ModuleResult moduleInstallResult = mock(ModuleAdminAgent.ModuleResult.class);
		when(moduleInstallResult.getDescriptor()).thenReturn(_moduleDescriptor);
		doReturn(moduleInstallResult).when(_moduleInstaller).installFsm(any(File.class));
		final ModuleInstallationParameters parameters = mock(ModuleInstallationParameters.class);
		doReturn(PROJECT_NAME).when(parameters).getProjectName();

		// mock answers for project app installation
		doAnswer(invocation -> Collections.emptyList()).when(moduleAdminAgent).getProjectAppUsages(anyString(), anyString());
		final RuntimeException customRuntimeException = new RuntimeException("custom runtime exception during install of TestProjectApp#1");
		doAnswer(invocation -> {
			throw customRuntimeException;
		}).when(moduleAdminAgent).installProjectApp("TestModule", "TestProjectApp#1", _project);
		doAnswer(invocation -> null).when(moduleAdminAgent).installProjectApp("TestModule", "TestProjectApp#2", _project);

		// test
		final List<ProjectAppComponentResult> installedProjectAppResults = _moduleInstaller.installProjectAppComponents(_moduleDescriptor, parameters);

		// verify
		assertThat(installedProjectAppResults).hasSize(2);
		assertThat(installedProjectAppResults.get(0).getThrowable()).describedAs("Installation of project app component 'TestProjectApp#1' in project 'TestProject#1' should fail").isSameAs(customRuntimeException);
		assertThat(installedProjectAppResults.get(1).getThrowable()).describedAs("Installation of project app component 'TestProjectApp#2' in project 'TestProject#1' should be successful").isNull();
	}

	@Test
	public void updateProjectAppComponents() throws IOException {
		// setup module installation parameters
		final ModuleAdminAgent moduleAdminAgent = _moduleInstaller.getModuleAdminAgent();
		final ModuleAdminAgent.ModuleResult moduleInstallResult = mock(ModuleAdminAgent.ModuleResult.class);
		when(moduleInstallResult.getDescriptor()).thenReturn(_moduleDescriptor);
		doReturn(moduleInstallResult).when(_moduleInstaller).installFsm(any(File.class));

		// mock answers for project app installation
		doAnswer(invocation -> Collections.singletonList(_project)).when(moduleAdminAgent).getProjectAppUsages("TestModule", "TestProjectApp#1");
		doAnswer(invocation -> Collections.singletonList(_project)).when(moduleAdminAgent).getProjectAppUsages("TestModule", "TestProjectApp#2");
		final RuntimeException customRuntimeException = new RuntimeException("custom runtime exception during install of TestProjectApp#1");
		doAnswer(invocation -> {
			throw customRuntimeException;
		}).when(moduleAdminAgent).installProjectApp("TestModule", "TestProjectApp#1", _project);
		doAnswer(invocation -> null).when(moduleAdminAgent).installProjectApp("TestModule", "TestProjectApp#2", _project);

		// test
		final List<ProjectAppComponentResult> updatedProjectAppResults = _moduleInstaller.updateProjectAppComponents(_moduleDescriptor, Collections.emptyList(), null);

		// verify
		assertThat(updatedProjectAppResults).hasSize(2);
		assertThat(updatedProjectAppResults.get(0).getThrowable()).describedAs("Update of project app component 'TestProjectApp#1' in project 'TestProject#1' should fail").isSameAs(customRuntimeException);
		assertThat(updatedProjectAppResults.get(1).getThrowable()).describedAs("Update of project app component 'TestProjectApp#2' in project 'TestProject#1' should be successful").isNull();
	}

	@Test
	public void updateProjectAppComponents_withProjectName() throws IOException {
		// setup module installation parameters
		final ModuleAdminAgent moduleAdminAgent = _moduleInstaller.getModuleAdminAgent();
		final ModuleAdminAgent.ModuleResult moduleInstallResult = mock(ModuleAdminAgent.ModuleResult.class);
		when(moduleInstallResult.getDescriptor()).thenReturn(_moduleDescriptor);
		doReturn(moduleInstallResult).when(_moduleInstaller).installFsm(any(File.class));

		// mock answers for project app installation
		doAnswer(invocation -> Collections.singletonList(_project)).when(moduleAdminAgent).getProjectAppUsages("TestModule", "TestProjectApp#1");
		doAnswer(invocation -> Collections.singletonList(_project)).when(moduleAdminAgent).getProjectAppUsages("TestModule", "TestProjectApp#2");
		final RuntimeException customRuntimeException = new RuntimeException("custom runtime exception during install of TestProjectApp#1");
		doAnswer(invocation -> {
			throw customRuntimeException;
		}).when(moduleAdminAgent).installProjectApp("TestModule", "TestProjectApp#1", _project);
		doAnswer(invocation -> null).when(moduleAdminAgent).installProjectApp("TestModule", "TestProjectApp#2", _project);

		// test
		final List<ProjectAppComponentResult> updatedProjectAppResults = _moduleInstaller.updateProjectAppComponents(_moduleDescriptor, Collections.emptyList(), "AnotherProjectName");

		// verify
		assertThat(updatedProjectAppResults).hasSize(0);
	}

	@Test
	public void installAndUpdateProjectAppComponents() throws IOException {
		// setup module installation parameters
		final ModuleAdminAgent moduleAdminAgent = _moduleInstaller.getModuleAdminAgent();
		final ModuleAdminAgent.ModuleResult moduleInstallResult = mock(ModuleAdminAgent.ModuleResult.class);
		when(moduleInstallResult.getDescriptor()).thenReturn(_moduleDescriptor);
		doReturn(moduleInstallResult).when(_moduleInstaller).installFsm(any(File.class));
		final ModuleInstallationParameters parameters = mock(ModuleInstallationParameters.class);
		doReturn(PROJECT_NAME).when(parameters).getProjectName();

		// mock answers for project app installation
		doAnswer(invocation -> Arrays.asList(_project, _project2)).when(moduleAdminAgent).getProjectAppUsages("TestModule", "TestProjectApp#1");
		doAnswer(invocation -> Collections.singletonList(_project)).when(moduleAdminAgent).getProjectAppUsages("TestModule", "TestProjectApp#2");
		final RuntimeException customRuntimeException = new RuntimeException("custom runtime exception during install of TestProjectApp#1");
		doAnswer(invocation -> {
			throw customRuntimeException;
		}).when(moduleAdminAgent).installProjectApp("TestModule", "TestProjectApp#1", _project);
		doAnswer(invocation -> {
			throw customRuntimeException;
		}).when(moduleAdminAgent).installProjectApp("TestModule", "TestProjectApp#1", _project2);
		doAnswer(invocation -> null).when(moduleAdminAgent).installProjectApp("TestModule", "TestProjectApp#2", _project);
		doAnswer(invocation -> null).when(moduleAdminAgent).installProjectApp("TestModule", "TestProjectApp#2", _project2);

		// test & verify
		final List<ProjectAppComponentResult> installedProjectAppResults = _moduleInstaller.installProjectAppComponents(_moduleDescriptor, parameters);
		assertThat(installedProjectAppResults).hasSize(2);
		assertThat(installedProjectAppResults.get(0).getThrowable()).describedAs("Installation of project app component 'TestProjectApp#1' in project 'TestProject#1' should fail").isSameAs(customRuntimeException);
		assertThat(installedProjectAppResults.get(1).getThrowable()).describedAs("Installation of project app component 'TestProjectApp#2' in project 'TestProject#1' should be successful").isNull();

		final List<ProjectAppComponentResult> updatedProjectAppResults = _moduleInstaller.updateProjectAppComponents(_moduleDescriptor, installedProjectAppResults, null);
		assertThat(updatedProjectAppResults).hasSize(1);
		assertThat(updatedProjectAppResults.get(0).getThrowable()).describedAs("Installation of project app component 'TestProjectApp#1' in project 'TestProject#2' should fail").isSameAs(customRuntimeException);
	}

	@Test
	public void installWebAppComponents_globalWebApps() throws IOException {
		// setup module installation parameters
		final ModuleAdminAgent moduleAdminAgent = _moduleInstaller.getModuleAdminAgent();
		final ModuleAdminAgent.ModuleResult moduleInstallResult = mock(ModuleAdminAgent.ModuleResult.class);
		when(moduleInstallResult.getDescriptor()).thenReturn(_moduleDescriptor);
		doReturn(moduleInstallResult).when(_moduleInstaller).installFsm(any(File.class));
		final ModuleInstallationParameters parameters = mock(ModuleInstallationParameters.class);

		// mock answers for web app installation
		final WebAppIdentifier WEBEDIT = WebAppIdentifier.forGlobalWebApp("fs5webedit");
		final WebAppIdentifier STAGING = WebAppIdentifier.forGlobalWebApp("fs5staging");
		doAnswer(invocation -> {
			final ArrayList<WebAppIdentifier> webApps = new ArrayList<>();
			webApps.add(WEBEDIT);
			webApps.add(STAGING);
			return webApps;
		}).when(parameters).getWebAppScopes();
		doAnswer(invocation -> null).when(moduleAdminAgent).installWebApp("TestModule", "TestWebApp#1", WEBEDIT.createWebAppId(null), false);
		doAnswer(invocation -> null).when(moduleAdminAgent).installWebApp("TestModule", "TestWebApp#1", STAGING.createWebAppId(null), false);
		final RuntimeException customRuntimeException = new RuntimeException("custom runtime exception during install of TestWebApp#2");
		doAnswer(invocation -> {
			throw customRuntimeException;
		}).when(moduleAdminAgent).installWebApp("TestModule", "TestWebApp#2", WEBEDIT.createWebAppId(null), false);
		doAnswer(invocation -> {
			throw customRuntimeException;
		}).when(moduleAdminAgent).installWebApp("TestModule", "TestWebApp#2", STAGING.createWebAppId(null), false);

		// test
		final List<WebAppComponentResult> installedWebAppResults = _moduleInstaller.installWebAppComponents(_moduleDescriptor, parameters);

		// verify
		assertThat(installedWebAppResults).hasSize(4);
		assertThat(installedWebAppResults.get(0).getThrowable()).describedAs("Installation of global web app component 'TestWebApp#1' in global web app 'fs5webedit' should be successful").isNull();
		assertThat(installedWebAppResults.get(1).getThrowable()).describedAs("Installation of global web app component 'TestWebApp#1' in global web app 'fs5staging' should be successful").isNull();
		assertThat(installedWebAppResults.get(2).getThrowable()).describedAs("Installation of global web app component 'TestWebApp#2' in global web app 'fs5webedit' should fail").isSameAs(customRuntimeException);
		assertThat(installedWebAppResults.get(3).getThrowable()).describedAs("Installation of global web app component 'TestWebApp#2' in global web app 'fs5staging' should fail").isSameAs(customRuntimeException);
	}

	@Test
	public void updateWebAppComponents_globalWebApps() throws IOException {
		// setup module installation parameters
		final ModuleAdminAgent moduleAdminAgent = _moduleInstaller.getModuleAdminAgent();
		final ModuleAdminAgent.ModuleResult moduleInstallResult = mock(ModuleAdminAgent.ModuleResult.class);
		when(moduleInstallResult.getDescriptor()).thenReturn(_moduleDescriptor);
		doReturn(moduleInstallResult).when(_moduleInstaller).installFsm(any(File.class));

		// mock answers for web app installation
		final WebAppIdentifier WEBEDIT = WebAppIdentifier.forGlobalWebApp("fs5webedit");
		final WebAppIdentifier STAGING = WebAppIdentifier.forGlobalWebApp("fs5staging");
		doAnswer(invocation -> Arrays.asList(WEBEDIT.createWebAppId(null), STAGING.createWebAppId(null))).when(moduleAdminAgent).getWebAppUsages(anyString(), anyString());
		doAnswer(invocation -> null).when(moduleAdminAgent).installWebApp("TestModule", "TestWebApp#1", WEBEDIT.createWebAppId(null), false);
		doAnswer(invocation -> null).when(moduleAdminAgent).installWebApp("TestModule", "TestWebApp#1", STAGING.createWebAppId(null), false);
		final RuntimeException customRuntimeException = new RuntimeException("custom runtime exception during install of TestWebApp#2");
		doAnswer(invocation -> {
			throw customRuntimeException;
		}).when(moduleAdminAgent).installWebApp("TestModule", "TestWebApp#2", WEBEDIT.createWebAppId(null), false);
		doAnswer(invocation -> {
			throw customRuntimeException;
		}).when(moduleAdminAgent).installWebApp("TestModule", "TestWebApp#2", STAGING.createWebAppId(null), false);

		// test
		final List<WebAppComponentResult> updatedWebAppResults = _moduleInstaller.updateWebAppComponents(_moduleDescriptor, Collections.emptyList());

		// verify
		assertThat(updatedWebAppResults).hasSize(4);
		assertThat(updatedWebAppResults.get(0).getThrowable()).describedAs("Update of global web app component 'TestWebApp#1' in global web app 'fs5webedit' should be successful").isNull();
		assertThat(updatedWebAppResults.get(1).getThrowable()).describedAs("Update of global web app component 'TestWebApp#1' in global web app 'fs5staging' should be successful").isNull();
		assertThat(updatedWebAppResults.get(2).getThrowable()).describedAs("Update of global web app component 'TestWebApp#2' in global web app 'fs5webedit' should fail").isSameAs(customRuntimeException);
		assertThat(updatedWebAppResults.get(3).getThrowable()).describedAs("Update of global web app component 'TestWebApp#2' in global web app 'fs5staging' should fail").isSameAs(customRuntimeException);
	}

	@Test
	public void installAndUpdateWebAppComponents_globalWebApps() throws IOException {
		// setup module installation parameters
		final ModuleAdminAgent moduleAdminAgent = _moduleInstaller.getModuleAdminAgent();
		final ModuleAdminAgent.ModuleResult moduleInstallResult = mock(ModuleAdminAgent.ModuleResult.class);
		when(moduleInstallResult.getDescriptor()).thenReturn(_moduleDescriptor);
		doReturn(moduleInstallResult).when(_moduleInstaller).installFsm(any(File.class));
		final ModuleInstallationParameters parameters = mock(ModuleInstallationParameters.class);

		// mock answers for web app installation
		final WebAppIdentifier WEBEDIT = WebAppIdentifier.forGlobalWebApp("fs5webedit");
		final WebAppIdentifier STAGING = WebAppIdentifier.forGlobalWebApp("fs5staging");
		doAnswer(invocation -> {
			final ArrayList<WebAppIdentifier> webApps = new ArrayList<>();
			webApps.add(WEBEDIT);
			return webApps;
		}).when(parameters).getWebAppScopes();
		doAnswer(invocation -> List.of(STAGING.createWebAppId(null))).when(moduleAdminAgent).getWebAppUsages("TestModule", "TestWebApp#2");
		doAnswer(invocation -> null).when(moduleAdminAgent).installWebApp("TestModule", "TestWebApp#1", WEBEDIT.createWebAppId(null), false);
		doAnswer(invocation -> null).when(moduleAdminAgent).installWebApp("TestModule", "TestWebApp#1", STAGING.createWebAppId(null), false);
		doAnswer(invocation -> null).when(moduleAdminAgent).installWebApp("TestModule", "TestWebApp#2", WEBEDIT.createWebAppId(null), false);
		doAnswer(invocation -> null).when(moduleAdminAgent).installWebApp("TestModule", "TestWebApp#2", STAGING.createWebAppId(null), false);

		// test & verify
		final List<WebAppComponentResult> installedWebAppResults = _moduleInstaller.installWebAppComponents(_moduleDescriptor, parameters);
		assertThat(installedWebAppResults).hasSize(2);
		assertThat(installedWebAppResults.get(0).getThrowable()).describedAs("Installation of global web app component 'TestWebApp#1' in global web app 'fs5webedit' should be successful").isNull();
		assertThat(installedWebAppResults.get(1).getThrowable()).describedAs("Installation of global web app component 'TestWebApp#1' in global web app 'fs5staging' should be successful").isNull();

		final List<WebAppComponentResult> updatedWebAppResults = _moduleInstaller.updateWebAppComponents(_moduleDescriptor, installedWebAppResults);
		assertThat(updatedWebAppResults).hasSize(1);
		assertThat(updatedWebAppResults.get(0).getThrowable()).describedAs("Update of global web app component 'TestWebApp#2' in global web app 'fs5staging' should be successful").isNull();
	}

	@Test
	public void installWebAppComponents_projectWebApps() throws IOException {
		// setup module installation parameters
		final ModuleAdminAgent moduleAdminAgent = _moduleInstaller.getModuleAdminAgent();
		final ModuleAdminAgent.ModuleResult moduleInstallResult = mock(ModuleAdminAgent.ModuleResult.class);
		when(moduleInstallResult.getDescriptor()).thenReturn(_moduleDescriptor);
		doReturn(moduleInstallResult).when(_moduleInstaller).installFsm(any(File.class));
		final ModuleInstallationParameters parameters = mock(ModuleInstallationParameters.class);
		doReturn(PROJECT_NAME).when(parameters).getProjectName();

		// mock answers for web app installation
		final WebAppIdentifier WEBEDIT = WebAppIdentifier.WEBEDIT;
		final WebAppIdentifier STAGING = WebAppIdentifier.STAGING;
		doAnswer(invocation -> {
			final ArrayList<WebAppIdentifier> webApps = new ArrayList<>();
			webApps.add(WEBEDIT);
			webApps.add(STAGING);
			return webApps;
		}).when(parameters).getWebAppScopes();
		doAnswer(invocation -> null).when(moduleAdminAgent).installWebApp("TestModule", "TestWebApp#1", WEBEDIT.createWebAppId(_project), false);
		final RuntimeException customRuntimeException = new RuntimeException("custom runtime exception during install of TestWebApp");
		doAnswer(invocation -> {
			throw customRuntimeException;
		}).when(moduleAdminAgent).installWebApp("TestModule", "TestWebApp#1", STAGING.createWebAppId(_project), false);
		doAnswer(invocation -> {
			throw customRuntimeException;
		}).when(moduleAdminAgent).installWebApp("TestModule", "TestWebApp#2", WEBEDIT.createWebAppId(_project), false);
		doAnswer(invocation -> null).when(moduleAdminAgent).installWebApp("TestModule", "TestWebApp#2", STAGING.createWebAppId(_project), false);

		// test
		final List<WebAppComponentResult> installedWebAppResults = _moduleInstaller.installWebAppComponents(_moduleDescriptor, parameters);

		// verify
		assertThat(installedWebAppResults).hasSize(4);
		assertThat(installedWebAppResults.get(0).getThrowable()).describedAs("Installation of web app component 'TestWebApp#1' in 'webedit(TestProject#1)' should be successful").isNull();
		assertThat(installedWebAppResults.get(1).getThrowable()).describedAs("Installation of web app component 'TestWebApp#1' in 'staging(TestProject#1)' should fail").isSameAs(customRuntimeException);
		assertThat(installedWebAppResults.get(2).getThrowable()).describedAs("Installation of web app component 'TestWebApp#2' in 'webedit(TestProject#1)' should fail").isSameAs(customRuntimeException);
		assertThat(installedWebAppResults.get(3).getThrowable()).describedAs("Installation of web app component 'TestWebApp#2' in 'staging(TestProject#1)' should be successful").isNull();
	}

}