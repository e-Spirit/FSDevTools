/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2025 Crownpeak Technology GmbH
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

package com.espirit.moddev.cli.test.projectCommand;

import com.espirit.moddev.cli.CliContextImpl;
import com.espirit.moddev.cli.api.result.Result;
import com.espirit.moddev.cli.commands.test.projectCommand.TestProjectCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

public class TestProjectCommandTest {

	private TestProjectCommand testling;

	@BeforeEach
	public void setUp() throws Exception {
		testling = new TestProjectCommand() {
			@Override
			protected CliContextImpl create() {
				return mock(CliContextImpl.class, RETURNS_DEEP_STUBS);
			}
		};
	}

	@Test
	public void testCall() throws Exception {
		final Result result = testling.call();

		assertThat(result.isError()).as("Expect normal execution").isEqualTo(FALSE);
		assertThat(result.getError()).as("Expect null value").isNull();
	}

	@Test
	public void testCallError() throws Exception {
		testling = new TestProjectCommand() {
			@Override
			protected CliContextImpl create() {
				throw new RuntimeException("JUnit");
			}
		};

		final Result result = testling.call();

		assertThat(result.isError()).as("Expect abnormal execution").isEqualTo(TRUE);
		assertThat(result.getError()).as("Expect non-null value").isNotNull();
	}

	@Test
	public void testNeedsContext() throws Exception {
		assertThat(testling.needsContext()).as("This command creates his own context to initialize connection therefore doesn't need a context from outside").isEqualTo(FALSE);
	}

	@Test
	public void testCreatingProjectIfMissing() throws Exception {
		assertThat(testling.isCreatingProjectIfMissing()).as("Should not create missing projects").isEqualTo(FALSE);
	}

}
