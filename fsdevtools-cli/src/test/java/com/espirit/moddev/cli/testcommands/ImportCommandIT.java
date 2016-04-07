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
import com.espirit.moddev.cli.CliContextImpl;
import com.espirit.moddev.cli.commands.ImportCommand;
import com.espirit.moddev.cli.results.ImportResult;

import de.espirit.firstspirit.store.access.nexport.operations.ImportOperation;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.util.Arrays;

import static com.espirit.moddev.IntegrationTest.PROJECT_NAME;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author e-Spirit AG
 */
@Category(IntegrationTest.class)
public class ImportCommandIT extends AbstractIntegrationTest {

    @Test
    public void parameterLessCommandCreatesElements() {
        final ImportCommand command = new ImportCommand();

        initializeTestSpecificConfiguration(command);
        command.setProject(PROJECT_NAME + "123");
        final File syncDirectory = new File("./src/test/resources");
        assertTrue("syncDirectory is not a directory", syncDirectory.isDirectory());

        final boolean isSyncDirectory = Arrays.asList(syncDirectory.listFiles()).stream().anyMatch(o -> o.getName().equals(".FirstSpirit"));
        assertTrue("syncDirectory is not a FirstSpirit directory", isSyncDirectory);

        command.setSynchronizationDirectory(syncDirectory.getPath());
        command.setContext(new CliContextImpl(command));

        final ImportResult result = command.call();
        final ImportOperation.Result importResult = result.get();

        assertThat("Expected 0 problems: " + importResult.getProblems().size() + " -> " + importResult.getProblems().stream().map(
                       a -> a.getNodeId() + "@" + a.getMessage()).reduce((t, u) -> t + ", " + u).get(),
                   importResult.getProblems(), hasSize(0));
    }

}
