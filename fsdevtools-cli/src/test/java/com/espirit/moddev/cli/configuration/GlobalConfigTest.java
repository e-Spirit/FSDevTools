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

package com.espirit.moddev.cli.configuration;

import com.espirit.moddev.cli.CliConstants;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author e-Spirit AG
 */
public class GlobalConfigTest {

    @Test
    public void userIsNotFetchedFromEnvironmentIfConfigured() {
        final GlobalConfig config = new GlobalConfig();
        config.setUser("abc");
        config.getEnvironment().clear();
        config.getEnvironment().put(CliConstants.KEY_FS_USER.value(), "xyz");

        assertEquals("abc", config.getUser());
    }

    @Test
    public void userIsFetchedFromEnvironmentIfNotConfigured() {
        final GlobalConfig config = new GlobalConfig();
        config.getEnvironment().clear();
        config.getEnvironment().put(CliConstants.KEY_FS_USER.value(), "xyz");

        assertEquals("xyz", config.getUser());
    }

    @Test
    public void defaultUserIsReturnedIfNoUserIsConfigured() {
        final GlobalConfig config = new GlobalConfig();
        config.getEnvironment().clear();

        assertEquals(CliConstants.DEFAULT_USER.value(), config.getUser());
    }

    @Test
    public void nullProjectIsReturnedIfNoProjectIsConfigured() {
        final GlobalConfig config = new GlobalConfig();

        config.getEnvironment().clear();
        Assert.assertNull(config.getProject());
    }

    @Test
    public void defaultHTTPProxySettings() {
        final GlobalConfig config = new GlobalConfig();
        assertEquals("", config.getHttpProxyHost());
        assertEquals(Integer.valueOf(8080), config.getHttpProxyPort());
    }

    @Test
    public void nullHTTPProxySettings() {
        final GlobalConfig config = new GlobalConfig();
        config.setHttpProxyHost(null);
        config.setHttpProxyPort(null);
        assertEquals("", config.getHttpProxyHost());
        assertEquals(Integer.valueOf(8080), config.getHttpProxyPort());
    }

    @Test
    public void customHTTPProxySettings() {
        final String customHost = "myHost";
        final Integer customPort = Integer.valueOf(1337);
        final GlobalConfig config = new GlobalConfig();
        config.setHttpProxyHost(customHost);
        config.setHttpProxyPort(customPort);
        assertEquals(customHost, config.getHttpProxyHost());
        assertEquals(customPort, config.getHttpProxyPort());
    }

}