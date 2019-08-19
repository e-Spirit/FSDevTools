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

import java.io.File;
import java.util.Map;

public class ProjectImportParameters {

    private final String _projectName;
    private final File _projectFile;
    private final Map<String, String> _layerMapping;
    private final String _projectDescription;
    private final boolean _forceProjectActivation;

    public ProjectImportParameters(@NotNull final String projectName, @NotNull final String projectDescription, @NotNull final File projectFile, @NotNull final Map<String, String> layerMapping, final boolean forceProjectActivation) {
        _projectName = projectName;
        _projectFile = projectFile;
        _layerMapping = layerMapping;
        _projectDescription = projectDescription;
        _forceProjectActivation = forceProjectActivation;
    }

    @NotNull
    public String getProjectName() {
        return _projectName;
    }

    @NotNull
    public File getProjectFile() {
        return _projectFile;
    }

    @NotNull
    public Map<String, String> getLayerMapping() {
        return _layerMapping;
    }

    @NotNull
    public String getProjectDescription() {
        return _projectDescription;
    }

    public boolean forceProjectActivation() {
        return _forceProjectActivation;
    }

}
