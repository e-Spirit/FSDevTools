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

import de.espirit.firstspirit.io.ServerConnection;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProjectImporterTest {

    ProjectImporter testling;

    @Before
    public void setUp() {
        testling = new ProjectImporter();
    }

    @Test(expected = IllegalStateException.class)
    public void testExceptionWhenNotConnected() throws IOException {
        ServerConnection connectionMock = mock(ServerConnection.class);
        when(connectionMock.isConnected()).thenReturn(false);
        ProjectImportParameters importParameters = new ProjectImportParametersBuilder().setProjectName("asd").setProjectFile("asd").create();
        testling.importProject(connectionMock, importParameters);
    }

}
