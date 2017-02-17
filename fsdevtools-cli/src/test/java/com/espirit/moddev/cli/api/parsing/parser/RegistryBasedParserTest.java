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
import com.espirit.moddev.cli.api.parsing.identifier.Identifier;
import com.espirit.moddev.cli.api.parsing.identifier.RootNodeIdentifier;
import com.espirit.moddev.cli.api.parsing.identifier.UidIdentifier;
import com.google.common.collect.Lists;
import de.espirit.firstspirit.access.store.IDProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;

public class RegistryBasedParserTest {

    private RegistryBasedParser testling;

    @Before
    public void setUp() {
        testling = new RegistryBasedParser();
    }
    @Test(expected = IllegalArgumentException.class)
    public void registerNullParser() {
        testling.registerParser(null);
    }
    @Test
    public void registerParser() {
        Parser parser = new Parser() {
            @Override
            public List parse(List input) {
                return null;
            }

            @Override
            public boolean appliesTo(String input) {
                return false;
            }
        };
        boolean registeredParser = testling.registerParser(parser);
        Assert.assertTrue("Parser wasn't registered successfully", registeredParser);
        Assert.assertTrue("Parser wasn't unregistered successfully", testling.unregisterParser(parser));
    }

    @Test
    public void appliesTo() {
        boolean registeredParser = testling.registerParser(new Parser() {
            @Override
            public List parse(List input) {
                return null;
            }

            @Override
            public boolean appliesTo(String input) {
                return input.startsWith("xxx");
            }
        });
        Assert.assertTrue("Parser wasn't registered successfully", registeredParser);
        Assert.assertTrue("Parser should delegate appliesTo to registered parsers", testling.appliesTo("xxxaaa"));
        Assert.assertFalse("Parser should not apply to other input strings", testling.appliesTo("aaabbb"));
    }

    @Test
    public void testParseMultipleElements() throws Exception {
        testling.registerParser(new RootNodeIdentifierParser());
        testling.registerParser(new UidIdentifierParser());
        testling.registerParser(new EntitiesIdentifierParser());
        final List<Identifier> list = testling.parse(Arrays.asList("root:templatestore", "mediafolder:layout", "entities:news"));
        Assert.assertEquals("List should contain two identifiers!", 3, list.size());
        Assert.assertThat(list.contains(new RootNodeIdentifier(IDProvider.UidType.TEMPLATESTORE)), equalTo(true));
        Assert.assertThat(list.contains(new UidIdentifier(IDProvider.UidType.MEDIASTORE_FOLDER, "layout")), equalTo(true));
        Assert.assertThat(list.contains(new EntitiesIdentifier("news")), equalTo(true));
    }

    @Test
    public void testDEVEX69() {
        testling.registerParser(new Parser<Identifier>() {
            @Override
            public List<Identifier> parse(List<String> input) {
                return new ArrayList<Identifier>() {{
                    add((storeAgent, exportOperation) -> {});
                }};
            }

            @Override
            public boolean appliesTo(String input) {
                return input.startsWith("path:");
            }
        });
        testling.registerParser(new Parser<Identifier>() {
            @Override
            public List<Identifier> parse(List<String> input) {
                return new ArrayList<Identifier>() {{
                    add((storeAgent, exportOperation) -> {});
                }};
            }

            @Override
            public boolean appliesTo(String input) {
                return input.startsWith("entities:");
            }
        });
        testling.registerParser(new Parser<Identifier>() {
            @Override
            public List<Identifier> parse(List<String> input) {
                return new ArrayList<Identifier>() {{
                    add((storeAgent, exportOperation) -> {});
                }};
            }

            @Override
            public boolean appliesTo(String input) {
                return input.startsWith("projectprops:");
            }
        });
        testling.registerParser(new UidIdentifierParser());

        Assert.assertTrue(testling.appliesTo("template:homepage"));
        Assert.assertTrue(testling.appliesTo("path:/TemplateStore/Pagetemplates/FOLDER_NAME/UID"));
        Assert.assertTrue(testling.appliesTo("entities:Produkte"));
        Assert.assertTrue(testling.appliesTo("projectprops:RESOLUTION"));

        List<Identifier> result = testling.parse(Lists.newArrayList("path:/TemplateStore/Pagetemplates/<FOLDER_NAME>/UID", "entities:Produkte", "projectprops:RESOLUTION", "template:homepage", "projectprops:COMMON"));
        Assert.assertEquals(5, result.size());
    }
}
