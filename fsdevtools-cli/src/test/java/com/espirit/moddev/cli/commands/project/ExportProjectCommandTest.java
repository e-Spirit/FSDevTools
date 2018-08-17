/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2016 e-Spirit AG
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

package com.espirit.moddev.cli.commands.project;

import com.espirit.moddev.cli.results.SimpleResult;
import com.espirit.moddev.projectservice.projectexport.ProjectExportParameters;
import com.espirit.moddev.projectservice.projectexport.ProjectExportParametersBuilder;
import com.espirit.moddev.projectservice.projectexport.ProjectExporter;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.io.ServerConnection;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ExportProjectCommandTest.
 */
public class ExportProjectCommandTest {
    private ExportProjectCommand testling;

    @Before
    public void setUp() throws Exception {
        testling = new ExportProjectCommand();
    }

    /**
     * Test that the default constructor has no dependencies or exceptions.
     */
    @Test
    public void testDefaultConstructor() {
        assertThat("Expect not null", testling, is(notNullValue()));
    }

    /**
     * Test if call() throws exception when createConnection() fails.
     */
    @Test
    public void testCallHandlesExceptionAndReturnsSimpleResultWithError() {
        // Arrange
        final ExportProjectCommand spyTestling = spy(testling);
        doThrow(Exception.class).when(spyTestling).createConnection();

        // Act
        final SimpleResult<Boolean> simpleResult = spyTestling.call();

        // Assert
        assertThat("Expected instance of Exception", simpleResult.getError(), instanceOf(Exception.class));
    }

    /**
     * Test if call() requires an instance of ServerConnection.
     */
    @Test
    public void testCallNoServerConnectionReturnsSimpleResultWithError() {
        // Arrange
        final Connection mockConnection = mock(Connection.class);
        final ExportProjectCommand spyTestling = spy(testling);
        doReturn(mockConnection).when(spyTestling).createConnection();

        // Act
        final SimpleResult<Boolean> simpleResult = spyTestling.call();

        // Assert
        assertThat("Expected instance of IllegalStateException.", simpleResult.getError(), instanceOf(IllegalStateException.class));
        assertThat("Expected equal.", simpleResult.getError().getMessage(), equalTo("Connection is not a ServerConnection implementation."));
    }

    /**
     * Test if successful project export returns a SimpleResult containing true.
     */
    @Test
    public void testCallExportProjectReturnsTrue() {
        // Arrange
        final ServerConnection mockServerConnection = mock(ServerConnection.class);
        when(mockServerConnection.isConnected()).thenReturn(true);

        final ProjectExportParameters mockProjectExportParameters = mock(ProjectExportParameters.class);

        final ExportProjectCommand spyTestling = spy(testling);
        doReturn(mockServerConnection).when(spyTestling).createConnection();
        doReturn(mockProjectExportParameters).when(spyTestling).getProjectExportParameters(any(ProjectExportParametersBuilder.class));
        doReturn(true).when(spyTestling).callExportProject(any(ProjectExporter.class), any(ServerConnection.class), any(ProjectExportParameters.class));

        // Act
        final SimpleResult<Boolean> simpleResult = spyTestling.call();

        // Assert
        assertThat("Expected equal.", simpleResult.get(), equalTo(Boolean.TRUE));
    }

    /**
     * Test if failed export returns a SimpleResult containing an IllegalStateException.
     */
    @Test
    public void testCallExportProjectReturnsSimpleResultWithError() {
        // Arrange
        final ServerConnection mockServerConnection = mock(ServerConnection.class);
        when(mockServerConnection.isConnected()).thenReturn(true);

        final ProjectExportParameters mockProjectExportParameters = mock(ProjectExportParameters.class);

        final ExportProjectCommand spyTestling = spy(testling);
        doReturn(mockServerConnection).when(spyTestling).createConnection();
        doReturn(mockProjectExportParameters).when(spyTestling).getProjectExportParameters(any(ProjectExportParametersBuilder.class));
        doReturn(false).when(spyTestling).callExportProject(any(ProjectExporter.class), any(ServerConnection.class), any(ProjectExportParameters.class));

        // Act
        final SimpleResult simpleResult = spyTestling.call();

        // Assert
        assertThat("Expected instance of IllegalStateException.", simpleResult.get(), instanceOf(IllegalStateException.class));
        assertThat("Expected equal.", ((Exception)simpleResult.get()).getMessage(), equalTo("Export was not successful"));
    }

    /**
     * Test if createConnection returns an instance of Connection.class
     */
    @Test
    public void testCreateConnectionReturnsInstanceOfConnection() {
        // Arrange

        // Act
        final Connection connection = testling.createConnection();

        // Assert
        assertThat("Expect instance of Connection.class", connection, instanceOf(Connection.class));
    }

    /**
     * Test if getProjectExportParameters builds ProjectExportParameters with given builder
     */
    @Test
    public void testGetProjectExportParametersBuildsProjectExportParameters() {
        // Arrange
        final ProjectExportParametersBuilder mockProjectExportParametersBuilder = mock(ProjectExportParametersBuilder.class);
        final ProjectExportParameters mockProjectExportParameters = mock(ProjectExportParameters.class);
        when(mockProjectExportParametersBuilder.build()).thenReturn(mockProjectExportParameters);

        // Act
        final ProjectExportParameters projectExportParameters = testling.getProjectExportParameters(mockProjectExportParametersBuilder);

        // Assert
        verify(mockProjectExportParametersBuilder).build();
        assertThat("Expect equal", projectExportParameters, is(mockProjectExportParameters));
    }

    /**
     * Tests if callExportProject
     */
    @Test
    public void testCallExportProject() {
        // Arrange
        final ProjectExporter mockProjectExporter = mock(ProjectExporter.class);
        final ServerConnection mockServerConnection = mock(ServerConnection.class);
        final ProjectExportParameters mockProjectExportParameters = mock(ProjectExportParameters.class);

        // Act
        final boolean exported = testling.callExportProject(mockProjectExporter, mockServerConnection, mockProjectExportParameters);

        // Assert

    }

    /**
     * Test if needsContext() is false.
     */
    @Test
    public void testNeedsContextReturnsFalse() {
        // Arrange

        // Act

        // Assert
        assertThat("Expected equal", testling.needsContext(), equalTo(false));
    }
}