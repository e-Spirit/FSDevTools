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

import com.espirit.moddev.cli.api.parsing.identifier.EntitiesIdentifier;
import com.espirit.moddev.cli.api.parsing.identifier.UidIdentifier;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Parser implementation that is able to parse FirstSpirit Content2 identifiers from a list of strings
 * and return a list of entity identifier instances.
 * Is applicable to strings of the form "entities:news" with "entities" as a prefix, ":" as a delimiter
 * and a Content2 uid as a postfix.
 */
public class EntitiesIdentifierParser  implements Parser<EntitiesIdentifier> {
    private static final Pattern DELIMITER = Pattern.compile("\\s*:\\s*");

    protected static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(EntitiesIdentifierParser.class);
    public static final String ENTITIES_IDENTIFIER = "entities";

    @Override
    public List<EntitiesIdentifier> parse(List<String> input) {
        if (input == null) {
            throw new IllegalArgumentException("input is null!");
        }
        if (input.isEmpty()) {
            return Collections.emptyList();
        }

        final List<EntitiesIdentifier> list = new ArrayList<>(input.size());
        for (final String identifier : input) {
            try(Scanner uidScanner = new Scanner(identifier)) {
                uidScanner.useDelimiter(DELIMITER);
                if (uidScanner.hasNext()) {
                    final String firstPart = uidScanner.next();
                    if (uidScanner.hasNext()) {
                        final String secondPart = uidScanner.next();
                        final EntitiesIdentifier entitiesIdentifier = new EntitiesIdentifier(secondPart);
                        list.add(entitiesIdentifier);
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

        return splitted[0].toLowerCase(Locale.UK).trim().equals(ENTITIES_IDENTIFIER);
    }
}
