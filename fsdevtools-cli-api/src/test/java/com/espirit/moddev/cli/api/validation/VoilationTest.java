package com.espirit.moddev.cli.api.validation;

/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2016 e-Spirit AG
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License"),
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

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class VoilationTest {

    private Voilation testling;

    @Before
    public void setUp() throws Exception {
        testling = new Voilation("field", "is wrong!");
    }

    @Test
    public void testEquals() throws Exception {
        Voilation copy = new Voilation("field", "is wrong!");
        Voilation newOne = new Voilation("field", "is blank!");

        assertThat("Expect identity", testling, is(testling));
        assertThat("Expect identity", testling, is(copy));
        assertThat("Expect non-identity", testling, is(not(newOne)));
    }

    @Test
    public void testToString() throws Exception {
        assertThat(testling.toString(), is("field is wrong!"));
    }

}
