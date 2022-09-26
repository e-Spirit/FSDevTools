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

package com.espirit.moddev.cli.commands.project.deleteCommand;

import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.admin.ProjectStorage;
import de.espirit.firstspirit.access.project.Project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProjectDeleterTest {

	@Mock
	private Project mockProject;
	@Mock
	private Connection mockConnection;
	private ProjectDeleter testling;

	@BeforeEach
	public void setUp() {
		testling = new ProjectDeleter();
		mockProject = mock(Project.class);
		Connection mockConnection = mock(Connection.class);
		when(mockConnection.getProjectByName("test")).thenReturn(mockProject);
		when(mockConnection.isConnected()).thenReturn(true);
		when(mockConnection.getProjectByName(anyString())).thenReturn(null);
		Project[] projects = new Project[1];
		projects[0] = mockProject;
		when(mockConnection.getProjects()).thenReturn(projects);
		this.mockConnection = mockConnection;
	}

	/**
	 * Test that the default constructor has no dependencies or exceptions.
	 */
	@Test
	public void testDefaultConstructor() {
		assertThat("Expect not null.", testling, is(notNullValue()));
	}

	/**
	 * Test that the default constructor has no dependencies or exceptions.
	 */
	@Test
	public void testReturnProjectStorage() {
		ProjectStorage projectStorage = testling.returnProjectStorage(null, null);
		assertNull(projectStorage);

		projectStorage = testling.returnProjectStorage(mockConnection, null);
		assertNull(projectStorage);

		projectStorage = testling.returnProjectStorage(null, mockProject);
		assertNull(projectStorage);
	}

	@Test
	public void testDeleteProject() {
		ProjectDeleter testling = new ProjectDeleter() {
			@Override
			ProjectStorage returnProjectStorage(Connection connection, Project project) {
				return mock(ProjectStorage.class);
			}
		};

		when(mockConnection.getProjectByName("test")).thenReturn(mockProject);
		boolean result = testling.deleteProject(mockConnection, "test");
		assertTrue(result);
	}
}
