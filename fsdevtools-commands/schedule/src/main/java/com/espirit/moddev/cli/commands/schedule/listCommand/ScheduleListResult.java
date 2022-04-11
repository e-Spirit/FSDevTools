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

import com.espirit.moddev.cli.results.SimpleResult;
import com.espirit.moddev.shared.annotation.VisibleForTesting;

import de.espirit.firstspirit.access.schedule.ScheduleEntry;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


/**
 * Represents a {@link com.espirit.moddev.cli.api.result.Result} for a list of {@link ScheduleEntry schedule entries}.
 */
public class ScheduleListResult extends SimpleResult<List<ScheduleEntry>> {

	static String MESSAGE_TOPIC_PROJECT_BOUND = "List of schedule entries for project '%s':\n";
	static String MESSAGE_TOPIC_SERVER_SIDE = "List of server side schedule entries:\n";

	private final String _projectName;
	private final List<ScheduleEntry> _scheduleEntryList;


	/**
	 * Constructor to use for server sided {@link ScheduleEntry schedule entries}.
	 *
	 * @param scheduleEntryList the list of {@link ScheduleEntry schedule entries} without project binding (server sided).
	 * @see #ScheduleListResult(String, List)
	 */
	public ScheduleListResult(@NotNull final List<ScheduleEntry> scheduleEntryList) {
		this(null, scheduleEntryList);
	}


	/**
	 * Constructor to use for project bound {@link ScheduleEntry schedule entries}.
	 *
	 * @param projectName       the name of the project, must be {@code != null}
	 * @param scheduleEntryList the list of {@link ScheduleEntry schedule entries} with project binding.
	 */
	public ScheduleListResult(@Nullable final String projectName, @NotNull final List<ScheduleEntry> scheduleEntryList) {
		_projectName = projectName;
		_scheduleEntryList = scheduleEntryList;
	}


	/**
	 * Constructor to use if an exception occurs.
	 *
	 * @param exception the exception
	 */
	public ScheduleListResult(@NotNull final Exception exception) {
		super(exception);
		_scheduleEntryList = new ArrayList<>();
		_projectName = null;
	}


	@VisibleForTesting
	@Nullable
	String getProjectName() {
		return _projectName;
	}


	@VisibleForTesting
	@NotNull
	List<ScheduleEntry> getScheduleEntryList() {
		return _scheduleEntryList;
	}


	@Override
	public void log() {
		if (isError()) {
			return;
		}
		LOGGER.info(buildLog());
	}


	@VisibleForTesting
	@NotNull
	public String buildLog() {
		final StringBuilder builder = new StringBuilder();
		if (_projectName != null) {
			builder.append(String.format(MESSAGE_TOPIC_PROJECT_BOUND, getProjectName()));
		} else {
			builder.append(MESSAGE_TOPIC_SERVER_SIDE);
		}
		getScheduleEntryList().forEach(scheduleEntry -> builder.append("  - ").append(scheduleEntry.getName()).append('\n'));
		return builder.toString();
	}

}
