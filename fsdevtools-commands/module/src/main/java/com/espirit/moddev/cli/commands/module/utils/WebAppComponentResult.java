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

import com.espirit.moddev.shared.webapp.GlobalWebAppIdentifier;
import com.espirit.moddev.shared.webapp.WebAppIdentifier;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.agency.ProjectWebAppId;
import de.espirit.firstspirit.agency.WebAppId;
import de.espirit.firstspirit.module.descriptor.WebAppDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class WebAppComponentResult extends ComponentResult<WebAppDescriptor> {

	private final WebAppIdentifier _webAppIdentifier;
	private final String _projectName;

	WebAppComponentResult(@NotNull final WebAppDescriptor descriptor, @Nullable final Exception exception, @NotNull final WebAppId webAppId) {
		this(descriptor, exception, WebAppIdentifier.fromWebAppId(webAppId), webAppId instanceof ProjectWebAppId ? ((ProjectWebAppId) webAppId).getProject().getName() : null);
	}

	WebAppComponentResult(@NotNull final WebAppDescriptor descriptor, @Nullable final Exception exception, @NotNull final WebAppIdentifier webAppIdentifier, @Nullable final String projectName) {
		super(descriptor, exception);
		_webAppIdentifier = webAppIdentifier;
		_projectName = projectName;
	}

	@Nullable
	WebAppId getWebAppId(@NotNull final Connection connection) {
		final WebAppIdentifier webAppIdentifier = getWebAppIdentifier();
		if (webAppIdentifier instanceof GlobalWebAppIdentifier) {
			return webAppIdentifier.createWebAppId(null);
		} else {
			final Project project = connection.getProjectByName(getProjectName());
			if (project == null) {
				return null;
			}
			return webAppIdentifier.createWebAppId(project);
		}
	}

	@Nullable
	public String getProjectName() {
		return _projectName;
	}

	@NotNull
	public WebAppIdentifier getWebAppIdentifier() {
		return _webAppIdentifier;
	}

	@Override
	public String toString() {
		final Exception exception = getException();
		String webAppName = getWebAppIdentifier().toString();
		final String projectName = getProjectName();
		if (projectName != null) {
			webAppName = webAppName + '(' + projectName + ')';
		}
		if (exception == null) {
			return String.format("Web app component '%s:%s' (version='%s') installed/updated in web app '%s'.", getDescriptor().getModuleName(), getDescriptor().getName(), getDescriptor().getVersion(), webAppName);
		} else {
			return String.format("Error installing/updating web app component '%s:%s' (version='%s') in web app '%s': %s", getDescriptor().getModuleName(), getDescriptor().getName(), getDescriptor().getVersion(), webAppName, exception.getMessage());
		}
	}

}
