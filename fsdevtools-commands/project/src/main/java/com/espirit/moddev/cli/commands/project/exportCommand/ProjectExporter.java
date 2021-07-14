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

package com.espirit.moddev.cli.commands.project.exportCommand;

import de.espirit.firstspirit.access.AdminService;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.ServerActionHandle;
import de.espirit.firstspirit.access.admin.ProjectStorage;
import de.espirit.firstspirit.access.export.ExportFile;
import de.espirit.firstspirit.access.export.ExportParameters;
import de.espirit.firstspirit.access.export.ExportProgress;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.access.script.ExecutionException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 * Class that can export a given FirstSpirit project from a server.
 */
public class ProjectExporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectExporter.class);

    /**
     * Exports a project specified by projectExportParameters from a FirstSpirit server.
     *
     * @param connection              The connection that is used to access the FirstSpirit server
     * @param projectExportParameters ProjectExportParameters representing the project which is going to be exported.
     * @return true if the project was exported successfully, false otherwise.
     * @throws IllegalStateException If the given connection is null or not connected.
     * @throws ExecutionException    If a project with the given name does not exist on the server.
     */
    public boolean exportProject(@Nullable final Connection connection, @NotNull final ProjectExportParameters projectExportParameters) {
        if (connection == null || !connection.isConnected()) {
            throw new IllegalStateException("Please provide a connected connection");
        }
        if (!projectExistsOnServer(connection, projectExportParameters)) {
            throw new ExecutionException("Project with name '"
                    + projectExportParameters.getProjectName()
                    + "' does not exist on server and could not be exported!");
        }

        final String exportDir = projectExportParameters.getProjectExportPath();
        if (!exportPathIsWritable(exportDir)) {
            throw new ExecutionException("Export directory '"
                    + projectExportParameters.getProjectExportPath()
                    + "' is not writable!");
        }

        return performExport(connection, projectExportParameters);
    }

    /**
     * Check whether a project with the given name exists on the connected server.
     *
     * @param connection              A connected connection to the FirstSpirit server.
     * @param projectExportParameters ProjectExportParameters containing the projectName to lookup.
     * @return true if the project exists on the server, false otherwise.
     */
    protected boolean projectExistsOnServer(@NotNull final Connection connection, @NotNull final ProjectExportParameters projectExportParameters) {
        return connection.getProjectByName(projectExportParameters.getProjectName()) != null;
    }

    /**
     * Fail-fast approach when export directory is not writable.
     *
     * @param exportDirectory Export directory to check
     * @return true if the directory is writable, false otherwise
     */
    protected boolean exportPathIsWritable(String exportDirectory) {
        if (exportDirectory == null || exportDirectory.isEmpty()) {
            return false;
        }

        final File exportDir = new File(exportDirectory);
        if (exportDir.exists()) {
            return exportDir.canWrite();
        } else {
            final File parentExportDir = exportDir.getParentFile();
            if (parentExportDir != null) {
                return exportPathIsWritable(parentExportDir.getAbsolutePath());
            } else {
                return false;
            }
        }
    }

    /**
     * Perform the actual export.
     * Sub-tasks are:
     * Check if project is available and activated, start project export, download project export to the filesystem.
     *
     * @param connection              A connected connection to the FirstSpirit server.
     * @param projectExportParameters ProjectExportParameters containing information about the project to export
     * @return true if the export performed without any errors, false otherwise.
     */
    protected boolean performExport(@NotNull final Connection connection, @NotNull final ProjectExportParameters projectExportParameters) {
        final String projectName = projectExportParameters.getProjectName();
        final Project fsProject = connection.getProjectByName(projectName);

        if (fsProject != null) {
            if (!fsProject.isActive()) {
                if (projectExportParameters.isFsForceProjectActivation()) {
                    if (!activateProjectByForce(connection, fsProject)) {
                        LOGGER.error("Project could not be activated.");
                        return false;
                    }
                } else {
                    LOGGER.error("Project is deactivated. Please set --fsForceProjectActivation to activate the project.");
                    return false;
                }
            }

            // Project must be active at this point
            final ProjectStorage projectStorage = connection.getService(AdminService.class).getProjectStorage();
            final ExportParameters exportParameters = new ExportParameters(
                    fsProject.getId(),
                    projectName
            );

            exportParameters.setExportDeletedElements(projectExportParameters.isExportDeletedElements());
            exportParameters.setMaxRevisionCount(projectExportParameters.getMaxRevisionCount());

            final List<ExportFile> exportFiles = triggerExport(projectStorage, exportParameters);
            final boolean downloadSuccessful = downloadExportFilesToFileSystem(projectExportParameters.getProjectExportPath(), projectStorage, exportFiles);
            if (downloadSuccessful && projectExportParameters.isDeleteExportFiles()) {
                deleteExportFiles(projectStorage, exportFiles);
            }
            return downloadSuccessful;
        } else {
            LOGGER.error("Project '" + projectName + "' not found on server.");
            return false;
        }
    }

    private void deleteExportFiles(ProjectStorage projectStorage, List<ExportFile> exportFiles) {
        try {
            for (ExportFile exportFile : exportFiles) {
                projectStorage.deleteExportFile(exportFile);
            }
        } catch (FileNotFoundException e) {
            LOGGER.warn("Export files could not be deleted.", e);
        }
    }

    /**
     * Enforce project activation.
     *
     * @param connection A connected connection a FirstSpirit server.
     * @param fsProject  The project to check for activation.
     * @return Whether the activation was successful or not.
     */
    protected boolean activateProjectByForce(@NotNull final Connection connection, @NotNull final Project fsProject) {
        LOGGER.info("Project '" + fsProject.getName() + "' is not active! Trying to activate...");
        AdminService adminService = connection.getService(AdminService.class);
        adminService.getProjectStorage().activateProject(fsProject);

        fsProject.refresh();
        return fsProject.isActive();
    }

    protected List<ExportFile> triggerExport(final ProjectStorage projectStorage, final ExportParameters exportParameters) {
        try {
            final ServerActionHandle<ExportProgress, Boolean> exportHandle = projectStorage.startExport(exportParameters);
            return waitUntilExportFinished(exportHandle);
        } catch (IOException e) {
            LOGGER.error("Export failed due to an i/o exception.", e);
            return Collections.emptyList();
        }
    }

    /**
     * Wait for the server export to finish.
     *
     * @param exportHandle Handle of the active export job.
     */
    protected List<ExportFile> waitUntilExportFinished(ServerActionHandle<ExportProgress, Boolean> exportHandle) {
        ExportProgress exportProgress = exportHandle.getProgress(true);
        while (!exportProgress.isFinished()) {
            exportProgress = exportHandle.getProgress(true);
            LOGGER.info("Exporting...");
            try {
                Thread.sleep(2500);
            } catch (InterruptedException e) {
                LOGGER.error("Thread sleep failed!", e);
                Thread.currentThread().interrupt();
            }
        }

        LOGGER.info("ExportProgress finished");
        return exportProgress.getExportFiles();
    }

    /**
     * Downloads the exported file(s) to the filesystem.
     * Ensures that the export directory exists beforehand.
     *
     * @param projectExportPath The download directory for the exported project.
     * @param projectStorage    ProjectStorage who processed the export.
     * @param exportFiles       List of all exported files.
     * @return true if the whole process was successful, false otherwise.
     */
    protected boolean downloadExportFilesToFileSystem(String projectExportPath, ProjectStorage projectStorage, List<ExportFile> exportFiles) {
        if (exportFiles.isEmpty()) {
            LOGGER.error("No exported files found.");
            return false;
        }

        File projectParentDir = new File(projectExportPath);
        if (!projectParentDir.exists()) {
            if (projectParentDir.mkdirs()) {
                LOGGER.info("Created directory " + projectParentDir);
            } else {
                LOGGER.error("Could not create download directory.");
                return false;
            }
        }

        for (ExportFile exportFile : exportFiles) {
            File projectExportFile = new File(projectParentDir + File.separator + exportFile.getName());

            try (InputStream downloadInputStream = projectStorage.downloadExportFile(exportFile); FileOutputStream exportFileOutputStream = new FileOutputStream(projectExportFile)) {

                int read;
                byte[] bytes = new byte[8192];
                while ((read = downloadInputStream.read(bytes)) != -1) {
                    exportFileOutputStream.write(bytes, 0, read);
                }

                if (projectExportFile.exists()) {
                    LOGGER.info("Export file download successful.");
                } else {
                    LOGGER.info("Export file download failed.");
                    return false;
                }
            } catch (FileNotFoundException e) {
                LOGGER.error("", e);
                return false;
            } catch (IOException e) {
                LOGGER.error("", e);
                return false;
            }
        }

        return true;
    }
}
