package com.espirit.moddev.cli.commands.module;

import de.espirit.firstspirit.module.WebEnvironment;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

public class InstallModuleCommandTest {
    InstallModuleCommand testling;

    @Before
    public void setUp() {
        testling = new InstallModuleCommand();
    }

    @Test
    public void getWebScopeFileMap() throws Exception {
        testling.setWebAppConfigurationFiles("staging=temp/myConfig.ini,preview=temp/myConfig2.ini");
        Assert.assertThat(testling.getWebScopeFileMap().get(WebEnvironment.WebScope.STAGING), is(new File("temp/myConfig.ini")));
        Assert.assertThat(testling.getWebScopeFileMap().get(WebEnvironment.WebScope.PREVIEW), is(new File("temp/myConfig2.ini")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getWebScopeFileMapWithNonExistentWebScope() throws Exception {
        testling.setWebAppConfigurationFiles("staging=temp/myConfig.ini, XXX=temp/myConfig2.ini");
        testling.getWebScopeFileMap().get(WebEnvironment.WebScope.STAGING);
    }

    @Test
    public void getOptionalProjectAppConfigurationFile() throws Exception {
        testling.setProjectAppConfigurationFile("staging=temp/myConfig.ini");
        Assert.assertNotNull(testling.getOptionalProjectAppConfigurationFile());
    }

    @Test
    public void getStringFilesMap() throws Exception {
        Map<String, File> stringFilesMap = testling.getStringFilesMap("staging=temp/myConfig.ini,preview=temp/myConfig2.ini");
        Assert.assertThat(stringFilesMap.get("staging"), is(new File("temp/myConfig.ini")));
        Assert.assertThat(stringFilesMap.get("preview"), is(new File("temp/myConfig2.ini")));
    }

    @Test
    public void extractWebScopes() throws Exception {
        testling.setWebAppScopes("preview,staging");
        List<WebEnvironment.WebScope> extractedScopes = testling.extractWebScopes();
        Assert.assertThat(extractedScopes, contains(WebEnvironment.WebScope.PREVIEW, WebEnvironment.WebScope.STAGING));
    }

}
