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

package com.espirit.moddev.projectservice.projectexport;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for ProjectExportParameters.
 */
public class ProjectExportParametersTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    private ProjectExportParameters testling;

    /**
     * Test that a constructor call with valid parameters returns an object.
     */
    @Test
    public void testConstructorHasValidParametersReturnsInstance() {
        final String validProjectName = "validProjectName";
        final String validProjectExportPath = "valid/Project/Export/Path";

        testling = new ProjectExportParameters(validProjectName, validProjectExportPath, true, false);

        assertThat("Expect equal.", testling, is(notNullValue()));
    }

    /**
     * Test that an empty project name throws IllegalArgumentException.
     */
    @Test
    public void testConstructorHasEmptyProjectNameThrowsException() {
        final String emptyProjectName = "";
        final String validProjectExportPath = "valid/Project/Export/Path";

        exception.expect(IllegalArgumentException.class);

        testling = new ProjectExportParameters(emptyProjectName, validProjectExportPath, true, false);
    }

    /**
     * Test that a null project name throws IllegalArgumentException.
     */
    @Test
    public void testConstructorHasNullProjectNameThrowsException() {
        final String nullProjectName = null;
        final String validProjectExportPath = "valid/Project/Export/Path";

        exception.expect(IllegalArgumentException.class);

        testling = new ProjectExportParameters(nullProjectName, validProjectExportPath, true, false);
    }

    /**
     * Test that an empty project export path throws IllegalArgumentException.
     */
    @Test
    public void testConstructorHasEmptyProjectExportPathThrowsException() {
        final String validProjectName = "validProjectName";
        final String emptyProjectExportPath = "";

        exception.expect(IllegalArgumentException.class);

        testling = new ProjectExportParameters(validProjectName, emptyProjectExportPath, true, false);
    }

    /**
     * Test that an null project export path throws IllegalArgumentException.
     */
    @Test
    public void testConstructorHasNullProjectExportPathThrowsException() {
        final String validProjectName = "validProjectName";
        final String nullProjectExportPath = null;

        exception.expect(IllegalArgumentException.class);

        testling = new ProjectExportParameters(validProjectName, nullProjectExportPath, true, false);
    }

    /**
     * Test that getProjectName returns a String.
     */
    @Test
    public void testGetProjectNameReturnsAString() {
        final String validProjectName = "validProjectName";
        final String validProjectExportPath = "valid/Project/Export/Path";

        testling = new ProjectExportParameters(validProjectName, validProjectExportPath, true, false);

        assertThat("Expect equal.", testling.getProjectName(), equalTo(validProjectName));
    }

    /**
     * Test that getProjectExportPath returns a String.
     */
    @Test
    public void testGetProjectExportPathReturnsAString() {
        final String validProjectName = "validProjectName";
        final String validProjectExportPath = "valid/Project/Export/Path";

        testling = new ProjectExportParameters(validProjectName, validProjectExportPath, true, false);

        assertThat("Expect equal.", testling.getProjectExportPath(), equalTo(validProjectExportPath));
    }

    /**
     * Test that isFsForceProjectActivation returns a boolean.
     */
    @Test
    public void testIsForceActivationReturnsABoolean() {
        final String validProjectName = "validProjectName";
        final String validProjectExportPath = "valid/Project/Export/Path";

        testling = new ProjectExportParameters(validProjectName, validProjectExportPath, true, false);

        assertThat("Expect equal.", testling.isFsForceProjectActivation(), is(true));
    }
}
