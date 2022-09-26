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

package com.espirit.moddev.cli.commands.project.deleteCommand;

import com.espirit.moddev.shared.annotation.VisibleForTesting;
import de.espirit.firstspirit.access.AdminService;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.ServicesBroker;
import de.espirit.firstspirit.access.admin.ProjectStorage;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.access.script.ExecutionException;
import de.espirit.firstspirit.access.store.LockException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that can delete a given FirstSpirit project from a server.
 */
public class ProjectDeleter {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProjectDeleter.class);
	private static final String EXCEPTIONSTRING = "Project cannot be deleted! ";

	/**
	 * This methods deletes a FirstSpirit project from a server
	 *
	 * @param connection  to the FirstSpirit server
	 * @param projectName of the project you want to delete
	 * @return true if the project was deleted successfully, false otherwise.
	 * @throws ExecutionException If a project with the given name does not exist on the server or it does exists, but cannot be locked.
	 */
	public boolean deleteProject(Connection connection, String projectName) {
		LOGGER.info("Start deleting project: '{}'", projectName);
		if (connection == null) {
			LOGGER.error("Connection is not set!");
			throw new IllegalArgumentException("Connection is null.");
		}

		Project project = connection.getProjectByName(projectName);
		if (project == null) {
			LOGGER.error("Cannot find project!");
			throw new ExecutionException(EXCEPTIONSTRING + projectName + " does not exist on server.");
		}

		LOGGER.debug("Lock project");
		try {
			project.lock();
			LOGGER.debug("Project is locked.");
		} catch (LockException e) {
			LOGGER.error("Cannot lock project. ", e);
			throw new ExecutionException(EXCEPTIONSTRING + projectName + " could not be locked.");
		}

		ProjectStorage projectStorage = returnProjectStorage(connection, project);
		if (projectStorage == null) {
			LOGGER.error("Cannot process deletion. Preparation failed.");
			throw new ExecutionException(EXCEPTIONSTRING + "ProjectStorage is missing.");
		}
		return performDeletion(project, projectStorage);
	}

	@VisibleForTesting
	ProjectStorage returnProjectStorage(Connection connection, Project project) {
		if (connection == null || !canAccessProject(connection, project)) {
			LOGGER.info("Cannot access project.");
			return null;
		}

		ProjectStorage projectStorage = connection.getBroker().requireSpecialist(ServicesBroker.TYPE).getService(AdminService.class).getProjectStorage();
		if (projectStorage == null) {
			LOGGER.info("Cannot access project storage.");
		}
		return projectStorage;
	}

	@SuppressWarnings("squid:S2221")
	private static boolean performDeletion(Project project, ProjectStorage projectStorage) {
		try {
			LOGGER.debug("Deactivate project.");
			projectStorage.deactivateProject(project);
			LOGGER.debug("Project was locked and deactivated.");
			LOGGER.debug("Remove Project.");
			projectStorage.refreshProjects();
			projectStorage.removeProject(project);
			projectStorage.refreshProjects();
		} catch (Exception e) {
			LOGGER.error("Cannot delete project!", e);
			return false;
		}
		LOGGER.info("Successfully deleted project from server!");
		return true;
	}

	private static boolean canAccessProject(Connection connection, Project project) {
		if (connection.getProjects().length < 1 || project == null) {
			LOGGER.info("Cannot find project on server.");
			return false;
		}
		return true;
	}
}
