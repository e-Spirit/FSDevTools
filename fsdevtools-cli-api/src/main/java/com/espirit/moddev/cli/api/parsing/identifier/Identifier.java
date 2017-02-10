package com.espirit.moddev.cli.api.parsing.identifier;

import de.espirit.firstspirit.agency.StoreAgent;
import de.espirit.firstspirit.store.access.nexport.operations.ExportOperation;

/**
 * Shared interface for all identifier implementations. Identifiers can be parsed
 * from a string. Furthermore, the implementation knows how to handle
 * addition to an export operation by itself.
 */
public interface Identifier {
    void addToExportOperation(StoreAgent storeAgent, ExportOperation exportOperation);
}
