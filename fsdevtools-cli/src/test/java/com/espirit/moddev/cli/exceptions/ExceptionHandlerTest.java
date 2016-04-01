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

package com.espirit.moddev.cli.exceptions;

import com.espirit.moddev.cli.Cli;
import com.espirit.moddev.cli.exception.ExceptionHandler;
import com.espirit.moddev.cli.exception.CliErrorEvent;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author e-Spirit AG
 */
public class ExceptionHandlerTest {

    private ExceptionHandler testling;
    private Cli app;

    @Before
    public void setUp() throws Exception {
        app = new Cli();
        app.addListener(new ExceptionHandler(app, null));
    }

    @Test
    public void testUncaughtExceptionNoArgs() throws Exception {
        testling = new ExceptionHandler(app, new String[]{});
        try {
            testling.uncaughtException(null, new Exception("JUnit"));
        } catch (Exception e) {
            fail("Not expected: " + e);
        }
    }


    @Test
    public void testUncaughtException() throws Exception {
        testling = new ExceptionHandler(app, new String[]{"bla", "la", "-v"});
        try {
            testling.uncaughtException(null, new Exception("JUnit"));
        } catch (Exception e) {
            fail("Not expected: " + e);
        }
    }

    @Test
    public void testUncaughtExceptionNotVerbose() throws Exception {
        testling = new ExceptionHandler(app, new String[]{"bla", "la"});
        try {
            testling.uncaughtException(null, new Exception("JUnit"));
        } catch (Exception e) {
            fail("Not expected: " + e);
        }
    }

    @Test
    public void testUncaughtExceptionRootCause() throws Exception {
        testling = new ExceptionHandler(app, new String[]{"bla", "la", "-v"});
        try {
            testling.uncaughtException(null, new Exception("JUnit", new Exception("root cause")));
        } catch (Exception e) {
            fail("Not expected: " + e);
        }
    }

    @Test
    public void testErrorOccurred() throws Exception {
        testling = new ExceptionHandler(app, new String[]{"bla", "la", "-v"});
        try {
            testling.errorOccurred(new CliErrorEvent(this, new Exception("JUnit")));
        } catch (Exception e) {
            fail("Not expected: " + e);
        }
    }

    @Test
    public void testArgumentsVerbose() throws Exception {
        testling = new ExceptionHandler(app, new String[]{"bla", "la", "-v"});
        assertTrue("Verbose option expected", testling.argumentsContains("-v"));
    }

    @Test
    public void testArgumentsContainsNotVerbose() throws Exception {
        testling = new ExceptionHandler(new Cli(), new String[]{"bla", "la"});
        assertFalse("Verbose option not expected", testling.argumentsContains("-v"));
    }


}
