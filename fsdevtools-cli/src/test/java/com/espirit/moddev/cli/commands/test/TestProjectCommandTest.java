package com.espirit.moddev.cli.commands.test;

import com.espirit.moddev.cli.api.result.Result;

import org.junit.Before;
import org.junit.Test;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class TestProjectCommandTest {

    private TestProjectCommand testling;

    @Before
    public void setUp() throws Exception {
        testling = new TestProjectCommand(){
            @Override
            protected AutoCloseable create() {
                return mock(AutoCloseable.class);
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
            protected AutoCloseable create() {
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
