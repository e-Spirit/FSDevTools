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

import com.espirit.moddev.cli.ConnectionBuilder;
import com.espirit.moddev.cli.commands.SimpleCommand;
import com.espirit.moddev.cli.commands.project.ProjectCommandGroup;
import com.espirit.moddev.cli.commands.project.ProjectCommandNames;
import com.espirit.moddev.cli.results.SimpleResult;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.help.Examples;
import de.espirit.firstspirit.access.Connection;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(name = ProjectCommandNames.EXPORT, groupNames = ProjectCommandGroup.NAME, description = "Exports an existing FirstSpirit project from a FirstSpirit Server.")
@Examples(
		examples = {
				"project export -epn \"Mithras Energy\" -epp \"D:\\my-server-exports\""
		},
		descriptions = {
				"Exports the project \"Mithras Energy\""
		}
)
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
	@Option(type = OptionType.COMMAND, name = {"-mrc", "--maxRevisionCount"}, description = "Maximum number of revisions to export.")
	private long maxRevisionCount = -1L;
	@Option(type = OptionType.COMMAND, name = {"-sde", "--skipDeletedElements"}, description = "Do not add deleted elements to the export.")
	private boolean skipDeletedElements;

	@Override
	public SimpleResult<Boolean> call() {
		try (final Connection connection = createConnection()) {
			connection.connect();

			final ProjectExportParametersBuilder exportParametersBuilder = new ProjectExportParametersBuilder()
					.setProjectName(projectName)
					.setProjectExportPath(projectExportPath)
					.setFsForceProjectActivation(fsForceProjectActivation)
					.setDeleteExportFiles(deleteExportFiles)
					.setMaxRevisionCount(maxRevisionCount);

			if (skipDeletedElements) {
				exportParametersBuilder.skipDeletedElements();
			} else {
				exportParametersBuilder.exportDeletedElements();
			}

			final ProjectExporter projectExporter = new ProjectExporter();
			boolean exported = callExportProject(projectExporter, connection, getProjectExportParameters(exportParametersBuilder));

			return new SimpleResult(exported ? exported : new IllegalStateException("Export was not successful"));
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
	@NotNull
	public Connection createConnection() {
		return ConnectionBuilder.with(this).build();
	}

	/**
	 * Sets the name of the FirstSpirit project to export from.
	 *
	 * @param projectName the name of the FirstSpirit project to export
	 */
	public void setProjectName(@NotNull final String projectName) {
		this.projectName = projectName;
	}

	/**
	 * Sets the maximum number of revisions to export or -1 if all
	 * revisions should be exported.
	 *
	 * @param maxRevisionCount the maximum number of revisions to export
	 */
	public void setMaxRevisionCount(final long maxRevisionCount) {
		this.maxRevisionCount = maxRevisionCount;
	}

	/**
	 * If set to {@code false} if deleted elements should be added to the export or
	 * {@code true} if deleted elements should be skipped.
	 *
	 * @param skipDeletedElements if deleted elements should be added
	 */
	public void setSkipDeletedElements(final boolean skipDeletedElements) {
		this.skipDeletedElements = skipDeletedElements;
	}

	/**
	 * Creates ProjectExportParameters from a builder.
	 *
	 * @param projectExportParametersBuilder Builder for the export
	 * @return Export parameters based on the given builder
	 */
	@NotNull
	public ProjectExportParameters getProjectExportParameters(@NotNull final ProjectExportParametersBuilder projectExportParametersBuilder) {
		return projectExportParametersBuilder.build();
	}

	/**
	 * Exports the project.
	 *
	 * @param projectExporter         Instance of a ProjectExporter.
	 * @param connection              Connection to the FirstSpirit server.
	 * @param projectExportParameters Parameters of the project which is going to be exported.
	 * @return Whether the export was successful or not.
	 */
	protected boolean callExportProject(@NotNull final ProjectExporter projectExporter, @NotNull final Connection connection, @NotNull final ProjectExportParameters projectExportParameters) {
		return projectExporter.exportProject(connection, projectExportParameters);
	}

	@Override
	public boolean needsContext() {
		return false;
	}
}
