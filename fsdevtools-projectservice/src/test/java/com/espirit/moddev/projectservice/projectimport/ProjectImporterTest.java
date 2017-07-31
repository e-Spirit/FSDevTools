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

package com.espirit.moddev.projectservice.projectimport;

import de.espirit.firstspirit.access.Connection;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProjectImporterTest {
    private File fileMock;

    ProjectImporter testling;

    @Before
    public void setUp() {
        testling = new ProjectImporter();

        fileMock = mock(File.class);
        when(fileMock.exists()).thenReturn(true);
        when(fileMock.isFile()).thenReturn(true);
    }

    @Test(expected = IllegalStateException.class)
    public void testExceptionWhenNotConnected() throws IOException {
        Connection connectionMock = mock(Connection.class);
        when(connectionMock.isConnected()).thenReturn(false);
        ProjectImportParameters importParameters = new ProjectImportParametersBuilder().setProjectName("asd").setProjectFile(fileMock).create();
        testling.importProject(connectionMock, importParameters);
    }

}
