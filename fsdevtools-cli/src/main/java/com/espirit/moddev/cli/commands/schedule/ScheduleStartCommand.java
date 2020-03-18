package com.espirit.moddev.cli.commands.schedule;

import com.espirit.moddev.cli.ConnectionBuilder;
import com.espirit.moddev.cli.commands.SimpleCommand;
import com.espirit.moddev.shared.annotation.VisibleForTesting;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.help.Examples;
import com.github.rvesse.airline.annotations.restrictions.Required;
import de.espirit.firstspirit.access.AdminService;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.ServicesBroker;
import de.espirit.firstspirit.access.admin.ProjectStorage;
import de.espirit.firstspirit.access.schedule.ScheduleEntry;
import de.espirit.firstspirit.access.schedule.ScheduleEntryControl;
import de.espirit.firstspirit.access.schedule.ScheduleEntryRunningException;
import de.espirit.firstspirit.access.schedule.ScheduleStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Command(name = "start", groupNames = "schedule", description = "Starts a schedule task with the name.")
@Examples(examples = {
		"schedule start -n \"Clean up logs\"",
		"schedule -p \"Mitras Energy\" start -n \"generate full\""
},
		descriptions = {
				"Starts a task from schedule with the name 'ScheduleName' on the Server"
		})
public class ScheduleStartCommand extends SimpleCommand<ScheduleStartResult> {

	protected static final Logger LOGGER = LoggerFactory.getLogger(ScheduleStartCommand.class);

	@Option(type = OptionType.COMMAND, name = {"-n", "--name"}, description = "Name of the target schedule task")
	@Required
	private String _scheduleName;


	@Override
	public ScheduleStartResult call() {
		try (final Connection connection = getConnection()) {
			connection.connect();
			return getScheduleStartResult(connection);
		} catch (final Exception e) {
			return new ScheduleStartResult(e);
		}
	}


	@VisibleForTesting
	@NotNull
	ScheduleStartResult getScheduleStartResult(final Connection connection) throws ScheduleEntryRunningException, InterruptedException {
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
		return connection.getBroker().requestSpecialist(ServicesBroker.TYPE).getService(AdminService.class);
	}


	@VisibleForTesting
	@NotNull
	ScheduleStartResult getServerScheduleEntries(@NotNull final ScheduleStorage scheduleStorage) throws ScheduleEntryRunningException, InterruptedException {
		List<ScheduleEntry> scheduleEntryList = scheduleStorage.getScheduleEntries(false);
		final ScheduleEntry scheduleEntry = getScheduleEntry(scheduleEntryList);
		final ScheduleStartInformation scheduleStartInformation = executeSchedule(scheduleEntry);
		return new ScheduleStartResult(scheduleStartInformation);
	}


	@VisibleForTesting
	@NotNull
	ScheduleStartResult getProjectScheduleEntries(@NotNull final AdminService adminService, @NotNull final ScheduleStorage scheduleStorage) throws ScheduleEntryRunningException, InterruptedException {
		final ScheduleEntry scheduleEntry = getProjectScheduleEntry(adminService, scheduleStorage);
		final ScheduleStartInformation scheduleStartInformation = executeSchedule(scheduleEntry);
		return new ScheduleStartResult(getProject(), scheduleStartInformation);
	}


	@VisibleForTesting
	@Nullable
	ScheduleEntry getProjectScheduleEntry(@NotNull final AdminService adminService, @NotNull final ScheduleStorage scheduleStorage) {
		final ProjectStorage projectStorage = adminService.getProjectStorage();
		if (projectStorage.getProject(getProject()) == null) {
			throw new IllegalStateException("Project named '" + getProject() + "' does not exist!");
		}
		LOGGER.info("schedule name: " + _scheduleName + ", project name: " + getProject());
		final List<ScheduleEntry> scheduleEntryList;
		scheduleEntryList = scheduleStorage.getScheduleEntries(projectStorage.getProject(getProject()));
		return getScheduleEntry(scheduleEntryList);
	}


	@VisibleForTesting
	@NotNull
	ScheduleStartInformation executeSchedule(@Nullable final ScheduleEntry scheduleEntry) throws ScheduleEntryRunningException, IllegalStateException, InterruptedException {
		if (scheduleEntry != null) {
			if (!scheduleEntry.getRunningEntries().isEmpty()) {
				throw new IllegalStateException("Schedule task is already running!");
			}
			final Date startTime = new Date();
			ScheduleEntryControl scheduleEntryControl = scheduleEntry.execute();
			int counter = 0;
			while (scheduleEntryControl.isRunning()) {
				Thread.sleep(100);
				scheduleEntryControl.refresh();
				if (counter % 50 == 0) {
					LOGGER.info("Schedule task '" + scheduleEntry.getName() + "' is still running. Please wait...");
				}
				counter++;
			}
			return new ScheduleStartInformation(scheduleEntry, startTime, new Date());
		} else {
			throw new IllegalStateException("Schedule task named '" + _scheduleName + "' does not exist!");
		}
	}


	@VisibleForTesting
	@Nullable
	ScheduleEntry getScheduleEntry(@NotNull final List<ScheduleEntry> scheduleEntryList) {
		for (ScheduleEntry scheduleEntry : scheduleEntryList) {
			if (scheduleEntry.getName().equals(_scheduleName)) {
				return scheduleEntry;
			}
		}
		return null;
	}


	@VisibleForTesting
	@NotNull
	Connection getConnection() {
		return ConnectionBuilder.with(this).build();
	}

	@VisibleForTesting
	void setScheduleName(final String scheduleName){
		_scheduleName = scheduleName;
	}
}
