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

package com.espirit.moddev.cli.testcommands;

import com.espirit.moddev.IntegrationTest;
import com.espirit.moddev.cli.CliContextImpl;
import com.espirit.moddev.cli.commands.ImportCommand;
import com.espirit.moddev.cli.commands.export.ExportCommand;
import com.espirit.moddev.cli.results.ImportResult;

import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.access.project.ProjectScriptContext;
import de.espirit.firstspirit.store.access.nexport.operations.ImportOperation;

import org.junit.Rule;
import org.junit.experimental.categories.Category;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.espirit.moddev.IntegrationTest.PROJECT_NAME;
import static com.espirit.moddev.IntegrationTest.PROJECT_NAME_WITH_DB;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * This test expects to have an FirstSpirit server running at HTTP port 8000 with default
 * Admin/Admin access. There must be tow project, one with a database layer and one without.
 *
 * @author e-Spirit AG
 */
@Category(IntegrationTest.class)
@RunWith(Theories.class)
public class LayerMappingImportIT extends AbstractIntegrationTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @DataPoints
    public static String[] projectName = {PROJECT_NAME_WITH_DB, PROJECT_NAME};

    private static final Logger LOGGER = LoggerFactory.getLogger(LayerMappingImportIT.class);

    /**
     * Test as theory (mass test) if the import of name based layer mapping works.
     *
     * @param projectName the project name
     * @throws Exception the exception
     */
    @Theory
    public void testImportNameBasedLayerMapping(final String projectName) throws Exception {

        LOGGER.info("Run with project: {}", projectName);

        final String sourceProjectName = projectName;
        final String targetProjectName = projectName + " " + System.currentTimeMillis();
        final File syncDirectory = temp.newFolder("testproject");

        // Export project first to reimport it, because we don't want to change the base project
        final List<String> sourceLayers = exportProjectAndGetLayers(sourceProjectName, syncDirectory);
        LOGGER.info("Found sourceLayers: {}", sourceLayers.size());

        final String layerMapping = configureLayerMapping(projectName);
        final ImportCommand importCommand =
                getImportCommandForJustExportedProject(targetProjectName, syncDirectory, layerMapping);

        final ImportResult result = importCommand.call();

        final ImportOperation.Result importResult = result.get();

        if (result.isError()) {
            LOGGER.error("Import failed with exception: " + result.getError().getMessage(), result.getError());
        }
        assertThat("Import failed with exception: " + result.getError(), importResult, is(notNullValue()));

        final Project importedProject = importCommand.getContext().getProject();

        final Optional<String> optionalReason = importResult.getProblems().stream().map(problem -> problem.getNodeId() + "@" + problem.getMessage()).reduce((t, u) -> t + ", " + u);
        final String reason =  "Expected 0 problems: " + importResult.getProblems().size() + " -> " + (optionalReason.isPresent() ? optionalReason.get() : "Got 0 problems");
        assertThat(reason, importResult.getProblems(), hasSize(0));

        if (PROJECT_NAME_WITH_DB.equals(projectName)) {
            LOGGER.info("Layers empty? {}", importedProject.getLayers().isEmpty());

            assertThat("Expected imported project '" + importedProject.getName() + "' to have database layers", importedProject.getLayers().isEmpty(),
                    is(false));
        }
    }


    private String configureLayerMapping(final String projectName) {
        final String layerMapping;
        if (PROJECT_NAME_WITH_DB.equals(projectName)) {
            layerMapping = "mithras:CREATE_NEW";
        } else {
            // No mapping needed if there is no database
            layerMapping = null;
        }
        return layerMapping;
    }


    private ImportCommand getImportCommandForJustExportedProject(final String targetProjectName, final File importSyncDirectory,
            final String layerMapping) {
        final ImportCommand importCommand = new ImportCommand();
        importCommand.setProject(targetProjectName);
        initContextWithDefaultConfiguration(importCommand);
        final ProjectScriptContext context = new CliContextImpl(importCommand);
        importCommand.setContext(context);
        importCommand.setCreateProjectIfMissing(true);
        if (layerMapping != null) {
            importCommand.setLayerMapping(layerMapping);
        }
        assertTrue("importSyncDirectory is not a directory", importSyncDirectory.isDirectory());
        final boolean isSyncDirectory = Arrays.asList(importSyncDirectory.listFiles()).stream().anyMatch(o -> o.getName().equals(".FirstSpirit"));
        assertTrue("importSyncDirectory is not a FirstSpirit directory", isSyncDirectory);
        importCommand.setSynchronizationDirectory(importSyncDirectory.getPath());
        return importCommand;
    }

    private List<String> exportProjectAndGetLayers(final String sourceProjectName, final File exportSyncDirectory) throws Exception {

        LOGGER.info("Export project '" + sourceProjectName + "' to " + exportSyncDirectory.getPath());

        final ExportCommand exportCommand = new ExportCommand();
        exportCommand.setProject(sourceProjectName);
        initContextWithDefaultConfiguration(exportCommand);
        final ProjectScriptContext context = new CliContextImpl(exportCommand);
        exportCommand.setContext(context);
        exportCommand.setSynchronizationDirectory(exportSyncDirectory.getPath());
        exportCommand.setIncludeProjectProperties(true);
        final com.espirit.moddev.cli.results.ExportResult exportResult = exportCommand.call();
        assertFalse("Export not successful! Take a look at " + exportSyncDirectory.getPath(), exportResult.isError());

        final List<String> sourceLayers = exportCommand.getContext().getProject().getLayers();

        if (PROJECT_NAME_WITH_DB.equals(sourceProjectName)) {
            assertThat("Expected exported project to have database layers", sourceLayers.isEmpty(), is(false));
            System.out.println("sourceLayers: " + sourceLayers);
        }

        return sourceLayers;
    }

}
