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

package com.espirit.moddev.cli.commands.module.utils;

import de.espirit.firstspirit.module.descriptor.ComponentDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class ComponentResult<T extends ComponentDescriptor> {

	private final T _descriptor;
	private final Exception _exception;

	protected ComponentResult(@NotNull final T descriptor, @Nullable final Exception exception) {
		_descriptor = descriptor;
		_exception = exception;
	}

	@NotNull
	public final T getDescriptor() {
		return _descriptor;
	}

	@Nullable
	public final Exception getException() {
		return _exception;
	}

}
