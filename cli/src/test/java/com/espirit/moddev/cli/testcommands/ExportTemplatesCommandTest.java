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
import com.espirit.moddev.cli.api.FullQualifiedUid;
import com.espirit.moddev.cli.commands.export.ExportTemplatesCommand;
import de.espirit.firstspirit.access.store.IDProvider;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author e-Spirit AG
 */
@Category(IntegrationTest.class)
public class ExportTemplatesCommandTest extends AbstractIntegrationTest {

    @Test
    public void parameterLessCommandCreatesFiles() {
        ExportTemplatesCommand command = new ExportTemplatesCommand();
        initializeTestSpecificConfiguration(command);

        ExportResult result = command.call();

        // This value depends on the used test project
        Assert.assertTrue(result.get().getCreatedFiles().size() > 100);
    }

    @Test
    public void singleParameterCommandCreatesFiles() {
        ExportTemplatesCommand command = new ExportTemplatesCommand();

        command.getArgs().add("page:homepage");
        initializeTestSpecificConfiguration(command);
        Assert.assertTrue(command.getFullQualifiedUids().contains(new FullQualifiedUid(IDProvider.UidType.PAGESTORE, "homepage")));

        ExportResult result = command.call();
        Assert.assertTrue(result.get().getCreatedFiles().size() > 0);
    }
}
