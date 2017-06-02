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

package com.espirit.moddev.cli.testcommands;

import com.espirit.moddev.IntegrationTest;
import com.espirit.moddev.cli.commands.server.ServerStartCommand;
import com.espirit.moddev.cli.results.SimpleResult;
import com.espirit.moddev.serverrunner.NativeServerRunner;
import com.espirit.moddev.serverrunner.ServerProperties;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author e-Spirit AG
 */
@Category(IntegrationTest.class)
public class ServerStartCommandIT extends AbstractIntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerStartCommandIT.class);

    @Test
    public void simpleStartIsSuccessful() throws Exception {

        ServerProperties.ServerPropertiesBuilder serverPropertiesBuilder = ServerProperties.builder();

        serverPropertiesBuilder.serverRoot(new File(System.getProperty("fsServerRoot")).toPath());

        List<File> jarsFromClasspath = ServerProperties.getFirstSpiritJarsFromClasspath();
        Assert.assertThat("The FirstSpirit and wrapper jars couldn't be found on the classpath!", jarsFromClasspath.size(), is(greaterThanOrEqualTo(2)));

        ServerProperties serverProperties = serverPropertiesBuilder.firstSpiritJars(jarsFromClasspath).build();
        NativeServerRunner serverRunner = new NativeServerRunner(serverProperties);
        assertThat("Expected stopping server to be successful", serverRunner.stop(), is(true));

        ServerStartCommand command = new ServerStartCommand();
        command.initializeFromProperties(serverProperties);

        SimpleResult<String> firstStartResult = command.call();
        assertThat("Expected command to start a server", firstStartResult.isError(), is(false));

        assertThat("Expected server to be running now", serverRunner.isRunning(), is(true));
        SimpleResult<String> secondStartResult = command.call();
        assertThat("Expected command to start a server", secondStartResult.isError(), is(false));

    }

}
