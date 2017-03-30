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

package com.espirit.moddev.cli.api.parsing.identifier;

import java.util.EnumSet;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import de.espirit.firstspirit.transport.PropertiesTransportOptions;

/**
 *
 * @author kohlbrecher
 */
@RunWith(Theories.class)
public class ProjectPropertiesIdentifierTest {
     @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test(expected = IllegalArgumentException.class)
    public void testNullStore() {
        new ProjectPropertiesIdentifier(null);
    }

    @Test
    public void testEquality() {
        EnumSet<PropertiesTransportOptions.ProjectPropertyType> enumSet = EnumSet.noneOf(PropertiesTransportOptions.ProjectPropertyType.class);
        enumSet.add(PropertiesTransportOptions.ProjectPropertyType.GROUPS);
        enumSet.add(PropertiesTransportOptions.ProjectPropertyType.LANGUAGES);
        ProjectPropertiesIdentifier identifier = new ProjectPropertiesIdentifier(enumSet);
        
        enumSet = EnumSet.noneOf(PropertiesTransportOptions.ProjectPropertyType.class);
        enumSet.add(PropertiesTransportOptions.ProjectPropertyType.GROUPS);
        enumSet.add(PropertiesTransportOptions.ProjectPropertyType.LANGUAGES);
        ProjectPropertiesIdentifier equalIdentifier = new ProjectPropertiesIdentifier(enumSet);
        
        enumSet = EnumSet.noneOf(PropertiesTransportOptions.ProjectPropertyType.class);
        enumSet.add(PropertiesTransportOptions.ProjectPropertyType.LANGUAGES);
        ProjectPropertiesIdentifier notEqualIdentifier = new ProjectPropertiesIdentifier(enumSet);
        
        assertThat("Expected two equal project properties identifier", identifier, equalTo(equalIdentifier));
        assertThat("Expected two different project properties identifier", identifier, not(equalTo(notEqualIdentifier)));
    }
}
