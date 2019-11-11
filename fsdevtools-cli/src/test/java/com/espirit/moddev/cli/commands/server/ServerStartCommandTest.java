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

package com.espirit.moddev.cli.commands.server;

import com.espirit.moddev.cli.results.SimpleResult;
import com.espirit.moddev.serverrunner.ServerProperties;
import com.espirit.moddev.serverrunner.ServerRunner;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.File;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class ServerStartCommandTest extends AbstractServerCommandTest {

    @Rule
    public TemporaryFolder _temporaryFolder = new TemporaryFolder();

    @Rule
    public MockitoRule _injectMocks = MockitoJUnit.rule();

    @Mock
    private ServerRunner _runner;

    @Before
    public void setUp() {
    }

    @Test
    public void testCall() throws Exception {

        when(_runner.start()).thenReturn(Boolean.TRUE);

        System.out.println("call");
        ServerStartCommand instance = createTestling();
        SimpleResult<String> result = instance.call();
        assertNotNull(result);
    }

    @Test
    public void testGetServerProperties() {
        System.out.println("getServerProperties");
        ServerStartCommand instance = createTestling();
        ServerProperties result = instance.getServerProperties();
        assertNotNull(result);
    }

    @Test
    public void testGetServerJar() {
        System.out.println("getServerJar");
        ServerStartCommand instance = createTestling();
        instance.setServerJar("test");
        String expResult = "test";
        String result = instance.getServerJar();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetEmptyServerJar() {
        System.out.println("getServerJar");
        ServerStartCommand instance = createTestling();
        String result = instance.getServerJar();
        assertNull(result);
    }

    @Test
    public void testGetWrapperJar() {
        System.out.println("getWrapperJar");
        ServerStartCommand instance = createTestling();
        instance.setWrapperJar("test");
        String expResult = "test";
        String result = instance.getWrapperJar();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetEmptyWrapperJar() {
        System.out.println("getWrapperJar");
        ServerStartCommand instance = createTestling();
        String result = instance.getWrapperJar();
        assertNull(result);
    }

    @Test
    public void testGetWaitTimeInSeconds() {
        System.out.println("getWaitTimeInSeconds");
        ServerStartCommand instance = createTestling();
        long expResult = 600;
        long result = instance.getWaitTimeInSeconds();
        assertEquals(expResult, result);
    }

    @NotNull
    @Override
    protected ServerStartCommand createTestling() {
        return new ServerStartCommand() {
            @NotNull
            @Override
            protected ServerRunner getOrCreateServerRunner(final ServerProperties serverProperties) {
                return _runner;
            }
        };
    }

    // this test only works with a license allowing api calls - specify one by passing e.g.
    // -DfsLicenseFile=/home/[username]/cmsserver/conf/fs-license.conf to the Java VM options
    @Test
    public void simpleStartIsSuccessful() throws Exception {
        ServerProperties.ServerPropertiesBuilder serverPropertiesBuilder = ServerProperties.builder();
        serverPropertiesBuilder.serverRoot(_temporaryFolder.newFolder("FirstSpirit").toPath());

        // determine random ports
        int httpPort = ServerProperties.port(0);
        int socketPort = httpPort;
        while (socketPort == httpPort) {
            socketPort = ServerProperties.port(0);
        }
        serverPropertiesBuilder.httpPort(httpPort);
        serverPropertiesBuilder.socketPort(socketPort);
        serverPropertiesBuilder.timeout(Duration.ofMinutes(10));

        List<File> jarsFromClasspath = ServerProperties.getFirstSpiritJarsFromClasspath();
        Assert.assertThat("The FirstSpirit and wrapper jars couldn't be found on the classpath!", jarsFromClasspath.size(), is(greaterThanOrEqualTo(2)));

        // setup command
        final ServerProperties serverProperties = serverPropertiesBuilder.firstSpiritJars(jarsFromClasspath).connectionMode(ServerProperties.ConnectionMode.SOCKET_MODE).build();
        ServerStartCommand command = new ServerStartCommand();
        try {
            command.initializeFromProperties(serverProperties);
            command.setWaitTimeInSeconds(serverProperties.getTimeout().getSeconds());
            command.setLicenseFilePath(System.getProperty("fsLicenseFile"));
            command.setConnectionMode(serverProperties.getMode());

            // start the server
            SimpleResult<String> firstStartResult = command.call();
            assertThat(firstStartResult.isError()).describedAs("Expected command to start a server").isFalse();
            assertThat(command.getServerRunner()).describedAs("Expected server runner to be null").isNotNull();
            assertThat(command.getServerRunner().isRunning()).describedAs("Expected server to be running now").isTrue();

            // test the connection again --> command will throw an error
            SimpleResult<String> secondStartResult = command.call();
            assertThat(secondStartResult.isError()).describedAs("Second start command should throw an error").isFalse();
        } finally {
            // stop the server
            final ServerRunner serverRunner = command.getServerRunner();
            if (serverRunner != null) {
                serverRunner.stop();
            }
        }
    }

}
