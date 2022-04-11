/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2021 e-Spirit GmbH
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

import de.espirit.firstspirit.access.AdminService;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.admin.ProjectStorage;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.access.schedule.ScheduleEntry;
import de.espirit.firstspirit.access.schedule.ScheduleStorage;

import com.espirit.moddev.cli.commands.schedule.utils.ScheduleTestUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import static com.espirit.moddev.cli.commands.schedule.Messages.EXCEPTION_PROJECT_NOT_FOUND;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ScheduleListCommandTest {

	@Test
	public void getServerScheduleEntries() {
		// setup entries
		final ArrayList<ScheduleEntry> list = new ArrayList<>();
		final ScheduleEntry createdEntry = ScheduleTestUtils.createScheduleEntry(1337, "myEntry");
		list.add(createdEntry);
		// setup storage
		final ScheduleStorage scheduleStorage = mock(ScheduleStorage.class);
		when(scheduleStorage.getScheduleEntries(false)).thenReturn(list);
		// setup command
		final ScheduleListCommand command = new ScheduleListCommand();
		// test
		final ScheduleListResult scheduleListResult = command.getServerScheduleEntries(scheduleStorage);
		// verify
		assertNull(scheduleListResult.getProjectName());
		final List<ScheduleEntry> entryList = scheduleListResult.getScheduleEntryList();
		assertEquals(1, entryList.size(), "entry size mismatch");
		final ScheduleEntry entry = entryList.get(0);
		assertEquals(createdEntry.getId(), entry.getId(), "entry id mismatch");
		assertEquals(createdEntry.getName(), entry.getName(), "entry name mismatch");
	}

	@Test
	public void getProjectScheduleEntries() {
		// mock project
		final String projectName = "myProject";
		final Project project = mock(Project.class);
		when(project.getName()).thenReturn(projectName);
		// mock project storage & admin service
		final ProjectStorage projectStorage = mock(ProjectStorage.class);
		when(projectStorage.getProject(projectName)).thenReturn(project);
		final AdminService adminService = mock(AdminService.class);
		when(adminService.getProjectStorage()).thenReturn(projectStorage);
		// mock entries & schedule storage
		final ArrayList<ScheduleEntry> list = new ArrayList<>();
		final ScheduleEntry createdEntry = ScheduleTestUtils.createScheduleEntry(1337, "myEntry");
		list.add(createdEntry);
		final ScheduleStorage scheduleStorage = mock(ScheduleStorage.class);
		when(scheduleStorage.getScheduleEntries(project)).thenReturn(list);

		// setup command
		final ScheduleListCommand command = new ScheduleListCommand();
		command.setProject(projectName);

		// test
		final ScheduleListResult scheduleListResult = command.getProjectScheduleEntries(adminService, scheduleStorage);

		// verify
		assertEquals(projectName, scheduleListResult.getProjectName());
		final List<ScheduleEntry> entryList = scheduleListResult.getScheduleEntryList();
		assertEquals(1, entryList.size(), "entry size mismatch");
		final ScheduleEntry entry = entryList.get(0);
		assertEquals(createdEntry.getId(), entry.getId(), "entry id mismatch");
		assertEquals(createdEntry.getName(), entry.getName(), "entry name mismatch");
	}

	@Test
	public void getProjectScheduleEntries_project_not_found() {
		final String projectName = "myProject";
		// setup mocks
		final ArrayList<ScheduleEntry> list = new ArrayList<>();
		final ScheduleEntry createdEntry = ScheduleTestUtils.createScheduleEntry(1337, "myEntry");
		list.add(createdEntry);

		final ScheduleStorage scheduleStorage = mock(ScheduleStorage.class);
		when(scheduleStorage.getScheduleEntries(false)).thenReturn(list);
		final ProjectStorage projectStorage = mock(ProjectStorage.class);
		final AdminService adminService = mock(AdminService.class);
		when(adminService.getProjectStorage()).thenReturn(projectStorage);

		// setup command
		final ScheduleListCommand command = new ScheduleListCommand();
		command.setProject(projectName);

		// test
		try {
			command.getProjectScheduleEntries(adminService, scheduleStorage);
			failBecauseExceptionWasNotThrown(IllegalStateException.class);
		} catch (final IllegalStateException e) {
			assertTrue(e.getMessage().contains(String.format(EXCEPTION_PROJECT_NOT_FOUND, projectName)));
		}
	}

	@Test
	public void getScheduleEntries_server_side() {
		// setup entries
		final ArrayList<ScheduleEntry> list = new ArrayList<>();
		final ScheduleEntry createdEntry = ScheduleTestUtils.createScheduleEntry(1337, "myEntry");
		list.add(createdEntry);
		// setup storage
		final ScheduleStorage scheduleStorage = mock(ScheduleStorage.class);
		when(scheduleStorage.getScheduleEntries(false)).thenReturn(list);
		// setup adminService & command
		final AdminService adminService = mock(AdminService.class);
		when(adminService.getScheduleStorage()).thenReturn(scheduleStorage);
		final ScheduleListCommand command = new ScheduleListCommand() {
			@NotNull
			@Override
			AdminService getAdminService(@NotNull final Connection connection) {
				return adminService;
			}
		};
		// test
		final ScheduleListResult scheduleListResult = command.getScheduleEntries(mock(Connection.class));
		// verify
		assertNull(scheduleListResult.getProjectName());
		final List<ScheduleEntry> entryList = scheduleListResult.getScheduleEntryList();
		assertEquals(1, entryList.size(), "entry size mismatch");
		final ScheduleEntry entry = entryList.get(0);
		assertEquals(createdEntry.getId(), entry.getId(), "entry id mismatch");
		assertEquals(createdEntry.getName(), entry.getName(), "entry name mismatch");
	}

	@Test
	public void getScheduleEntries_project_bound() {
		// mock project
		final String projectName = "myProject";
		final Project project = mock(Project.class);
		when(project.getName()).thenReturn(projectName);
		// mock project storage & admin service
		final ProjectStorage projectStorage = mock(ProjectStorage.class);
		when(projectStorage.getProject(projectName)).thenReturn(project);
		// mock entries & schedule storage
		final ArrayList<ScheduleEntry> list = new ArrayList<>();
		final ScheduleEntry createdEntry = ScheduleTestUtils.createScheduleEntry(1337, "myEntry");
		list.add(createdEntry);
		final ScheduleStorage scheduleStorage = mock(ScheduleStorage.class);
		when(scheduleStorage.getScheduleEntries(project)).thenReturn(list);
		// setup adminService & command
		final AdminService adminService = mock(AdminService.class);
		when(adminService.getScheduleStorage()).thenReturn(scheduleStorage);
		when(adminService.getProjectStorage()).thenReturn(projectStorage);
		final ScheduleListCommand command = new ScheduleListCommand() {
			@NotNull
			@Override
			AdminService getAdminService(@NotNull final Connection connection) {
				return adminService;
			}
		};
		command.setProject(projectName);
		// test
		final ScheduleListResult scheduleListResult = command.getScheduleEntries(mock(Connection.class));
		// verify
		assertEquals(projectName, scheduleListResult.getProjectName());
		final List<ScheduleEntry> entryList = scheduleListResult.getScheduleEntryList();
		assertEquals(1, entryList.size(), "entry size mismatch");
		final ScheduleEntry entry = entryList.get(0);
		assertEquals(createdEntry.getId(), entry.getId(), "entry id mismatch");
		assertEquals(createdEntry.getName(), entry.getName(), "entry name mismatch");
	}

	@Test
	public void call_exception_handling() {
		// setup
		final ScheduleListCommand command = new ScheduleListCommand() {
			@NotNull
			@Override
			Connection getConnection() {
				throw new UnsupportedOperationException();
			}
		};
		// test
		final ScheduleListResult result = command.call();
		// verify
		assertTrue(result.isError(), "error result expected");
		assertNotNull(result.getError(), "exception expected");
		assertEquals(UnsupportedOperationException.class, result.getError().getClass(), "exception class mismatch");
	}

}
