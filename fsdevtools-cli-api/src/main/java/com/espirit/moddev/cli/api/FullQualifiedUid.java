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

package com.espirit.moddev.cli.api;

import com.espirit.moddev.cli.api.exceptions.UnknownRootNodeException;
import com.espirit.moddev.cli.api.exceptions.UnregisteredPrefixException;

import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.access.store.ReferenceType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * FirstSpirit's uids are unique across all stores only in conjunction with their {@link de.espirit.firstspirit.access.store.IDProvider.UidType}.
 * This class encapsulates a uid and its {@link de.espirit.firstspirit.access.store.IDProvider.UidType} and therewith provides a full qualified representation of the uid.
 * It also allows to parse an arbitrary number of {@link java.lang.String} representations of combinations of uids and {@link de.espirit.firstspirit.access.store.IDProvider.UidType}s to instances of this class.
 *
 * @author e-Spirit AG
 */
public class FullQualifiedUid {

    /**
     * Identifier of FirstSpirit store rood nodes.
     */
    public static final String ROOT_NODE_IDENTIFIER = "root";

    private static final Logger LOGGER = Logger.getLogger(FullQualifiedUid.class);
    private static final Map<String, IDProvider.UidType> STORE_POSTFIXES;
    private static final Pattern DELIMITER = Pattern.compile("\\s*:\\s*");

    /**
     * This collection stores a custom mapping from uid prefixes to UidTypes.
     * The collection can be used to add or override prefixes for later usage.
     * It also includes store postfixes, even if those aren't used as prefix.
     */
    private static final Map<String, IDProvider.UidType> CUSTOM_PREFIX_UIDTYPE_MAPPINGS;

    /**
     * This block initializes custom mappings between prefixes or postfixes and UidTypes.
     * They are used for cases, where FirstSpirit doesn't provide a default mapping.
     */
    static {
        STORE_POSTFIXES = new HashMap<>();
        STORE_POSTFIXES.put("templatestore", IDProvider.UidType.TEMPLATESTORE);
        STORE_POSTFIXES.put("pagestore", IDProvider.UidType.PAGESTORE);
        STORE_POSTFIXES.put("contentstore", IDProvider.UidType.CONTENTSTORE);
        STORE_POSTFIXES.put("sitestore", IDProvider.UidType.SITESTORE_FOLDER);
        STORE_POSTFIXES.put("mediastore", IDProvider.UidType.MEDIASTORE_FOLDER);
        STORE_POSTFIXES.put("globalstore", IDProvider.UidType.GLOBALSTORE);

        CUSTOM_PREFIX_UIDTYPE_MAPPINGS = new HashMap<>();
        CUSTOM_PREFIX_UIDTYPE_MAPPINGS.put("page", IDProvider.UidType.PAGESTORE);
        CUSTOM_PREFIX_UIDTYPE_MAPPINGS.put("pagetemplate", IDProvider.UidType.TEMPLATESTORE);
        CUSTOM_PREFIX_UIDTYPE_MAPPINGS.putAll(STORE_POSTFIXES);
    }

    protected static final Map<String, IDProvider.UidType> KNOWN_PREFIXES = calculateKnownPrefixes();

    private final IDProvider.UidType uidType;
    private final String uid;

    /**
     * Instantiates a new full qualified uid.
     *
     * @param uidType {@link de.espirit.firstspirit.access.store.IDProvider.UidType} of the uid
     * @param uid the uid
     * @throws IllegalArgumentException if uidType or uid is null or blank
     */
    public FullQualifiedUid(final IDProvider.UidType uidType, final String uid) {
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
     * Parse a list of full qualified uid strings.
     * The strings must match the following pattern:<br>
     * <code>&lt;TYPE_PREFIX&gt;:&lt;UID&gt;</code><br>
     * The allowed values for <code>TYPE_PREFIX</code> are defined by {@link #getAllKnownPrefixStrings()}.
     *
     * @param fullQualifiedUids the {@link java.util.List} of full qualified uids following the above pattern
     * @throws IllegalArgumentException if fullQualifiedUids is null or if a string does not follow the above pattern
     * @throws UnknownRootNodeException if a requested store root does not exist
     * @return the {@link java.util.List} of objects of this class representing fullQualifiedUids
     */
    public static List<FullQualifiedUid> parse(final List<String> fullQualifiedUids) {
        if (fullQualifiedUids == null) {
            throw new IllegalArgumentException("fullQualifiedUids is null!");
        }
        if (fullQualifiedUids.isEmpty()) {
            return Collections.emptyList();
        }

        final List<FullQualifiedUid> list = new ArrayList<>(fullQualifiedUids.size());
        for (final String fullQualifiedUid : fullQualifiedUids) {
            try(Scanner uidScanner = new Scanner(fullQualifiedUid)) {
                uidScanner.useDelimiter(DELIMITER);
                if (uidScanner.hasNext()) {
                    final String firstPart = uidScanner.next();
                    if (uidScanner.hasNext()) {
                        final String secondPart = uidScanner.next();
                        final FullQualifiedUid fqUid;
                        fqUid = getFullQualifiedUid(firstPart, secondPart);
                        list.add(fqUid);
                    } else {
                        throw new IllegalArgumentException("Wrong input format for input string " + firstPart);
                    }
                }
            }
        }
        return list;
    }

    /**
     * Creates an instance of FullQualifiedUid for the given String elements.
     * @param firstPart the type prefix, that identifies the corresponding FirstSpirit namespace, or the root identifier (see @link{#parse})
     * @param secondPart the uid of an object or a store root identifier (see @link{#parse})
     * @throws UnknownRootNodeException if the first part is the root node identifier and the second part is an unknown store root identifier
     * @throws UnregisteredPrefixException if the first part is not a root node identifier and an unknown prefix
     * @return an instance of FullQualifiedUid that represents the input parameters
     */
    private static FullQualifiedUid getFullQualifiedUid(final String firstPart, final String secondPart) {
        final FullQualifiedUid fqUid;
        if (firstPart.equals(ROOT_NODE_IDENTIFIER)) {
            try {
                fqUid = new FullQualifiedUid(getUidTypeForPrefix(secondPart.toLowerCase()), ROOT_NODE_IDENTIFIER);
            } catch (final UnregisteredPrefixException e) {
                LOGGER.trace(e);
                throw new UnknownRootNodeException("Store root node not known: " + secondPart.toLowerCase());
            }
        } else {
            fqUid = new FullQualifiedUid(getUidTypeForPrefix(firstPart.toLowerCase()), secondPart);
        }
        return fqUid;
    }

    /**
     * Maps a full qualified uid prefix string to a UidType.
     *
     * @param prefix the prefix string the UidType needs to be known
     * @throws UnregisteredPrefixException if the given prefix can not be mapped
     * @throws IllegalArgumentException if the given prefix is null or empty
     * @return the UidType for the prefix
     */
    private static IDProvider.UidType getUidTypeForPrefix(final String prefix) {
        if(prefix == null || prefix.isEmpty()) {
            throw new IllegalArgumentException("A prefix must be provided for every uid");
        }

        if(KNOWN_PREFIXES.containsKey(prefix)) {
            return KNOWN_PREFIXES.get(prefix);
        }

        throw new UnregisteredPrefixException("No UidType registered for prefix \"" + prefix + "\""
                                              + ". Available prefixes are " + KNOWN_PREFIXES.keySet());
    }

    /**
     * Retrieves all known full qualified uid prefixes and their corresponding UidType.
     * @return a collection of prefixes and UidTypes
     */
    private static Map<String, IDProvider.UidType> calculateKnownPrefixes() {
        final Map<String, IDProvider.UidType> result = new HashMap<>();

        for (final ReferenceType referenceType : Arrays.asList(ReferenceType.values())) {
            result.put(referenceType.type(), referenceType.getUidType());
        }

        for (final Map.Entry<String, IDProvider.UidType> customPrefixUidTypeMapping : CUSTOM_PREFIX_UIDTYPE_MAPPINGS.entrySet()) {
            if(!result.containsKey(customPrefixUidTypeMapping.getKey())) {
                result.put(customPrefixUidTypeMapping.getKey(), customPrefixUidTypeMapping.getValue());
            }
        }

        return result;
    }

    /**
     * Retrieves all FirstSpirit store postfix identifiers that are used as export uids.
     * @return a collection of postfixes and UidTypes
     */
    private static Map<String, IDProvider.UidType> getAllStorePostfixes() {
        return Collections.unmodifiableMap(STORE_POSTFIXES);
    }

    public static Set<String> getAllKnownPrefixStrings() {
        return Collections.unmodifiableSet(KNOWN_PREFIXES.keySet());
    }

    /**
     * Get a {@link java.util.List} of all known store postfixes.
     * @return {@link java.util.List} of all known store postfixes
     */
    public static Set<String> getAllStorePostfixStrings() {
        return Collections.unmodifiableSet(getAllStorePostfixes().keySet());
    }

    /**
     * Retrieves a uid prefix for the given UidType.
     * @param uidType the UidType to retrieve the prefix for.
     * @return the corresponding prefix
     * @throws IllegalArgumentException if no prefix is registered for the given UidType
     */
    private static String getPrefixForUidType(final IDProvider.UidType uidType) {
        final Map<String, IDProvider.UidType> knownPrefixes = calculateKnownPrefixes();
        for (final Map.Entry<String, IDProvider.UidType> entry : knownPrefixes.entrySet()) {
            if(entry.getValue().equals(uidType)) {
                final String prefix = entry.getKey();
                return prefix;
            }
        }
        throw new IllegalArgumentException("No prefix registered for UidType " + uidType.name() + ". Known prefixes are " + KNOWN_PREFIXES);
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
            final FullQualifiedUid that = (FullQualifiedUid) o;
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
        if(getUid().equals(ROOT_NODE_IDENTIFIER)) {
            return uid + ":" + getPrefixForUidType(uidType);
        }
        return getPrefixForUidType(uidType) + ":" + uid;
    }
}
