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

import com.espirit.moddev.cli.api.parsing.exceptions.UnregisteredPrefixException;

import com.espirit.moddev.cli.api.parsing.identifier.UidIdentifier;
import com.espirit.moddev.cli.api.parsing.identifier.UidMapping;
import de.espirit.firstspirit.access.store.IDProvider;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author e-Spirit AG
 */
@RunWith(Theories.class)
public class UidIdentifierParserTest {

    @DataPoints
    public static List[] testcases =
        new List[]{ Arrays.asList("page:myuid"),
                    Arrays.asList("PAGE:myuid"),
                    Arrays.asList("PAGE :myuid"),
                    Arrays.asList("PAGE : myuid")};

    private UidIdentifierParser testling;

    @Before
    public void setUp() {
        testling = new UidIdentifierParser();
    }


    @Theory
    public void testAppliesTo(List<String> uids) throws Exception {
        for(String current : uids) {
            boolean appliesTo = testling.appliesTo(current);
            Assert.assertTrue("Parser should apply to string " + current, appliesTo);
        }
    }

    @Test
    public void testDontApplyTo() {
        boolean appliesTo = testling.appliesTo("pagexyz :bla");
        Assert.assertFalse("Parser should apply to string pagexyz :bla", appliesTo);
    }

    @Theory
    public void testParse(List<String> uids) throws Exception {
        final List<UidIdentifier> list = testling.parse(uids);

        assertThat("Expected PAGE but got: " + uids, list.get(0).getUidMapping(), Matchers.is(UidMapping.PAGE));
        assertThat("Expected 'myuid' but got: " + uids, list.get(0).getUid(), is("myuid"));
    }

    @Test(expected = UnregisteredPrefixException.class)
    public void testParseWithNonExistentPrefix() throws Exception {
        testling.parse(Arrays.asList("xxxxx:myuid"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseWithNoStore() throws Exception {
        testling.parse(Arrays.asList("myuid"));
    }

}
