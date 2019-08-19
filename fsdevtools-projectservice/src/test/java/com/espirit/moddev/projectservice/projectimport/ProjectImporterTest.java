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
import de.espirit.firstspirit.access.export.ProjectInfo;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProjectImporterTest {

    private File _fileMock;
    private ProjectImporter _testling;

    @Before
    public void setUp() {
        _testling = new ProjectImporter();

        _fileMock = mock(File.class);
        when(_fileMock.exists()).thenReturn(true);
        when(_fileMock.isFile()).thenReturn(true);
    }

    @Test(expected = IllegalStateException.class)
    public void testExceptionWhenNotConnected() throws Exception {
        Connection connectionMock = mock(Connection.class);
        when(connectionMock.isConnected()).thenReturn(false);
        ProjectImportParameters importParameters = new ProjectImportParametersBuilder().setProjectName("asd").setProjectFile(_fileMock).create();
        _testling.importProject(connectionMock, importParameters);
    }

    private void addProperty(final ArrayList<Properties> properties, final String thirdLayer) {
        final Properties property = new Properties();
        property.setProperty("name", thirdLayer);
        properties.add(property);
    }

    @Test
    public void testLayerMapping_existing_layers() {
        // setup source infos
        final ProjectInfo mock = mock(ProjectInfo.class);
        final ArrayList<Properties> properties = new ArrayList<>();
        final String firstLayer = "firstLayer";
        final String secondLayer = "secondLayer";
        final String thirdLayer = "thirdLayer";
        final String fourthLayer = "fourthLayer";

        addProperty(properties, firstLayer);
        addProperty(properties, secondLayer);
        addProperty(properties, thirdLayer);
        addProperty(properties, fourthLayer);
        when(mock.getUsedLayers()).thenReturn(properties);

        // setup target mapping
        final HashMap<String, String> preImportMapping = new HashMap<>();
        final String targetLayer1 = "mappedFirstLayer";
        final String targetLayer2 = "mappedSecondLayer";
        preImportMapping.put(firstLayer, targetLayer1);
        preImportMapping.put(secondLayer, targetLayer1);
        preImportMapping.put(thirdLayer, targetLayer2);

        // execute
        final ProjectImportParameters parameters = new ProjectImportParameters("abc", "", _fileMock, preImportMapping, false);
        final HashMap<String, String> mapping = ProjectImporter.getLayerMapping(parameters, mock);

        // verify
        assertThat(mapping).hasSize(4);
        assertThat(mapping.get(firstLayer)).isEqualTo(targetLayer1);
        assertThat(mapping.get(secondLayer)).isEqualTo(targetLayer1);
        assertThat(mapping.get(thirdLayer)).isEqualTo(targetLayer2);
        assertThat(mapping.get(fourthLayer)).isNull();
    }

    @Test
    public void testLayerMapping_wildcard() {
        // setup source infos
        final ProjectInfo mock = mock(ProjectInfo.class);
        final ArrayList<Properties> properties = new ArrayList<>();
        final String firstLayer = "firstLayer";
        final String secondLayer = "secondLayer";

        addProperty(properties, firstLayer);
        addProperty(properties, secondLayer);
        when(mock.getUsedLayers()).thenReturn(properties);

        // setup target mapping
        final HashMap<String, String> preImportMapping = new HashMap<>();
        final String targetLayer = "mappedLayer";
        preImportMapping.put("*", targetLayer);

        // execute
        final ProjectImportParameters parameters = new ProjectImportParameters("abc", "", _fileMock, preImportMapping, false);
        final HashMap<String, String> mapping = ProjectImporter.getLayerMapping(parameters, mock);

        // verify
        assertThat(mapping).hasSize(2);
        assertThat(mapping.get(firstLayer)).isEqualTo(targetLayer);
        assertThat(mapping.get(secondLayer)).isEqualTo(targetLayer);
    }

    @Test
    public void testLayerMapping_wildcard_should_not_override_specific_mapping() {
        // setup source infos
        final ProjectInfo mock = mock(ProjectInfo.class);
        final ArrayList<Properties> properties = new ArrayList<>();
        final String firstLayer = "firstLayer";
        final String secondLayer = "secondLayer";

        addProperty(properties, firstLayer);
        addProperty(properties, secondLayer);
        when(mock.getUsedLayers()).thenReturn(properties);

        // setup target mapping
        final HashMap<String, String> preImportMapping = new HashMap<>();
        final String targetLayer1 = "targetLayer1";
        final String targetLayer2 = "targetLayer2";
        preImportMapping.put("*", targetLayer1);
        preImportMapping.put(secondLayer, targetLayer2);

        // execute
        final ProjectImportParameters parameters = new ProjectImportParameters("abc", "", _fileMock, preImportMapping, false);
        final HashMap<String, String> mapping = ProjectImporter.getLayerMapping(parameters, mock);

        // verify
        assertThat(mapping).hasSize(2);
        assertThat(mapping.get(firstLayer)).isEqualTo(targetLayer1);
        assertThat(mapping.get(secondLayer)).isEqualTo(targetLayer2);
    }

}
