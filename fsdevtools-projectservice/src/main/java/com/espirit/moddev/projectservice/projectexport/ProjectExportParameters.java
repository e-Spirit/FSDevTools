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

    /**
     * This constant value used as maxRevisionCount for the FirstSpirit export
     * means that all revisions should be part of the export.
     */
    public static long UNLIMITED_REVISIONS = -1L;

    private final boolean deleteExportFiles;
    private final String projectName;
    private final String projectExportPath;
    private final boolean fsForceProjectActivation;
    private final long maxRevisionCount;
    private final boolean exportDeletedElements;

    /**
     * Creates a parameter object that describes how a project export should happen.
     *
     * @param projectName the project's name that should be exported
     * @param projectExportPath the path where the project should be exported into
     * @param fsForceProjectActivation whether the project's activation should be forced or not
     * @param deleteExportFiles whether internal export files should be deleted after export
     * @param maxRevisionCount states how many revisions should be part of the export. Pass @{value UNLIMITED_REVISIONS} for unlimited revisions.
     * @param exportDeletedElements whether deleted elements should be part of the export or not
     */
    public ProjectExportParameters(String projectName, String projectExportPath, boolean fsForceProjectActivation, boolean deleteExportFiles,
                                   long maxRevisionCount, boolean exportDeletedElements) {
        if(projectName == null || projectName.isEmpty()) {
            throw new IllegalArgumentException("Project name should not be null or empty");
        }
        if(projectExportPath == null || projectExportPath.isEmpty()) {
            throw new IllegalArgumentException("Project export path should not be null or empty");
        }

        this.projectName = projectName;
        this.projectExportPath = projectExportPath;
        this.fsForceProjectActivation = fsForceProjectActivation;
        this.deleteExportFiles = deleteExportFiles;
        this.maxRevisionCount = maxRevisionCount;
        this.exportDeletedElements = exportDeletedElements;
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

    /**
     * @return Whether to delete the export files on the server after they have been downloaded.
     */
    public boolean isDeleteExportFiles() {
        return deleteExportFiles;
    }

    /**
     * @return the maximum number of revisions to export or -1 if all revisions should be exported.
     */
    public long getMaxRevisionCount() {
        return maxRevisionCount;
    }

    /**
     * @return {@code true} if deleted elements should also be exported, {@code false} otherwise.
     */
    public boolean isExportDeletedElements() {
        return exportDeletedElements;
    }
}
