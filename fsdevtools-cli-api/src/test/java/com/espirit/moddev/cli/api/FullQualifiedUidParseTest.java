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

package com.espirit.moddev.cli.api;

import com.espirit.moddev.cli.api.exceptions.UnknownRootNodeException;
import com.espirit.moddev.cli.api.exceptions.UnregisteredPrefixException;

import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.base.store.StoreType;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author e-Spirit AG
 */
@RunWith(Theories.class)
public class FullQualifiedUidParseTest {

    @DataPoints
    public static List[]
        testcases =
        new List[]{ Arrays.asList("page:myuid"),
                    Arrays.asList("PAGE:myuid"),
                    Arrays.asList("PAGE :myuid"),
                    Arrays.asList("PAGE :myuid"),
                    Arrays.asList("PAGE : myuid")};

    @Theory
    public void testParse(List<String> uids) throws Exception {
        final List<FullQualifiedUid> list = FullQualifiedUid.parse(uids);

        assertThat("Expected PAGE but got: " + uids, list.get(0).getUidType(), is(IDProvider.UidType.PAGESTORE));
        assertThat("Expected 'myuid' but got: " + uids, list.get(0).getUid(), is("myuid"));
    }

    @Test(expected = UnregisteredPrefixException.class)
    public void testParseWithNonExistentPrefix() throws Exception {
        FullQualifiedUid.parse(Arrays.asList("xxxxx:myuid"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseWithNoneStore() throws Exception {
        FullQualifiedUid.parse(Arrays.asList("myuid"));
    }

    @Test
    public void testParseWithTemplateStoreRoot() throws Exception {
        FullQualifiedUid.parse(Arrays.asList("root:templatestore"));
    }
    @Test
    public void testParseStoreRootRequestWithExistingStore() throws Exception {
        final List<FullQualifiedUid> list = FullQualifiedUid.parse(Arrays.asList("root:templatestore"));
        Assert.assertThat(list.contains(new FullQualifiedUid(IDProvider.UidType.TEMPLATESTORE, "root")), equalTo(true));
    }

    @Test(expected = UnknownRootNodeException.class)
    public void testParseWithNonExistingStore() throws Exception {
        FullQualifiedUid.parse(Arrays.asList("root:xyz"));
    }
}
