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

import de.espirit.firstspirit.access.schedule.ScheduleEntry;

import com.espirit.moddev.cli.commands.schedule.utils.ScheduleTestUtils;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ScheduleStartInformationTest {

	@Test
	public void constructor() {
		//setup
		final ScheduleEntry entry = ScheduleTestUtils.createScheduleEntry(1, "test task");
		final Date start = new Date();
		final Date finish = new Date(start.getTime() + 100);
		//test
		final ScheduleStartInformation scheduleStartInformation = new ScheduleStartInformation(entry, start, finish);
		//verify
		assertEquals(entry, scheduleStartInformation.getScheduleEntry(), "entry mismatch");
		assertEquals(start, scheduleStartInformation.getStartTime(), "start time mismatch");
		assertEquals(finish, scheduleStartInformation.getFinishTime(), "finish time mismatch");
		assertEquals(100, scheduleStartInformation.getDuration(), "duration mismatch");
	}

}
