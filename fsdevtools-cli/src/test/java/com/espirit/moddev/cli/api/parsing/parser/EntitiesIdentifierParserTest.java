/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2020 e-Spirit AG
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
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(Theories.class)
public class EntitiesIdentifierParserTest {

    @DataPoints
    public static List[] testcases =
            new List[]{ Arrays.asList("entities:myuid"),
                    Arrays.asList("ENTITIES:myuid"),
                    Arrays.asList("ENTITIES :myuid"),
                    Arrays.asList("ENTITIES : myuid")};

    private EntitiesIdentifierParser testling;

    @Before
    public void setUp() {
        testling = new EntitiesIdentifierParser();
    }


    @Theory
    public void testAppliesTo(List<String> uids) throws Exception {
        for(String current : uids) {
            boolean appliesTo = testling.appliesTo(current);
            assertTrue("Parser should apply to string " + current, appliesTo);
        }
    }

    @Test
    public void parse() throws Exception {
        List<EntitiesIdentifier> result = testling.parse(Arrays.asList("entities:xyz"));
        assertEquals(1, result.size());
        assertEquals(new EntitiesIdentifier("xyz"), result.get(0));
    }

    @Test
    public void testAppliesTo() throws Exception {
        assertTrue(testling.appliesTo("entities:products"));
    }
    @Test
    public void testDontApplyTo() throws Exception {
        assertFalse(testling.appliesTo("asdasd"));
    }
    @Test
    public void testDontApplyToStartsWithEntitiesIdentifier() throws Exception {
        assertFalse(testling.appliesTo("entitiesaasd:asd"));
    }

}
