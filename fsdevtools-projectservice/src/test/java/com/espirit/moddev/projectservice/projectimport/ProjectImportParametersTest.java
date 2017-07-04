package com.espirit.moddev.projectservice.projectimport;

import com.espirit.moddev.projectservice.projectimport.ProjectImportParameters;
import org.junit.Assert;
import org.junit.Test;

public class ProjectImportParametersTest {

    @Test(expected = IllegalArgumentException.class)
    public void testExceptionOnNullProjectName() {
        new ProjectImportParameters(null, "asdasd", "asdasd", null, true);
    }
    @Test(expected = IllegalArgumentException.class)
    public void testExceptionOnEmptyProjectName() {
        new ProjectImportParameters("", "asdasd", "asdasd", null, true);
    }
    @Test(expected = IllegalArgumentException.class)
    public void testExceptionOnNullProjectFilePath() {
        new ProjectImportParameters("asd", "asdasd", null, null, true);
    }
    @Test(expected = IllegalArgumentException.class)
    public void testExceptionOnEmptyProjectFilePath() {
        new ProjectImportParameters("asd", "asdasd", "", null, true);
    }
    @Test
    public void testValidParameters() {
        ProjectImportParameters parameters = new ProjectImportParameters("myName", "myDescription", "myFilepath", null, true);
        Assert.assertEquals("myName", parameters.getProjectName());
        Assert.assertEquals("myDescription", parameters.getProjectDescription());
        Assert.assertEquals("myFilepath", parameters.getProjectFilePath());
    }

}
