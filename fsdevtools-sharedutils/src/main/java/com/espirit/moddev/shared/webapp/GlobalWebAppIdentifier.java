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

package com.espirit.moddev.shared.webapp;

import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.agency.WebAppId;
import de.espirit.firstspirit.module.WebEnvironment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static de.espirit.firstspirit.module.WebEnvironment.WebScope.GLOBAL;

public class GlobalWebAppIdentifier implements WebAppIdentifier {

	private final String _globalWebAppId;

	GlobalWebAppIdentifier(@NotNull final String globalWebAppId) {
		_globalWebAppId = globalWebAppId;
	}

	@Override
	@NotNull
	public WebAppId createWebAppId(@Nullable final Project project) {
		return WebAppId.Factory.create(_globalWebAppId);
	}

	@Override
	@NotNull
	public WebEnvironment.WebScope getScope() {
		return GLOBAL;
	}

	@Override
	public boolean isGlobal() {
		return true;
	}

	@NotNull
	public String getGlobalWebAppId() {
		return _globalWebAppId;
	}

	@Override
	public String toString() {
		return "global(" + _globalWebAppId + ")";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		final GlobalWebAppIdentifier that = (GlobalWebAppIdentifier) o;
		return Objects.equals(_globalWebAppId, that._globalWebAppId);
	}

	@Override
	public int hashCode() {
		return _globalWebAppId.hashCode();
	}
}
