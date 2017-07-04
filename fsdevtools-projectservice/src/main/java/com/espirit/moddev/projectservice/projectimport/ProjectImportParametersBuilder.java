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

/**
 * Builder for ProjectImportParameters. This is used to perform a zip file based project import,
 * so at least a project name and a project file should be provided.
 */
public class ProjectImportParametersBuilder {
    private String projectName;
    private String projectFile;
    private Map<String, String> databases;
    private String projectDescription;
    private boolean fsForceProjectActivation;

    /**
     * Sets the project name - this is the name of the (new) target project where the import should go to.
     * @param projectName the name of the FirstSpirit project
     * @return this
     */
    public ProjectImportParametersBuilder setProjectName(String projectName) {
        this.projectName = projectName;
        return this;
    }

    /**
     * Sets the project file - a FirstSpirit project export zip.
     * @param projectFile the export file that should be imported
     * @return this
     */
    public ProjectImportParametersBuilder setProjectFile(String projectFile) {
        this.projectFile = projectFile;
        return this;
    }

    /**
     * Sets a layer mapping - it defines a mapping from a configured project layer from the exported project
     * to a given target layer on the server. Use layer names.
     * @param databases the database layer mapping that should be applied
     * @return this
     */
    public ProjectImportParametersBuilder setDatabaseLayerMapping(Map<String, String> databases) {
        this.databases = databases;
        return this;
    }

    /**
     * Sets a project description. Used to add additional information for the FirstSpirit target project.
     * @param projectDescription the project description
     * @return this
     */
    public ProjectImportParametersBuilder setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
        return this;
    }

    /**
     * Specifies, if project activation should be forced or not. Deactivated projects can occur in some
     * rare cases, where there happened errors while importing.
     * @param fsForceProjectActivation whether the activation should be forced or not
     * @return this
     */
    public ProjectImportParametersBuilder setForceProjectActivation(boolean fsForceProjectActivation) {
        this.fsForceProjectActivation = fsForceProjectActivation;
        return this;
    }

    /**
     * Creates {@link ProjectImportParameters} from this builder object. For further information,
     * have a look at the corresponding {@link ProjectImportParameters} constructor.
     * @return a new parameters instance
     */
    public ProjectImportParameters create() {
        return new ProjectImportParameters(projectName, projectDescription, projectFile, databases, fsForceProjectActivation);
    }
}
