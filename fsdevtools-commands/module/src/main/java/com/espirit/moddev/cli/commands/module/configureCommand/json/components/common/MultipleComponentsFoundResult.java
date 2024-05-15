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

package com.espirit.moddev.cli.commands.module.configureCommand.json.components.common;

import com.espirit.moddev.cli.api.result.ExecutionErrorResult;
import org.jetbrains.annotations.VisibleForTesting;
import de.espirit.firstspirit.module.descriptor.ComponentDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class MultipleComponentsFoundResult implements ExecutionErrorResult<IllegalStateException> {

	@VisibleForTesting
	static final String MESSAGE = "Multiple components from type '%s' with display name '%s' found in module '%s': %s";

	private final String _moduleName;
	private final ComponentDescriptor.Type _type;
	private final String _componentNames;
	private final String _displayName;

	public MultipleComponentsFoundResult(@NotNull final String moduleName, @NotNull final String displayName, @NotNull final ComponentDescriptor.Type type, @NotNull final List<ComponentDescriptor> components) {
		_moduleName = moduleName;
		_displayName = displayName;
		_type = type;
		_componentNames = components
				.stream()
				.map(ComponentDescriptor::getName)
				.collect(Collectors.joining(", ", "[ ", " ]"));
	}

	@NotNull
	@Override
	public IllegalStateException getThrowable() {
		return new IllegalStateException(toString());
	}

	@Override
	public String toString() {
		return String.format(MESSAGE, _type, _displayName, _moduleName, _componentNames);
	}

}
