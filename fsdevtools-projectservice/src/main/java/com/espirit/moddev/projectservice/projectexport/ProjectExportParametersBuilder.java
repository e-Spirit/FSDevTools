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
 * Class that build ProjectExportParameters
 */
public class ProjectExportParametersBuilder {
    private String projectName;
    private String projectExportPath;
    private boolean fsForceProjectActivation;

    /**
     * @param projectName The name of the project. Note: Not a reference name.
     * @return The builder
     */
    public ProjectExportParametersBuilder setProjectName(String projectName) {
        this.projectName = projectName;
        return this;
    }

    /**
     * @param projectExportPath Download directory for the exported files.
     * @return The builder
     */
    public ProjectExportParametersBuilder setProjectExportPath(String projectExportPath) {
        this.projectExportPath = projectExportPath;
        return this;
    }

    /**
     * @param fsForceProjectActivation Whether a deactivated project will be activated by force on export or not.
     * @return The builder
     */
    public ProjectExportParametersBuilder setFsForceProjectActivation(boolean fsForceProjectActivation) {
        this.fsForceProjectActivation = fsForceProjectActivation;
        return this;
    }

    /**
     * @return A ProjectExportParameters instance
     */
    public ProjectExportParameters build() {
        return new ProjectExportParameters(projectName, projectExportPath, fsForceProjectActivation);
    }
}
