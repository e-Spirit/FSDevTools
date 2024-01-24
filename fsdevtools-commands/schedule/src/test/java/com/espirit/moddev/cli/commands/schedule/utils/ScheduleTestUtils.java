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

package com.espirit.moddev.cli.commands.schedule.utils;

import com.espirit.moddev.cli.commands.schedule.startCommand.ScheduleStartInformation;
import de.espirit.firstspirit.access.schedule.ScheduleEntry;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ScheduleTestUtils {

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
