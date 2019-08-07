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
import com.espirit.moddev.serverrunner.NativeServerRunner;
import com.espirit.moddev.serverrunner.ServerRunner;
import static org.junit.Assert.assertNotNull;

import org.jetbrains.annotations.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;


public class ServerRestartCommandTest extends AbstractServerCommandTest {
    
    @Rule
    public MockitoRule injectMocks = MockitoJUnit.rule();
    
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
    
    @Override
    protected ServerRestartCommand createTestling() {
        return new ServerRestartCommand(){
            @Override
            ServerRunner getServerRunner() {
                return _runner;
            }

            @Override
            public SimpleResult<String> call() throws Exception {
                return new SimpleResult(true);
            }
        };
    }
    
}
