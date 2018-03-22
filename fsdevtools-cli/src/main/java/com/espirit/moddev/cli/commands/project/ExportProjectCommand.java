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

import com.espirit.moddev.cli.ConnectionBuilder;
import com.espirit.moddev.cli.commands.SimpleCommand;
import com.espirit.moddev.cli.results.SimpleResult;
import com.espirit.moddev.projectservice.projectexport.ProjectExportParameters;
import com.espirit.moddev.projectservice.projectexport.ProjectExportParametersBuilder;
import com.espirit.moddev.projectservice.projectexport.ProjectExporter;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.help.Examples;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.io.ServerConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(name = "export", groupNames = {"project"}, description = "Exports an existing FirstSpirit project from a FirstSpirit Server.")
@Examples(
        examples = {"fs-cli -h localhost -p 8000 project export --epn \"Mithras Energy\" -epp \"D:\\my-server-exports\" -fpa"},
        descriptions = {"Imports the project export into a new project that is named newProjectName"})
public class ExportProjectCommand extends SimpleCommand<SimpleResult<Boolean>> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(ExportProjectCommand.class);

    @Option(type = OptionType.COMMAND, name = {"-epn", "--exportProjectName"}, description = "Name of the FirstSpirit project to export from.")
    private String projectName;
    @Option(type = OptionType.COMMAND, name = {"-epp", "--exportProjectPath"}, description = "Path of HDD to write the export file to. Must be writable. Non-existent directories will be created by the CLI.")
    private String projectExportPath;
    @Option(type = OptionType.COMMAND, name = {"-fpa", "--fsForceProjectActivation"}, description = "Whether to force the project activation if the project is deactivated before export somehow. Default is false.")
    private boolean fsForceProjectActivation;
    @Option(type = OptionType.COMMAND, name = {"-def", "--deleteExportFiles"}, description = "Whether to delete the export files on the server after they have been downloaded.")
    private boolean deleteExportFiles;

    @Override
    public SimpleResult<Boolean> call() {
        try(final Connection connection = createConnection()) {
            connection.connect();

            if(connection instanceof ServerConnection) {
                ProjectExportParametersBuilder exportParametersBuilder = new ProjectExportParametersBuilder()
                        .setProjectName(projectName)
                        .setProjectExportPath(projectExportPath)
                        .setFsForceProjectActivation(fsForceProjectActivation)
                        .setDeleteExportFiles(deleteExportFiles);

                final ProjectExporter projectExporter = new ProjectExporter();
                boolean exported = callExportProject(projectExporter, (ServerConnection) connection, getProjectExportParameters(exportParametersBuilder));

                return new SimpleResult(exported ? exported : new IllegalStateException("Export was not successful"));
            } else {
                return new SimpleResult<>(new IllegalStateException("Connection is not a ServerConnection implementation."));
            }

        } catch (final Exception e) {
            return new SimpleResult<>(e);
        }
    }

    /**
     * Creates a connection to a FirstSpirit Server with this instance as config.
     *
     * @return A connection from a ConnectionBuild.
     * @see ConnectionBuilder
     */
    protected Connection createConnection() {
        return ConnectionBuilder.with(this).build();
    }

    /**
     * Creates ProjectExportParameters from a builder.
     *
     * @param projectExportParametersBuilder Builder for the export
     * @return Export parameters based on the given builder
     */
    protected ProjectExportParameters getProjectExportParameters(ProjectExportParametersBuilder projectExportParametersBuilder) { return projectExportParametersBuilder.build(); }

    /**
     * Exports the project.
     *
     * @param projectExporter         Instance of a ProjectExporter.
     * @param serverConnection        Connection to the FirstSpirit server.
     * @param projectExportParameters Parameters of the project which is going to be exported.
     * @return Whether the export was successful or not.
     */
    protected boolean callExportProject(ProjectExporter projectExporter, ServerConnection serverConnection, ProjectExportParameters projectExportParameters) {
        return projectExporter.exportProject(serverConnection, projectExportParameters);
    }

    @Override
    public boolean needsContext() {
        return false;
    }
}
