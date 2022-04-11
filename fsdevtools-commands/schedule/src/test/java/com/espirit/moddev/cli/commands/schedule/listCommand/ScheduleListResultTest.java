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

import com.espirit.moddev.cli.commands.schedule.utils.ScheduleTestUtils;
import de.espirit.firstspirit.access.schedule.ScheduleEntry;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ScheduleListResultTest {

	@Test
	public void constructor_server_side() {
		// setup
		final ArrayList<ScheduleEntry> list = new ArrayList<>();
		final ScheduleEntry createdEntry = ScheduleTestUtils.createScheduleEntry(1337, "myEntry");
		list.add(createdEntry);
		// test
		final ScheduleListResult scheduleListResult = new ScheduleListResult(list);
		// verify
		assertNull(scheduleListResult.getProjectName());
		final List<ScheduleEntry> entryList = scheduleListResult.getScheduleEntryList();
		assertEquals(1, entryList.size(), "entry size mismatch");
		final ScheduleEntry entry = entryList.get(0);
		assertEquals(createdEntry.getId(), entry.getId(), "entry id mismatch");
		assertEquals(createdEntry.getName(), entry.getName(), "entry name mismatch");
	}

	@Test
	public void constructor_project_bound() {
		// setup
		final String projectName = "myProject";
		final ArrayList<ScheduleEntry> list = new ArrayList<>();
		final ScheduleEntry createdEntry = ScheduleTestUtils.createScheduleEntry(1337, "myEntry");
		list.add(createdEntry);
		// test
		final ScheduleListResult scheduleListResult = new ScheduleListResult(projectName, list);
		// verify
		assertEquals(projectName, scheduleListResult.getProjectName(), "project name mismatch");
		final List<ScheduleEntry> entryList = scheduleListResult.getScheduleEntryList();
		assertEquals(1, entryList.size(), "entry size mismatch");
		final ScheduleEntry entry = entryList.get(0);
		assertEquals(createdEntry.getId(), entry.getId(), "entry id mismatch");
		assertEquals(createdEntry.getName(), entry.getName(), "entry name mismatch");
	}

	@Test
	public void constructor_exception() {
		// setup
		final String exceptionMessage = "myException";
		// test
		final ScheduleListResult scheduleListResult = new ScheduleListResult(new RuntimeException(exceptionMessage));
		// verify
		assertTrue(scheduleListResult.isError(), "result should be an error result");
		assertNotNull(scheduleListResult.getError(), "exception must be != null");
		assertEquals(RuntimeException.class, scheduleListResult.getError().getClass(), "exception class mismatch");
		assertEquals(exceptionMessage, scheduleListResult.getError().getMessage(), "message mismatch");
	}

	@Test
	public void buildLog_server_side() {
		// setup
		final ArrayList<ScheduleEntry> list = new ArrayList<>();
		final ScheduleEntry createdEntry = ScheduleTestUtils.createScheduleEntry(1337, "myEntry");
		list.add(createdEntry);
		final ScheduleListResult scheduleListResult = new ScheduleListResult(list);
		// test
		final String log = scheduleListResult.buildLog();
		// verify
		assertTrue(log.contains(ScheduleListResult.MESSAGE_TOPIC_SERVER_SIDE), "topic mismatch");
		assertTrue(log.contains("- " + createdEntry.getName()), "entry not found in log");
		assertEquals(1, StringUtils.countMatches(log, "- " + createdEntry.getName()), "entry found multiple times");
	}

	@Test
	public void buildLog_project_bound() {
		// setup
		final String projectName = "myProject";
		final ArrayList<ScheduleEntry> list = new ArrayList<>();
		final ScheduleEntry createdEntry = ScheduleTestUtils.createScheduleEntry(1337, "myEntry");
		list.add(createdEntry);
		final ScheduleListResult scheduleListResult = new ScheduleListResult(projectName, list);
		// test
		final String log = scheduleListResult.buildLog();
		// verify
		assertTrue(log.contains(String.format(ScheduleListResult.MESSAGE_TOPIC_PROJECT_BOUND, projectName)), "topic mismatch");
		assertTrue(log.contains("- " + createdEntry.getName()), "entry not found in log");
		assertEquals(1, StringUtils.countMatches(log, "- " + createdEntry.getName()), "entry found multiple times");
	}

}
