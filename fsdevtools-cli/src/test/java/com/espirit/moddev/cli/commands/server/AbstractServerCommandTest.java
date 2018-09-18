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

import java.nio.file.Paths;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public abstract class AbstractServerCommandTest {
    
    public AbstractServerCommandTest() {
    }

    @Before
    public void setUp() {
    }

    @Test
    public void testGetServerRoot() {
        System.out.println("getServerRoot");
        AbstractServerCommand instance = createTestling();
        String expResult = Paths.get(System.getProperty("user.home"), "opt", "FirstSpirit").toString();
        String result = instance.getServerRoot();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetHost() {
        System.out.println("getHost");
        AbstractServerCommand instance = createTestling();
        String expResult = "localhost";
        String result = instance.getHost();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetPort() {
        System.out.println("getPort");
        AbstractServerCommand instance = createTestling();
        Integer expResult = 8000;
        Integer result = instance.getPort();
        assertEquals(expResult, result);
    }


    @Test
    public void testGetPassword() {
        System.out.println("getPassword");
        AbstractServerCommand instance = createTestling();
        String expResult = "Admin";
        String result = instance.getPassword();
        assertEquals(expResult, result);
    }

    protected abstract <T extends AbstractServerCommand> T createTestling();
    
    
}
