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


import com.espirit.moddev.cli.results.ExportResult;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import de.espirit.common.util.Listable;
import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.access.store.Store;
import de.espirit.firstspirit.access.store.StoreElement;
import de.espirit.firstspirit.access.store.templatestore.Schema;
import de.espirit.firstspirit.access.store.templatestore.TemplateStoreRoot;
import de.espirit.firstspirit.agency.OperationAgent;
import de.espirit.firstspirit.agency.StoreAgent;
import de.espirit.firstspirit.store.access.nexport.operations.ExportOperation;

/**
 * {@link com.github.rvesse.airline.annotations.Command} to export elements from the FirstSpirit template store.
 * @author e-Spirit AG
 */
@Command(name = "templatestore", groupNames = {"export"},
        description = "Export FirstSpirit templatestore")
public class ExportTemplatesCommand extends AbstractExportCommand {

    @Option(name = "--withoutFullTemplateStore", description = "export without full templatestore")
    private boolean withoutExportFullTemplateStore;

    @Option(name = "--withProjectProperties", override = true, hidden = true, description = "export with project properties like resolutions or fonts")
    private boolean withProjectProperties;

    @Option(name = "--withoutReleaseEntities", override = true, hidden = true, description = "export without release entities")
    private boolean withoutExportReleaseEntities;

    @Override
    public ExportResult call() {
        this.getContext().logInfo("Exporting...");
        try {
            final ExportOperation exportOperation = this.getContext().requireSpecialist(OperationAgent.TYPE).getOperation(ExportOperation.TYPE);
            exportOperation.setDeleteObsoleteFiles(getDeleteObsoleteFiles());
            exportOperation.setExportChildElements(getExportChildElements());
            exportOperation.setExportParentElements(getExportParentElements());
            exportOperation.setExportReleaseEntities(getExportReleaseEntities());

            addExportElements(getContext().requireSpecialist(StoreAgent.TYPE), exportOperation);

            final ExportOperation.Result result = exportOperation.perform(getSynchronizationDirectory());

            return new ExportResult(result);
        } catch (Exception e) { //NOSONAR
            return new ExportResult(e);
        }
    }

    private boolean exportFullTemplateStore() {
        return !withoutExportFullTemplateStore;
    }

    private void addDatabaseDefinitions(final ExportOperation exportOperation, final TemplateStoreRoot store) {
        addTableDefinitions(exportOperation, store);
        addDataSourceDefinitions(exportOperation);
    }

    private void addTableDefinitions(ExportOperation exportOperation, TemplateStoreRoot store) {
        context.logDebug("Adding schemes...");
        for (final StoreElement storeElement : store.getSchemes().getChildren()) {
            exportOperation.addSchema((Schema) storeElement);
        }
    }

    private void addDataSourceDefinitions(ExportOperation exportOperation) {
        //add all datasource definitions
        getContext().logDebug("Adding datasource definitions...");
        final Listable<StoreElement> contentStoreChildren =
            getContext().requireSpecialist(StoreAgent.TYPE).getStore(Store.Type.CONTENTSTORE, false).getChildren();
        addChildren(exportOperation, contentStoreChildren);
    }

    @Override
    protected void addStoreRoots(StoreAgent storeAgent, ExportOperation exportOperation) {
        exportOperation.addElement(storeAgent.getStore(Store.Type.TEMPLATESTORE));
    }

    /**
     * add all elements of the list to the export operation thread.
     *
     * @param operation     that is used by the export thread
     * @param storeElements that should be added to the export operation
     */
    private static void addChildren(final ExportOperation operation, final Listable<StoreElement> storeElements) {
        for (final StoreElement storeElement : storeElements) {
            if (storeElement instanceof IDProvider) {
                operation.addElement((IDProvider) storeElement);
            }
        }
    }
}
