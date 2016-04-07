package com.espirit.moddev.cli;

import com.espirit.moddev.cli.exception.ExceptionHandler;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * @author e-Spirit AG
 */
public class SimpleExecutionsTest {
    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    private Cli testling;
    private transient Throwable error;

    @Before
    public void setUp() throws Exception {
        testling = new Cli();
        testling.addListener(new ExceptionHandler(testling, CliConstants.FS_CLI.value(), null));
        testling.addListener(e -> error = e.getError());
        error = null;
    }

    @Test
    public void testMain() throws Exception {
        testling.execute(new String[0]);
        assertThat("Expect a null value: " + error, error, is(nullValue()));
    }

    @Test
    public void testMainVerbose() throws Exception {
        testling.execute(new String[]{"-v", "help"});
        assertThat("Expect a null value: " + error, error, is(nullValue()));
    }

    @Test
    public void testMainError() throws Exception {
        exit.expectSystemExitWithStatus(1);
        testling.execute(new String[]{"-c bla"});
        assertThat("Expect a non-null value", error, is(notNullValue()));
        assertThat("Expect a specific value: " + error.getMessage(), error.getMessage(),
                containsString("Value for option 'fsMode' was given as ' bla' which is not in the list of allowed values: [HTTP, SOCKET]"));
    }
}
