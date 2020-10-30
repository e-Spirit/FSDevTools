/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2020 e-Spirit AG
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

package com.espirit.moddev.cli.commands.project.importCommand;

import com.espirit.moddev.shared.annotation.VisibleForTesting;
import de.espirit.common.tools.Strings;
import de.espirit.firstspirit.access.AdminService;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.ServerActionHandle;
import de.espirit.firstspirit.access.UserService;
import de.espirit.firstspirit.access.admin.ProjectStorage;
import de.espirit.firstspirit.access.database.Layer;
import de.espirit.firstspirit.access.export.ExportFile;
import de.espirit.firstspirit.access.export.ImportParameters;
import de.espirit.firstspirit.access.export.ImportProgress;
import de.espirit.firstspirit.access.export.ProjectInfo;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.access.schedule.DeployTask;
import de.espirit.firstspirit.access.schedule.MailTask;
import de.espirit.firstspirit.access.schedule.ScheduleStorage;
import de.espirit.firstspirit.access.schedule.ScheduleTask;
import de.espirit.firstspirit.access.schedule.ScheduleTaskTemplate;
import de.espirit.firstspirit.access.schedule.ScriptTask;
import de.espirit.firstspirit.access.script.ExecutionException;
import de.espirit.firstspirit.server.scheduler.DeployTaskDTO;
import de.espirit.firstspirit.server.scheduler.MailTaskDTO;
import de.espirit.firstspirit.server.scheduler.ScheduleTaskDTO;
import de.espirit.firstspirit.server.scheduler.ScheduleTaskTemplateDTO;
import de.espirit.firstspirit.server.scheduler.ScriptTaskDTO;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

/**
 * Class that can import a given FirstSpirit project into a server.
 */
public class ProjectImporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectImporter.class);
    private static final int WAIT_MS_UNTIL_CHECK_FOR_IMPORT = 250;
    private static final String LAYER_CREATE_NEW = "CREATE_NEW";

    public ProjectImporter() {
        // Nothing to do here
    }

    /**
     * Imports a project specified by projectImportParameters into a FirstSpirit server.
     * Uses the given connection to obtain all necessary managers.
     *
     * @param connection the connection that is used to access the FirstSpirit server
     * @param parameters the parameters for the project import
     * @return true if the project was imported successfully, false otherwise
     * @throws IllegalStateException if the given connection is null or not connected
     * @throws ExecutionException    if a project with the given name already exists on the server
     */
    public boolean importProject(@NotNull final Connection connection, @NotNull final ProjectImportParameters parameters) throws Exception {
        if (!connection.isConnected()) {
            throw new IllegalStateException("Please provide a connected connection");
        }
        if (projectExists(connection, parameters.getProjectName())) {
            throw new ExecutionException("Project with name '" + parameters.getProjectName() + "' seems to exist already! Either delete/rename the existing project or rename the project you want to import!");
        }

        try {
            // get project storage
            final AdminService adminService = connection.getService(AdminService.class);
            final ProjectStorage projectStorage = adminService.getProjectStorage();

            // remove export file, if it already exists
            deleteExportFile(parameters, projectStorage);

            // get project info from export file
            final ExportFile exportFile;
            try (final FileInputStream fileInputStream = new FileInputStream(parameters.getProjectFile())) {
                exportFile = projectStorage.uploadExportFile(parameters.getProjectFile().getName(), fileInputStream);
            }
            final ProjectInfo projectInfo = projectStorage.getProjectInfo(exportFile);

            // get layer mapping & verify
            final Map<String, String> layerMapping = getLayerMapping(parameters, projectInfo);
            verifyTargetLayers(adminService, layerMapping);

            // get schedule task template mapping
            final Map<Long, Long> scheduleTaskTemplateMapping = getScheduleTaskTemplateMapping(adminService.getScheduleStorage(), projectInfo);

            // setup parameters
            final ImportParameters importParameters = new ImportParameters(exportFile, projectInfo,
                    parameters.getProjectName(), parameters.getProjectDescription(),
                    layerMapping, scheduleTaskTemplateMapping);

            // start import
            final ServerActionHandle<ImportProgress, Boolean> handle = projectStorage.startImport(importParameters);

            // wait for the import to finish and handle the result
            if (waitUntilImportFinished(handle)) {
                refreshProjects(connection);

                // get the project from the server
                final Project project = connection.getProjectByName(parameters.getProjectName());
                if (project == null) {
                    // no project found --> throw error (this should probably never happen)
                    throw new ExecutionException("Project '" + parameters.getProjectName() + "' imported but not found on the server!");
                }

                // activate project, if needed
                if (parameters.forceProjectActivation()) {
                    activateProject(project);
                }
                return true;
            } else {
                // project not imported: log errors
                LOGGER.error("The import has been finished with errors. The project was not activated. See server.log for details.");
                return false;
            }
        } catch (final Exception e) {
            LOGGER.error("Not able to perform import!", e);
            throw e;
        }
    }

    private void verifyTargetLayers(@NotNull final AdminService adminService, @NotNull final Map<String, String> layerMapping) {
        // iterate over targets and verify
        final Set<String> missingTargetLayers = new HashSet<>();
        for (final String targetLayer : layerMapping.values()) {
            // "null" layers should be ignored --> "null" means the layer will be created by FirstSpirit on the fly
            if (targetLayer == null) {
                continue;
            }

            final Layer lookup = adminService.getDatabaseLayer(targetLayer);
            if (lookup == null) {
                missingTargetLayers.add(targetLayer);
            }
        }

        // throw exception
        if (!missingTargetLayers.isEmpty()) {
            throw new IllegalStateException("The following target layers do not exist: [ " + Strings.implode(missingTargetLayers, ", ") + " ]");
        }
    }

    private static void activateProject(@NotNull final Project project) {
        if (!project.isActive()) {
            LOGGER.warn("Project '" + project.getName() + "' is not active! Try to activate...");
            final UserService userService = project.getUserService();
            final AdminService adminService = userService.getConnection().getService(AdminService.class);
            adminService.getProjectStorage().activateProject(project);
        }
        if (!project.isActive()) {
            throw new ExecutionException("Project with name '" + project.getName() + "' seems to be deactivated! To force activation, configure fsForceProjectActivation with true!");
        }
    }

    private static void refreshProjects(@NotNull final Connection connection) {
        final AdminService adminService = connection.getService(AdminService.class);
        adminService.getProjectStorage().refreshProjects();
    }

    private static boolean waitUntilImportFinished(@NotNull final ServerActionHandle<ImportProgress, Boolean> handle) throws Exception {
        while (true) {
            // sleep some time
            try {
                Thread.sleep(WAIT_MS_UNTIL_CHECK_FOR_IMPORT);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                return Boolean.FALSE;
            }

            // get the current progress and print it
            final ImportProgress progress = handle.getProgress(false);
            LOGGER.info(progress.getProgress() + "%");

            // progress is finished --> break
            if (progress.isFinished()) {
                break;
            }
        }

        // check the result of the import
        final Boolean result = handle.getResult();
        if (result) {
            LOGGER.info("ImportProgress finished");
            return true;
        } else {
            // check the handle for exceptions and throw them if needed
            handle.checkAndThrow();
            return false;
        }
    }

    @VisibleForTesting
    @NotNull
    static Map<String, String> getLayerMapping(@NotNull final ProjectImportParameters parameters, @NotNull final ProjectInfo info) {
        final HashMap<String, String> layerMapping = new HashMap<>();
        final List<Properties> usedLayers = info.getUsedLayers();
        for (final Properties property : usedLayers) {
            final String name = property.getProperty("name");
            String mappedLayer;
            final Map<String, String> databases = parameters.getLayerMapping();
            // lookup the exact mapping for this layer
            mappedLayer = databases.get(name);
            if (mappedLayer == null) {
                // still no mapped layer --> fallback to wildcard or NULL (if no wildcard is set)
                mappedLayer = databases.get("*");
            }
            // "CREATE_NEW" --> map to null (which means that the layer will be created)
            if (LAYER_CREATE_NEW.equals(mappedLayer)) {
                mappedLayer = null;
            }
            layerMapping.put(name, mappedLayer);
        }
        return layerMapping;
    }

    @VisibleForTesting
    @NotNull
    static Map<Long, Long> getScheduleTaskTemplateMapping(@NotNull final ScheduleStorage scheduleStorage, @NotNull final ProjectInfo projectInfo) {
        // map all serverScheduleTemplateMapping to -1 for all templates, which means we will always import the template from the project file
        final List<ScheduleTaskTemplate> serverSideTaskTemplates = scheduleStorage.getScheduleTaskTemplates(null);
        final List<ScheduleTaskTemplateDTO> taskTemplatesToImport = projectInfo.getServerScheduleTaskTemplates();
        final HashMap<Long, Long> templateMapping = new HashMap<>();
        for (final ScheduleTaskTemplateDTO templateToImport : taskTemplatesToImport) {
            templateMapping.put(templateToImport.getId(), getMappedScheduleTaskId(serverSideTaskTemplates, templateToImport));
        }
        return templateMapping;
    }

    /**
     * This method will check if the imported {@link ScheduleTaskTemplate} already exists on the server and will return the id of the existing template.
     * If the {@link ScheduleTaskTemplate} does not exist yet, this method will return {@code -1}.
     *
     * @param serverSideTaskTemplates current list of {@link ScheduleTaskTemplate templates} existing on the server
     * @param templateToImport        the {@link ScheduleTaskTemplateDTO dto} of the {@link ScheduleTaskTemplate template} to lookup
     * @return the mapped id for the {@link ScheduleTaskTemplate} of {@code -1} if it does not yet exist
     */
    private static long getMappedScheduleTaskId(@NotNull final List<ScheduleTaskTemplate> serverSideTaskTemplates, @NotNull final ScheduleTaskTemplateDTO templateToImport) {
        for (final ScheduleTaskTemplate serverSideTemplate : serverSideTaskTemplates) {
            final ScheduleTask serverSideScheduleTask = serverSideTemplate.getTask();
            // we assume that a task is similar if the type, the name and the description is equal
            final ScheduleTaskDTO toImportTaskDTO = templateToImport.getTask();
            if (isTypeEqual(serverSideScheduleTask, toImportTaskDTO) && Objects.equals(serverSideScheduleTask.getName(), toImportTaskDTO.getName()) && Objects.equals(serverSideScheduleTask.getDescription(), toImportTaskDTO.getDescription())) {
                final long mappedId = serverSideTemplate.getId();
                LOGGER.warn("Schedule task '" + toImportTaskDTO.getName() + "' (id=" + templateToImport.getId() + ") will be mapped to exisiting task with id=" + mappedId + ".");
                return mappedId;
            }
        }
        // no mapping found --> return -1
        return -1;
    }

    /**
     * Checks if the type of the given {@link ScheduleTask} is equal to the type of the {@link ScheduleTaskDTO} and returns the result.
     *
     * @param scheduleTask    the {@link ScheduleTask}
     * @param scheduleTaskDTO the {@link ScheduleTaskDTO}
     * @return {@code true} if the types are equal, otherwise {@code false}.
     */
    private static boolean isTypeEqual(@NotNull final ScheduleTask scheduleTask, @NotNull final ScheduleTaskDTO scheduleTaskDTO) {
        if (scheduleTaskDTO instanceof ScriptTaskDTO) {
            return scheduleTask instanceof ScriptTask;
        } else if (scheduleTaskDTO instanceof MailTaskDTO) {
            return scheduleTask instanceof MailTask;
        } else if (scheduleTaskDTO instanceof DeployTaskDTO) {
            return scheduleTask instanceof DeployTask;
        }
        return false;
    }

    private static void deleteExportFile(@NotNull final ProjectImportParameters parameters, @NotNull final ProjectStorage projectStorage) {
        try {
            final List<ExportFile> exportFiles = projectStorage.listExportFiles();
            for (final ExportFile exportFile : exportFiles) {
                if (exportFile.getName().equals(parameters.getProjectFile().getName())) {
                    projectStorage.deleteExportFile(exportFile);
                }
            }
        } catch (IOException e) {
            LOGGER.warn("Problem while trying to remove old export file(s)", e);
        }
    }

    private static boolean projectExists(@NotNull final Connection connection, @NotNull final String projectName) {
        final Project[] projects = connection.getProjects();
        if (projects == null || projects.length < 1) {
            LOGGER.debug("Could not find any projects on the server.");
            return false;
        }
        for (final Project project : connection.getProjects()) {
            LOGGER.debug("Found project: " + project.getName());
            if (project.getName().equals(projectName)) {
                return true;
            }
        }
        LOGGER.debug("Could not find project " + projectName);
        return false;
    }
}
