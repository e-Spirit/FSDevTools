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

import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.access.store.Store;
import de.espirit.firstspirit.agency.StoreAgent;
import de.espirit.firstspirit.store.access.nexport.operations.ExportOperation;

/**
 * {@link com.github.rvesse.airline.annotations.Command} to export elements from the FirstSpirit mediastore.
 * @author e-Spirit AG
 */
@Command(name = "mediastore", groupNames = {"export"},
        description = "Export FirstSpirit mediastore")
public class ExportMediaCommand extends AbstractExportCommand {

    @Option(name = "--withProjectProperties", override = true, hidden = true, description = "export with project properties like resolutions or fonts")
    private boolean withProjectProperties;

    /**
     * Overrides the default behaviour of {@link #addStoreRoots(StoreAgent, ExportOperation)},
     * because it is used if no further uid arguments are given and a
     * naked ExportMediaCommand should only export the MediaStore.
     *
     * @param storeAgent the StoreAgent to retrieve store roots from
     * @param exportOperation the ExportOperation to add the store roots to
     */
    @Override
    protected void addStoreRoots(StoreAgent storeAgent, ExportOperation exportOperation) {
        exportOperation.addElement(storeAgent.getStore(Store.Type.MEDIASTORE));
    }

    @Override
    protected void logReleaseState(final IDProvider idProvider) {
        getContext().logDebug(idProvider.getUid() + " is release state? " + idProvider.getStore().isRelease());
    }

    @Override
    public ExportResult call() {
        this.getContext().logInfo("Exporting...");
        return exportStoreElements();
    }
}
