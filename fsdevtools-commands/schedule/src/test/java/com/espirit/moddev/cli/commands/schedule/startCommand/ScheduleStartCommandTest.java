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

package com.espirit.moddev.cli.commands.schedule.startCommand;

import com.espirit.moddev.cli.commands.schedule.utils.ScheduleTestUtils;
import de.espirit.firstspirit.access.AdminService;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.admin.ProjectStorage;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.access.schedule.RunState;
import de.espirit.firstspirit.access.schedule.ScheduleEntry;
import de.espirit.firstspirit.access.schedule.ScheduleEntryControl;
import de.espirit.firstspirit.access.schedule.ScheduleEntryRunningException;
import de.espirit.firstspirit.access.schedule.ScheduleEntryState;
import de.espirit.firstspirit.access.schedule.ScheduleStorage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;

import static com.espirit.moddev.cli.commands.schedule.Messages.EXCEPTION_PROJECT_NOT_FOUND;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ScheduleStartCommandTest {

	@Test
	public void getServerScheduleEntries() throws ScheduleEntryRunningException, InterruptedException {
		//setup entries
		final ArrayList<ScheduleEntry> list = new ArrayList<>();
		final String scheduleName = "test task";
		final ScheduleEntry createdEntry = ScheduleTestUtils.createScheduleEntry(1, scheduleName);
		list.add(createdEntry);
		ScheduleEntryControl scheduleEntryControl = mock(ScheduleEntryControl.class);
		when(scheduleEntryControl.isRunning()).thenReturn(false);
		when(scheduleEntryControl.getStartTime()).thenReturn(new Date());
		when(scheduleEntryControl.getFinishTime()).thenReturn(new Date(new Date().getTime() + 1000));
		when(createdEntry.execute()).thenReturn(scheduleEntryControl);
		//setup storage
		final ScheduleStorage scheduleStorage = mock(ScheduleStorage.class);
		when(scheduleStorage.getScheduleEntries(false)).thenReturn(list);
		//setup Command
		final ScheduleStartCommand scheduleStartCommand = new ScheduleStartCommand();
		scheduleStartCommand.setScheduleName(scheduleName);
		// setup state check
		final ScheduleEntryState taskState = mock(ScheduleEntryState.class);
		when(taskState.getState()).thenReturn(RunState.SUCCESS);
		when(scheduleEntryControl.getState()).thenReturn(taskState);
		when(createdEntry.execute()).thenReturn(scheduleEntryControl);
		//test
		final ScheduleStartResult scheduleStartResult = scheduleStartCommand.getServerScheduleEntries(scheduleStorage);
		//verify
		assertNull(scheduleStartResult.getProjectName(), "project must be null");
		assertEquals(createdEntry.getId(), scheduleStartResult.getScheduleStartInformation().getScheduleEntry().getId(), "entry id mismatch");
		assertEquals(createdEntry.getName(), scheduleStartResult.getScheduleStartInformation().getScheduleEntry().getName(), "entry name mismatch");
	}

	@Test
	public void getProjectScheduleEntries() throws ScheduleEntryRunningException, InterruptedException {
		// mock project
		final String projectName = "myProject";
		final Project project = mock(Project.class);
		when(project.getName()).thenReturn(projectName);
		//setup entries
		final ArrayList<ScheduleEntry> list = new ArrayList<>();
		final String scheduleName = "test task";
		final ScheduleEntry createdEntry = ScheduleTestUtils.createScheduleEntry(1, scheduleName);
		list.add(createdEntry);
		ScheduleEntryControl scheduleEntryControl = mock(ScheduleEntryControl.class);
		when(scheduleEntryControl.isRunning()).thenReturn(false);
		when(scheduleEntryControl.getStartTime()).thenReturn(new Date());
		when(scheduleEntryControl.getFinishTime()).thenReturn(new Date(new Date().getTime() + 1000));
		when(createdEntry.execute()).thenReturn(scheduleEntryControl);
		//setup admin service & storage
		final ScheduleStorage scheduleStorage = mock(ScheduleStorage.class);
		when(scheduleStorage.getScheduleEntries(project)).thenReturn(list);
		final ProjectStorage projectStorage = mock(ProjectStorage.class);
		when(projectStorage.getProject(projectName)).thenReturn(project);
		final AdminService adminService = mock(AdminService.class);
		when(adminService.getProjectStorage()).thenReturn(projectStorage);
		//setup Command
		final ScheduleStartCommand scheduleStartCommand = new ScheduleStartCommand();
		scheduleStartCommand.setProject(projectName);
		scheduleStartCommand.setScheduleName(scheduleName);
		// setup state check
		final ScheduleEntryState taskState = mock(ScheduleEntryState.class);
		when(taskState.getState()).thenReturn(RunState.SUCCESS);
		when(scheduleEntryControl.getState()).thenReturn(taskState);
		when(createdEntry.execute()).thenReturn(scheduleEntryControl);
		//test
		final ScheduleStartResult scheduleStartResult = scheduleStartCommand.getProjectScheduleEntries(adminService, scheduleStorage);
		//verify
		assertEquals(projectName, scheduleStartResult.getProjectName(), "project name mismatch");
		assertEquals(createdEntry.getId(), scheduleStartResult.getScheduleStartInformation().getScheduleEntry().getId(), "entry id mismatch");
		assertEquals(createdEntry.getName(), scheduleStartResult.getScheduleStartInformation().getScheduleEntry().getName(), "entry name mismatch");
	}

	@Test
	public void getScheduleStartResult_server_side() throws ScheduleEntryRunningException, InterruptedException {
		// setup entries
		final ArrayList<ScheduleEntry> list = new ArrayList<>();
		final String scheduleName = "test Task";
		final ScheduleEntry createdEntry = ScheduleTestUtils.createScheduleEntry(1, scheduleName);
		ScheduleEntryControl scheduleEntryControl = mock(ScheduleEntryControl.class);
		when(scheduleEntryControl.isRunning()).thenReturn(false);
		when(scheduleEntryControl.getStartTime()).thenReturn(new Date());
		when(scheduleEntryControl.getFinishTime()).thenReturn(new Date(new Date().getTime() + 1000));
		when(createdEntry.execute()).thenReturn(scheduleEntryControl);
		list.add(createdEntry);
		// setup storage
		final ScheduleStorage scheduleStorage = mock(ScheduleStorage.class);
		when(scheduleStorage.getScheduleEntries(false)).thenReturn(list);
		// setup adminService & command
		final AdminService adminService = mock(AdminService.class);
		when(adminService.getScheduleStorage()).thenReturn(scheduleStorage);
		final ScheduleStartCommand scheduleStartCommand = new ScheduleStartCommand() {
			@NotNull
			@Override
			AdminService getAdminService(@NotNull final Connection connection) {
				return adminService;
			}
		};
		scheduleStartCommand.setScheduleName(scheduleName);
		// setup state check
		final ScheduleEntryState taskState = mock(ScheduleEntryState.class);
		when(taskState.getState()).thenReturn(RunState.SUCCESS);
		when(scheduleEntryControl.getState()).thenReturn(taskState);
		when(createdEntry.execute()).thenReturn(scheduleEntryControl);
		// test
		final ScheduleStartResult scheduleStartResult = scheduleStartCommand.getScheduleStartResult(mock(Connection.class));
		// verify
		assertNull(scheduleStartResult.getProjectName(), "project must be null");
		final ScheduleEntry entry = scheduleStartResult.getScheduleStartInformation().getScheduleEntry();
		assertEquals(createdEntry.getId(), entry.getId(), "entry id mismatch");
		assertEquals(createdEntry.getName(), entry.getName(), "entry name mismatch");
	}

	@Test
	public void getScheduleStartResult_project_bound() throws ScheduleEntryRunningException, InterruptedException {
		// setup entries
		final ArrayList<ScheduleEntry> list = new ArrayList<>();
		final String scheduleName = "test Task";
		final ScheduleEntry createdEntry = ScheduleTestUtils.createScheduleEntry(1, scheduleName);
		ScheduleEntryControl scheduleEntryControl = mock(ScheduleEntryControl.class);
		when(scheduleEntryControl.isRunning()).thenReturn(false);
		when(scheduleEntryControl.getStartTime()).thenReturn(new Date());
		when(scheduleEntryControl.getFinishTime()).thenReturn(new Date(new Date().getTime() + 1000));
		when(createdEntry.execute()).thenReturn(scheduleEntryControl);
		list.add(createdEntry);
		//setup project
		final String projectName = "test Project";
		final Project project = mock(Project.class);
		when(project.getName()).thenReturn(projectName);
		final ProjectStorage projectStorage = mock(ProjectStorage.class);
		when(projectStorage.getProject(projectName)).thenReturn(project);
		// setup storage
		final ScheduleStorage scheduleStorage = mock(ScheduleStorage.class);
		when(scheduleStorage.getScheduleEntries(project)).thenReturn(list);
		// setup adminService & command
		final AdminService adminService = mock(AdminService.class);
		when(adminService.getScheduleStorage()).thenReturn(scheduleStorage);
		when(adminService.getProjectStorage()).thenReturn(projectStorage);
		final ScheduleStartCommand scheduleStartCommand = new ScheduleStartCommand() {
			@NotNull
			@Override
			AdminService getAdminService(@NotNull final Connection connection) {
				return adminService;
			}
		};
		scheduleStartCommand.setScheduleName(scheduleName);
		scheduleStartCommand.setProject(projectName);
		// setup state check
		final ScheduleEntryState taskState = mock(ScheduleEntryState.class);
		when(taskState.getState()).thenReturn(RunState.SUCCESS);
		when(scheduleEntryControl.getState()).thenReturn(taskState);
		when(createdEntry.execute()).thenReturn(scheduleEntryControl);
		// test
		final ScheduleStartResult scheduleStartResult = scheduleStartCommand.getScheduleStartResult(mock(Connection.class));
		// verify
		assertEquals(projectName, scheduleStartResult.getProjectName(), "project name mismatch");
		final ScheduleEntry entry = scheduleStartResult.getScheduleStartInformation().getScheduleEntry();
		assertEquals(createdEntry.getId(), entry.getId(), "entry id mismatch");
		assertEquals(createdEntry.getName(), entry.getName(), "entry name mismatch");
	}

	@Test
	public void getProjectScheduleEntry() {
		//setup
		// mock project
		final String projectName = "myProject";
		final Project project = mock(Project.class);
		when(project.getName()).thenReturn(projectName);
		//setup entries
		final ArrayList<ScheduleEntry> list = new ArrayList<>();
		final String scheduleName = "test task";
		final ScheduleEntry createdEntry = ScheduleTestUtils.createScheduleEntry(1, scheduleName);
		list.add(createdEntry);
		//setup admin service & storage
		final ScheduleStorage scheduleStorage = mock(ScheduleStorage.class);
		when(scheduleStorage.getScheduleEntries(project)).thenReturn(list);
		final ProjectStorage projectStorage = mock(ProjectStorage.class);
		when(projectStorage.getProject(projectName)).thenReturn(project);
		final AdminService adminService = mock(AdminService.class);
		when(adminService.getProjectStorage()).thenReturn(projectStorage);
		//setup Command
		final ScheduleStartCommand scheduleStartCommand = new ScheduleStartCommand();
		scheduleStartCommand.setProject(projectName);
		scheduleStartCommand.setScheduleName(scheduleName);
		//test
		final ScheduleEntry testEntry = scheduleStartCommand.getProjectScheduleEntry(adminService, scheduleStorage);
		//verify
		assertEquals(createdEntry.getId(), testEntry.getId(), "entry id mismatch");
		assertEquals(createdEntry.getName(), testEntry.getName(), "entry name mismatch");
	}

	@Test
	public void getProjectScheduleEntry_project_not_found() {
		//setup
		// mock project
		final String projectName = "myProject";
		final Project project = mock(Project.class);
		when(project.getName()).thenReturn(projectName);
		//setup entries
		final ArrayList<ScheduleEntry> list = new ArrayList<>();
		final String scheduleName = "test task";
		final ScheduleEntry createdEntry = ScheduleTestUtils.createScheduleEntry(1, scheduleName);
		list.add(createdEntry);
		//setup admin service & storage
		final ScheduleStorage scheduleStorage = mock(ScheduleStorage.class);
		when(scheduleStorage.getScheduleEntries(project)).thenReturn(list);
		final ProjectStorage projectStorage = mock(ProjectStorage.class);
		when(projectStorage.getProject(projectName)).thenReturn(null);
		final AdminService adminService = mock(AdminService.class);
		when(adminService.getProjectStorage()).thenReturn(projectStorage);
		//setup Command
		final ScheduleStartCommand scheduleStartCommand = new ScheduleStartCommand();
		scheduleStartCommand.setProject(projectName);
		scheduleStartCommand.setScheduleName(scheduleName);
		//test
		try {
			final ScheduleEntry testEntry = scheduleStartCommand.getProjectScheduleEntry(adminService, scheduleStorage);
			failBecauseExceptionWasNotThrown(IllegalStateException.class);
		} catch (final IllegalStateException e) {
			assertTrue(e.getMessage().contains(String.format(EXCEPTION_PROJECT_NOT_FOUND, projectName)), "exception message mismatch");
		}
	}

	@Test
	public void getScheduleEntry() {
		//setup
		ArrayList<ScheduleEntry> entryList = new ArrayList<>();
		final String scheduleName = "test Task";
		final ScheduleEntry createdEntry = ScheduleTestUtils.createScheduleEntry(1, scheduleName);
		entryList.add(createdEntry);
		final ScheduleStartCommand scheduleStartCommand = new ScheduleStartCommand();
		scheduleStartCommand.setScheduleName(scheduleName);
		//test
		final ScheduleEntry testEntry = scheduleStartCommand.getScheduleEntry(entryList);
		//verify
		assertEquals(createdEntry.getId(), testEntry.getId(), "entry id mismatch");
		assertEquals(createdEntry.getName(), testEntry.getName(), "entry name mismatch");
	}

	@Test
	public void executeSchedule() throws ScheduleEntryRunningException, InterruptedException {
		//setup
		final String scheduleName = "test Task";
		final ScheduleEntry createdEntry = ScheduleTestUtils.createScheduleEntry(1, scheduleName);
		ScheduleEntryControl scheduleEntryControl = mock(ScheduleEntryControl.class);
		when(scheduleEntryControl.isRunning()).thenReturn(false);
		final ScheduleEntryState taskState = mock(ScheduleEntryState.class);
		when(taskState.getState()).thenReturn(RunState.SUCCESS);
		when(scheduleEntryControl.getState()).thenReturn(taskState);
		when(createdEntry.execute()).thenReturn(scheduleEntryControl);
		final ScheduleStartCommand scheduleStartCommand = new ScheduleStartCommand();
		//test
		final ScheduleStartInformation testInformation = scheduleStartCommand.executeSchedule(createdEntry);
		//verify
		assertEquals(testInformation.getScheduleEntry().getId(), createdEntry.getId(), "entry id mismatch");
		assertEquals(createdEntry.getName(), testInformation.getScheduleEntry().getName(), "entry name mismatch");

	}

	@Test
	public void call_exception_handling() {
		// setup
		final ScheduleStartCommand command = new ScheduleStartCommand() {
			@NotNull
			@Override
			Connection getConnection() {
				throw new UnsupportedOperationException();
			}
		};
		// test
		final ScheduleStartResult result = command.call();
		// verify
		assertTrue(result.isError(), "error result expected");
		assertNotNull(result.getError(), "exception expected");
		assertEquals(UnsupportedOperationException.class, result.getError().getClass(), "exception class mismatch");
	}
}
