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

import com.espirit.moddev.cli.api.FullQualifiedUid;
import com.espirit.moddev.cli.api.exceptions.IDProviderNotFoundException;
import com.espirit.moddev.cli.commands.SimpleCommand;
import com.espirit.moddev.cli.results.ExportResult;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Option;

import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.access.store.Store;
import de.espirit.firstspirit.agency.OperationAgent;
import de.espirit.firstspirit.agency.StoreAgent;
import de.espirit.firstspirit.store.access.nexport.operations.ExportOperation;
import de.espirit.firstspirit.transport.PropertiesTransportOptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

/**
 * This class gathers shared logic and options for different export commands. It can be extended for custom implementations of uid filtering, or to
 * override configurations.
 *
 * @author e -Spirit AG
 */
public abstract class AbstractExportCommand extends SimpleCommand<ExportResult> {

    protected static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Option(name = "--keepObsoleteFiles", description = "keep obsolete files in sync dir which are deleted in project")
    private boolean keepObsoleteFiles;

    @Option(name = "--excludeChildElements", description = "exclude child store elements")
    private boolean excludeChildElements;

    @Option(name = "--excludeParentElements", description = "exclude parent store elements")
    private boolean excludeParentElements;

    @Option(name = "--useReleaseState",
            description = "export only the release state of store elements; default is false (export of current state)")
    private boolean exportReleaseState;

    @Option(name = "--includeProjectProperties", description = "export with project properties like resolutions or fonts")
    private boolean includeProjectProperties;

    @Arguments(title = "uids", description = "A list of unique identifiers, in the form of 'pagetemplate:default' (<uid type>:<uid>)")
    private List<String> fullQualifiedUidsAsStrings = new LinkedList<>();

    /**
     * Gets delete obsolete files.
     *
     * @return the delete obsolete files
     */
    public boolean isDeleteObsoleteFiles() {
        return !keepObsoleteFiles;
    }

    /**
     * Gets export child elements.
     *
     * @return the export child elements
     */
    public boolean isExportChildElements() {
        return !excludeChildElements;
    }

    /**
     * Gets export parent elements.
     *
     * @return the export parent elements
     */
    public boolean isExportParentElements() {
        return !excludeParentElements;
    }

    /**
     * Indicates whether the release state should be used for belonging ExportOperation.
     *
     * @return true: operate on release state or false (default): on the current state.
     */
    public boolean isExportReleaseState() {
        return exportReleaseState;
    }


    /**
     * Defines whether this command should export the release state or not (default)
     *
     * @param exportReleaseState use {@code true} to export release state, {@code false} otherwise (default)
     * @see #isExportReleaseState()
     */
    public void setExportReleaseState(boolean exportReleaseState) {
        this.exportReleaseState = exportReleaseState;
    }

    /**
     * Retrieves IDProviders via the StoreAgent instance. Uses the result of {@link #getFullQualifiedUids()} to query all stores.
     *
     * @param storeAgent that is used to search for IDProviders
     * @return a list of IDProviders that match the uid parameters
     * @throws IDProviderNotFoundException if no IDProvider can be retrieved for a given uid
     */
    protected List<IDProvider> getIDProvidersForFullQualifiedUids(final StoreAgent storeAgent) {
        return getIDProvidersForFullQualifiedUids(storeAgent, getFullQualifiedUids());
    }
    /**
     * Retrieves IDProviders via the StoreAgent instance. Queries all stores for objects corresponding to fullQualifiedUids.
     *
     * @param storeAgent that is used to search for IDProviders
     * @param fullQualifiedUids the uids used to query the store
     * @return a list of IDProviders that match the uid parameters
     * @throws IDProviderNotFoundException if no IDProvider can be retrieved for a given uid
     */
    protected List<IDProvider> getIDProvidersForFullQualifiedUids(final StoreAgent storeAgent, List<FullQualifiedUid> fullQualifiedUids) {
        final List<IDProvider> result = new ArrayList<>();
        for (final FullQualifiedUid uid : fullQualifiedUids) {
            if(uid.getUid().equals(FullQualifiedUid.ROOT_NODE_IDENTIFIER)) {
                result.add(storeAgent.getStore(uid.getUidType().getStoreType()));
            } else {
                final IDProvider storeElement = storeAgent.getStore(uid.getUidType().getStoreType()).getStoreElement(uid.getUid(), uid.getUidType());
                if(storeElement != null) {
                    result.add(storeElement);
                } else {
                    throw new IDProviderNotFoundException("IDProvider cannot be retrieved for " + uid);
                }
            }
        }
        return result;
    }

    /**
     * Log release state.
     *
     * @param idProvider the id provider
     */
    protected void logReleaseState(final IDProvider idProvider) {
        LOGGER.debug(idProvider.getUid() + " is release state? " + idProvider.getStore().isRelease());
    }

    /**
     * Adds elements to the given ExportOperation. Calls {@link #addExportElements(StoreAgent, List, ExportOperation)} with the command's uid
     * arguments.
     *
     * @param storeAgent      the StoreAgent to retrieve IDProviders with
     * @param exportOperation the ExportOperation to add the elements to
     */
    public void addExportElements(final StoreAgent storeAgent, final ExportOperation exportOperation) {
        addExportElements(storeAgent, getFullQualifiedUids(), exportOperation);
    }

    /**
     * Adds elements to the given export operation. Uses {@link #getIDProvidersForFullQualifiedUids(StoreAgent)} to retrieve IDProviders matching the {@code
     * fullQualifiedUidsAsStrings} parameter. {@link IDProviderNotFoundException} from #getIDProvidersForFullQualifiedUids(StoreAgent) is handled by aborting
     * the whole operation.
     *
     * @param storeAgent      the StoreAgent to retrieve IDProviders with
     * @param uids            the identifiers of elements that should be added to the ExportOperation
     * @param exportOperation the ExportOperation to add the elements to
     * @throws IllegalArgumentException if the ExportOperation is null
     */
    public void addExportElements(final StoreAgent storeAgent, final List<FullQualifiedUid> uids, final ExportOperation exportOperation) {
        if(exportOperation == null) {
            throw new IllegalArgumentException("No null ExportOperation allowed");
        }

        LOGGER.debug("Adding export elements...");
        if (uids.isEmpty()) {
            LOGGER.debug("Adding store roots...");
            addStoreRoots(storeAgent, exportOperation);
        } else {
            LOGGER.debug("addExportedElements - UIDs " + uids);
            try {
                final List<IDProvider> elements = getIDProvidersForFullQualifiedUids(storeAgent, uids);
                for (final IDProvider element : elements) {
                    LOGGER.debug("Adding store element: " + element);
                    exportOperation.addElement(element);
                }
                LOGGER.debug("Added " + elements.size() + " elements");
            } catch (IDProviderNotFoundException e) {
                LOGGER.error("Cannot retrieve IDProvider for one or more given uids. No elements added to the export operation.", e);
            }
        }

        if (isIncludeProjectProperties()) {
            addProjectProperties(exportOperation);
        }
    }

    /**
     * Get a list of {@link com.espirit.moddev.cli.api.FullQualifiedUid}s that specify the elements that should be synchronized.
     *
     * @return a {@link java.util.List} of {@link com.espirit.moddev.cli.api.FullQualifiedUid}s that specify the elements that should be synchronized
     */
    public List<FullQualifiedUid> getFullQualifiedUids() {
        return fullQualifiedUidsAsStrings.isEmpty() ? Collections.emptyList() : FullQualifiedUid.parse(fullQualifiedUidsAsStrings);
    }

    /**
     * Add project properties.
     *
     * @param exportOperation the export operation
     */
    public static void addProjectProperties(final ExportOperation exportOperation) {
        final PropertiesTransportOptions options = exportOperation.configurePropertiesExport();
        final EnumSet<PropertiesTransportOptions.ProjectPropertyType>
                propertiesToTransport = EnumSet.allOf(PropertiesTransportOptions.ProjectPropertyType.class);
        options.setProjectPropertiesTransport(propertiesToTransport);
    }

    /**
     * Adds store root nodes to the given export operation directly. This operation is different from adding a store root node's children
     * individually. This method can be overridden to add a subset of store roots only.
     *
     * @param storeAgent      the StoreAgent to retrieve store roots from
     * @param exportOperation the ExportOperation to add the store roots to
     */
    protected void addStoreRoots(final StoreAgent storeAgent, final ExportOperation exportOperation) {
        for (final Store.Type storeType : Store.Type.values()) {
            exportOperation.addElement(storeAgent.getStore(storeType));
        }
    }

    /**
     * Sets include project properties.
     *
     * @param includeProjectProperties the with project properties
     */
    public void setIncludeProjectProperties(final boolean includeProjectProperties) {
        this.includeProjectProperties = includeProjectProperties;
    }

    /**
     * Include project properties.
     *
     * @return the boolean
     */
    public boolean isIncludeProjectProperties() {
        return includeProjectProperties;
    }

    /**
     * Creates an {@link de.espirit.firstspirit.store.access.nexport.operations.ExportOperation} based on the current configuration and exports the
     * elements to the file system.
     *
     * @return the export result
     */
    protected ExportResult exportStoreElements() {
        try {
            final ExportOperation exportOperation = this.getContext().requireSpecialist(OperationAgent.TYPE).getOperation(ExportOperation.TYPE);
            exportOperation.setDeleteObsoleteFiles(isDeleteObsoleteFiles());
            exportOperation.setExportChildElements(isExportChildElements());
            exportOperation.setExportParentElements(isExportParentElements());
            exportOperation.setExportRelease(isExportReleaseState());

            addExportElements(getContext().requireSpecialist(StoreAgent.TYPE), exportOperation);

            final ExportOperation.Result result = exportOperation.perform(getSynchronizationDirectory());

            return new ExportResult(result);
        } catch (final Exception e) {
            return new ExportResult(e); // NOSONAR
        }
    }

    /**
     * Adds the given string based FullQualifiedUid to this command's argument list. This method doesn't validate the input at all.
     *
     * @param fullQualifiedUid the string based FullQualifiedUid that should be added to this command's argument list
     */
    public void addUid(final String fullQualifiedUid) {
        fullQualifiedUidsAsStrings.add(fullQualifiedUid);
    }

}
