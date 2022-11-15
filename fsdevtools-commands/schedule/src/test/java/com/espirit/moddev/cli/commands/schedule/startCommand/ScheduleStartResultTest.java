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
import de.espirit.firstspirit.access.schedule.ScheduleEntry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ScheduleStartResultTest {

	@Test
	public void constructor_server_side() {
		//setup
		final ScheduleEntry entry = ScheduleTestUtils.createScheduleEntry(1, "test task");
		final ScheduleStartInformation information = ScheduleTestUtils.createScheduleStartInformation(entry);
		//test
		final ScheduleStartResult result = new ScheduleStartResult(information);
		//verify
		assertNull(result.getProjectName(), "project must be null");
		assertEquals(entry.getId(), result.getScheduleStartInformation().getScheduleEntry().getId(), "entry id mismatch");
		assertEquals(entry.getName(), result.getScheduleStartInformation().getScheduleEntry().getName(), "entry name mismatch");
	}

	@Test
	public void constructor_project_bound() {
		//setup
		final String projectName = "myProject";
		final ScheduleEntry entry = ScheduleTestUtils.createScheduleEntry(1, "test task");
		final ScheduleStartInformation information = ScheduleTestUtils.createScheduleStartInformation(entry);
		//test
		final ScheduleStartResult result = new ScheduleStartResult(projectName, information);
		//verify
		assertEquals(projectName, result.getProjectName(), "project name mismatch");
		assertEquals(entry.getId(), result.getScheduleStartInformation().getScheduleEntry().getId(), "entry id mismatch");
		assertEquals(entry.getName(), result.getScheduleStartInformation().getScheduleEntry().getName(), "entry name mismatch");
	}

	@Test
	public void constructor_exception() {
		//setup
		final String exceptionMessage = "myException";
		//test
		final ScheduleStartResult result = new ScheduleStartResult(new RuntimeException(exceptionMessage));
		//verify
		assertTrue(result.isError(), "result should be an error result");
		assertNotNull(result.getError(), "exception must be != null");
		assertEquals(RuntimeException.class, result.getError().getClass(), "exception class mismatch");
		assertEquals(exceptionMessage, result.getError().getMessage(), "message mismatch");
	}

	@Test
	public void buildLog_server_side() {
		//setup
		final ScheduleEntry entry = ScheduleTestUtils.createScheduleEntry(1, "test task");
		final ScheduleStartInformation information = ScheduleTestUtils.createScheduleStartInformation(entry);
		final ScheduleStartResult result = new ScheduleStartResult(information);
		//test
		final String log = result.buildLog();
		//verify
		assertTrue(log.contains(String.format(ScheduleStartResult.MESSAGE_TOPIC_SERVER_SIDE, information.getScheduleEntry().getName(), information.getDuration())));
	}

	@Test
	public void buildLog_project_bound() {
		//setup
		final String projectName = "myProject";
		final ScheduleEntry entry = ScheduleTestUtils.createScheduleEntry(1, "test task");
		final ScheduleStartInformation information = ScheduleTestUtils.createScheduleStartInformation(entry);
		final ScheduleStartResult result = new ScheduleStartResult(projectName, information);
		//test
		final String log = result.buildLog();
		//verify
		assertTrue(log.contains(String.format(ScheduleStartResult.MESSAGE_TOPIC_PROJECT_BOUND, information.getScheduleEntry().getName(), information.getDuration())));
	}
}
