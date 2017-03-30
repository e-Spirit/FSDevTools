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

package com.espirit.moddev.cli.commands;

import com.espirit.moddev.cli.commands.export.ExportCommand;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * The class ExportCommandTest tests the default settings of ExportCommand.
 */
public class ExportCommandTest {

    private ExportCommand testling;

    @Before
    public void setUp() throws Exception {
        testling = new ExportCommand();
    }

    /**
     * Tests {@link ExportCommand#isExportReleaseState()}
     */
    @Test
    public void testIsExportReleaseState() throws Exception {
        assertThat("export current by default", testling.isExportReleaseState(), is(Boolean.FALSE));
    }


    /**
     * Tests {@link ExportCommand#isExportReleaseState()}
     */
    @Test
    public void testIsExportReleaseState_setter() throws Exception {
        testling.setExportReleaseState(true);
        assertThat("export current by default", testling.isExportReleaseState(), is(Boolean.TRUE));
    }


    /**
     * Tests {@link ExportCommand#isDeleteObsoleteFiles()}
     */
    @Test
    public void testIsDeleteObsoleteFiles() throws Exception {
        assertThat("delete obsolete files by default", testling.isDeleteObsoleteFiles(), is(Boolean.TRUE));
    }


    /**
     * Tests {@link ExportCommand#isIncludeProjectProperties()}
     */
    @Test
    public void testisIncludeProjectProperties() throws Exception {
        assertThat("by default no project properties should be exported", testling.isIncludeProjectProperties(), is(Boolean.FALSE));
    }


}
