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
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author e-Spirit AG
 */
public class TestConnectionCommandIT extends AbstractIntegrationTest {

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
