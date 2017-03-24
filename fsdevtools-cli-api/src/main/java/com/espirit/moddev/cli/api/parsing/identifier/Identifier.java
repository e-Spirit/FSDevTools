package com.espirit.moddev.cli.api.parsing.identifier;

import de.espirit.firstspirit.access.store.Store;
import de.espirit.firstspirit.agency.StoreAgent;
import de.espirit.firstspirit.store.access.nexport.operations.ExportOperation;

/**
 * Shared interface for all identifier implementations. Identifiers can be parsed
 * from a string. Furthermore, the implementation knows how to handle
 * addition to an export operation by itself.
 */
public interface Identifier {

    /**
     * Add element(s) specified by this identifier to given export operation. Keep in mind to request elements in correct
     * release state considering given parameter {@code exportReleaseState}.
     *
     * @param storeAgent store agent to request elements from
     * @param useReleaseState indicates whether to request elements from {@link Store#isRelease() release} or current store via given {@link StoreAgent}
     * @param exportOperation export operation to pass elements to
     */
    void addToExportOperation(StoreAgent storeAgent, boolean useReleaseState, ExportOperation exportOperation);
}
