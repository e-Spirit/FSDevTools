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

package com.espirit.moddev.cli.commands.project.importCommand;

import com.espirit.moddev.cli.ConnectionBuilder;
import com.espirit.moddev.cli.commands.SimpleCommand;
import com.espirit.moddev.cli.commands.project.ProjectCommandGroup;
import com.espirit.moddev.cli.commands.project.ProjectCommandNames;
import com.espirit.moddev.cli.common.StringPropertiesMap;
import com.espirit.moddev.cli.results.SimpleResult;
import com.espirit.moddev.shared.annotation.VisibleForTesting;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.help.Examples;
import com.github.rvesse.airline.annotations.restrictions.Required;
import de.espirit.firstspirit.access.Connection;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

@Command(name = ProjectCommandNames.IMPORT, groupNames = ProjectCommandGroup.NAME, description = "Imports a FirstSpirit project export into a FirstSpirit Server as a new project.")
@Examples(
		examples = {
				"project import --importProjectName \"newProjectName\" --projectFile \"D:\\my-project-export.tar.gz\"",
				"project import --importProjectName \"newProjectName\" --projectFile \"D:\\my-project-export.tar.gz\" -dlm *:CREATE_NEW",
				"project import --importProjectName \"newProjectName\" --projectFile \"D:\\my-project-export.tar.gz\" -dlm sourceLayer_A:targetLayer_A,sourceLayer_B:targetLayer_B"
		},
		descriptions = {
				"Imports the project export into a new project that is named newProjectName",
				"Import project and create for every unknown source schema a new target layer (use if uncertain)",
				"Import project and use specified mapping for source layers and existing target layers. The target layers must be attached to the project! (use with caution)"
		}
)
public class ImportProjectCommand extends SimpleCommand<SimpleResult<Boolean>> {

	protected static final Logger LOGGER = LoggerFactory.getLogger(ImportProjectCommand.class);

	@Option(type = OptionType.COMMAND, name = {"-ipn", "--importProjectName"}, description = "Name of the FirstSpirit target project where the import should go", title = "projectName")
	@Required
	private String _projectName;

	@Option(type = OptionType.COMMAND, name = {"-ipd", "--importProjectDescription"}, description = "Description of the FirstSpirit target project", title = "projectDescription")
	private String _projectDescription;

	@Option(type = OptionType.COMMAND, name = {"-pf", "--projectFile"}, description = "Path to the project export file that should be imported", title = "projectFile")
	@Required
	private String _projectFile;

	@Option(type = OptionType.COMMAND, name = {"-fpa", "--forceProjectActivation"}, description = "Whether to force the project activation if the project is deactivated after import somehow. Default is false.", title = "forceActivation")
	private boolean _forceProjectActivation;

	@Option(type = OptionType.COMMAND, name = {"-dlm", "--databaseLayerMapping"}, description = "Define a map-like layerMapping with comma-separated key-value pairs by : or =; . See command examples.", title = "layerMapping")
	private String _layerMapping;

	@Override
	public SimpleResult<Boolean> call() {
		try (final Connection connection = create()) {
			connection.connect();
			return importProject(connection);
		} catch (final Exception e) {
			return new SimpleResult<>(e);
		}
	}

	@NotNull
	private SimpleResult<Boolean> importProject(@NotNull final Connection connection) throws Exception {
		// verify
		if (_projectFile == null) {
			return new SimpleResult<>(new IllegalArgumentException("Missing parameter for project file"));
		}

		// setup parameters
		final ProjectImportParametersBuilder importParametersBuilder = new ProjectImportParametersBuilder()
				.setProjectName(_projectName)
				.setProjectFile(new File(_projectFile))
				.setProjectDescription(_projectDescription)
				.forceProjectActivation(_forceProjectActivation)
				.setLayerMapping(new StringPropertiesMap(_layerMapping));

		// import project
		final boolean imported = new ProjectImporter().importProject(connection, importParametersBuilder.create());
		// return result
		return new SimpleResult(imported ? true : new IllegalStateException("Import was not successful"));
	}

	@VisibleForTesting
	public void setProjectFile(@NotNull final String projectFile) {
		_projectFile = projectFile;
	}

	@VisibleForTesting
	public void setProjectName(@NotNull final String projectName) {
		_projectName = projectName;
	}

	@VisibleForTesting
	public void setProjectDescription(@NotNull final String projectDescription) {
		_projectDescription = projectDescription;
	}

	@NotNull
	private Connection create() {
		return ConnectionBuilder.with(this).build();
	}

	@Override
	public boolean needsContext() {
		return false;
	}
}
