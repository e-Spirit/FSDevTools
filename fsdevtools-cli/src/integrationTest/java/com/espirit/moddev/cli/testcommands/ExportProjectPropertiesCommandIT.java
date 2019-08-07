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

import com.espirit.moddev.cli.api.parsing.identifier.ProjectPropertiesIdentifier;
import com.espirit.moddev.cli.commands.export.ExportCommand;
import com.espirit.moddev.cli.results.ExportResult;
import de.espirit.firstspirit.transport.PropertiesTransportOptions;
import org.junit.Assert;
import org.junit.Test;

import java.util.EnumSet;

/**
 * The type Project config command test.
 *
 * @author e-Spirit AG
 */
public class ExportProjectPropertiesCommandIT extends AbstractIntegrationTest {

    @Test
    public void noParameterCommandWithProjectPropertiesCreatesFiles() throws Exception {
        ExportCommand command = new ExportCommand();
        command.addIdentifier("projectproperty:languages");
        command.setProject(PROJECT_NAME);
        initContextWithDefaultConfiguration(command);

        Assert.assertTrue(command.getIdentifiers().contains(new ProjectPropertiesIdentifier(EnumSet.of(PropertiesTransportOptions.ProjectPropertyType.LANGUAGES))));


        ExportResult result = command.call();
        Assert.assertTrue("Export folder for project properties not found.",
                          containsSubDirectory(getFirstSpiritFileSyncFolder(testFolder.getRoot()), "Global"));
        Assert.assertTrue(result.get().getCreatedFiles().size() > 0);
    }

}
