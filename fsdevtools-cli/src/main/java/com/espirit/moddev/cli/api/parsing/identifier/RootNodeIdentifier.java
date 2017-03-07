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

package com.espirit.moddev.cli.api.parsing.identifier;

import com.espirit.moddev.cli.api.parsing.exceptions.UnknownRootNodeException;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.agency.StoreAgent;
import de.espirit.firstspirit.store.access.nexport.operations.ExportOperation;
import org.slf4j.LoggerFactory;

import static com.espirit.moddev.cli.api.parsing.parser.RootNodeIdentifierParser.getAllStorePostfixes;

/**
 * Identifier for FirstSpirit store root nodes.
 */
public class RootNodeIdentifier implements Identifier {
    protected static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(RootNodeIdentifier.class);
    public static final String ROOT_NODE_IDENTIFIER = "root";

    private final IDProvider.UidType uidType;

    /**
     * Instantiates a root node identifier.
     *
     * @param uidType {@link IDProvider.UidType} of the uid
     * @throws IllegalArgumentException if uidType or uid is null or blank
     */
    public RootNodeIdentifier(IDProvider.UidType uidType) {
        if (uidType == null) {
            throw new IllegalArgumentException("uidType is null.");
        }
        if(getAllStorePostfixes().inverse().get(uidType) == null) {
            throw new IllegalArgumentException("UidType unknown for " + uidType);
        }
        this.uidType = uidType;
    }

    @Override
    public void addToExportOperation(StoreAgent storeAgent, ExportOperation exportOperation) {
        final IDProvider storeRoot = storeAgent.getStore(uidType.getStoreType());
        if(storeRoot != null) {
            LOGGER.debug("Adding store element: {}", storeRoot);
            exportOperation.addElement(storeRoot);
        } else {
            throw new UnknownRootNodeException("Store root cannot be retrieved for uidType " + uidType.getStoreType());
        }
    }

    @Override
    public String toString() {
        return ROOT_NODE_IDENTIFIER + ":" + getAllStorePostfixes().inverse().get(uidType);
    }

    @Override
    public boolean equals(final Object o) {
        if(o == null || o.getClass() != this.getClass()) {
            return false;
        } else if (this == o) {
            return true;
        } else {
            final RootNodeIdentifier that = (RootNodeIdentifier) o;
            return uidType.equals(that.uidType);
        }
    }

    @Override
    public int hashCode() {
        int result = uidType.hashCode();
        return result;
    }

}
