/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2021 e-Spirit GmbH
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
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;


public class EntitiesIdentifierParserTest {

    private EntitiesIdentifierParser testling;

    @NotNull
    private static Stream<Arguments> parameterSet() {
        return Stream.of(
                Arguments.of(List.of("entities:myuid")),
                Arguments.of(List.of("ENTITIES :myuid")),
                Arguments.of(List.of("ENTITIES : myuid"))
        );
    }

    @BeforeEach
    public void setUp() {
        testling = new EntitiesIdentifierParser();
    }

    @ParameterizedTest
    @MethodSource("parameterSet")
    public void testAppliesTo(List<String> uids) {
        for (String current : uids) {
            boolean appliesTo = testling.appliesTo(current);
            assertTrue(appliesTo, "Parser should apply to string " + current);
        }
    }

    @Test
    public void parse() {
        List<EntitiesIdentifier> result = testling.parse(Arrays.asList("entities:xyz"));
        assertEquals(1, result.size());
        assertEquals(new EntitiesIdentifier("xyz"), result.get(0));
    }

    @Test
    public void testAppliesTo() {
        assertTrue(testling.appliesTo("entities:products"));
    }

    @Test
    public void testDontApplyTo() {
        assertFalse(testling.appliesTo("asdasd"));
    }

    @Test
    public void testDontApplyToStartsWithEntitiesIdentifier() {
        assertFalse(testling.appliesTo("entitiesaasd:asd"));
    }

}
