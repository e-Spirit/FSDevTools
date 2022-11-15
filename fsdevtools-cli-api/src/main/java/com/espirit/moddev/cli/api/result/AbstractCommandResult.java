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

import com.espirit.moddev.cli.api.json.serializer.DefaultExecutionResultSerializer;
import com.espirit.moddev.shared.exception.MultiException;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.stream.Collectors;

@JsonSerialize(using = DefaultExecutionResultSerializer.class)
public abstract class AbstractCommandResult implements Result<ExecutionResults> {

	public static final String LINE_SEPARATOR = "=========================================================================";

	private final String _errorMessage;
	protected final Logger _logger;
	protected final ExecutionResults _results;
	protected Exception _exception;

	public AbstractCommandResult(@NotNull final String errorMessage, @NotNull final ExecutionResults results) {
		_errorMessage = errorMessage;
		_logger = LoggerFactory.getLogger(getClass());
		_results = results;
		if (results.hasError()) {
			_exception = new MultiException(errorMessage, buildExceptions(results));
		} else {
			_exception = null;
		}
	}

	public AbstractCommandResult(@NotNull final Exception exception) {
		_errorMessage = exception.getMessage();
		_logger = LoggerFactory.getLogger(getClass());
		_results = new ExecutionResults();
		_results.add(new ExecutionErrorResult<>() {
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
		return results.stream()
				.filter(result -> result instanceof ExecutionErrorResult)
				.map(result -> ((ExecutionErrorResult<?>) result).getException())
				.collect(Collectors.toList());
	}

	@Override
	public abstract void log();

	protected void logResults() {
		_results.stream().forEach(result -> {
			if (result instanceof ExecutionErrorResult) {
				_logger.error(result.toString());
			} else {
				_logger.info(result.toString());
			}
		});
		if (!_results.isEmpty()) {
			_logger.info(LINE_SEPARATOR);
		}
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
		if (_exception == null && _results.hasError()) {
			_exception = new MultiException(_errorMessage, buildExceptions(_results));
		}
		return _exception;
	}

}
