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

package com.espirit.moddev.cli.commands.project.activateWebServerCommand;

import com.espirit.moddev.shared.webapp.WebAppIdentifier;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.project.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProjectWebServerActivatorTest {
	private ProjectWebServerActivator testling;

	@BeforeEach
	public void setUp() {
		testling = new ProjectWebServerActivator();
	}

	@Test
	public void activateWebServer() {
		List<WebAppIdentifier> scopes = new ArrayList<>();
		scopes.add(WebAppIdentifier.WEBEDIT);
		ProjectWebServerActivationParameterBuilder builder = new ProjectWebServerActivationParameterBuilder();
		builder.withServerName("serverName")
				.atProjectName("dummyProjectName")
				.forScopes(scopes)
				.withForceActivation(true);
		Connection connectionMock = mock(Connection.class);
		when(connectionMock.isConnected()).thenReturn(false);

		testling.activateWebServer(connectionMock, builder.build());
	}

	@Test
	public void testPreconditionsWithUnconnectedConnection() {
		ProjectWebServerActivationParameter parameter = getValidParameters().build();
		Connection mockedConnection;
		mockedConnection = mock(Connection.class);
		when(mockedConnection.isConnected()).thenReturn(false);
		assertFalse(testling.arePreconditionsFulfilled(mockedConnection, parameter));
	}

	@Test
	public void testPreconditionsWithInvalidProject() {
		ProjectWebServerActivationParameter parameter = getValidParameters().build();
		final Connection mockedConnection = mock(Connection.class);
		when(mockedConnection.isConnected()).thenReturn(true);
		when(mockedConnection.getProjects()).thenReturn(new Project[1]);
		when(mockedConnection.getProjectByName(ArgumentMatchers.anyString())).thenReturn(null);

		assertFalse(testling.arePreconditionsFulfilled(mockedConnection, parameter));
	}

	@Test
	public void testPreconditionsWithNullScope() {
		List<WebAppIdentifier> invalidScopes = new ArrayList<>();
		invalidScopes.add(null);
		ProjectWebServerActivationParameter parameter = getValidParameters().forScopes(invalidScopes).build();
		final Project mockedProject = mock(Project.class);
		when(mockedProject.getName()).thenReturn("validProjectName");

		final Connection mockedConnection = mock(Connection.class);
		when(mockedConnection.isConnected()).thenReturn(true);
		when(mockedConnection.getProjects()).thenReturn(new Project[1]);
		when(mockedConnection.getProjectByName(ArgumentMatchers.anyString())).thenReturn(mockedProject);

		assertFalse(testling.arePreconditionsFulfilled(mockedConnection, parameter));
	}

	private ProjectWebServerActivationParameterBuilder getValidParameters() {
		final ProjectWebServerActivationParameterBuilder parameterBuilder = ProjectWebServerActivationParameter.builder();
		List<WebAppIdentifier> validScopes = new ArrayList<>();
		validScopes.add(WebAppIdentifier.LIVE);
		validScopes.add(WebAppIdentifier.FS5_ROOT);
		parameterBuilder.atProjectName("validProjectName").forScopes(validScopes).withServerName("ValidServerName").withForceActivation(true);
		return parameterBuilder;
	}

	@Test
	public void testPreconditionsWithValidParameters() {
		ProjectWebServerActivationParameter parameter = getValidParameters().build();
		final Project mockedProject = mock(Project.class);
		when(mockedProject.getName()).thenReturn("validProjectName");

		final Connection mockedConnection = mock(Connection.class);
		when(mockedConnection.isConnected()).thenReturn(true);
		when(mockedConnection.getProjects()).thenReturn(new Project[1]);
		when(mockedConnection.getProjectByName(ArgumentMatchers.anyString())).thenReturn(mockedProject);

		assertTrue(testling.arePreconditionsFulfilled(mockedConnection, parameter));
	}
}
