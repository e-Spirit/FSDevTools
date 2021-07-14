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

package com.espirit.moddev.cli.commands.module.configureCommand.json.components.common;

import com.espirit.moddev.cli.api.result.ExecutionErrorResult;
import com.espirit.moddev.shared.annotation.VisibleForTesting;
import de.espirit.firstspirit.module.descriptor.ComponentDescriptor;
import org.jetbrains.annotations.NotNull;

public class ComponentNotFoundResult implements ExecutionErrorResult<IllegalStateException> {

	@VisibleForTesting
	static final String MESSAGE = "Component '%s' ( type = %s ) not found in module '%s'!";

	private final String _moduleName;
	private final ComponentDescriptor.Type _type;
	private final String _componentName;

	public ComponentNotFoundResult(@NotNull final String moduleName, @NotNull final ComponentDescriptor.Type type, @NotNull final String componentName) {
		_moduleName = moduleName;
		_type = type;
		_componentName = componentName;
	}

	@NotNull
	@Override
	public IllegalStateException getException() {
		return new IllegalStateException(toString());
	}

	@Override
	public String toString() {
		return String.format(MESSAGE, _componentName, _type, _moduleName);
	}

}
