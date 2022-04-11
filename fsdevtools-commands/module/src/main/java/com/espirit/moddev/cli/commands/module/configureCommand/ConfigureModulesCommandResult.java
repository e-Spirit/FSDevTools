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

package com.espirit.moddev.cli.commands.module.configureCommand;

import com.espirit.moddev.cli.api.result.ExecutionErrorResult;
import com.espirit.moddev.cli.api.result.ExecutionResults;
import com.espirit.moddev.cli.api.result.Result;
import com.espirit.moddev.shared.exception.MultiException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Implementation of a {@link Result} for the {@link ConfigureModulesCommand}.
 */
public class ConfigureModulesCommandResult implements Result<ExecutionResults> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigureModulesCommandResult.class);
	private static final String LINE_SEPARATOR = "=========================================================================";

	private final ExecutionResults _results;
	private final Exception _exception;

	public ConfigureModulesCommandResult(@NotNull final ExecutionResults results) {
		_results = results;
		if (results.hasError()) {
			_exception = new MultiException("Error configuring modules!", buildExceptions(results));
		} else {
			_exception = null;
		}
	}

	public ConfigureModulesCommandResult(@NotNull final Exception exception) {
		_results = new ExecutionResults();
		_results.add(new ExecutionErrorResult<Exception>() {
			@NotNull
			@Override
			public Exception getException() {
				return exception;
			}

			@Override
			public String toString() {
				return getException().toString();
			}
		});
		_exception = exception;
	}

	@NotNull
	private static Collection<Exception> buildExceptions(@NotNull final ExecutionResults results) {
		final ArrayList<Exception> exceptions = new ArrayList<>();
		results.stream().forEach(result -> {
			if (!(result instanceof ExecutionErrorResult)) {
				return;
			}
			final ExecutionErrorResult<?> errorResult = (ExecutionErrorResult<?>) result;
			exceptions.add(errorResult.getException());
		});
		return exceptions;
	}

	@Override
	public void log() {
		LOGGER.info(LINE_SEPARATOR);
		if (isError()) {
			LOGGER.error("Module configuration completed with errors!");
		} else {
			LOGGER.info("Module configuration successfully completed.");
		}
		LOGGER.info(LINE_SEPARATOR);
		_results.stream().forEach(result -> {
			if (result instanceof ExecutionErrorResult) {
				LOGGER.error(result.toString());
			} else {
				LOGGER.info(result.toString());
			}
		});
		LOGGER.info(LINE_SEPARATOR);
	}

	@Override
	public ExecutionResults get() {
		return _results;
	}

	@Override
	public boolean isError() {
		return getError() != null;
	}

	@Override
	public Exception getError() {
		return _exception;
	}

}
