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

package com.espirit.moddev.cli.commands.module.configureCommand.json.components.common;

import com.espirit.moddev.cli.api.result.ExecutionErrorResult;
import com.espirit.moddev.shared.annotation.VisibleForTesting;
import org.jetbrains.annotations.NotNull;

public class ProjectNotFoundResult implements ExecutionErrorResult<IllegalStateException> {

	@VisibleForTesting
	public static final String MESSAGE = "Project '%s' not found!";

	private final String _projectName;

	public ProjectNotFoundResult(@NotNull final String projectName) {
		_projectName = projectName;
	}

	@NotNull
	@Override
	public IllegalStateException getThrowable() {
		return new IllegalStateException(toString());
	}

	@Override
	public String toString() {
		return String.format(MESSAGE, _projectName);
	}

}
