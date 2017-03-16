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
import com.espirit.moddev.cli.api.parsing.identifier.UidMapping;
import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.access.store.ReferenceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;

public class UidIdentifierParser implements Parser<UidIdentifier> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UidIdentifierParser.class);

    private static final Pattern DELIMITER = Pattern.compile("\\s*:\\s*");

    /**
     * Parse a list of full qualified uid strings.
     * The strings must match the following pattern:<br>
     * <code>&lt;TYPE_PREFIX&gt;:&lt;UID&gt;</code><br>
     * The allowed values for <code>TYPE_PREFIX</code> are defined by {@link UidMapping}.
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
                        try {
                            final UidIdentifier fqUid = new UidIdentifier(UidMapping.valueOf(firstPart.toUpperCase(Locale.UK)), secondPart);
                            list.add(fqUid);
                        } catch (IllegalArgumentException e) {
                            LOGGER.trace("Identifier string caused an exception, leading to an UnregisteredPrefixException", e);
                            throw new UnregisteredPrefixException("No uid mapping found for identifier " + firstPart);
                        }
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
        try {
            UidMapping.valueOf(splitted[0].trim().toUpperCase(Locale.UK));
            return true;
        } catch (IllegalArgumentException e) {
            LOGGER.trace("Identifier string caused an exception, leading to an UnregisteredPrefixException", e);
            return false;
        }
    }
}
