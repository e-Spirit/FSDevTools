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

package com.espirit.moddev.cli.api.parsing.exceptions;

import com.espirit.moddev.cli.exception.SystemExitHandler;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import static org.junit.Assert.fail;

/**
 * @author e-Spirit AG
 */
public class SystemExitHandlerTest {

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    private SystemExitHandler tesling;

    @Before
    public void setUp() throws java.lang.Exception {
        tesling = new SystemExitHandler();
    }

    @Test
    public void testHandlerCallsSystemExit() throws java.lang.Exception {
        exit.expectSystemExitWithStatus(1);

        tesling.afterExceptionalTermination(new Exception());

        fail("Schould not be called if rule works");
    }
}
