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

package com.espirit.moddev.projectservice.projectexport;

import de.espirit.firstspirit.access.AdminService;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.ServerActionHandle;
import de.espirit.firstspirit.access.admin.ProjectStorage;
import de.espirit.firstspirit.access.export.ExportFile;
import de.espirit.firstspirit.access.export.ExportParameters;
import de.espirit.firstspirit.access.export.ExportProgress;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.access.script.ExecutionException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ProjectExporterTest.
 */
public class ProjectExporterTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    private ProjectExporter testling;

    @Before
    public void setUp() {
        testling = new ProjectExporter();
    }

    /**
     * Test that the default constructor has no dependencies or exceptions.
     */
    @Test
    public void testDefaultConstructor() {
        assertThat("Expect not null.", testling, is(notNullValue()));
    }

    /**
     * Test that an export with null connection throws an IllegalStateException.
     */
    @Test
    public void testExportProjectWithNullConnectionThrowsException() {
        // Arrange
        final Connection nullConnection = null;
        final ProjectExportParameters mockProjectExportParameters = mock(ProjectExportParameters.class);

        // Assert
        exception.expect(IllegalStateException.class);
        exception.expectMessage("Please provide a connected connection");

        // Act
        testling.exportProject(nullConnection, mockProjectExportParameters);
    }

    /**
     * Test that an export on a non-existent project throws an ExecutionException.
     */
    @Test
    public void testExportProjectThrowsExceptionIfProjectDoesNotExistOnServer() {
        // Arrange
        final Connection mockConnection = mock(Connection.class);
        when(mockConnection.isConnected()).thenReturn(true);
        when(mockConnection.getProjectByName(anyString())).thenReturn(null);

        final ProjectExportParameters mockProjectExportParameters = mock(ProjectExportParameters.class);

        // Assert
        exception.expect(ExecutionException.class);

        // Act
        testling.exportProject(mockConnection, mockProjectExportParameters);
    }

    /**
     * Test that projectExistsOnServer is true if a project exists.
     */
    @Test
    public void testProjectExistsOnServerReturnsTrueIfProjectExists() {
        // Arrange
        final Project mockProject = mock(Project.class);
        final Connection mockConnection = mock(Connection.class);
        when(mockConnection.isConnected()).thenReturn(true);
        when(mockConnection.getProjectByName(anyString())).thenReturn(mockProject);

        final ProjectExportParameters mockProjectExportParameters = mock(ProjectExportParameters.class);
        when(mockProjectExportParameters.getProjectName()).thenReturn("anyString");

        // Act
        final boolean projectExists = testling.projectExistsOnServer(mockConnection, mockProjectExportParameters);

        // Assert
        assertThat("Expected equal.", projectExists, equalTo(true));
    }

    /**
     * Test that projectExistsOnServer is false if a project does not exist.
     */
    @Test
    public void testProjectExistsOnServerReturnsFalseIfProjectDoesNotExist() {
        // Arrange
        final Connection mockConnection = mock(Connection.class);
        when(mockConnection.isConnected()).thenReturn(true);
        when(mockConnection.getProjectByName(anyString())).thenReturn(null);

        final ProjectExportParameters mockProjectExportParameters = mock(ProjectExportParameters.class);

        // Act
        final boolean projectExists = testling.projectExistsOnServer(mockConnection, mockProjectExportParameters);

        // Assert
        assertThat("Expected equal.", projectExists, equalTo(false));
    }

    /**
     * Test that the project to be exported does not exist on the FirstSpirit server during export.
     */
    @Test
    public void testPerformExportHasNullProjectReturnsFalse() {
        // Arrange
        final ProjectExportParameters mockProjectExportParameters = mock(ProjectExportParameters.class);

        final Connection mockConnection = mock(Connection.class);
        when(mockConnection.getProjectByName(mockProjectExportParameters.getProjectName())).thenReturn(null);

        // Act
        final boolean exported = testling.performExport(mockConnection, mockProjectExportParameters);

        // Assert
        assertThat("Expect equals.", exported, is(false));
    }

    /**
     * Test that export fails if the project is not active and will not be forced to be active.
     */
    @Test
    public void testPerformExportProjectIsNotActiveAndActivationWasNotForced() {
        // Arrange
        final Project mockProject = mock(Project.class);
        when(mockProject.isActive()).thenReturn(false);

        final ProjectExportParameters mockProjectExportParameters = mock(ProjectExportParameters.class);
        when(mockProjectExportParameters.isFsForceProjectActivation()).thenReturn(false);

        final Connection mockConnection = mock(Connection.class);
        when(mockConnection.getProjectByName(mockProjectExportParameters.getProjectName())).thenReturn(mockProject);

        // Act
        final boolean exported = testling.performExport(mockConnection, mockProjectExportParameters);

        // Assert
        assertThat("Expect equals", exported, is(false));
    }

    /**
     * Test that export fails if the project could not be activated by the FirstSpirit server.
     */
    @Test
    public void testPerformExportProjectWhenActivationFails() {
        // Arrange
        final Project mockProject = mock(Project.class);
        when(mockProject.isActive()).thenReturn(false);

        final ProjectExportParameters mockProjectExportParameters = mock(ProjectExportParameters.class);
        when(mockProjectExportParameters.isFsForceProjectActivation()).thenReturn(true);

        final Connection mockConnection = mock(Connection.class);
        when(mockConnection.getProjectByName(mockProjectExportParameters.getProjectName())).thenReturn(mockProject);

        final ProjectExporter spyProjectExporter = spy(testling);
        doReturn(false).when(spyProjectExporter).activateProjectByForce(mockConnection, mockProject);

        // Act
        final boolean exported = spyProjectExporter.performExport(mockConnection, mockProjectExportParameters);

        // Assert
        assertThat("Expect equals", exported, is(false));
    }

    /**
     * Test that exportProject calls performExport.
     */
    @Test
    public void testExportProjectCallsPerformExport() {
        // Arrange
        final ProjectExporter spyProjectExporter = spy(testling);

        final Connection mockConnection = mock(Connection.class);
        when(mockConnection.isConnected()).thenReturn(true);

        final ProjectExportParameters mockProjectExportParameters = mock(ProjectExportParameters.class);
        doReturn(true).when(spyProjectExporter).projectExistsOnServer(mockConnection, mockProjectExportParameters);
        doReturn(true).when(spyProjectExporter).exportPathIsWritable(any());
        doReturn(true).when(spyProjectExporter).performExport(mockConnection, mockProjectExportParameters);

        // Act
        final boolean exported = spyProjectExporter.exportProject(mockConnection, mockProjectExportParameters);

        // Assert
        verify(spyProjectExporter).performExport(mockConnection, mockProjectExportParameters);
        assertThat("Expect equals.", exported, is(true));
    }

    /**
     * Test that a deactivated project is activated by force when flag is set.
     */
    @Test
    public void testPerformExportCallsActivateProjectByForce() throws Exception {
        // Arrange
        final Project mockProject = mock(Project.class);
        when(mockProject.isActive()).thenReturn(false);

        final ProjectExportParameters mockProjectExportParameters = mock(ProjectExportParameters.class);
        when(mockProjectExportParameters.isFsForceProjectActivation()).thenReturn(true);
        when(mockProjectExportParameters.getProjectExportPath()).thenReturn("validProjectExportPath");

        final ServerActionHandle mockExportHandle = mock(ServerActionHandle.class);
        final Connection mockConnection = mock(Connection.class);
        when(mockConnection.getProjectByName(mockProjectExportParameters.getProjectName())).thenReturn(mockProject);

        final ProjectStorage mockProjectStorage = mock(ProjectStorage.class);
        when(mockProjectStorage.startExport(any(ExportParameters.class))).thenReturn(mockExportHandle);

        final AdminService mockAdminService = mock(AdminService.class);
        when(mockAdminService.getProjectStorage()).thenReturn(mockProjectStorage);
        when(mockConnection.getService(AdminService.class)).thenReturn(mockAdminService);

        final List mockExportFiles = mock(List.class);

        final ProjectExporter spyProjectExporter = spy(testling);
        doReturn(true).when(spyProjectExporter).activateProjectByForce(mockConnection, mockProject);
        doReturn(mockExportFiles).when(spyProjectExporter).waitUntilExportFinished(mockExportHandle);
        doReturn(true).when(spyProjectExporter).downloadExportFilesToFileSystem("validProjectExportPath", mockProjectStorage, mockExportFiles);

        // Act
        spyProjectExporter.performExport(mockConnection, mockProjectExportParameters);

        // Assert
        verify(spyProjectExporter).activateProjectByForce(mockConnection, mockProject);
    }

    /**
     * Test that the AdminService tries to activate the project.
     */
    @Test
    public void testActivateProjectByForceCallsAdminService() {
        // Arrange
        final Project mockProject = mock(Project.class);

        final ProjectStorage mockProjectStorage = mock(ProjectStorage.class);
        final AdminService mockAdminService = mock(AdminService.class);
        when(mockAdminService.getProjectStorage()).thenReturn(mockProjectStorage);

        final Connection mockConnection = mock(Connection.class);
        when(mockConnection.getService(AdminService.class)).thenReturn(mockAdminService);

        // Act
        testling.activateProjectByForce(mockConnection, mockProject);

        // Assert
        verify(mockProjectStorage).activateProject(mockProject);
    }

    /**
     * Test that all exported files are provided after the export is done.
     */
    @Test
    public void testWaitUntilExportFinishedReturnsExportedFilesWhenProgressIsFinished() {
        // Arrange
        final List mockExportFiles = mock(List.class);
        final ExportProgress mockExportProgress = mock(ExportProgress.class);
        when(mockExportProgress.isFinished()).thenReturn(true);
        when(mockExportProgress.getExportFiles()).thenReturn(mockExportFiles);

        final ServerActionHandle mockExportHandle = mock(ServerActionHandle.class);
        when(mockExportHandle.getProgress(true)).thenReturn(mockExportProgress);

        // Act
        final List<ExportFile> exportFiles = testling.waitUntilExportFinished(mockExportHandle);

        // Assert
        assertThat("Expect equals", exportFiles, equalTo(mockExportFiles));
    }

    /**
     * Test that downloadExportFilesToFileSystem() is false when no export files are given.
     */
    @Test
    public void testDownloadExportFilesIsFalseWhenExportFilesAreEmpty() {
        // Arrange
        final String projectExportPath = "validPath";
        final ProjectStorage projectStorage = mock(ProjectStorage.class);
        final List mockExportFiles = mock(List.class);
        when(mockExportFiles.isEmpty()).thenReturn(true);

        // Act
        final boolean downloaded = testling.downloadExportFilesToFileSystem(projectExportPath, projectStorage, mockExportFiles);

        // Assert
        assertThat("Expect equals", downloaded, is(false));
    }

    /**
     * Test that the IOException potentially thrown by projectStorage.startExport is handled.
     */
    @Test
    public void testTriggerExportReturnsEmptyListIfExportThrowsIOException() throws Exception {
        final ProjectStorage mockProjectStorage = mock(ProjectStorage.class);
        when(mockProjectStorage.startExport(any())).thenThrow(IOException.class);

        List<ExportFile> exportFiles = testling.triggerExport(mockProjectStorage, null);

        assertThat("Expected an empty list", exportFiles, hasSize(0));
    }
}
