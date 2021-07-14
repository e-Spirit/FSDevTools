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

package com.espirit.moddev.cli.commands.module.utils;

import com.espirit.moddev.cli.api.result.ExecutionErrorResult;
import com.espirit.moddev.cli.api.result.ExecutionResults;
import com.espirit.moddev.shared.webapp.WebAppIdentifier;
import com.google.common.collect.Lists;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.agency.GlobalWebAppId;
import de.espirit.firstspirit.agency.ModuleAdminAgent;
import de.espirit.firstspirit.agency.ProjectWebAppId;
import de.espirit.firstspirit.agency.SpecialistsBroker;
import de.espirit.firstspirit.agency.WebAppId;
import de.espirit.firstspirit.module.WebEnvironment;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static de.espirit.firstspirit.access.ConnectionManager.HTTP_MODE;
import static de.espirit.firstspirit.access.ConnectionManager.SOCKET_MODE;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WebAppUtilTest {

	private Connection _connection;
	private SpecialistsBroker _broker;
	private ModuleAdminAgent _moduleAdminAgent;

	@Before
	public void setup() {
		_connection = mock(Connection.class);
		_broker = mock(SpecialistsBroker.class);
		_moduleAdminAgent = mock(ModuleAdminAgent.class);
		when(_connection.getBroker()).thenReturn(_broker);
		when(_broker.requireSpecialist(ModuleAdminAgent.TYPE)).thenReturn(_moduleAdminAgent);
	}

	@Test
	public void isRootWebAppAndNotInSocketMode_socketMode_rootWebApp() {
		when(_connection.getMode()).thenReturn(SOCKET_MODE);
		assertThat(WebAppUtil.isRootWebAppAndNotInSocketMode(_connection, WebAppIdentifier.FS5_ROOT.createWebAppId(null))).isFalse();
	}

	@Test
	public void isRootWebAppAndNotInSocketMode_httpMode_rootWebApp() {
		when(_connection.getMode()).thenReturn(HTTP_MODE);
		assertThat(WebAppUtil.isRootWebAppAndNotInSocketMode(_connection, WebAppIdentifier.FS5_ROOT.createWebAppId(null))).isTrue();
	}

	@Test
	public void isRootWebAppAndNotInSocketMode_socketMode_nonRootWebApp() {
		when(_connection.getMode()).thenReturn(SOCKET_MODE);
		assertThat(WebAppUtil.isRootWebAppAndNotInSocketMode(_connection, WebAppIdentifier.forGlobalWebApp("global-web-app").createWebAppId(null))).isFalse();
	}

	@Test
	public void isRootWebAppAndNotInSocketMode_httpMode_nonRootWebApp() {
		when(_connection.getMode()).thenReturn(HTTP_MODE);
		assertThat(WebAppUtil.isRootWebAppAndNotInSocketMode(_connection, WebAppIdentifier.forGlobalWebApp("global-web-app").createWebAppId(null))).isFalse();
	}

	@Test
	public void deployWebApps_emptyList() {
		assertThat(WebAppUtil.deployWebApps(_connection, Collections.emptyList()).isEmpty()).isTrue();
	}

	@Test
	public void filterWebApps_activeProject_with_webserver() {
		// setup
		final Project project = mock(Project.class);
		when(project.isActive()).thenReturn(true);
		when(project.getActiveWebServer(anyString())).thenReturn("testWebServer");
		final ProjectWebAppId projectWebApp = mock(ProjectWebAppId.class);
		when(projectWebApp.getWebScope()).thenReturn(WebEnvironment.WebScope.WEBEDIT);
		when(projectWebApp.getProject()).thenReturn(project);
		final GlobalWebAppId globalWebApp = mock(GlobalWebAppId.class);
		// test
		final Collection<WebAppId> webAppIds = WebAppUtil.filterWebApps(Lists.newArrayList(projectWebApp, globalWebApp));
		// verify
		assertThat(webAppIds).hasSize(2);
		assertThat(webAppIds).containsExactly(projectWebApp, globalWebApp);
	}

	@Test
	public void filterWebApps_inactiveProject() {
		// setup
		final Project project = mock(Project.class);
		when(project.isActive()).thenReturn(false);
		final ProjectWebAppId projectWebApp = mock(ProjectWebAppId.class);
		when(projectWebApp.getProject()).thenReturn(project);
		final GlobalWebAppId globalWebApp = mock(GlobalWebAppId.class);
		// test
		final Collection<WebAppId> webAppIds = WebAppUtil.filterWebApps(Lists.newArrayList(projectWebApp, globalWebApp));
		// verify
		assertThat(webAppIds).hasSize(1);
		assertThat(webAppIds).containsExactly(globalWebApp);
	}

	@Test
	public void filterWebApps_noActiveWebServer() {
		// setup
		final Project project = mock(Project.class);
		when(project.isActive()).thenReturn(true);
		when(project.getActiveWebServer(anyString())).thenReturn(null);
		final ProjectWebAppId projectWebApp = mock(ProjectWebAppId.class);
		when(projectWebApp.getWebScope()).thenReturn(WebEnvironment.WebScope.WEBEDIT);
		when(projectWebApp.getProject()).thenReturn(project);
		final GlobalWebAppId globalWebApp = mock(GlobalWebAppId.class);
		// test
		final Collection<WebAppId> webAppIds = WebAppUtil.filterWebApps(Lists.newArrayList(projectWebApp, globalWebApp));
		// verify
		assertThat(webAppIds).hasSize(1);
		assertThat(webAppIds).containsExactly(globalWebApp);
	}

	@Test
	public void deployWebApps_nonSocketRootWebApp_throwNoException() {
		// setup
		when(_connection.getMode()).thenReturn(HTTP_MODE);
		final WebAppId webAppId = WebAppIdentifier.FS5_ROOT.createWebAppId(null);
		// test
		final ExecutionResults results = WebAppUtil.deployWebApps(_connection, Lists.newArrayList(webAppId));
		assertThat(results.size()).isEqualTo(1);
		assertThat(results.get(0)).isInstanceOf(WebAppUtil.RootWebAppDeployNotAllowedResult.class);
		assertThat(((ExecutionErrorResult<?>) results.get(0)).getException().getMessage()).isEqualTo(WebAppUtil.SOCKET_FS_5_ROOT_ERROR_MESSAGE);
	}

	@Test
	public void deployWebApps() {
		// setup
		when(_connection.getMode()).thenReturn(HTTP_MODE);
		final WebAppId rootWebApp = WebAppIdentifier.FS5_ROOT.createWebAppId(null);
		final WebAppId webApp1 = WebAppIdentifier.forGlobalWebApp("global-web-app#1").createWebAppId(null);
		final WebAppId webApp2 = WebAppIdentifier.forGlobalWebApp("global-web-app#2").createWebAppId(null);
		when(_moduleAdminAgent.deployWebApp(webApp1)).thenReturn(true);
		when(_moduleAdminAgent.deployWebApp(webApp2)).thenReturn(false);
		// test
		final ExecutionResults results = WebAppUtil.deployWebApps(_connection, Lists.newArrayList(rootWebApp, webApp1, webApp2));
		assertThat(results.size()).isEqualTo(3);
		assertThat(results.get(0)).isInstanceOf(WebAppUtil.RootWebAppDeployNotAllowedResult.class);
		assertThat(results.get(1)).isInstanceOf(WebAppUtil.WebAppDeployedResult.class);
		assertThat(results.get(1).toString()).isEqualTo(String.format(WebAppUtil.WebAppDeployedResult.MESSAGE, WebAppIdentifier.getName(webApp1)));
		assertThat(results.get(2)).isInstanceOf(WebAppUtil.WebAppDeployFailedResult.class);
		assertThat(((ExecutionErrorResult<?>) results.get(2)).getException().getMessage()).isEqualTo(String.format(WebAppUtil.WebAppDeployFailedResult.MESSAGE, WebAppIdentifier.getName(webApp2)));
	}

}
