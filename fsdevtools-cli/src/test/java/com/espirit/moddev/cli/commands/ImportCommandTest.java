package com.espirit.moddev.cli.commands;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class ImportCommandTest {

    private ImportCommand testling;

    @Before
    public void setUp() throws Exception {
        testling = new ImportCommand();
    }

    @Test
    public void testIsCreateEntities() throws Exception {
        assertThat("Expect true", testling.isCreateEntities(), is(Boolean.TRUE));
    }

    @Test
    public void testIsCreatingProjectIfMissing() throws Exception {
        assertThat("Expect true", testling.isCreatingProjectIfMissing(), is(Boolean.TRUE));
    }

    @Test
    public void testGetImportComment() throws Exception {
        assertThat("Expect null value", testling.getImportComment(), is("Imported by cli"));
    }
}
