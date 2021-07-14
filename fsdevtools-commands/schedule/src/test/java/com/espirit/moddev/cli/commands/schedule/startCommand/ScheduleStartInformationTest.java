/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2021 e-Spirit AG
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
import de.espirit.firstspirit.access.schedule.ScheduleEntry;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;


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
		assertEquals("entry mismatch", entry, scheduleStartInformation.getScheduleEntry());
		assertEquals("start time mismatch", start, scheduleStartInformation.getStartTime());
		assertEquals("finish time mismatch", finish, scheduleStartInformation.getFinishTime());
		assertEquals("duration mismatch", 100, scheduleStartInformation.getDuration());
	}

}
