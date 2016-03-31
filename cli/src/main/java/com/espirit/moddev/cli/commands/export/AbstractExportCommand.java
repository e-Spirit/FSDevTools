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
import com.espirit.moddev.cli.api.FullQualifiedUid;
import com.espirit.moddev.cli.results.ExportResult;
import com.github.rvesse.airline.annotations.Option;
import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.access.store.Store;
import de.espirit.firstspirit.agency.StoreAgent;
import de.espirit.firstspirit.store.access.nexport.operations.ExportOperation;
import de.espirit.firstspirit.transport.PropertiesTransportOptions;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * This class gathers shared logic and options for different export commands. It can
 * be extended for custom implementations of uid filtering, or to override configurations.
 *
 * @author e-Spirit AG
 */
public abstract class AbstractExportCommand extends SimpleCommand<ExportResult> {

    @Option(name = "--withoutDeleteObsoleteFiles", description = "delete without obsolete files")
    protected boolean withoutDeleteObsoleteFiles;

    @Option(name = "--withoutChildElements", description = "export without child elements")
    protected boolean withoutExportChildElements;

    @Option(name = "--withoutParentElements", description = "export without parent elements")
    protected boolean withoutExportParentElements;

    @Option(name = "--withoutReleaseEntities", description = "export without release entities")
    private boolean withoutExportReleaseEntities;

    @Option(name = "--withProjectProperties", description = "export with project properties like resolutions or fonts")
    private boolean withProjectProperties;


    public boolean getDeleteObsoleteFiles() {
        return !withoutDeleteObsoleteFiles;
    }

    public boolean getExportChildElements() {
        return !withoutExportChildElements;
    }

    public boolean getExportParentElements() {
        return !withoutExportParentElements;
    }

    public boolean getExportReleaseEntities() {
        return !withoutExportReleaseEntities;
    }

    /**
     * Retrieves IDProviders via the StoreAgent instance. Uses the result of {@link #getFullQualifiedUids()}
     * to query all stores.
     *
     * @param storeAgent that is used to search IDProviders
     * @return a list of IDProviders that match the uid parameters
     * @throws IllegalArgumentException if no IDProvider can be retrieved for a uid
     */
    public List<IDProvider> filterByUIDs(StoreAgent storeAgent){
        List<IDProvider> result = new ArrayList<>();
        for(FullQualifiedUid uid : getFullQualifiedUids()) {
            if(uid.getUid().equals(FullQualifiedUid.ROOT_NODE_IDENTIFIER)) {
                result.add(storeAgent.getStore(uid.getUidType().getStoreType()));
            } else {
                IDProvider storeElement = storeAgent.getStore(uid.getUidType().getStoreType()).getStoreElement(uid.getUid(), uid.getUidType());
                if(storeElement != null) {
                    result.add(storeElement);
                } else {
                    throw new IllegalArgumentException("IDProvider cannot be retrieved for " + uid);
                }
            }
        }
        return result;
    }

    protected void logReleaseState(final IDProvider idProvider) {
        getContext().logDebug(idProvider.getUid() + " is release state? " + idProvider.getStore().isRelease());
    }

    /**
     * Adds elements to the given ExportOperation. Calls {@link #addExportElements(StoreAgent, List, ExportOperation)}
     * with the command's uid arguments.
     *
     * @param storeAgent the StoreAgent to retrieve IDProviders with
     * @param exportOperation the ExportOperation to add the elements to
     */
    public void addExportElements(final StoreAgent storeAgent, final ExportOperation exportOperation) {
        addExportElements(storeAgent, getFullQualifiedUids(), exportOperation);
    }

    /**
     * Adds elements to the given export operation. Uses {@link #filterByUIDs(StoreAgent)} to retrieve
     * IDProviders matching the {@code uids} parameter.
     *
     * @param storeAgent the StoreAgent to retrieve IDProviders with
     * @param uids the identifiers of elements that should be added to the ExportOperation
     * @param exportOperation the ExportOperation to add the elements to
     * @throws IllegalArgumentException if the ExportOperation is null
     * @throws IllegalStateException if no IDProviders exist for the given parameters
     */
    public void addExportElements(final StoreAgent storeAgent, final List<FullQualifiedUid> uids, final ExportOperation exportOperation) {
        if(exportOperation == null) {
            throw new IllegalArgumentException("No null ExportOperation allowed");
        }

        getContext().logDebug("Adding export elements...");
        if (uids.isEmpty()) {
            getContext().logDebug("Adding store roots...");
            addStoreRoots(storeAgent, exportOperation);
        } else {
            getContext().logDebug("addExportedElements - UIDs " + uids);
            List<IDProvider> elements = filterByUIDs(storeAgent);
            if(elements.isEmpty()) {
                throw new IllegalStateException("No IDProviders can be retrieved for the given arguments");
            }
            for (IDProvider element : elements) {
                getContext().logDebug("Adding store element: " + element);
                exportOperation.addElement(element);
            }
        }

        if(isWithProjectProperties()) {
            addProjectProperties(exportOperation);
        }
    }

    public static void addProjectProperties(ExportOperation exportOperation) {
        final PropertiesTransportOptions options = exportOperation.configurePropertiesExport();
        final EnumSet<PropertiesTransportOptions.ProjectPropertyType>
                propertiesToTransport = EnumSet.allOf(PropertiesTransportOptions.ProjectPropertyType.class);
        options.setProjectPropertiesTransport(propertiesToTransport);
    }

    /**
     * Adds store root nodes to the given export operation directly. This operation is
     * different from adding a store root node's children individually. This method
     * can be overridden to add a subset of store roots only.
     *
     * @param storeAgent the StoreAgent to retrieve store roots from
     * @param exportOperation the ExportOperation to add the store roots to
     */
    protected void addStoreRoots(StoreAgent storeAgent, ExportOperation exportOperation) {
        for(Store.Type storeType : Store.Type.values()) {
            exportOperation.addElement(storeAgent.getStore(storeType));
        }
    }

    public void setWithProjectProperties(boolean withProjectProperties) {
        this.withProjectProperties = withProjectProperties;
    }
    public boolean isWithProjectProperties() {
        return withProjectProperties;
    }
}
