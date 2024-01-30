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

package com.espirit.moddev.cli.commands.project.activateWebServerCommand;

import com.espirit.moddev.shared.webapp.WebAppIdentifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class that represents all parameters for the "activatewebserver" command.
 */
public class ProjectWebServerActivationParameter {

	private final String projectName;
	private final List<WebAppIdentifier> scopes;
	private final String serverName;
	private final boolean forceActivation;

	public ProjectWebServerActivationParameter(String projectName, List<WebAppIdentifier> scopes, String serverName, boolean forceActivation) {
		if (projectName == null || projectName.isEmpty()) {
			throw new IllegalArgumentException("Project name should not be null or empty");
		}
		if (scopes == null || scopes.isEmpty()) {
			throw new IllegalArgumentException("Scopes should not be null or empty");
		}
		if (serverName == null || serverName.isEmpty()) {
			throw new IllegalArgumentException("Server name should not be null or empty");
		}
		this.projectName = projectName;
		this.scopes = new ArrayList<>(scopes);
		this.serverName = serverName;
		this.forceActivation = forceActivation;
	}

	public String getProjectName() {
		return projectName;
	}

	public List<WebAppIdentifier> getScopes() {
		return Collections.unmodifiableList(scopes);
	}

	public String getServerName() {
		return serverName;
	}

	public boolean isForceActivation() {
		return forceActivation;
	}

	public static ProjectWebServerActivationParameterBuilder builder() {
		return new ProjectWebServerActivationParameterBuilder();
	}
}
