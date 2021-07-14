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

package com.espirit.moddev.cli.commands.project.activateWebServerCommand;

import com.espirit.moddev.shared.webapp.WebAppIdentifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for {@link ProjectWebServerActivationParameter}.
 */
public class ProjectWebServerActivationParameterBuilder {
    private String projectName;
    private List<WebAppIdentifier> scopes;
    private String serverName;
    private boolean forceActivation;

    /**
     * Empty constructor to avoid implicit constructor
     */
    public ProjectWebServerActivationParameterBuilder() {
        // Nothing to do here
    }

    /**
     * Sets the builder's project name.
     * @param projectName
     * @return the current builder
     */
    public ProjectWebServerActivationParameterBuilder atProjectName(String projectName) {
        this.projectName = projectName;
        return this;
    }

    /**
     * Sets the builder's scopes.
     * @param scopes
     * @return the current builder
     */
    public ProjectWebServerActivationParameterBuilder forScopes(List<WebAppIdentifier> scopes) {
        this.scopes = scopes == null ? null : new ArrayList<>(scopes);
        return this;
    }

    /**
     * Sets the builder's web server name.
     * @param serverName
     * @return the current builder
     */
    public ProjectWebServerActivationParameterBuilder withServerName(String serverName) {
        this.serverName = serverName;
        return this;
    }

    /**
     * Sets the builder's force activation flag.
     * @param forceActivation
     * @return the current builder
     */
    public ProjectWebServerActivationParameterBuilder withForceActivation(boolean forceActivation) {
        this.forceActivation = forceActivation;
        return this;
    }

    /**
     * This methods creates a {@link ProjectWebServerActivationParameter} with all attributes, which were set before.
     * @return a new instance of {@link ProjectWebServerActivationParameter}.
     */
    public ProjectWebServerActivationParameter build() {
        return new ProjectWebServerActivationParameter(projectName, scopes, serverName, forceActivation);
    }
}
