/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2025 Crownpeak Technology GmbH
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

package com.espirit.moddev.cli.api.result;

import com.espirit.moddev.cli.api.json.serializer.DefaultExecutionResultSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonSerialize(using = DefaultExecutionResultSerializer.class)
public abstract class AbstractModuleCommandResult extends AbstractCommandResult {

	public AbstractModuleCommandResult(@NotNull final String errorMessage, @NotNull final ExecutionResults results) {
		super(errorMessage, results);
	}

	public AbstractModuleCommandResult(@NotNull final Throwable throwable, @Nullable final String pathToFsm) {
		super(throwable.getMessage(), new ExecutionResults());
		_results.add(new ExecutionErrorResult<>() {
			@NotNull
			@Override
			public Throwable getThrowable() {
				return throwable;
			}

			@Override
			public String toString() {
				if (pathToFsm != null) {
					return String.format("Error occurred while processing '%s': %s", pathToFsm, getThrowable());
				} else {
					return getThrowable().toString();
				}
			}
		});
		_throwable = throwable;
	}

}
