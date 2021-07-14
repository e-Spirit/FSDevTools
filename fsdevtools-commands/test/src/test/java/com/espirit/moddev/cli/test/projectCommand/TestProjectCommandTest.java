/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2021 e-Spirit AG
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

package com.espirit.moddev.cli.test.projectCommand;

import com.espirit.moddev.cli.CliContextImpl;
import com.espirit.moddev.cli.api.result.Result;

import com.espirit.moddev.cli.commands.test.projectCommand.TestProjectCommand;
import org.junit.Before;
import org.junit.Test;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

public class TestProjectCommandTest {

    private TestProjectCommand testling;

    @Before
    public void setUp() throws Exception {
        testling = new TestProjectCommand(){
            @Override
            protected CliContextImpl create() {
                return mock(CliContextImpl.class, RETURNS_DEEP_STUBS);
            }
        };
    }

    @Test
    public void testCall() throws Exception {
        final Result result = testling.call();

        assertThat("Expect normal execution", result.isError(), is(FALSE));
        assertThat("Expect null value", result.getError(), is(nullValue()));
    }

    @Test
    public void testCallError() throws Exception {
        testling = new TestProjectCommand(){
            @Override
            protected CliContextImpl create() {
                throw new RuntimeException("JUnit");
            }
        };

        final Result result = testling.call();

        assertThat("Expect abnormal execution", result.isError(), is(TRUE));
        assertThat("Expect non-null value", result.getError(), is(notNullValue()));
    }

    @Test
    public void testNeedsContext() throws Exception {
        assertThat("This command creates his own context to initialize connection therefore doesn't need a context from outside",
                   testling.needsContext(), is(FALSE));
    }

    @Test
    public void testCreatingProjectIfMissing() throws Exception {
        assertThat("Should not create missing projects", testling.isCreatingProjectIfMissing(), is(FALSE));
    }

}
