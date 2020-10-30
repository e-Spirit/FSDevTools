/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2020 e-Spirit AG
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
