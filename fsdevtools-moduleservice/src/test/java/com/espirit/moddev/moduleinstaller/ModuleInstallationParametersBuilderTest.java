package com.espirit.moddev.moduleinstaller;

import de.espirit.firstspirit.module.WebEnvironment;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

public class ModuleInstallationParametersBuilderTest {

    private ModuleInstallationParametersBuilder testling;

    @Before
    public void setUp() throws Exception {
        testling = new ModuleInstallationParametersBuilder();
    }

    @Test
    public void getWebScopeFileMap() throws Exception {
        String testWebAppConfigurationFiles ="staging=temp/myConfig.ini,preview=temp/myConfig2.ini";
        Assert.assertThat(testling.getWebScopeFileMap(testWebAppConfigurationFiles).get(WebEnvironment.WebScope.STAGING), is(new File("temp/myConfig.ini")));
        Assert.assertThat(testling.getWebScopeFileMap(testWebAppConfigurationFiles).get(WebEnvironment.WebScope.PREVIEW), is(new File("temp/myConfig2.ini")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getWebScopeFileMapWithNonExistentWebScope() throws Exception {
        String testWebAppConfigurationFiles ="staging=temp/myConfig.ini, XXX=temp/myConfig2.ini";
        testling.getWebScopeFileMap(testWebAppConfigurationFiles).get(WebEnvironment.WebScope.STAGING);
    }

    @Test
    public void getOptionalProjectAppConfigurationFile() throws Exception {
        String testProjectAppConfigurationFile = "staging=temp/myConfig.ini";
        Assert.assertNotNull(testling.createOptionalProjectAppConfigurationFile(testProjectAppConfigurationFile));
    }

    @Test
    public void getStringFilesMap() throws Exception {
        Map<String, File> stringFilesMap = testling.getStringFilesMap("staging=temp/myConfig.ini,preview=temp/myConfig2.ini");
        Assert.assertThat(stringFilesMap.get("staging"), is(new File("temp/myConfig.ini")));
        Assert.assertThat(stringFilesMap.get("preview"), is(new File("temp/myConfig2.ini")));
    }

    @Test
    public void extractWebScopes() throws Exception {
        String testWebAppScopes ="preview,staging";
        List<WebEnvironment.WebScope> extractedScopes = testling.extractWebScopes(testWebAppScopes);
        Assert.assertThat(extractedScopes, contains(WebEnvironment.WebScope.PREVIEW, WebEnvironment.WebScope.STAGING));
    }
}