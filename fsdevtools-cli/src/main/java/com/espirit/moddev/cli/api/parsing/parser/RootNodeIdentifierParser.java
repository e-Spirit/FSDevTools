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
import com.espirit.moddev.cli.api.parsing.identifier.RootNodeIdentifier;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import de.espirit.firstspirit.access.store.IDProvider;

import java.util.*;
import java.util.regex.Pattern;

import static com.espirit.moddev.cli.api.parsing.identifier.RootNodeIdentifier.ROOT_NODE_IDENTIFIER;

/**
 * Parser implementation that is able to parse FirstSpirit StoreRoot nodes from a list of strings
 * and return a list of RootNodeIdentifier instances.
 * Is applicable to strings of the form "root:templatestore" with "root" as a prefix, ":" as a delimiter
 * and a known store root identifier as a postfix.
 */
public class RootNodeIdentifierParser implements Parser<RootNodeIdentifier> {

    private static final Pattern DELIMITER = Pattern.compile("\\s*:\\s*");

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

    /**
     * Retrieves all FirstSpirit store postfix identifiers that are used as export uids.
     * @return a collection of postfixes and UidTypes
     */
    public static BiMap<String, IDProvider.UidType> getAllStorePostfixes() {
        return Maps.unmodifiableBiMap(STORE_POSTFIXES);
    }

    public RootNodeIdentifierParser() {
    }

    /**
     * Parses a given list of strings and returns a list of RootNodeIdentifier instances that
     * represent FirstSpirit StoreRoot nodes.
     * @throws UnknownRootNodeException when an unknown postfix is supplied
     * @param input a list of strings to parse
     * @return a list of RootNodeIdentifiers
     */
    @Override
    public List<RootNodeIdentifier> parse(List<String> input) {
        if (input == null) {
            throw new IllegalArgumentException("input is null!");
        }
        if (input.isEmpty()) {
            return Collections.emptyList();
        }

        final List<RootNodeIdentifier> list = new ArrayList<>(input.size());
        for (final String identifier : input) {
            try(Scanner uidScanner = new Scanner(identifier)) {
                uidScanner.useDelimiter(DELIMITER);
                if (uidScanner.hasNext()) {
                    final String firstPart = uidScanner.next();
                    IDProvider.UidType uidType;
                    if (uidScanner.hasNext()) {
                        final String secondPart = uidScanner.next();
                        if(!STORE_POSTFIXES.containsKey(secondPart)) {
                            throw new UnknownRootNodeException("No root node found for '" + secondPart + "'");
                        }
                        uidType = getAllStorePostfixes().get(secondPart);
                    } else {
                        if(STORE_POSTFIXES.keySet().contains(firstPart)) {
                            uidType = STORE_POSTFIXES.get(firstPart);
                        } else {
                            throw new UnknownRootNodeException("No root node found for '" + firstPart + "'");
                        }
                    }
                    final RootNodeIdentifier rootNodeIdentifier = new RootNodeIdentifier(uidType);
                    list.add(rootNodeIdentifier);
                }
            }
        }
        return list;
    }

    @Override
    public boolean appliesTo(String input) {
        String[] splitted = input.split(DELIMITER.pattern());

        if(couldBeNakedStoreIdentifier(splitted)) {
            return isKnownNakedStoreIdentifier(input);
        } else if(hasTwoTokens(splitted)) {
            return splitted[0].toLowerCase(Locale.UK).trim().equals(ROOT_NODE_IDENTIFIER);
        }

        return false;
    }

    private boolean hasTwoTokens(String[] splitted) {
        return splitted.length == 2;
    }

    private boolean couldBeNakedStoreIdentifier(String[] splitted) {
        return splitted.length == 1;
    }

    private boolean isKnownNakedStoreIdentifier(String input) {
        for(String storeIdentifier : STORE_POSTFIXES.keySet()) {
            boolean isKnownStoreIdentifier = storeIdentifier.equals(input);
            if(isKnownStoreIdentifier) {
                return true;
            }
        }
        return false;
    }
}
