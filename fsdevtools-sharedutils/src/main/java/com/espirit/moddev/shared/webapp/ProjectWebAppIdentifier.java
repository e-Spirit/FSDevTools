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

package com.espirit.moddev.shared.webapp;

import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.agency.WebAppId;
import de.espirit.firstspirit.module.WebEnvironment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class ProjectWebAppIdentifier implements WebAppIdentifier {

	private final WebEnvironment.WebScope _scope;

	ProjectWebAppIdentifier(@NotNull final WebEnvironment.WebScope scope) {
		_scope = scope;
	}

	@Override
	@NotNull
	public WebAppId createWebAppId(@Nullable final Project project) {
		if (project == null) {
			throw new IllegalArgumentException("Cannot create non global WebAppId with null project!");
		}
		return WebAppId.Factory.create(project, _scope);
	}

	@Override
	@NotNull
	public WebEnvironment.WebScope getScope() {
		return _scope;
	}

	@Override
	public boolean isGlobal() {
		return false;
	}

	@Override
	public String toString() {
		return _scope.toString().toLowerCase(Locale.ROOT);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		final ProjectWebAppIdentifier that = (ProjectWebAppIdentifier) o;
		return _scope == that._scope;
	}

	@Override
	public int hashCode() {
		return _scope.hashCode();
	}
}
