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

import com.espirit.moddev.cli.Cli;
import com.espirit.moddev.cli.CliConstants;
import com.espirit.moddev.cli.api.event.CliErrorEvent;
import com.espirit.moddev.cli.exception.ExceptionHandler;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author e-Spirit AG
 */
public class ExceptionHandlerTest {

    private ExceptionHandler testling;
    private Cli app;

    @Before
    public void setUp() throws Exception {
        app = new Cli();
        app.addListener(new ExceptionHandler(app, CliConstants.FS_CLI.value(), null));
    }

    @Test
    public void testUncaughtExceptionNoArgs() throws Exception {
        testling = new ExceptionHandler(app, CliConstants.FS_CLI.value(), new String[]{});
        try {
            testling.uncaughtException(null, new Exception("JUnit"));
        } catch (final Exception e) {
            fail("Not expected: " + e);
        }
    }


    @Test
    public void testUncaughtException() throws Exception {
        testling = new ExceptionHandler(app, CliConstants.FS_CLI.value(), new String[]{"bla", "la", "-v"});
        try {
            testling.uncaughtException(null, new Exception("JUnit"));
        } catch (final Exception e) {
            fail("Not expected: " + e);
        }
    }

    @Test
    public void testUncaughtExceptionNotVerbose() throws Exception {
        testling = new ExceptionHandler(app, CliConstants.FS_CLI.value(), new String[]{"bla", "la"});
        try {
            testling.uncaughtException(null, new Exception("JUnit"));
        } catch (final Exception e) {
            fail("Not expected: " + e);
        }
    }

    @Test
    public void testUncaughtExceptionRootCause() throws Exception {
        testling = new ExceptionHandler(app, CliConstants.FS_CLI.value(), new String[]{"bla", "la", "-v"});
        try {
            testling.uncaughtException(null, new Exception("JUnit", new Exception("root cause")));
        } catch (final Exception e) {
            fail("Not expected: " + e);
        }
    }

    @Test
    public void testErrorOccurred() throws Exception {
        testling = new ExceptionHandler(app, CliConstants.FS_CLI.value(), new String[]{"bla", "la", "-v"});
        try {
            testling.errorOccurred(new CliErrorEvent(this, new Exception("JUnit")));
        } catch (final Exception e) {
            fail("Not expected: " + e);
        }
    }

    @Test
    public void testArgumentsVerbose() throws Exception {
        testling = new ExceptionHandler(app, CliConstants.FS_CLI.value(), new String[]{"bla", "la", "-v"});
        assertTrue("Verbose option expected", testling.argumentsContains("-v"));
    }

    @Test
    public void testArgumentsContainsNotVerbose() throws Exception {
        testling = new ExceptionHandler(new Cli(), CliConstants.FS_CLI.value(), new String[]{"bla", "la"});
        assertFalse("Verbose option not expected", testling.argumentsContains("-v"));
    }
}
