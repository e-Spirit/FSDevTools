/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2021 e-Spirit AG
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

package com.espirit.moddev.cli.commands.project.importCommand;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProjectImportParametersBuilderTest {

    private ProjectImportParametersBuilder _builder;
    private File _fileMock;
    private File _directoryFileMock;

    @Before
    public void setUp() {
        _builder = new ProjectImportParametersBuilder();
        _fileMock = mock(File.class);
        when(_fileMock.exists()).thenReturn(true);
        when(_fileMock.isFile()).thenReturn(true);
        _directoryFileMock = mock(File.class);
        when(_directoryFileMock.isFile()).thenReturn(false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExceptionOnNullProjectName() {
        _builder.setProjectName(null).setProjectDescription("myDescription").setProjectFile(_fileMock).setLayerMapping(null).forceProjectActivation(true).create();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExceptionOnEmptyProjectName() {
        _builder.setProjectName("").setProjectDescription("myDescription").setProjectFile(_fileMock).setLayerMapping(null).forceProjectActivation(true).create();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExceptionOnNullProjectFile() {
        _builder.setProjectName("projectName").setProjectDescription("myDescription").setProjectFile(null).setLayerMapping(null).forceProjectActivation(true).create();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExceptionODirectoryProjectFile() {
        _builder.setProjectName("projectName").setProjectDescription("myDescription").setProjectFile(_directoryFileMock).setLayerMapping(null).forceProjectActivation(true).create();
    }

    @Test
    public void testValidParameters() {
        final String projectName = "projectName";
        final String description = "myDescription";
        final ProjectImportParameters parameters = _builder.setProjectName(projectName).setProjectDescription(description).setProjectFile(_fileMock).setLayerMapping(null).forceProjectActivation(true).create();
        Assert.assertEquals(projectName, parameters.getProjectName());
        Assert.assertEquals(description, parameters.getProjectDescription());
        Assert.assertEquals(_fileMock, parameters.getProjectFile());
        Assert.assertTrue(parameters.forceProjectActivation());
    }

}
