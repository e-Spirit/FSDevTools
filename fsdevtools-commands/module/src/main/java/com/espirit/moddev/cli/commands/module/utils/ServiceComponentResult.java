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

package com.espirit.moddev.cli.commands.module.utils;

import de.espirit.firstspirit.module.descriptor.ServiceDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class ServiceComponentResult extends ComponentResult<ServiceDescriptor> {

	ServiceComponentResult(@NotNull final ServiceDescriptor descriptor, @Nullable final Throwable throwable) {
		super(descriptor, throwable);
	}

	@Override
	public String toString() {
		final Throwable throwable = getThrowable();
		if (throwable == null) {
			return String.format("Service '%s:%s' (version='%s') successfully configured and started.", getDescriptor().getModuleName(), getDescriptor().getName(), getDescriptor().getVersion());
		} else {
			return String.format("Error configuring and starting service '%s:%s' (version='%s'): %s", getDescriptor().getModuleName(), getDescriptor().getName(), getDescriptor().getVersion(), throwable.getMessage());
		}
	}

}
