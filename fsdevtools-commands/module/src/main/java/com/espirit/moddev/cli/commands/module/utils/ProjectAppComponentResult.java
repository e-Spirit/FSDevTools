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

import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.module.descriptor.ProjectAppDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class ProjectAppComponentResult extends ComponentResult<ProjectAppDescriptor> {

	private final Project _project;

	ProjectAppComponentResult(@NotNull final ProjectAppDescriptor descriptor, @Nullable final Throwable throwable, @NotNull final Project project) {
		super(descriptor, throwable);
		_project = project;
	}

	@NotNull
	public Project getProject() {
		return _project;
	}

	@Override
	public String toString() {
		final Throwable throwable = getThrowable();
		if (throwable == null) {
			return String.format("Project app component '%s:%s' (version='%s') installed/updated in project '%s'.", getDescriptor().getModuleName(), getDescriptor().getName(), getDescriptor().getVersion(), getProject().getName());
		} else {
			return String.format("Error installing/updating project app component '%s:%s' (version='%s') in project '%s': %s", getDescriptor().getModuleName(), getDescriptor().getName(), getDescriptor().getVersion(), getProject().getName(), throwable.getMessage());
		}
	}

}
