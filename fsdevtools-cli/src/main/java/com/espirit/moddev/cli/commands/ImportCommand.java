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

package com.espirit.moddev.cli.commands;

import com.espirit.moddev.cli.CliConstants;
import com.espirit.moddev.cli.api.configuration.ImportConfig;
import com.espirit.moddev.cli.results.ImportResult;
import com.espirit.moddev.core.SchemaUidToNameBasedLayerMapper;
import com.espirit.moddev.core.StringPropertiesMap;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.help.Examples;

import de.espirit.firstspirit.agency.OperationAgent;
import de.espirit.firstspirit.store.access.nexport.operations.ImportOperation;
import de.espirit.firstspirit.transport.LayerMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Command that executes a FirstSpirit ImportOperation. Uses a FirstSpirit context.
 *
 * @author e-Spirit AG
 */
@Command(name = "import", description = "Imports a FirstSpirit project with optional schema to target layer mapping")
@Examples(
        examples = {"fs-cli import -lm *:CREATE_NEW", "fs-cli import -lm my_schema:CREATE_NEW", "fs-cli import -lm *:derby_project14747_0",
        "fs-cli import -lm schema_a:derby_project14747_0,schema_b:derby_project14747_1"},
        descriptions = 
                {"Import project and create for every unknown source schema a new target layer (use if uncertain)",
                "Import project and create for source schema 'my_schema' a new layer",
                "Import project and redirect every unknown source schema into given target layer. The target layer must be attached to the project! (use with caution)",
                "Import project and use specified mapping for source schemas and existing target layers. The target layers must be attached to the project! (use with caution)"})
public class ImportCommand extends SimpleCommand<ImportResult> implements ImportConfig {

    /** The Constant LOGGER. */
    protected static final Logger LOGGER = LoggerFactory.getLogger(ImportCommand.class);

    /** The import comment. */
    @Option(name = {"-i", "--import-comment"}, description = "Import comment for FirstSpirit revision")
    private String importComment;

    /** The dont create project if missing. */
    @Option(name = {"--dont-create-project"}, description = "Do not create project in FirstSpirit if it is missing")
    private boolean dontCreateProjectIfMissing;

    /** The dont create entities. */
    @Option(name = {"--dont-create-entities"}, description = "Don not create entities when importing")
    private boolean dontCreateEntities;

    /** The layer mapping. */
    @Option(name = {"-lm", "--layerMapping"},
            description = "Defines how unknown layers should be mapped in the target; comma-separated key-value pairs by : or =; key is source schema UID; value is target layer name; see EXAMPLES for more information",
            type = OptionType.COMMAND)
    private String layerMapping;

    @Override
    public boolean isCreatingProjectIfMissing() {
        return !dontCreateProjectIfMissing;
    }

    @Override
    public String getImportComment() {
        if(importComment == null || importComment.isEmpty()) {
            final boolean environmentContainsImportComment = getEnvironment().containsKey(CliConstants.KEY_FS_IMPORT_COMMENT.value());
            if (environmentContainsImportComment) {
                return getEnvironment().get(CliConstants.KEY_FS_IMPORT_COMMENT.value()).trim();
            }
            return CliConstants.DEFAULT_IMPORT_COMMENT.value();
        }
        return importComment;
    }

    @Override
    public ImportResult call() {
        LOGGER.info("Importing...");
        try {
            final OperationAgent opertionAgent = getContext().requireSpecialist(OperationAgent.TYPE);
            final ImportOperation importOperation = opertionAgent.getOperation(ImportOperation.TYPE);
            importOperation.setIgnoreEntities(dontCreateEntities);
            importOperation.setRevisionComment(getImportComment());
            importOperation.setLayerMapper(configureLayerMapper());
            final ImportOperation.Result result = importOperation.perform(getSynchronizationDirectory());
            return new ImportResult(result);
        } catch (final Exception e) { //NOSONAR
            return new ImportResult(e);
        }
    }


    private LayerMapper configureLayerMapper() {
        final LayerMapper layerMapper;
        if (layerMapping == null || layerMapping.trim().isEmpty()) {
            LOGGER.debug("Layer mapping is empty!");
            layerMapper = SchemaUidToNameBasedLayerMapper.empty();
        } else {
            LOGGER.debug("Layer mapping: " + layerMapping);
            final StringPropertiesMap mappingParser = new StringPropertiesMap(layerMapping);
            layerMapper = SchemaUidToNameBasedLayerMapper.from(mappingParser);
        }
        return layerMapper;
    }

    /**
     * Sets the layer mapping.
     *
     * @param layerMapping the new layer mapping
     */
    public void setLayerMapping(final String layerMapping) {
        this.layerMapping = layerMapping;
    }

    /**
     * Sets the creates the project if missing.
     *
     * @param createProjectIfMissing the new creates the project if missing
     */
    public void setCreateProjectIfMissing(final boolean createProjectIfMissing) {
        this.dontCreateProjectIfMissing = !createProjectIfMissing;
    }
}
