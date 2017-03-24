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

import com.google.common.base.Strings;
import de.espirit.firstspirit.access.store.Store;
import de.espirit.firstspirit.access.store.contentstore.Content2;
import de.espirit.firstspirit.access.store.contentstore.ContentStoreRoot;
import de.espirit.firstspirit.access.store.contentstore.Dataset;
import de.espirit.firstspirit.access.store.templatestore.Schema;
import de.espirit.firstspirit.agency.StoreAgent;
import de.espirit.firstspirit.store.access.nexport.operations.ExportOperation;
import org.slf4j.LoggerFactory;

/**
 * Identifier for FirstSpirit datasources. The uid parameter of the Content2 object is
 * used to reference datasources.
 */
public class EntitiesIdentifier implements Identifier {
    protected static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(EntitiesIdentifier.class);

    private final String uid;

    /**
     * Instantiates a new identifier for entities.
     * @throws IllegalArgumentException if a null or empty string is passed as uid
     * @param uid the uid of the Content2 object
     */
    public EntitiesIdentifier(String uid) {
        if(Strings.isNullOrEmpty(uid)) {
            throw new IllegalArgumentException("Don't pass an empty or null uid to content2 identifier!");
        }
        this.uid = uid;
    }

    @Override
    public void addToExportOperation(StoreAgent storeAgent, boolean useReleaseState, ExportOperation exportOperation) {
        ContentStoreRoot store = (ContentStoreRoot) storeAgent.getStore(Store.Type.CONTENTSTORE, useReleaseState);

        final Content2 content2 = store.getContent2ByName(uid);
        if(content2 == null) {
            throw new IllegalStateException("Content2 with uid '" + uid + "' couldn't be found.");
        }
        Schema schema = content2.getSchema();
        if(schema == null) {
            throw new IllegalStateException("Schema for content2 object with uid " + uid + " couldn't be found.");
        }
        final ExportOperation.SchemaOptions schemaOptions = exportOperation.addSchema(schema);

        for (Dataset dataset : content2.getDatasets()) {
            schemaOptions.addEntity(dataset.getEntity());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EntitiesIdentifier that = (EntitiesIdentifier) o;

        return uid.equals(that.uid);

    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }
}
