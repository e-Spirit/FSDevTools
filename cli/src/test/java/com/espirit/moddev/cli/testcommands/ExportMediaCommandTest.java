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
import com.espirit.moddev.cli.results.ExportResult;
import com.espirit.moddev.cli.commands.export.ExportMediaCommand;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author e-Spirit AG
 */
@Category(IntegrationTest.class)
public class ExportMediaCommandTest extends AbstractIntegrationTest {

    @Test
    public void parameterLessCommandCreatesFiles() {
        ExportMediaCommand command = new ExportMediaCommand();

        initializeTestSpecificConfiguration(command);

        ExportResult result = command.call();
        // This value depends on the used test project
        Assert.assertTrue(result.get().getCreatedFiles().size() > 100);
    }

}
