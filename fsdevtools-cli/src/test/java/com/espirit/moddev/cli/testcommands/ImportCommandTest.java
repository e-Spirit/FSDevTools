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

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.util.Arrays;

/**
 * @author e-Spirit AG
 */
@Category(IntegrationTest.class)
public class ImportCommandTest extends AbstractIntegrationTest {

    @Test
    public void parameterLessCommandCreatesElements() {
        ImportCommand command = new ImportCommand();

        initializeTestSpecificConfiguration(command);
        File syncDirectory = new File("./src/test/resources");
        Assert.assertTrue(syncDirectory.isDirectory());
        boolean isSyncDirectory = Arrays.asList(syncDirectory.listFiles()).stream().anyMatch(o -> o.getName().equals(".FirstSpirit"));
        Assert.assertTrue(isSyncDirectory);

        command.getArgs().add(syncDirectory.getPath());
        command.setContext(new CliContextImpl(command));

        ImportResult result = command.call();
        ImportOperation.Result importResult = result.get();

        Assert.assertTrue(importResult.getProblems().size() == 0);
    }

}
