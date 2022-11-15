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

import com.espirit.moddev.shared.StringUtils;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.agency.GlobalWebAppId;
import de.espirit.firstspirit.agency.ProjectWebAppId;
import de.espirit.firstspirit.agency.WebAppId;
import de.espirit.firstspirit.module.WebEnvironment.WebScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static de.espirit.firstspirit.module.WebEnvironment.WebScope.GLOBAL;

public interface WebAppIdentifier {

	@NotNull
	WebAppId createWebAppId(@Nullable Project project);

	@NotNull
	WebScope getScope();

	boolean isGlobal();

	WebAppIdentifier PREVIEW = forScope(WebScope.PREVIEW);
	WebAppIdentifier STAGING = forScope(WebScope.STAGING);
	WebAppIdentifier WEBEDIT = forScope(WebScope.WEBEDIT);
	WebAppIdentifier LIVE = forScope(WebScope.LIVE);

	WebAppIdentifier FS5_ROOT = forGlobalWebApp("fs5root");

	@NotNull
	static WebAppIdentifier fromWebAppId(WebAppId webAppId) {
		if (webAppId instanceof GlobalWebAppId) {
			return forGlobalWebApp(((GlobalWebAppId) webAppId).getGlobalId());
		} else {
			return forScope(((ProjectWebAppId) webAppId).getWebScope());
		}
	}

	static WebAppIdentifier forScope(WebScope scope) {
		return forScope(scope, null);
	}

	static WebAppIdentifier forGlobalWebApp(String globalWebAppName) {
		return forScope(GLOBAL, globalWebAppName);
	}

	static WebAppIdentifier forScope(WebScope scope, String globalWebAppName) {
		if (scope == null) {
			throw new IllegalArgumentException("Scope for WebApp identifier shouldn't be null!");
		}

		if (GLOBAL.equals(scope)) {
			if (StringUtils.isNullOrEmpty(globalWebAppName)) {
				throw new IllegalArgumentException("WebApp name missing for global WebApp.");
			}
			return new GlobalWebAppIdentifier(globalWebAppName);
		} else {
			return new ProjectWebAppIdentifier(scope);
		}
	}

}
