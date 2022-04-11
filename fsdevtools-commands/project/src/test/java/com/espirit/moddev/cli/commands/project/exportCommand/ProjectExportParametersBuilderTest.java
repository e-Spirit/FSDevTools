/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2021 e-Spirit GmbH
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

package com.espirit.moddev.cli.commands.project.exportCommand;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Unit tests for ProjectExportParametersBuilder.
 */
public class ProjectExportParametersBuilderTest {

    private ProjectExportParametersBuilder testling;

    @BeforeEach
    public void setUp() {
        testling = new ProjectExportParametersBuilder();
    }

    /**
     * Test that the default constructor has no dependencies or exceptions.
     */
    @Test
    public void testDefaultConstructor() {
        assertThat("Expect not null.", testling, is(notNullValue()));
    }

    /**
     * Test that missing builder parameters throw an IllegalArgumentException.
     */
    @Test
    public void testBuildWithMissingParametersThrowsException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            testling.build();
        });
    }

    /**
     * Test that enough valid parameters return an instance of ProjectExportParameters
     */
    @Test
    public void testBuildWithValidParametersReturnsProjectExportParametersInstance() {
        // Arrange
        testling.setProjectName("validProjectName")
                .setProjectExportPath("validProjectExportPath");

        // Assert
        ProjectExportParameters parameters = testling.build();

        // Act
        assertThat("Expect not null.", parameters, is(notNullValue()));
    }
}
