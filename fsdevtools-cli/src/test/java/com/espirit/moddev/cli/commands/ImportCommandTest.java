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

import com.espirit.moddev.cli.CliConstants;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * The Class ImportCommandTest tests the default settings.
 */
public class ImportCommandTest {

	private ImportCommand _testling;

	@Before
	public void setUp() {
		_testling = new ImportCommand();
	}

	@Test
	public void testIsCreatingProjectIfMissing() {
		assertThat("Expect true", _testling.isCreatingProjectIfMissing(), is(Boolean.TRUE));
	}

	@Test
	public void default_value_importScheduleEntryActiveState() {
		assertThat("Expect false", _testling.importScheduleEntryActiveState, is(Boolean.FALSE));
	}

	@Test
	public void testGetImportComment() {
		assertThat("Expect null value", _testling.getImportComment(), is("Imported by " + CliConstants.FS_CLI));
	}

}
