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

import com.espirit.moddev.cli.StringPropertiesMap;
import com.espirit.moddev.cli.api.configuration.ImportConfig;
import com.espirit.moddev.cli.CliConstants;
import com.espirit.moddev.cli.results.ImportResult;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;

import de.espirit.firstspirit.agency.OperationAgent;
import de.espirit.firstspirit.store.access.nexport.operations.ImportOperation;
import de.espirit.firstspirit.transport.LayerMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Function;


/**
 * Command that executes a FirstSpirit ImportOperation. Uses a FirstSpirit context.
 *
 * @author e-Spirit AG
 */
@com.github.rvesse.airline.annotations.Command(name = "import", description = "Import FirstSpirit project")
public class ImportCommand extends SimpleCommand<ImportResult> implements ImportConfig {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ImportCommand.class);

    @Option(name = {"-i", "--import-comment"}, description = "Import comment for FirstSpirit revision")
    private String importComment;

    @Option(name = {"--dont-create-project"}, description = "Do not create project in FirstSpirit if it is missing")
    private boolean dontCreateProjectIfMissing;

    @Option(name = {"--dont-create-entities"}, description = "Don not create entities when importing")
    private boolean dontCreateEntities;

    // This has to be a class with a string-argument-constructor
    @Option(name = {"-lm", "--layerMapping"}, description = "Defines how layers should be mapped, comma-separated key-value pairs", type = OptionType.COMMAND)
    private StringPropertiesMap layerMapping = new StringPropertiesMap("");


    @Option(name = {"-lmt", "--layerMappingType"}, description = "Configures if layer name based or schema uid based mapping should be used. Default is layer name based.", type = OptionType.COMMAND)
    private LayerMappingType layerMappingType = LayerMappingType.LAYER_NAME_BASED;


    @Override
    public boolean isCreateEntities() {
        return !dontCreateEntities;
    }

    @Override
    public boolean isCreatingProjectIfMissing() {
        return !dontCreateProjectIfMissing;
    }

    @Override
    public String getImportComment() {
        if(importComment == null || importComment.isEmpty()) {
            boolean environmentContainsImportComment = getEnvironment().containsKey(CliConstants.KEY_FS_IMPORT_COMMENT.value());
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
            final ImportOperation importOperation = getContext().requireSpecialist(OperationAgent.TYPE).getOperation(ImportOperation.TYPE);
            importOperation.setCreateEntities(isCreateEntities());
            importOperation.setRevisionComment(getImportComment());

            LayerMapper layerMapper = LayerMapper.CREATE_NEW_DEFAULT_LAYER_MAPPER;
            if(!layerMapping.isEmpty()) {
                LOGGER.info("Found layermapping...");
                LOGGER.info("Using layermapping type " + layerMappingType);
                layerMapper = layerMappingType.getLayerMapper(layerMapping);
            }

            importOperation.setLayerMapper(layerMapper);
            final ImportOperation.Result result = importOperation.perform(getSynchronizationDirectory());
            return new ImportResult(result);
        } catch (final Exception e) { //NOSONAR
            return new ImportResult(e);
        }
    }

    public void setLayerMapping(StringPropertiesMap layerMapping) {
        this.layerMapping = layerMapping;
    }

    public enum LayerMappingType {
        LAYER_NAME_BASED((Map<String, String> layerMapping) -> LayerMapper.LayerNameBasedLayerMapper.from(layerMapping)),
        SCHEMA_UID_BASED((Map<String, String> layerMapping) -> LayerMapper.SchemaUidBasedLayerMapper.from(layerMapping));

        private final Function<Map<String, String>, LayerMapper> layerMapper;

        LayerMappingType(Function<Map<String, String>, LayerMapper> layerMapper) {
            this.layerMapper = layerMapper;
        }

        public LayerMapper getLayerMapper(Map<String, String> layerMapping) {
            return layerMapper.apply(layerMapping);
        }
    }

    public void setCreateProjectIfMissing(boolean createProjectIfMissing) {
        this.dontCreateProjectIfMissing = !createProjectIfMissing;
    }
}
