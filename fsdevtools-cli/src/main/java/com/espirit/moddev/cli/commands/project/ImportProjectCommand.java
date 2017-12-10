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
import com.espirit.moddev.core.StringPropertiesMap;
import com.espirit.moddev.projectservice.projectimport.ProjectImportParametersBuilder;
import com.espirit.moddev.projectservice.projectimport.ProjectImporter;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.help.Examples;
import com.github.rvesse.airline.annotations.restrictions.Required;
import de.espirit.firstspirit.access.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

@Command(name = "import", groupNames = {"project"}, description = "Imports a FirstSpirit project export into a FirstSpirit Server as a new project.")
@Examples(
        examples = {"fs-cli project import -h localhost -p 8000 project import --importProjectName \"newProjectName\" --projectFile \"D:\\my-project-export.tar.gz\"",
                    "fs-cli project import --importProjectName \"newProjectName\" --projectFile \"D:\\my-project-export.tar.gz\" -lm *:CREATE_NEW",
                    "fs-cli project import --importProjectName \"newProjectName\" --projectFile \"D:\\my-project-export.tar.gz\" -lm schema_a:derby_project14747_0,schema_b:derby_project14747_1"
        },
        descriptions = {"Imports the project export into a new project that is named newProjectName",
                        "Import project and create for every unknown source schema a new target layer (use if uncertain)",
                        "Import project and use specified mapping for source schemas and existing target layers. The target layers must be attached to the project! (use with caution)"})
public class ImportProjectCommand extends SimpleCommand<SimpleResult<Boolean>>{
    protected static final Logger LOGGER = LoggerFactory.getLogger(ImportProjectCommand.class);

    @Option(type = OptionType.COMMAND, name = {"-ipn", "--importProjectName"}, description = "Name of the FirstSpirit target project where the import should go")
    @Required
    private String projectName;
    @Option(type = OptionType.COMMAND, name = {"-ipd", "--importProjectDescription"}, description = "Description of the FirstSpirit target project")
    @Required
    private String projectDescription;
    @Option(type = OptionType.COMMAND, name = {"-pf", "--projectFile"}, description = "Path to the project export file that should be imported")
    @Required
    private String projectFile;
    @Option(type = OptionType.COMMAND, name = {"-fpa", "--forceProjectActivation"}, description = "Whether to force the project activation if the project is deactivated after import somehow. Default is false.")
    private boolean forceProjectActivation;
    @Option(type = OptionType.COMMAND, name = {"-dlm", "--databaseLayerMapping"}, description = "Define a map-like layerMapping with comma-separated key-value pairs by : or =; . See command examples.")
    private String databaseLayerMapping;

    @Override
    public SimpleResult<Boolean> call() {
        try(final Connection connection = create()) {
            connection.connect();

        if (projectFile == null) {
            return new SimpleResult<>(new IllegalArgumentException("Missing parameter for project file"));
        }


        ProjectImportParametersBuilder importParametersBuilder = new ProjectImportParametersBuilder()
                .setProjectName(projectName)
                .setProjectFile(new File(projectFile))
                .setProjectDescription(projectDescription)
                .setForceProjectActivation(forceProjectActivation)
                .setDatabaseLayerMapping(new StringPropertiesMap(databaseLayerMapping));

        boolean imported = new ProjectImporter().importProject(connection, importParametersBuilder.create());

        return new SimpleResult(imported ? imported : new IllegalStateException("Import was not successful"));

        } catch (final Exception e) {
            return new SimpleResult<>(e);
        }
    }

    protected Connection create() {
        return ConnectionBuilder.with(this).build();
    }

    @Override
    public boolean needsContext() {
        return false;
    }
}
