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

package com.espirit.moddev.cli.api.parsing.parser;

import com.espirit.moddev.cli.api.parsing.exceptions.UnknownRootNodeException;
import com.espirit.moddev.cli.api.parsing.exceptions.UnregisteredPrefixException;
import com.espirit.moddev.cli.api.parsing.identifier.UidIdentifier;
import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.access.store.ReferenceType;

import java.util.*;
import java.util.regex.Pattern;

public class UidIdentifierParser implements Parser<UidIdentifier> {

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

        CUSTOM_PREFIX_UIDTYPE_MAPPINGS = new HashMap<>();
        CUSTOM_PREFIX_UIDTYPE_MAPPINGS.put("page", IDProvider.UidType.PAGESTORE);
        CUSTOM_PREFIX_UIDTYPE_MAPPINGS.put("pagetemplate", IDProvider.UidType.TEMPLATESTORE);
    }

    protected static final Map<String, IDProvider.UidType> KNOWN_PREFIXES = UidIdentifierParser.calculateKnownPrefixes();

    /**
     * Parse a list of full qualified uid strings.
     * The strings must match the following pattern:<br>
     * <code>&lt;TYPE_PREFIX&gt;:&lt;UID&gt;</code><br>
     * The allowed values for <code>TYPE_PREFIX</code> are defined by {@link #getAllKnownPrefixStrings()}.
     *
     * @param input the {@link java.util.List} of full qualified uids following the above pattern
     * @throws IllegalArgumentException if input is null or if a string does not follow the above pattern
     * @throws UnknownRootNodeException if a requested store root does not exist
     * @return the {@link java.util.List} of identifiers representing the input strings
     */
    @Override
    public List<UidIdentifier> parse(final List<String> input) {
        if (input == null) {
            throw new IllegalArgumentException("input is null!");
        }
        if (input.isEmpty()) {
            return Collections.emptyList();
        }

        final List<UidIdentifier> list = new ArrayList<>(input.size());
        for (final String identifier : input) {
            try(Scanner uidScanner = new Scanner(identifier)) {
                uidScanner.useDelimiter(DELIMITER);
                if (uidScanner.hasNext()) {
                    final String firstPart = uidScanner.next();
                    if (uidScanner.hasNext()) {
                        final String secondPart = uidScanner.next();
                        final UidIdentifier fqUid = new UidIdentifier(getUidTypeForPrefix(firstPart.toLowerCase(Locale.UK)), secondPart);
                        list.add(fqUid);
                    } else {
                        throw new IllegalArgumentException("Wrong input format for input string " + identifier);
                    }
                }
            }
        }
        return list;
    }

    @Override
    public boolean appliesTo(String input) {
        String[] splitted = input.split(DELIMITER.pattern());
        if(splitted.length != 2) {
            return false;
        }
        for(String prefix : getAllKnownPrefixStrings()) {
            if(splitted[0].toLowerCase(Locale.UK).trim().equals(prefix)) {
                return true;
            }
        }
        return false;
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
    public static Map<String, IDProvider.UidType> calculateKnownPrefixes() {
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

    public static Set<String> getAllKnownPrefixStrings() {
        return Collections.unmodifiableSet(KNOWN_PREFIXES.keySet());
    }

    /**
     * Retrieves a uid prefix for the given UidType.
     * @param uidType the UidType to retrieve the prefix for.
     * @return the corresponding prefix
     * @throws IllegalArgumentException if no prefix is registered for the given UidType
     */
    public static String getPrefixForUidType(final IDProvider.UidType uidType) {
        final Map<String, IDProvider.UidType> knownPrefixes = calculateKnownPrefixes();
        for (final Map.Entry<String, IDProvider.UidType> entry : knownPrefixes.entrySet()) {
            if(entry.getValue().equals(uidType)) {
                return entry.getKey();
            }
        }
        throw new IllegalArgumentException("No prefix registered for UidType " + uidType.name() + ". Known prefixes are " + KNOWN_PREFIXES);
    }
}
