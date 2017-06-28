package com.espirit.moddev.cli.commands.test;

import com.espirit.moddev.cli.api.result.Result;

import de.espirit.firstspirit.access.Connection;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestConnectionCommandTest {

    private TestConnectionCommand testling;
    private Connection connection;

    @Before
    public void setUp() throws Exception {
        connection = mock(Connection.class);
        testling = new TestConnectionCommand(){
            @Override
            protected Connection create() {
                return connection;
            }
        };
    }

    @Test
    public void testCall() throws Exception {
        final Result result = testling.call();

        assertThat("Expect normal execution", result.isError(), is(FALSE));
        assertThat("Expect null value", result.getError(), is(nullValue()));

        verify(connection, times(1)).connect();
    }

    @Test
    public void testCallError() throws Exception {
        doThrow(new IOException("Junit")).when(connection).connect();

        final Result result = testling.call();

        assertThat("Expect normal execution", result.isError(), is(TRUE));
        assertThat("Expect non-null value", result.getError(), is(notNullValue()));

        verify(connection, times(1)).connect();
    }

    @Test
    public void testCallNullConnection() throws Exception {
        connection = null;

        final Result result = testling.call();

        assertThat("Expect normal execution", result.isError(), is(TRUE));
        assertThat("Expect non-null value", result.getError(), is(notNullValue()));
    }

    @Test
    public void testNeedsContext() throws Exception {
        assertThat("This command creates his own FS connection therefore doesn't need one from outside",
                   testling.needsContext(), is(FALSE));
    }

}
