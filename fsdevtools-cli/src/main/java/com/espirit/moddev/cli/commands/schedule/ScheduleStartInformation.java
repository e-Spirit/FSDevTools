package com.espirit.moddev.cli.commands.schedule;

import de.espirit.firstspirit.access.schedule.ScheduleEntry;

import org.jetbrains.annotations.NotNull;

import java.util.Date;


public class ScheduleStartInformation {

	private final ScheduleEntry _scheduleEntry;
	private final Date _startTime;
	private final Date _finishTime;
	private final long _duration;


	public ScheduleStartInformation(@NotNull final ScheduleEntry scheduleEntry, @NotNull final Date startTime, @NotNull final Date finishTime) {
		_scheduleEntry = scheduleEntry;
		_startTime = startTime;
		_finishTime = finishTime;
		_duration = _finishTime.getTime() - _startTime.getTime();
	}


	@NotNull
	public ScheduleEntry getScheduleEntry() {
		return _scheduleEntry;
	}


	@NotNull
	public Date getStartTime() {
		return _startTime;
	}


	@NotNull
	public Date getFinishTime() {
		return _finishTime;
	}


	public long getDuration() {
		return _duration;
	}
}
