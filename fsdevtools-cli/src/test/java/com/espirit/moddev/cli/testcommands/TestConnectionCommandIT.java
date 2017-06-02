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

import com.espirit.moddev.cli.api.result.Result;
import com.espirit.moddev.cli.commands.test.TestConnectionCommand;
import com.espirit.moddev.serverrunner.NativeServerRunner;
import com.espirit.moddev.serverrunner.ServerProperties;
import com.sun.corba.se.spi.activation.Server;
import org.junit.*;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

import static com.espirit.moddev.IntegrationTest.PROJECT_NAME;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author e-Spirit AG
 */
@Category(com.espirit.moddev.IntegrationTest.class)
public class TestConnectionCommandIT extends AbstractIntegrationTest {

    @BeforeClass
    public static void startServer() {
        Optional<File> serverJarFileFromClasspath = ServerProperties.getServerJarFileFromClasspath();
        Optional<File> wrapperJarFileFromClasspath = ServerProperties.getWrapperJarFileFromClasspath();
        Assert.assertTrue("FirstSpirit server jar should be present on the classpath", serverJarFileFromClasspath.isPresent());
        Assert.assertTrue("FirstSpirit wrapper jar should be present on the classpath", wrapperJarFileFromClasspath.isPresent());

        Path fsServerRoot = new File(System.getProperty("fsServerRoot")).toPath();

        ServerProperties serverProperties = ServerProperties.builder()
                .firstSpiritJar(serverJarFileFromClasspath.get())
                .firstSpiritJar(wrapperJarFileFromClasspath.get())
                .serverRoot(fsServerRoot)
                .build();
        NativeServerRunner serverRunner = new NativeServerRunner(serverProperties);
        if(!serverRunner.isRunning()) {
            Assert.assertTrue("FirstSpirit server wasn't running and is started now", serverRunner.start());
        }
    }

    @Ignore("TODO: DEVEX-40")
    @Test
    public void connectionCanBeEstablished() {
        TestConnectionCommand command = new TestConnectionCommand();
        command.setProject(PROJECT_NAME);
        initDefaultConfiguration(command);

        Result result = command.call();
        assertFalse("The connection should be established correctly", result.isError());
    }

    @Test
    public void connectionCannotBeEstablishedWithWrongHost() {
        TestConnectionCommand command = new TestConnectionCommand();
        command.setProject(PROJECT_NAME);
        command.setHost("nonexistenthost");

        Result result = command.call();

        assertTrue("The connection should not be established correctly, because the host doesn't exist", result.isError());
    }
}
