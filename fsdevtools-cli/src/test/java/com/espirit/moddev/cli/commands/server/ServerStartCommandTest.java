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
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;


public class ServerStartCommandTest extends AbstractServerCommandTest {
    
    @Rule
    public MockitoRule injectMocks = MockitoJUnit.rule();
    
    @Mock
    private ServerRunner runner;
    
    @Before
    public void setUp() {
    }

    @Test
    public void testCall() throws Exception {
        
        when(runner.start()).thenReturn(Boolean.TRUE);
        
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
        long expResult = -1L;
        long result = instance.getWaitTimeInSeconds();
        assertEquals(expResult, result);
    }

    @Override
    protected ServerStartCommand createTestling() {
        return new ServerStartCommand(){
            @Override
            protected ServerRunner createRunner(ServerProperties serverProperties) {
               return runner;
            }
        };
    }

    
    
}
