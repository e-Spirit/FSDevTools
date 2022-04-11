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

import com.espirit.moddev.cli.api.parsing.exceptions.UnknownRootNodeException;
import com.espirit.moddev.cli.api.parsing.identifier.RootNodeIdentifier;
import de.espirit.firstspirit.access.store.IDProvider;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RootNodeIdentifierParserTest {


    private RootNodeIdentifierParser testling;


    @BeforeEach
    public void setUp() {
        testling = new RootNodeIdentifierParser();
    }

    @ParameterizedTest
    @ValueSource(strings = {"root:myuid", "ROOT:myuid", "ROOT :myuid", "ROOT : myuid", "contentstore", "globalstore", "templatestore", "pagestore", "sitestore"})
    public void testAppliesTo(@NotNull final String uid) {
        boolean appliesTo = testling.appliesTo(uid);
        assertTrue(appliesTo, "Parser should apply to string " + uid);
    }

    @Test
    public void testParseWithNonExistingStore() {
        assertThrows(UnknownRootNodeException.class, () -> testling.parse(List.of("root:xyz")));
    }

    @Test
    public void testParseWithTemplateStoreRoot() {
        testling.parse(Arrays.asList("root:templatestore"));
    }

    @Test
    public void testParseStoreRootRequestWithExistingStore() {
        final List<RootNodeIdentifier> list = testling.parse(Arrays.asList("root:templatestore"));
        assertThat(list.contains(new RootNodeIdentifier(IDProvider.UidType.TEMPLATESTORE)), equalTo(true));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ROOT : contentstore", "contentstore", "globalstore", "templatestore", "pagestore", "sitestore"})
    public void testParseNakedStoreRoot(@NotNull final String uid) {
        final List<RootNodeIdentifier> list = testling.parse(Collections.singletonList(uid));
        assertThat(list.size(), equalTo(1));
        assertThat(list.get(0), isA(RootNodeIdentifier.class));
    }
}
