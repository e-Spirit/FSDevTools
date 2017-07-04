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

package com.espirit.moddev.projectservice.projectimport;

import de.espirit.firstspirit.access.AdminService;
import de.espirit.firstspirit.access.ServerActionHandle;
import de.espirit.firstspirit.access.UserService;
import de.espirit.firstspirit.access.export.ExportFile;
import de.espirit.firstspirit.access.export.ImportParameters;
import de.espirit.firstspirit.access.export.ImportProgress;
import de.espirit.firstspirit.access.export.ProjectInfo;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.access.script.ExecutionException;
import de.espirit.firstspirit.io.ServerConnection;
import de.espirit.firstspirit.manager.ExportManager;
import de.espirit.firstspirit.manager.ProjectManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 * Class that can import a given FirstSpirit project into a server.
 */
public class ProjectImporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectImporter.class);

    public ProjectImporter() {
    }

    /**
     * Imports a project specified by projectImportParameters into a FirstSpirit server.
     * Uses the given connection to obtain all necessary managers.
     *
     * @param connection              the connection that is used to access the FirstSpirit server
     * @param projectImportParameters
     * @return true if the project was imported successfully, false otherwise
     * @throws IllegalStateException if the given connection is null or not connected
     * @throws ExecutionException    if a project with the given name already exists on the server
     */
    public boolean importProject(ServerConnection connection, ProjectImportParameters projectImportParameters) {
        if(connection == null || !connection.isConnected()) {
            throw new IllegalStateException("Please provide a connected connection");
        }
        if(projectExistsOnServer(connection, projectImportParameters)) {
            throw new ExecutionException("Project with name '"
                    + projectImportParameters.getProjectName()
                    + "' seems to exist already! Either delete/rename the existing project or rename the project you want to import!");
        }

        return performImport(connection, projectImportParameters);
    }

    private boolean performImport(ServerConnection connection, ProjectImportParameters projectImportParameters) {
        try {
            final ExportManager exportManager = connection.getManager(ExportManager.class);
            removeExportFileFromServerIfExists(projectImportParameters, exportManager);

            final File projectFile = new File(projectImportParameters.getProjectFilePath());
            FileInputStream fileInputStream = getResourceOrFileBasedInputStream(projectImportParameters);

            final ExportFile exportFile = exportManager.uploadExportFile(projectFile.getName(), fileInputStream);
            final ProjectInfo info = exportManager.getProjectInfo(exportFile);

            HashMap<String, String> layerMapping = getLayerMappingDefinition(projectImportParameters, info);

            final ImportParameters importParameters = new ImportParameters(
                    exportFile, info,
                    projectImportParameters.getProjectName(), projectImportParameters.getProjectDescription(), layerMapping,
                    new HashMap<>());
            importParameters.getLayerMapping();
            final ServerActionHandle<ImportProgress, Boolean> importHandle = exportManager.startImport(importParameters);

            waitUntilImportFinished(importHandle);
            refreshProjects(connection);

            Project fsProject = connection.getProjectByName(projectImportParameters.getProjectName());
            boolean projectIsPresent = fsProject != null;
            if (projectIsPresent) {
                activeProjectIfNecessary(projectImportParameters, fsProject);
            }

            return projectIsPresent;
        } catch (ExecutionException | IOException e) {
            LOGGER.error("Not able to perform import!", e);
            return false;
        }
    }

    private void activeProjectIfNecessary(ProjectImportParameters projectImportParameters, Project fsProject) {
        boolean projectIsActive = fsProject.isActive();
        if (projectImportParameters.isFsForceProjectActivation()) {
            if (!projectIsActive) {
                LOGGER.warn("Project '" + projectImportParameters.getProjectName() + "' is not active! Try to activate...");
                UserService userService = fsProject.getUserService();
                AdminService adminService = userService.getConnection().getService(AdminService.class);
                adminService.getProjectStorage().activateProject(fsProject);
            }
            if(!fsProject.isActive()) {
                throw new ExecutionException("Project with name '"
                        + projectImportParameters.getProjectName()
                        + "' seems to be deactivated! To force activation, configure fsForceProjectActivation with true!");
            }
        }
    }

    private void refreshProjects(ServerConnection connection) {
        // TODO: Remove this call to refreshProjects() after the logic is implemented
        // by core somehow. Further information, see ts 186956
        AdminService as = connection.getService(AdminService.class);
        as.getProjectStorage().refreshProjects();
    }

    private void waitUntilImportFinished(ServerActionHandle<ImportProgress, Boolean> importHandle) {
        ImportProgress progress;
        while (true) {
            progress = importHandle.getProgress(true);
            LOGGER.info(progress.getProgress() + "%");
            if (progress.isFinished()) {
                break;
            } else {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    LOGGER.error("Thread sleep failed!", e);
                }
            }
        }
        LOGGER.info("ImportProgress finished");
    }

    private HashMap<String, String> getLayerMappingDefinition(ProjectImportParameters projectImportParameters, ProjectInfo info) {
        HashMap<String, String> layerMapping = new HashMap<>();
        List<Properties> usedLayers = info.getUsedLayers();
        for (Properties prop : usedLayers) {
            layerMapping.put(
                prop.getProperty("name"),
                projectImportParameters.getDatabases() != null ? projectImportParameters.getDatabases().get(prop.getProperty("name")) : null);
        }
        return layerMapping;
    }

    private FileInputStream getResourceOrFileBasedInputStream(ProjectImportParameters projectImportParameters) throws FileNotFoundException {
        FileInputStream fileInputStream = (FileInputStream) ProjectImporter.class
                .getClassLoader().getResourceAsStream(projectImportParameters.getProjectFilePath());
        if (fileInputStream == null) {
            fileInputStream = new FileInputStream(projectImportParameters.getProjectFilePath());
        }
        return fileInputStream;
    }

    private void removeExportFileFromServerIfExists(ProjectImportParameters projectImportParameters, ExportManager exportManager) {
        try {
            final List<ExportFile> exportFiles = exportManager.listExportFiles();
            for (final ExportFile exportFile : exportFiles) {
                if (exportFile.getName().equals(projectImportParameters.getProjectFilePath())) {
                    exportManager.deleteExportFile(exportFile);
                }
            }
        } catch (IOException e) {
            LOGGER.warn("Problem while trying to remove old export file(s)", e);
        }
    }

    private boolean projectExistsOnServer(ServerConnection connection, ProjectImportParameters projectImportParameters) {
        final ProjectManager projectManager = connection.getManager(ProjectManager.class);
        return projectManager.getProjectByName(projectImportParameters.getProjectName()) != null;
    }
}
