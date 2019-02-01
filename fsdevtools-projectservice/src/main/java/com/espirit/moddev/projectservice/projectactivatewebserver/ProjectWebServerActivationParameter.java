/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2019 e-Spirit AG
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

package com.espirit.moddev.projectservice.projectactivatewebserver;

import com.espirit.moddev.shared.webapp.WebAppIdentifier;

import de.espirit.firstspirit.module.WebEnvironment;

import java.util.List;

public class ProjectWebServerActivationParameter {

    private final String projectName;
    private final List<WebAppIdentifier> scopes;
    private final String serverName;
    private final boolean forceActivation;

    public ProjectWebServerActivationParameter(String projectName, List<WebAppIdentifier> scopes, String serverName, boolean forceActivation) {
        if(projectName == null || projectName.isEmpty()) {
            throw new IllegalArgumentException("Project name should not be null or empty");
        }
        if(scopes == null || scopes.isEmpty()) {
            throw new IllegalArgumentException("Scopes should not be null or empty");
        }
        if(serverName == null || serverName.isEmpty()) {
            throw new IllegalArgumentException("Server name should not be null or empty");
        }
        this.projectName = projectName;
        this.scopes = scopes;
        this.serverName = serverName;
        this.forceActivation = forceActivation;
    }

    public String getProjectName() {
        return projectName;
    }

    public List<WebAppIdentifier> getScopes() {
        return scopes;
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
