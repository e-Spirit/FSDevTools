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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.Map;

/**
 * Builder for ProjectImportParameters. This is used to perform a zip file based project import,
 * so at least a project name and a project file should be provided.
 */
public class ProjectImportParametersBuilder {

    private String _projectName;
    private File _projectFile;
    private Map<String, String> _layerMapping;
    private String _projectDescription;
    private boolean _forceProjectActivation;

    /**
     * Sets the project name - this is the name of the (new) target project where the import should go to.
     *
     * @param projectName the name of the FirstSpirit project
     * @return this
     */
    @NotNull
    public ProjectImportParametersBuilder setProjectName(@NotNull final String projectName) {
        _projectName = projectName;
        return this;
    }

    /**
     * Sets the project file - a FirstSpirit project export zip.
     *
     * @param projectFile the export file that should be imported
     * @return this
     */
    @NotNull
    public ProjectImportParametersBuilder setProjectFile(@NotNull final File projectFile) {
        _projectFile = projectFile;
        return this;
    }

    /**
     * Sets a layer mapping - it defines a mapping from a configured project layer from the exported project
     * to a given target layer on the server. Use layer names.
     *
     * @param layerMapping the database layer mapping that should be applied
     * @return this
     */
    @NotNull
    public ProjectImportParametersBuilder setLayerMapping(@Nullable final Map<String, String> layerMapping) {
        _layerMapping = layerMapping;
        return this;
    }

    /**
     * Sets a project description. Used to add additional information for the FirstSpirit target project.
     *
     * @param projectDescription the project description
     * @return this
     */
    @NotNull
    public ProjectImportParametersBuilder setProjectDescription(@NotNull final String projectDescription) {
        _projectDescription = projectDescription;
        return this;
    }

    /**
     * Specifies, if project activation should be forced or not. Deactivated projects can occur in some
     * rare cases, where there happened errors while importing.
     *
     * @param value whether the activation should be forced or not
     * @return this
     */
    @NotNull
    public ProjectImportParametersBuilder forceProjectActivation(final boolean value) {
        _forceProjectActivation = value;
        return this;
    }

    /**
     * Creates {@link ProjectImportParameters} from this builder object. For further information,
     * have a look at the corresponding {@link ProjectImportParameters} constructor.
     *
     * @return a new parameters instance
     */
    @NotNull
    public ProjectImportParameters create() {
        validateStringInput(_projectName, "Project name should not be null or empty");
        validateFile(_projectFile, "Project file is null, absent, or not a file");
        return new ProjectImportParameters(_projectName, _projectDescription == null ? "" : _projectDescription, _projectFile, _layerMapping == null ? Collections.emptyMap() : _layerMapping, _forceProjectActivation);
    }

    private void validateFile(@Nullable final File file, @NotNull final String message) {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new IllegalArgumentException(message);
        }
    }

    private void validateStringInput(@Nullable final String projectName, @NotNull final String message) {
        if (projectName == null || projectName.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

}
