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

package com.espirit.moddev.cli.commands.schedule.listCommand;

import com.espirit.moddev.cli.ConnectionBuilder;
import com.espirit.moddev.cli.commands.SimpleCommand;
import com.espirit.moddev.cli.commands.schedule.ScheduleCommandGroup;
import com.espirit.moddev.cli.commands.schedule.ScheduleCommandNames;
import com.espirit.moddev.shared.annotation.VisibleForTesting;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.help.Examples;
import de.espirit.firstspirit.access.AdminService;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.ServicesBroker;
import de.espirit.firstspirit.access.admin.ProjectStorage;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.access.schedule.ScheduleStorage;
import org.jetbrains.annotations.NotNull;

import static com.espirit.moddev.cli.commands.schedule.Messages.EXCEPTION_PROJECT_NOT_FOUND;

@Command(name = ScheduleCommandNames.LIST, groupNames = ScheduleCommandGroup.NAME, description = "Lists all schedules.")
@Examples(
		examples = {
				"schedule list",
				"-p \"Mithras Energy\" schedule list"
		},
		descriptions = {
				"Lists all server side schedule tasks",
				"Lists all schedule tasks of project 'Mithras Energy'"
		}
)
public class ScheduleListCommand extends SimpleCommand<ScheduleListResult> {

	@Override
	public ScheduleListResult call() {
		try (final Connection connection = getConnection()) {
			connection.connect();
			return getScheduleEntries(connection);
		} catch (final Exception e) {
			return new ScheduleListResult(e);
		}
	}

	@VisibleForTesting
	@NotNull
	Connection getConnection() {
		return ConnectionBuilder.with(this).build();
	}

	@VisibleForTesting
	@NotNull
	ScheduleListResult getScheduleEntries(@NotNull final Connection connection) {
		final AdminService adminService = getAdminService(connection);
		final ScheduleStorage scheduleStorage = adminService.getScheduleStorage();
		if (getProject() != null && !getProject().isEmpty()) {
			return getProjectScheduleEntries(adminService, scheduleStorage);
		} else {
			return getServerScheduleEntries(scheduleStorage);
		}
	}

	@VisibleForTesting
	@NotNull
	AdminService getAdminService(@NotNull final Connection connection) {
		return connection.getBroker().requireSpecialist(ServicesBroker.TYPE).getService(AdminService.class);
	}

	@VisibleForTesting
	@NotNull
	ScheduleListResult getServerScheduleEntries(@NotNull final ScheduleStorage scheduleStorage) {
		return new ScheduleListResult(scheduleStorage.getScheduleEntries(false));
	}

	@VisibleForTesting
	@NotNull
	ScheduleListResult getProjectScheduleEntries(@NotNull final AdminService adminService, @NotNull final ScheduleStorage scheduleStorage) {
		final ProjectStorage projectStorage = adminService.getProjectStorage();
		final Project project = projectStorage.getProject(getProject());
		if (project == null) {
			throw new IllegalStateException(String.format(EXCEPTION_PROJECT_NOT_FOUND, getProject()));
		}
		return new ScheduleListResult(project.getName(), scheduleStorage.getScheduleEntries(project));
	}

}
