package com.espirit.moddev.cli.api.event;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class CliErrorEventTest {

    private CliErrorEvent testling;
    private CliErrorEvent testlingWithNullValues;

    @Before
    public void setUp() throws Exception {
        testling = new CliErrorEvent(new Object(), new Exception("Junit"));
        testlingWithNullValues = new CliErrorEvent(null, null);
    }

    @Test
    public void testGetSource() throws Exception {
        assertThat("Expect non null value", testling.getSource(), is(notNullValue()));
        assertThat("Expect null value", testlingWithNullValues.getSource(), is(nullValue()));
    }

    @Test
    public void testGetError() throws Exception {
        assertThat("Expect non null value", testling.getError(), is(notNullValue()));
        assertThat("Expect null value", testlingWithNullValues.getError(), is(nullValue()));
    }
}
