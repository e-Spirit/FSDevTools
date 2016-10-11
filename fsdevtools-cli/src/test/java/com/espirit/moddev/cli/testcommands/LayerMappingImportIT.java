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
import com.espirit.moddev.cli.StringPropertiesMap;
import com.espirit.moddev.cli.commands.ImportCommand;
import com.espirit.moddev.cli.commands.export.ExportCommand;
import com.espirit.moddev.cli.results.ImportResult;

import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.store.access.nexport.operations.ImportOperation;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.espirit.moddev.IntegrationTest.PROJECT_NAME_WITH_DB;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author e-Spirit AG
 */
@Category(IntegrationTest.class)
public class LayerMappingImportIT extends AbstractIntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LayerMappingImportIT.class);

    @Test
    public void testImportNameBasedLayerMapping() {
        String sourceProjectName = PROJECT_NAME_WITH_DB;
        long timeSuffix = System.currentTimeMillis();
        String targetProjectName = PROJECT_NAME_WITH_DB + timeSuffix;
        final File exportSyncDirectory = new File("./target/export/" + targetProjectName);
        final File importSyncDirectory = exportSyncDirectory;

        // Export project first to reimport it, because we don't want to chance the base project
        List<String> sourceLayers = exportProjectAndGetLayers(sourceProjectName, exportSyncDirectory);
        LOGGER.info("Found sourceLayers: " + sourceLayers.size());

        StringPropertiesMap layerMapping = new StringPropertiesMap();
        int counter = 0;
        List<String> expectedTargetLayerNames = new ArrayList<>();
        for(String sourceLayerName : sourceLayers) {
            String targetLayerName = timeSuffix + "_targetLayer" +  counter++;
            layerMapping.put(sourceLayerName, targetLayerName);
            LOGGER.info("Mapped layer " + sourceLayerName + " to " + targetLayerName);
            expectedTargetLayerNames.add(targetLayerName);
        }

        final ImportCommand importCommand = getImportCommandForJustExportedProject(targetProjectName, importSyncDirectory, layerMapping);

        final ImportResult result = importCommand.call();
        final ImportOperation.Result importResult = result.get();
        assertNotNull("Import failed with exception: " + result.getError().getMessage(), importResult);
        Project importedProject = importCommand.getContext().getProject();

        final Optional<String> optionalReason = importResult.getProblems().stream().map(problem -> problem.getNodeId() + "@" + problem.getMessage()).reduce((t, u) -> t + ", " + u);
        final String reason =  "Expected 0 problems: " + importResult.getProblems().size() + " -> " + (optionalReason.isPresent() ? optionalReason.get() : "Got 0 problems");
        assertThat(reason, importResult.getProblems(), hasSize(0));

        assertThat("Expected imported project to have database layers", importedProject.getLayers().isEmpty(), is(false));
        assertThat("Expected specified layer name in imported projects layers", importedProject.getLayers(), is(expectedTargetLayerNames));
    }

    private ImportCommand getImportCommandForJustExportedProject(String targetProjectName, File importSyncDirectory,
                                                                 StringPropertiesMap layerMapping) {
        final ImportCommand importCommand = new ImportCommand();
        initializeTestSpecificConfiguration(importCommand);
        CliContextImpl importCommandContext = new CliContextImpl(importCommand);
        importCommand.setProject(targetProjectName);
        importCommand.setContext(importCommandContext);
        importCommand.setCreateProjectIfMissing(true);
        importCommand.setLayerMapping(layerMapping);
        assertTrue("importSyncDirectory is not a directory", importSyncDirectory.isDirectory());
        final boolean isSyncDirectory = Arrays.asList(importSyncDirectory.listFiles()).stream().anyMatch(o -> o.getName().equals(".FirstSpirit"));
        assertTrue("importSyncDirectory is not a FirstSpirit directory", isSyncDirectory);
        importCommand.setSynchronizationDirectory(importSyncDirectory.getPath());
        return importCommand;
    }

    private List<String> exportProjectAndGetLayers(String sourceProjectName, File exportSyncDirectory) {
        final ExportCommand exportCommand = new ExportCommand();
        initializeTestSpecificConfiguration(exportCommand);
        exportCommand.setProject(sourceProjectName);
        exportCommand.setContext(new CliContextImpl(exportCommand));
        exportCommand.setSynchronizationDirectory(exportSyncDirectory.getPath());
        com.espirit.moddev.cli.results.ExportResult exportResult = exportCommand.call();
        Assert.assertFalse("Export not successful! Take a look at " + exportSyncDirectory.getPath(), exportResult.isError());

        List<String> sourceLayers = exportCommand.getContext().getProject().getLayers();
        Assert.assertThat("Expected exported project to have database layers", sourceLayers.isEmpty(), is(false));
        return sourceLayers;
    }

}
