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

import static org.junit.Assert.*;


public class ScheduleStartResultTest {

	@Test
	public void constructor_server_side() {
		//setup
		final ScheduleEntry entry = ScheduleTestUtils.createScheduleEntry(1, "test task");
		final ScheduleStartInformation information = ScheduleTestUtils.createScheduleStartInformation(entry);
		//test
		final ScheduleStartResult result = new ScheduleStartResult(information);
		//verify
		assertNull("project must be null", result.getProjectName());
		assertEquals("entry id mismatch", entry.getId(), result.getScheduleStartInformation().getScheduleEntry().getId());
		assertEquals("entry name mismatch", entry.getName(), result.getScheduleStartInformation().getScheduleEntry().getName());
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
		assertEquals("project name mismatch", projectName, result.getProjectName());
		assertEquals("entry id mismatch", entry.getId(), result.getScheduleStartInformation().getScheduleEntry().getId());
		assertEquals("entry name mismatch", entry.getName(), result.getScheduleStartInformation().getScheduleEntry().getName());
	}

	@Test
	public void constructor_exception() {
		//setup
		final String exceptionMessage = "myException";
		//test
		final ScheduleStartResult result = new ScheduleStartResult(new RuntimeException(exceptionMessage));
		//verify
		assertTrue("result should be an error result", result.isError());
		assertNotNull("exception must be != null", result.getError());
		assertEquals("exception class mismatch", RuntimeException.class, result.getError().getClass());
		assertEquals("message mismatch", exceptionMessage, result.getError().getMessage());
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
		assertTrue("topic mismatch", log.contains(String.format(ScheduleStartResult.MESSAGE_TOPIC_SERVER_SIDE, information.getScheduleEntry().getName(), information.getDuration())));
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
		assertTrue("topic mismatch", log.contains(String.format(ScheduleStartResult.MESSAGE_TOPIC_PROJECT_BOUND, information.getScheduleEntry().getName(), information.getDuration())));
	}
}
