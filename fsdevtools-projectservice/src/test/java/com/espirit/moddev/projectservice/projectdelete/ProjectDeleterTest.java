/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2017 e-Spirit AG
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

package com.espirit.moddev.projectservice.projectdelete;

import de.espirit.firstspirit.access.AdminService;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.ServicesBroker;
import de.espirit.firstspirit.access.admin.ProjectStorage;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.io.ServerConnection;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProjectDeleterTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Mock
    private Project mockProject;
    @Mock
    private Connection mockConnection;
    private ProjectDeleter testling;
    @Before
    public void setUp() {
        testling = new ProjectDeleter();
        mockProject = mock(Project.class);
        Connection mockConnection = mock(ServerConnection.class);
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
        assertNull("Expect the projectStorage to be null.", projectStorage);

        projectStorage = testling.returnProjectStorage(mockConnection, null);
        assertNull("Expect the projectStorage to be null.", projectStorage);

        projectStorage = testling.returnProjectStorage(null, mockProject);
        assertNull("Expect the projectStorage to be null.", projectStorage);
    }

    @Test
    public void testDeleteProject() {
        ProjectDeleter testling = new ProjectDeleter() {
            @Override
            ProjectStorage returnProjectStorage (Connection connection,  Project project) {
                return mock(ProjectStorage.class);
            }
        };

        when(mockConnection.getProjectByName("test")).thenReturn(mockProject);
        boolean result = testling.deleteProject(mockConnection, "test");
        assertTrue("result of deletion should be true", result);
    }
}