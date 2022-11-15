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

package com.espirit.moddev.cli.api.result;

import org.jetbrains.annotations.NotNull;

public class DefaultExecutionErrorResult<T extends Exception> extends DefaultExecutionResult implements ExecutionErrorResult<T> {

	private final T _exception;

	public DefaultExecutionErrorResult(@NotNull final String message, @NotNull final T exception) {
		super(message);
		_exception = exception;
	}

	@NotNull
	@Override
	public T getException() {
		return _exception;
	}

}
