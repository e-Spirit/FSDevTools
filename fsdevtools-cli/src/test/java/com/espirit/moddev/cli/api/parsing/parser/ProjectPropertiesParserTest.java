/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2020 e-Spirit AG
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

package com.espirit.moddev.cli.api.parsing.parser;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import com.espirit.moddev.cli.api.parsing.identifier.ProjectPropertiesIdentifier;

import de.espirit.firstspirit.transport.PropertiesTransportOptions;

/**
 *
 * @author kohlbrecher
 */
@RunWith(Theories.class)
public class ProjectPropertiesParserTest {
    @DataPoints
    public static List[] testcases =
            new List[]{ Arrays.asList("Projectproperty:LANGUAGES"),
                    Arrays.asList("projectproperty:languages"),
                    Arrays.asList("projectproperty :LANGUAGES"),
                    Arrays.asList("projectproperty : LANGUAGES")};

    private ProjectPropertiesParser testling;

    @Before
    public void setUp() {
        testling = new ProjectPropertiesParser();
    }


    @Theory
    public void testAppliesTo(List<String> uids) throws Exception {
        for(String current : uids) {
            boolean appliesTo = testling.appliesTo(current);
            Assert.assertTrue("Parser should apply to string " + current, appliesTo);
        }
    }

    @Test
    public void parse() throws Exception {
        List<ProjectPropertiesIdentifier> result = testling.parse(Arrays.asList("projectproperty:LANGUAGES"));
        Assert.assertEquals(1, result.size());
        EnumSet<PropertiesTransportOptions.ProjectPropertyType> enumSet = EnumSet.noneOf(PropertiesTransportOptions.ProjectPropertyType.class);
        enumSet.add(PropertiesTransportOptions.ProjectPropertyType.LANGUAGES);
        Assert.assertEquals(new ProjectPropertiesIdentifier(enumSet), result.get(0));
    }

    @Test
    public void parseAll() throws Exception {
        List<ProjectPropertiesIdentifier> result = testling.parse(Arrays.asList("projectproperty:ALL"));
        Assert.assertEquals(1, result.size());
        EnumSet<PropertiesTransportOptions.ProjectPropertyType> enumSet = EnumSet.allOf(PropertiesTransportOptions.ProjectPropertyType.class);
        Assert.assertEquals(new ProjectPropertiesIdentifier(enumSet), result.get(0));
    }


    @Test
    public void parseAllWhitespace() throws Exception {
        List<ProjectPropertiesIdentifier> result = testling.parse(Arrays.asList("projectproperty: ALL"));
        Assert.assertEquals(1, result.size());
        EnumSet<PropertiesTransportOptions.ProjectPropertyType> enumSet = EnumSet.allOf(PropertiesTransportOptions.ProjectPropertyType.class);
        Assert.assertEquals(new ProjectPropertiesIdentifier(enumSet), result.get(0));
    }


    @Test
    public void parseAllWithAdditionalProperty() throws Exception {
        List<ProjectPropertiesIdentifier> result = testling.parse(Arrays.asList("projectproperty:LANGUAGES", "projectproperty:ALL"));
        Assert.assertEquals(1, result.size());
        EnumSet<PropertiesTransportOptions.ProjectPropertyType> enumSet = EnumSet.allOf(PropertiesTransportOptions.ProjectPropertyType.class);
        Assert.assertEquals(new ProjectPropertiesIdentifier(enumSet), result.get(0));
    }


    @Test
    public void parseAllWithAdditionalProperties() throws Exception {
        List<ProjectPropertiesIdentifier> result = testling.parse(Arrays.asList("projectproperty:LANGUAGES", "projectproperty:ALL", "projectproperty:RESOLUTIONS"));
        Assert.assertEquals(1, result.size());
        EnumSet<PropertiesTransportOptions.ProjectPropertyType> enumSet = EnumSet.allOf(PropertiesTransportOptions.ProjectPropertyType.class);
        Assert.assertEquals(new ProjectPropertiesIdentifier(enumSet), result.get(0));
    }

    @Test
    public void testAppliesTo() throws Exception {
        Assert.assertTrue(testling.appliesTo("projectproperty:languages"));
    }
    @Test
    public void testDontApplyTo() throws Exception {
        Assert.assertFalse(testling.appliesTo("asdasd"));
    }
    @Test
    public void testDontApplyToStartsWithEntitiesIdentifier() throws Exception {
        Assert.assertFalse(testling.appliesTo("entitiesaasd:asd"));
    }
}
