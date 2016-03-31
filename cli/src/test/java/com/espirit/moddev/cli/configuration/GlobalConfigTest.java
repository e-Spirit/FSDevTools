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

import junit.framework.Assert;
import org.junit.Test;

/**
 * @author e-Spirit AG
 */
public class GlobalConfigTest {

    @Test
    public void userIsNotFetchedFromEnvironmentIfConfigured() {
        GlobalConfig config = new GlobalConfig();
        config.setUser("abc");
        config.getEnvironment().clear();
        config.getEnvironment().put(CliConstants.KEY_FS_USER.value(), "xyz");

        Assert.assertEquals("abc", config.getUser());
    }
    @Test
    public void userIsFetchedFromEnvironmentIfNotConfigured() {
        GlobalConfig config = new GlobalConfig();
        config.getEnvironment().clear();
        config.getEnvironment().put(CliConstants.KEY_FS_USER.value(), "xyz");

        Assert.assertEquals("xyz", config.getUser());
    }
    @Test
    public void defaultUserIsReturnedIfNoUserIsConfigured() {
        GlobalConfig config = new GlobalConfig();
        config.getEnvironment().clear();

        Assert.assertEquals(CliConstants.DEFAULT_USER.value(), config.getUser());
    }

}
