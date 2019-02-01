package com.espirit.moddev.projectservice.projectactivatewebserver;

import com.espirit.moddev.shared.webapp.WebAppIdentifier;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

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