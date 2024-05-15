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

package com.espirit.moddev.cli.commands.feature.common;

import com.espirit.moddev.cli.ConnectionBuilder;
import com.espirit.moddev.cli.api.configuration.Config;
import com.espirit.moddev.cli.commands.SimpleCommand;
import com.espirit.moddev.cli.results.SimpleResult;
import com.espirit.moddev.shared.StringUtils;
import org.jetbrains.annotations.VisibleForTesting;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.project.Project;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common parent for all the feature related commands.
 */
public abstract class AbstractFeatureCommand extends SimpleCommand<SimpleResult<Boolean>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractFeatureCommand.class);

	@Override
	public boolean needsContext() {
		// do not create context with an additional FS connection
		return false;
	}

	/**
	 * Contains boilerplate logic for instantiating a connection,
	 * retrieving a project and then delegates to {@link #execute(Connection, Project)}.
	 *
	 * @return {@link SimpleResult} wrapping {@link Boolean#TRUE} in case of a success
	 * and {@link SimpleResult} wrapping an {@link Exception} in case of an error.
	 */
	@Override
	@NotNull
	public SimpleResult<Boolean> call() {
		try (final Connection connection = getConnection()) {
			connection.connect();
			final Project project = getFirstSpiritProject(connection);
			execute(connection, project);
			return new SimpleResult<>(Boolean.TRUE);
		} catch (final Exception e) {
			return new SimpleResult<>(e);
		}
	}

	/**
	 * Feature command should implement its specific logic here.
	 *
	 * @param connection {@link Connection} to the FirstSpirit server.
	 * @param project    {@link Project}.
	 * @throws Exception when the logic needs to signal that command failed.
	 */
	protected abstract void execute(@NotNull final Connection connection, @NotNull final Project project) throws Exception;

	/**
	 * Verifies {@link Project} with the given name
	 * exists on the FirstSpirit server and returns it.
	 *
	 * @param connection {@link Connection} to the FirstSpirit server.
	 * @return {@link Project} if it exists on the server.
	 * @throws IllegalStateException if project name is not specified
	 *                               or {@link Project} with the given name
	 *                               does not exist on the FirstSpirit server.
	 */
	@VisibleForTesting
	@NotNull
	Project getFirstSpiritProject(@NotNull final Connection connection) {
		final String projectName = getProject();
		if (StringUtils.isNullOrEmpty(projectName)) {
			throw new IllegalStateException("Project is not specified");
		}
		final Project project = connection.getProjectByName(projectName);
		if (project == null) {
			throw new IllegalStateException(String.format("Could not find project '%s' on the server (typo in the project name?)", projectName));
		}
		LOGGER.debug("Retrieved project with name '{}' and ID '{}')", project.getName(), project.getId());
		return project;
	}

	/**
	 * Delegates to {@link ConnectionBuilder#build()}.
	 * The {@link ConnectionBuilder} is provided by {@link #getConnectionBuilder()}.
	 *
	 * @return {@link Connection} to FirstSpirit server.
	 */
	@VisibleForTesting
	@NotNull
	Connection getConnection() {
		return getConnectionBuilder().build();
	}

	/**
	 * Delegates to {@link com.espirit.moddev.cli.ConnectionBuilder#with(Config)}.
	 *
	 * @return {@link ConnectionBuilder} with {@code this} as {@link Config} parameter.
	 */
	@VisibleForTesting
	@NotNull
	ConnectionBuilder getConnectionBuilder() {
		return ConnectionBuilder.with(this);
	}
}
