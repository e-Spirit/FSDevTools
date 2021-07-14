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

package com.espirit.moddev.cli.commands.project.activateWebServerCommand;

import com.espirit.moddev.shared.webapp.WebAppIdentifier;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ProjectWebServerActivationParameterBuilderTest {
    private ProjectWebServerActivationParameterBuilder testling;

    @Before
    public void setUp() {
        List<WebAppIdentifier> validScopes = new ArrayList<>();
        validScopes.add(WebAppIdentifier.LIVE);
        validScopes.add(WebAppIdentifier.FS5_ROOT);
        testling = ProjectWebServerActivationParameter.builder();
        testling.atProjectName("validProjectName").forScopes(validScopes).withServerName("ValidServerName").withForceActivation(true);
    }

    @Test
    public void testBuild() {
        final ProjectWebServerActivationParameter parameter = testling.withForceActivation(true).build();
        Assert.assertNotNull(parameter);
        Assert.assertTrue(parameter.isForceActivation());
        Assert.assertEquals("validProjectName", parameter.getProjectName());
        Assert.assertEquals("ValidServerName", parameter.getServerName());
        Assert.assertNotNull(parameter.getScopes());
        Assert.assertEquals(2, parameter.getScopes().size());
        Assert.assertSame(parameter.getScopes().get(0).getScope(), WebAppIdentifier.LIVE.getScope());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyAtProjectName() {
        testling.atProjectName("").build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullForScopes() {
        List<WebAppIdentifier> invalidScopes = null;
        testling.forScopes(invalidScopes).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void withInvalidServerName() {
        testling.atProjectName("").build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void withEmptyServerName() {
        testling.atProjectName(null).build();
    }

    @Test
    public void testWithoutForceActivation() {
        Assert.assertFalse(testling.withForceActivation(false).build().isForceActivation());
    }
}
