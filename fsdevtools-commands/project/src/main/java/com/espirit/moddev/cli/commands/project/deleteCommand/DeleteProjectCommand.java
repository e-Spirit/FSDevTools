/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2024 Crownpeak Technology GmbH
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

package com.espirit.moddev.cli.commands.project.deleteCommand;

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

@Command(name = ProjectCommandNames.DELETE, groupNames = ProjectCommandGroup.NAME, description = "Deletes an existing FirstSpirit project from a FirstSpirit Server.")
@Examples(
		examples = {
				"project delete -dpn \"Mithras Energy\""
		},
		descriptions = {"Deletes project \"Mithras Energy\"."
		}
)
public class DeleteProjectCommand extends SimpleCommand<SimpleResult> {
	@Option(type = OptionType.COMMAND, name = {"-dpn", "--deleteProjectName"}, description = "Name of the FirstSpirit project to delete.")
	private String projectName;

	@Override
	public SimpleResult call() {
		try (final Connection connection = createConnection()) {
			connection.connect();
			boolean deleted = new ProjectDeleter().deleteProject(connection, projectName);
			return new SimpleResult(deleted ? deleted : new IllegalStateException("Deletion was not successful."));
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

	@Override
	public boolean needsContext() {
		return false;
	}
}
