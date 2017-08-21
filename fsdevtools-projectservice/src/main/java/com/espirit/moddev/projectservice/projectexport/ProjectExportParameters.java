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

package com.espirit.moddev.projectservice.projectexport;

/**
 * Class that represents a FirstSpirit project export.
 */
public class ProjectExportParameters {
    private String projectName;
    private String projectExportPath;
    private final boolean fsForceProjectActivation;

    public ProjectExportParameters(String projectName, String projectExportPath, boolean fsForceProjectActivation) {
        if(projectName == null || projectName.isEmpty()) {
            throw new IllegalArgumentException("Project name should not be null or empty");
        }
        if(projectExportPath == null || projectExportPath.isEmpty()) {
            throw new IllegalArgumentException("Project export path should not be null or empty");
        }

        this.projectName = projectName;
        this.projectExportPath = projectExportPath;
        this.fsForceProjectActivation = fsForceProjectActivation;
    }

    /**
     * @return The name of the project. Note: Not a reference name.
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * @return Download directory for the exported files.
     */
    public String getProjectExportPath() {
        return projectExportPath;
    }

    /**
     * @return Whether a deactivated project will be activated by force on export or not.
     */
    public boolean isFsForceProjectActivation() {
        return fsForceProjectActivation;
    }
}
