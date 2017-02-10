package com.espirit.moddev.cli.api.parsing.identifier;

import com.espirit.moddev.cli.api.exceptions.IDProviderNotFoundException;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.agency.StoreAgent;
import de.espirit.firstspirit.store.access.nexport.operations.ExportOperation;
import org.slf4j.LoggerFactory;

public class RootNodeIdentifier implements Identifier {
    protected static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(RootNodeIdentifier.class);
    /**
     * Identifier of FirstSpirit store rood nodes.
     */
    public static final String ROOT_NODE_IDENTIFIER = "root";

    private static final BiMap<String, IDProvider.UidType> STORE_POSTFIXES;
    static {
        STORE_POSTFIXES = HashBiMap.create();
        STORE_POSTFIXES.put("templatestore", IDProvider.UidType.TEMPLATESTORE);
        STORE_POSTFIXES.put("pagestore", IDProvider.UidType.PAGESTORE);
        STORE_POSTFIXES.put("contentstore", IDProvider.UidType.CONTENTSTORE);
        STORE_POSTFIXES.put("sitestore", IDProvider.UidType.SITESTORE_FOLDER);
        STORE_POSTFIXES.put("mediastore", IDProvider.UidType.MEDIASTORE_FOLDER);
        STORE_POSTFIXES.put("globalstore", IDProvider.UidType.GLOBALSTORE);
    }

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
        this.uidType = uidType;
    }

    @Override
    public void addToExportOperation(StoreAgent storeAgent, ExportOperation exportOperation) {
        final IDProvider storeRoot = storeAgent.getStore(uidType.getStoreType());
        if(storeRoot != null) {
            LOGGER.debug("Adding store element: " + storeRoot);
            exportOperation.addElement(storeRoot);
        } else {
            throw new IDProviderNotFoundException("IDProvider cannot be retrieved for storeType " + uidType.getStoreType());
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

    /**
     * Retrieves all FirstSpirit store postfix identifiers that are used as export uids.
     * @return a collection of postfixes and UidTypes
     */
    public static BiMap<String, IDProvider.UidType> getAllStorePostfixes() {
        return Maps.unmodifiableBiMap(STORE_POSTFIXES);
    }
}
