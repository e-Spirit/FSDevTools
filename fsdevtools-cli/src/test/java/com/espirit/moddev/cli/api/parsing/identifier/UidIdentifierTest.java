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

package com.espirit.moddev.cli.api.parsing.identifier;

import de.espirit.firstspirit.access.store.IDProvider;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author e-Spirit AG
 */
@RunWith(Theories.class)
public class UidIdentifierTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @DataPoints
    public static String[] uids = {"", " ", null};

    @Test(expected = IllegalArgumentException.class)
    public void testNullStore() {
        new UidIdentifier(null, "");
    }

    @Theory
    public void testEmptyOrNullUid(String uid) {
        expectedException.expect(IllegalArgumentException.class);

        new UidIdentifier(IDProvider.UidType.TEMPLATESTORE, uid);
    }

    @Theory
    public void testEmptyOrNullUidWithSubStore(String uid) {
        expectedException.expect(IllegalArgumentException.class);

        new UidIdentifier(null, uid);
    }

    @Test
    public void testEquality() {
        UidIdentifier uid = new UidIdentifier(IDProvider.UidType.TEMPLATESTORE, "reference_name");
        UidIdentifier anEqualUid = new UidIdentifier(IDProvider.UidType.TEMPLATESTORE, "reference_name");

        UidIdentifier anUnequalUid = new UidIdentifier(IDProvider.UidType.TEMPLATESTORE, "another_reference_name");
        UidIdentifier anotherUnequalUid = new UidIdentifier(IDProvider.UidType.TEMPLATESTORE_FORMATTEMPLATE, "reference_name");

        assertThat("Expected two equal full qualified uids for equal template store and uid", uid, equalTo(anEqualUid));
        assertThat("Expected two different full qualified uids for non equal uid", uid, not(equalTo(anUnequalUid)));
        assertThat("Expected two different full qualified uids for non equal template store and same uid", uid, not(equalTo(anotherUnequalUid)));
    }
}
