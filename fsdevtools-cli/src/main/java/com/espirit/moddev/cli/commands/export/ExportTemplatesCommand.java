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

    // This field is not used here, but the annotation overrides the visibility of the option
    @Option(name = "--includeProjectProperties", override = true, hidden = true, description = "export with project properties like resolutions or fonts")
    private boolean includeProjectProperties;

    // This field is not used here, but the annotation overrides the visibility of the option
    @Option(name = "--excludeCurrentState", override = true, hidden = true, description = "export without release entities")
    private boolean excludeCurrentState;

    public ExportTemplatesCommand() {
    }

    @Override
    public ExportResult call() {
        LOGGER.info("Exporting...");
        return exportStoreElements();
    }

    private boolean exportFullTemplateStore() {
        return !withoutExportFullTemplateStore;
    }

    private void addDatabaseDefinitions(final ExportOperation exportOperation, final TemplateStoreRoot store) {
        addTableDefinitions(exportOperation, store);
        addDataSourceDefinitions(exportOperation);
    }

    private void addTableDefinitions(ExportOperation exportOperation, TemplateStoreRoot store) {
        LOGGER.debug("Adding schemes...");
        for (final StoreElement storeElement : store.getSchemes().getChildren()) {
            exportOperation.addSchema((Schema) storeElement);
        }
    }

    private void addDataSourceDefinitions(ExportOperation exportOperation) {
        LOGGER.debug("Adding datasource definitions...");
        final Listable<StoreElement> contentStoreChildren =
            getContext().requireSpecialist(StoreAgent.TYPE).getStore(Store.Type.CONTENTSTORE, false).getChildren();
        addChildren(exportOperation, contentStoreChildren);
    }

    /**
     * Calls the super implementation and adds datasource definitions, when {@link #exportFullTemplateStore} is true.
     * @param storeAgent      the StoreAgent to retrieve IDProviders with
     * @param exportOperation the ExportOperation to add the elements to
     */
    @Override
    public void addExportElements(final StoreAgent storeAgent, final ExportOperation exportOperation) {
        super.addExportElements(storeAgent, getIdentifiers(), exportOperation);
        if(exportFullTemplateStore()) {
            addDatabaseDefinitions(exportOperation, (TemplateStoreRoot) storeAgent.getStore(Store.Type.TEMPLATESTORE).getStore());
        }
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
