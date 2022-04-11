/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2021 e-Spirit GmbH
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
