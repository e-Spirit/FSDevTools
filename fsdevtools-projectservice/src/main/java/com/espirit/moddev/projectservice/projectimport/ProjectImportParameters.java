/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2016 e-Spirit AG
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

package com.espirit.moddev.projectservice.projectimport;

import java.util.Map;

public class ProjectImportParameters {
    private final String projectName;
    private final String projectFile;
    private final Map<String, String> databases;
    private final String projectDescription;
    private final boolean fsForceProjectActivation;

    public ProjectImportParameters(String projectName, String projectDescription, String projectFilePath, Map<String, String> databases, boolean forceProjectActivation) {
        validateStringInput(projectName, "Project name should not be null or empty");
        validateStringInput(projectFilePath, "Project file path should not be null or empty");

        this.projectName = projectName;
        this.projectFile = projectFilePath;
        this.databases = databases;
        this.projectDescription = projectDescription;
        this.fsForceProjectActivation = forceProjectActivation;
    }

    private void validateStringInput(String projectName, String message) {
        if(projectName == null || projectName.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProjectFilePath() {
        return projectFile;
    }

    public Map<String, String> getDatabases() {
        return databases;
    }

    public String getProjectDescription() {
        return projectDescription;
    }

    public boolean isFsForceProjectActivation() {
        return fsForceProjectActivation;
    }
}
