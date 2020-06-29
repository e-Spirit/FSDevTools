package com.espirit.moddev.cli.commands.schedule;

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
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class ScheduleStartCommandTest {

	@Test
	public void getServerScheduleEntries() throws ScheduleEntryRunningException, InterruptedException {
		//setup entries
		final ArrayList<ScheduleEntry> list = new ArrayList<>();
		final String scheduleName = "test task";
		final ScheduleEntry createdEntry = ScheduleUtils.createScheduleEntry(1, scheduleName);
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
		assertNull("project must be null", scheduleStartResult.getProjectName());
		assertEquals("entry id mismatch", createdEntry.getId(), scheduleStartResult.getScheduleStartInformation().getScheduleEntry().getId());
		assertEquals("entry name mismatch", createdEntry.getName(), scheduleStartResult.getScheduleStartInformation().getScheduleEntry().getName());
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
		final ScheduleEntry createdEntry = ScheduleUtils.createScheduleEntry(1, scheduleName);
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
		assertEquals("project name mismatch", projectName, scheduleStartResult.getProjectName());
		assertEquals("entry id mismatch", createdEntry.getId(), scheduleStartResult.getScheduleStartInformation().getScheduleEntry().getId());
		assertEquals("entry name mismatch", createdEntry.getName(), scheduleStartResult.getScheduleStartInformation().getScheduleEntry().getName());
	}

	@Test
	public void getScheduleStartResult_server_side() throws ScheduleEntryRunningException, InterruptedException {
		// setup entries
		final ArrayList<ScheduleEntry> list = new ArrayList<>();
		final String scheduleName = "test Task";
		final ScheduleEntry createdEntry = ScheduleUtils.createScheduleEntry(1, scheduleName);
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
		assertNull("project must be null", scheduleStartResult.getProjectName());
		final ScheduleEntry entry = scheduleStartResult.getScheduleStartInformation().getScheduleEntry();
		assertEquals("entry id mismatch", createdEntry.getId(), entry.getId());
		assertEquals("entry name mismatch", createdEntry.getName(), entry.getName());
	}

	@Test
	public void getScheduleStartResult_project_bound() throws ScheduleEntryRunningException, InterruptedException {
		// setup entries
		final ArrayList<ScheduleEntry> list = new ArrayList<>();
		final String scheduleName = "test Task";
		final ScheduleEntry createdEntry = ScheduleUtils.createScheduleEntry(1, scheduleName);
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
		assertEquals("project name mismatch", projectName, scheduleStartResult.getProjectName());
		final ScheduleEntry entry = scheduleStartResult.getScheduleStartInformation().getScheduleEntry();
		assertEquals("entry id mismatch", createdEntry.getId(), entry.getId());
		assertEquals("entry name mismatch", createdEntry.getName(), entry.getName());
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
		final ScheduleEntry createdEntry = ScheduleUtils.createScheduleEntry(1, scheduleName);
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
		assertEquals("entry id mismatch", createdEntry.getId(), testEntry.getId());
		assertEquals("entry name mismatch", createdEntry.getName(), testEntry.getName());
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
		final ScheduleEntry createdEntry = ScheduleUtils.createScheduleEntry(1, scheduleName);
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
			assertTrue("exception message mismatch", e.getMessage().contains(String.format(ScheduleListCommand.MESSAGE_EXCEPTION_PROJECT_NOT_FOUND, projectName)));
		}
	}

	@Test
	public void getScheduleEntry() {
		//setup
		ArrayList<ScheduleEntry> entryList = new ArrayList<>();
		final String scheduleName = "test Task";
		final ScheduleEntry createdEntry = ScheduleUtils.createScheduleEntry(1, scheduleName);
		entryList.add(createdEntry);
		final ScheduleStartCommand scheduleStartCommand = new ScheduleStartCommand();
		scheduleStartCommand.setScheduleName(scheduleName);
		//test
		final ScheduleEntry testEntry = scheduleStartCommand.getScheduleEntry(entryList);
		//verify
		assertEquals("entry id mismatch", createdEntry.getId(), testEntry.getId());
		assertEquals("entry name mismatch", createdEntry.getName(), testEntry.getName());
	}

	@Test
	public void executeSchedule() throws ScheduleEntryRunningException, InterruptedException {
		//setup
		final String scheduleName = "test Task";
		final ScheduleEntry createdEntry = ScheduleUtils.createScheduleEntry(1, scheduleName);
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
		assertEquals("entry id mismatch", createdEntry.getId(), testInformation.getScheduleEntry().getId());
		assertEquals("entry name mismatch", createdEntry.getName(), testInformation.getScheduleEntry().getName());

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
		assertTrue("error result expected", result.isError());
		assertNotNull("exception expected", result.getError());
		assertEquals("exception class mismatch", UnsupportedOperationException.class, result.getError().getClass());
	}
}