package com.espirit.moddev.moduleinstaller;

import com.espirit.moddev.shared.webapp.WebAppIdentifier;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class ModuleInstallationParametersBuilderTest {

    private ModuleInstallationParameters.ModuleInstallationParametersBuilder builder;

    @Before
    public void setUp() throws Exception {
        builder = ModuleInstallationParameters.builder();
    }

    @Test
    public void getWebScopeFileMap() throws Exception {
        String testWebAppConfigurationFiles = "staging=temp/myConfig.ini,preview=temp/myConfig2.ini";
        assertThat(builder.getWebScopeFileMap(testWebAppConfigurationFiles).get(WebAppIdentifier.STAGING), is(new File("temp/myConfig.ini")));
        assertThat(builder.getWebScopeFileMap(testWebAppConfigurationFiles).get(WebAppIdentifier.PREVIEW), is(new File("temp/myConfig2.ini")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getWebScopeFileMapWithNonExistentWebScope() throws Exception {
        String testWebAppConfigurationFiles = "staging=temp/myConfig.ini, XXX=temp/myConfig2.ini";
        builder.getWebScopeFileMap(testWebAppConfigurationFiles).get(WebAppIdentifier.STAGING);
    }

    @Test
    public void getOptionalProjectAppConfigurationFile() throws Exception {
        String testProjectAppConfigurationFile = "staging=temp/myConfig.ini";
        assertNotNull(builder.createOptionalProjectAppConfigurationFile(testProjectAppConfigurationFile));
    }

    @Test
    public void getStringFilesMap() throws Exception {
        Map<String, File> stringFilesMap = builder.getStringFilesMap("staging=temp/myConfig.ini,preview=temp/myConfig2.ini");
        assertThat(stringFilesMap.get("staging"), is(new File("temp/myConfig.ini")));
        assertThat(stringFilesMap.get("preview"), is(new File("temp/myConfig2.ini")));
    }

    @Test
    public void getDeploy() throws Exception {
        assertTrue("Default value mismatch", builder.shouldDeploy());
        assertTrue("Value mismatch", builder.deploy("true").shouldDeploy());
        assertTrue("Value mismatch", builder.deploy("trUE").shouldDeploy());
        assertTrue("Value mismatch", builder.deploy("TRUE").shouldDeploy());
        assertFalse("Value mismatch", builder.deploy("false").shouldDeploy());
        assertFalse("Value mismatch", builder.deploy("falSE").shouldDeploy());
        assertFalse("Value mismatch", builder.deploy("FALSE").shouldDeploy());
        assertTrue("Value mismatch", builder.deploy("illegalValue").shouldDeploy());
    }

}