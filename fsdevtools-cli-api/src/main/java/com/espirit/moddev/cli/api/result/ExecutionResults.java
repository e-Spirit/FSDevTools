/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2024 Crownpeak Technology GmbH
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

import com.espirit.moddev.cli.api.command.Command;
import com.espirit.moddev.cli.api.json.serializer.DefaultExecutionResultsSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Wrapper for a list of {@link ExecutionResult results} when executing a {@link Command}.
 * <br/><br/>
 * This class can be used if you want to have simple json support for a {@link Command}. Each instance will be serialized
 * as an json object containing some information (i.e. error status) and the list of {@link ExecutionResult results} as an json array.
 */
@JsonSerialize(using = DefaultExecutionResultsSerializer.class)
public class ExecutionResults implements ExecutionResult, Iterable<ExecutionResult> {

	@JsonProperty(value = "results")
	private final List<ExecutionResult> _results;
	@JsonProperty(value = "hasError")
	private boolean _hasError;

	public ExecutionResults() {
		_results = new ArrayList<>();
	}

	@NotNull
	public Stream<ExecutionResult> stream() {
		return _results.stream();
	}

	@NotNull
	@Override
	public Iterator<ExecutionResult> iterator() {
		return _results.iterator();
	}

	public void add(@NotNull final ExecutionResult result) {
		internalAdd(result, new ArrayList<>());
	}

	private void internalAdd(@NotNull final ExecutionResult result, @NotNull final List<ExecutionResult> handledResults) {
		if (this == result || handledResults.contains(result)) {
			// already added --> ignore and return to avoid circular dependencies
			return;
		}
		handledResults.add(result);

		if (result instanceof ExecutionResults) {
			// handle ExecutionResults separately by adding all inner elements
			for (final ExecutionResult innerResult : ((ExecutionResults) result)._results) {
				internalAdd(innerResult, handledResults);
			}
		} else {
			// avoid duplicated entries
			if (_results.contains(result)) {
				return;
			}
			// update error-flag
			if (result instanceof ExecutionErrorResult) {
				_hasError = true;
			}
			_results.add(result);
		}
	}

	public boolean hasError() {
		return _hasError;
	}

	public boolean isEmpty() {
		return _results.isEmpty();
	}

	public int size() {
		return _results.size();
	}

	@NotNull
	public ExecutionResult get(final int index) {
		return _results.get(index);
	}

}
