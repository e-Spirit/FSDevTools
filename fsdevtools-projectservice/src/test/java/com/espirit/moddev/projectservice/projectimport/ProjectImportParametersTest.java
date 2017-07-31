package com.espirit.moddev.projectservice.projectimport;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProjectImportParametersTest {
    private File fileMock;
    private File directoryFileMock;

    @Before
    public void setUp() {
        fileMock = mock(File.class);
        when(fileMock.exists()).thenReturn(true);
        when(fileMock.isFile()).thenReturn(true);
        directoryFileMock = mock(File.class);
        when(directoryFileMock.isFile()).thenReturn(false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExceptionOnNullProjectName() {
        new ProjectImportParameters(null, "asdasd", fileMock, null, true);
    }
    @Test(expected = IllegalArgumentException.class)
    public void testExceptionOnEmptyProjectName() {
        new ProjectImportParameters("", "asdasd", fileMock, null, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExceptionOnNullProjectFile() {
        new ProjectImportParameters("asd", "asdasd", null, null, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExceptionODirectoryProjectFile() {
        new ProjectImportParameters("asd", "asdasd", directoryFileMock, null, true);
    }

    @Test
    public void testValidParameters() {
        ProjectImportParameters parameters = new ProjectImportParameters("myName", "myDescription", fileMock, null, true);
        Assert.assertEquals("myName", parameters.getProjectName());
        Assert.assertEquals("myDescription", parameters.getProjectDescription());
        Assert.assertEquals(fileMock, parameters.getProjectFile());
    }

}
