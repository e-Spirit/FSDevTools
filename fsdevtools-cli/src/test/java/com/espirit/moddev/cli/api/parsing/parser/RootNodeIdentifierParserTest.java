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
import de.espirit.firstspirit.access.store.IDProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;

@RunWith(Theories.class)
public class RootNodeIdentifierParserTest {

    @DataPoints
    public static List[] testcases =
            new List[]{ Arrays.asList("root:myuid"),
                    Arrays.asList("ROOT:myuid"),
                    Arrays.asList("ROOT :myuid"),
                    Arrays.asList("ROOT :myuid"),
                    Arrays.asList("ROOT : myuid")};

    private RootNodeIdentifierParser testling;

    @Before
    public void setUp() {
        testling = new RootNodeIdentifierParser();
    }

    @Theory
    public void testAppliesTo(List<String> uids) throws Exception {
        for(String current : uids) {
            boolean appliesTo = testling.appliesTo(current);
            Assert.assertTrue("Parser should apply to string " + current, appliesTo);
        }
    }

    @Test(expected = UnknownRootNodeException.class)
    public void testParseWithNonExistingStore() throws Exception {
        testling.parse(Arrays.asList("root:xyz"));
    }

    @Test
    public void testParseWithTemplateStoreRoot() throws Exception {
        testling.parse(Arrays.asList("root:templatestore"));
    }
    @Test
    public void testParseStoreRootRequestWithExistingStore() throws Exception {
        final List<RootNodeIdentifier> list = testling.parse(Arrays.asList("root:templatestore"));
        Assert.assertThat(list.contains(new RootNodeIdentifier(IDProvider.UidType.TEMPLATESTORE)), equalTo(true));
    }
}
