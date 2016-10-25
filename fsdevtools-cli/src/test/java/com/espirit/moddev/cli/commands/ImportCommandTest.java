package com.espirit.moddev.cli.commands;

import com.espirit.moddev.cli.CliConstants;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * The Class ImportCommandTest tests the default settings.
 */
public class ImportCommandTest {

    private ImportCommand testling;

    @Before
    public void setUp() throws Exception {
        testling = new ImportCommand();
    }

    /**
     * Test is creating project if missing.
     *
     * @throws Exception the exception
     */
    @Test
    public void testIsCreatingProjectIfMissing() throws Exception {
        assertThat("Expect true", testling.isCreatingProjectIfMissing(), is(Boolean.TRUE));
    }

    /**
     * Test get import comment.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetImportComment() throws Exception {
        assertThat("Expect null value", testling.getImportComment(), is("Imported by " + CliConstants.FS_CLI));
    }
}
