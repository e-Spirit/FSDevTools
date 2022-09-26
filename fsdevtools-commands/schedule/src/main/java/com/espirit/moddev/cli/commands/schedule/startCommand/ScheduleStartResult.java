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

import com.espirit.moddev.cli.results.SimpleResult;
import com.espirit.moddev.shared.annotation.VisibleForTesting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a {@link com.espirit.moddev.cli.api.result.Result} for a {@link ScheduleStartInformation schedule start information}
 */
public class ScheduleStartResult extends SimpleResult<Boolean> {

	public static final String MESSAGE_TOPIC_PROJECT_BOUND = "Schedule task '%s' of project successfully completed in %s ms\n";
	public static final String MESSAGE_TOPIC_SERVER_SIDE = "Schedule side schedule task '%s' successfully completed in %s ms\n";

	private final String _projectName;
	private final ScheduleStartInformation _scheduleStartInformation;

	/**
	 * Constructor to use for server sided {@link ScheduleStartInformation schedule start information}
	 *
	 * @param scheduleStartInformation the {@link ScheduleStartInformation schedule start information} without project binding (server sided)
	 * @see #ScheduleStartResult(String, ScheduleStartInformation)
	 */
	public ScheduleStartResult(@NotNull final ScheduleStartInformation scheduleStartInformation) {
		this(null, scheduleStartInformation);
	}

	/**
	 * Constructor to use for project bound {@link ScheduleStartInformation schedule start information}
	 *
	 * @param projectName              the name of the project, must be {@code != null}
	 * @param scheduleStartInformation the {@link ScheduleStartInformation schedule start information}
	 */
	public ScheduleStartResult(@Nullable final String projectName, @NotNull final ScheduleStartInformation scheduleStartInformation) {
		_projectName = projectName;
		_scheduleStartInformation = scheduleStartInformation;
	}

	/**
	 * Constructor to use if an exception occurs
	 *
	 * @param exception the exception
	 */
	public ScheduleStartResult(@NotNull final Exception exception) {
		super(exception);
		_projectName = null;
		_scheduleStartInformation = null;
	}

	@VisibleForTesting
	@Nullable
	public String getProjectName() {
		return _projectName;
	}

	@VisibleForTesting
	@Nullable
	public ScheduleStartInformation getScheduleStartInformation() {
		return _scheduleStartInformation;
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
		if (_scheduleStartInformation != null) {
			if (_projectName != null) {
				builder.append(String.format(MESSAGE_TOPIC_PROJECT_BOUND, _scheduleStartInformation.getScheduleEntry().getName(), _scheduleStartInformation.getDuration()));
			} else {
				builder.append(String.format(MESSAGE_TOPIC_SERVER_SIDE, _scheduleStartInformation.getScheduleEntry().getName(), _scheduleStartInformation.getDuration()));
			}
		}
		return builder.toString();
	}
}
