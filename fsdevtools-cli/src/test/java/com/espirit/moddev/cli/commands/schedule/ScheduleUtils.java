package com.espirit.moddev.cli.commands.schedule;

import de.espirit.firstspirit.access.schedule.ScheduleEntry;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ScheduleUtils {

	@NotNull
	public static ScheduleEntry createScheduleEntry(final long id, @NotNull final String name) {
		final ScheduleEntry scheduleEntry = mock(ScheduleEntry.class);
		when(scheduleEntry.getId()).thenReturn(id);
		when(scheduleEntry.getName()).thenReturn(name);
		return scheduleEntry;
	}

	@NotNull
	public static ScheduleStartInformation createScheduleStartInformation(final ScheduleEntry entry) {
		Date startTime = new Date();
		Date finishTime = new Date(startTime.getTime() + 100);
		return new ScheduleStartInformation(entry, startTime, finishTime);
	}

}
