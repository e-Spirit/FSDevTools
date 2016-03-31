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

import com.espirit.moddev.cli.commands.export.ExportProjectPropertiesCommand;
import com.espirit.moddev.cli.results.ExportResult;

import org.junit.Assert;
import org.junit.Test;

/**
 * The type Project config command test.
 *
 * @author e-Spirit AG
 */
public class ExportProjectPropertiesCommandTest extends AbstractIntegrationTest {

    @Test
    public void noParameterCommandWithProjectPropertiesCreatesFiles() throws Exception {
        ExportProjectPropertiesCommand command = new ExportProjectPropertiesCommand();

        initializeTestSpecificConfiguration(command);

        ExportResult result = command.call();
        Assert.assertTrue("Export folder for project properties not found.",
                          containsSubDirectory(getFirstSpiritFileSyncFolder(testFolder.getRoot()), "Global"));
        Assert.assertTrue(result.get().getCreatedFiles().size() > 0);
    }

}
