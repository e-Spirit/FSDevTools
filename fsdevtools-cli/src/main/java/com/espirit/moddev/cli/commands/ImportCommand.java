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

import com.espirit.moddev.cli.api.configuration.ImportConfig;
import com.espirit.moddev.cli.configuration.CliConstants;
import com.espirit.moddev.cli.results.ImportResult;
import com.github.rvesse.airline.annotations.Option;

import de.espirit.firstspirit.agency.OperationAgent;
import de.espirit.firstspirit.store.access.nexport.operations.ImportOperation;


/**
 * Command that executes a FirstSpirit ImportOperation. Uses a FirstSpirit context.
 *
 * @author e-Spirit AG
 */
@com.github.rvesse.airline.annotations.Command(name = "import", description = "Import FirstSpirit project")
public class ImportCommand extends SimpleCommand<ImportResult> implements ImportConfig {

    @Option(name = {"-i", "--import-comment"}, description = "Import comment for FirstSpirit revision")
    private String importComment;

    @Option(name = {"--dont-create-project"}, description = "Do not create project in FirstSpirit if it is missing")
    private boolean dontCreateProjectIfMissing;

    @Option(name = {"--dont-create-entities"}, description = "Don not create entities when importing")
    private boolean dontCreateEntities;

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
        if (getEnvironment().containsKey(CliConstants.KEY_FS_IMPORT_COMMENT.value())) {
            return getEnvironment().get(CliConstants.KEY_FS_IMPORT_COMMENT.value()).trim();
        }
        return importComment;
    }

    @Override
    public ImportResult call() {
        getContext().logInfo("Importing...");
        try {
            final ImportOperation importOperation = getContext().requireSpecialist(OperationAgent.TYPE).getOperation(ImportOperation.TYPE);
            importOperation.setCreateEntities(isCreateEntities());
            importOperation.setRevisionComment(getImportComment());
            //TODO: add importOperation setDatabaseLayerMapper()
            final ImportOperation.Result result = importOperation.perform(getSynchronizationDirectory());
            return new ImportResult(result);
        } catch (final Exception e) { //NOSONAR
            return new ImportResult(e);
        }
    }

}
