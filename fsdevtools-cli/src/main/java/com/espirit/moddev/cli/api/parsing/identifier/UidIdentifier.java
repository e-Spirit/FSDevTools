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

import com.espirit.moddev.cli.api.parsing.exceptions.IDProviderNotFoundException;
import com.espirit.moddev.cli.api.parsing.parser.UidIdentifierParser;
import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.agency.StoreAgent;
import de.espirit.firstspirit.store.access.nexport.operations.ExportOperation;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;

/**
 * FirstSpirit's uids are unique across all stores only in conjunction with their {@link de.espirit.firstspirit.access.store.IDProvider.UidType}.
 * This class encapsulates a uid and its {@link de.espirit.firstspirit.access.store.IDProvider.UidType} and therewith provides a full qualified representation of the uid.
 * It also allows to parse an arbitrary number of {@link java.lang.String} representations of combinations of uids and {@link de.espirit.firstspirit.access.store.IDProvider.UidType}s to instances of this class.
 *
 * @author e-Spirit AG
 */
public class UidIdentifier implements Identifier {
    protected static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(UidIdentifier.class);

    private final IDProvider.UidType uidType;
    private final String uid;

    /**
     * Instantiates a new full qualified uid.
     *
     * @param uidType {@link de.espirit.firstspirit.access.store.IDProvider.UidType} of the uid
     * @param uid the uid
     * @throws IllegalArgumentException if uidType or uid is null or blank
     */
    public UidIdentifier(final IDProvider.UidType uidType, final String uid) {
        if (uidType == null) {
            throw new IllegalArgumentException("uidType is null.");
        }
        if (StringUtils.isBlank(uid)) {
            throw new IllegalArgumentException("Uid is null or empty.");
        }
        this.uidType = uidType;
        this.uid = uid;
    }

    /**
     * Get the {@link de.espirit.firstspirit.access.store.IDProvider.UidType} of this uid.
     *
     * @return the {@link de.espirit.firstspirit.access.store.IDProvider.UidType} of this uid.
     */
    public IDProvider.UidType getUidType() {
        return uidType;
    }

    /**
     * Get the uid.
     *
     * @return the uid
     */
    public String getUid() {
        return uid;
    }

    @Override
    public boolean equals(final Object o) {
        if(o == null || o.getClass() != this.getClass()) {
            return false;
        } else if (this == o) {
            return true;
        } else {
            final UidIdentifier that = (UidIdentifier) o;
            return uidType.equals(that.uidType) && uid.equals(that.uid);
        }
    }

    @Override
    public int hashCode() {
        int result = uidType.hashCode();
        result = 31 * result + uid.hashCode(); //NOSONAR
        return result;
    }

    @Override
    public String toString() {
        return UidIdentifierParser.getPrefixForUidType(uidType) + ":" + uid;
    }

    @Override
    public void addToExportOperation(StoreAgent storeAgent, ExportOperation exportOperation) {
        final IDProvider storeElement = storeAgent.getStore(getUidType().getStoreType()).getStoreElement(getUid(), getUidType());
        if(storeElement != null) {
            LOGGER.debug("Adding store element: " + storeElement);
            exportOperation.addElement(storeElement);
        } else {
            throw new IDProviderNotFoundException("IDProvider cannot be retrieved for " + uid);
        }
    }
}
