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

import com.espirit.moddev.cli.api.parsing.exceptions.NoSuitableParserRegisteredException;
import com.espirit.moddev.cli.api.parsing.identifier.Identifier;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class RegistryBasedParser implements Parser<Identifier> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(RegistryBasedParser.class);

    private final List<Parser> registeredParsers = new ArrayList<>();

    public RegistryBasedParser() {
    }

    /**
     * Parses a given list of strings. For each string, the first applicable parser
     * is chosen for parsing.
     * @throws NoSuitableParserRegisteredException if no applicable parser was found for one of the input strings
     * @throws IllegalStateException if a suitable parser returns null or an empty list for an input string he is applicable for
     * @param input a list of strings to parse
     * @return a list of identifiers parsed from the input string
     */
    @Override
    public List<Identifier> parse(List<String> input) {
        List result = new ArrayList(input.size());
        for(String currentInput : input) {
            boolean suitableParserRegistered = false;
            parserLoop:
            for(Parser currentParser : registeredParsers) {
                if(currentParser.appliesTo(currentInput)) {
                    suitableParserRegistered = true;
                    List parsed = currentParser.parse(Lists.newArrayList(currentInput));
                    if(parsed == null) {
                        throw new IllegalStateException("A parser of class " + currentParser.getClass() + " was invoked and returned null for input " + currentInput);
                    } else if(parsed.isEmpty()) {
                         LOGGER.warn("A parser of class {} was invoked and returned an empty list for input {}", currentParser.getClass(), currentInput);
                    }
                    result.addAll(parsed);
                    break parserLoop;
                }
            }
            if(!suitableParserRegistered) {
                throw new NoSuitableParserRegisteredException("No applicable parser found for input string " + currentInput);
            }
        }
        return result;
    }

    /**
     * Indicates if the given input string can be parsed by one of the registered parsers.
     * @param input the string to test applicability for
     * @return true if the given input can be handled somehow
     */
    @Override
    public boolean appliesTo(String input) {
        for(Parser current : registeredParsers) {
            if(current.appliesTo(input)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a parser implementation to this parser's registry, in order to use it
     * for parsing. Caution: Order matters for parser registration, as earlier registered
     * parsers are preferred for parsing over later registered parsers.
     * @throws IllegalArgumentException if the supplied parser is null
     * @param parser the parser implementation to register
     * @return true if the parser was registered successfully.
     */
    public boolean registerParser(Parser parser) {
        if(parser == null) {
            throw new IllegalArgumentException("Don't register null parsers!");
        }
        return registeredParsers.add(parser);
    }

    /**
     * Unregisters a parser implementation from this parsers registry.
     * @param parser the parser implementation to unregister
     * @return true if the supplied parser was unregistered successfully
     */
    public boolean unregisterParser(Parser parser) {
        return registeredParsers.remove(parser);
    }
}
