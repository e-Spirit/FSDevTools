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

package com.espirit.moddev.cli.commands.export;

import com.espirit.moddev.cli.commands.SimpleCommand;
import com.espirit.moddev.cli.results.ExportResult;
import com.github.rvesse.airline.annotations.Command;
import de.espirit.firstspirit.agency.OperationAgent;
import de.espirit.firstspirit.store.access.nexport.operations.ExportOperation;
import org.apache.log4j.Logger;


/**
 * The type Project properties command.
 *
 * @author e-Spirit AG
 */
@Command(name = "projectproperties", groupNames = "export", description = "Exports the project's properties, like resolutions, fonts etc.")
public class ExportProjectPropertiesCommand extends SimpleCommand<ExportResult> {

    private static final Logger LOGGER = Logger.getLogger(ExportProjectPropertiesCommand.class);

    @Override
    public ExportResult call() {
        LOGGER.info("Exporting...");
        try {
            final ExportOperation exportOperation = getContext().requireSpecialist(OperationAgent.TYPE).getOperation(ExportOperation.TYPE);
            AbstractExportCommand.addProjectProperties(exportOperation);
            final ExportOperation.Result result = exportOperation.perform(getSynchronizationDirectory());
            return new ExportResult(result);
        } catch (Exception e) { //NOSONAR
            return new ExportResult(e);
        }
    }
}
