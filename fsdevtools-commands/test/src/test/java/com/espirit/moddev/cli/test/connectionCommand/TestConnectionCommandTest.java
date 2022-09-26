/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2022 Crownpeak Technology GmbH
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

package com.espirit.moddev.cli.test.connectionCommand;

import com.espirit.moddev.cli.api.result.Result;

import com.espirit.moddev.cli.commands.test.connectionCommand.TestConnectionCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.espirit.firstspirit.access.Connection;

import java.io.IOException;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TestConnectionCommandTest {

	private TestConnectionCommand testling;
	private Connection connection;

	@BeforeEach
	public void setUp() throws Exception {
		connection = mock(Connection.class);
		testling = new TestConnectionCommand() {
			@Override
			protected Connection create() {
				return connection;
			}
		};
	}

	@Test
	public void testCall() throws Exception {
		final Result result = testling.call();

		assertThat("Expect normal execution", result.isError(), is(FALSE));
		assertThat("Expect null value", result.getError(), is(nullValue()));

		verify(connection, times(1)).connect();
	}

	@Test
	public void testCallError() throws Exception {
		doThrow(new IOException("Junit")).when(connection).connect();

		final Result result = testling.call();

		assertThat("Expect normal execution", result.isError(), is(TRUE));
		assertThat("Expect non-null value", result.getError(), is(notNullValue()));

		verify(connection, times(1)).connect();
	}

	@Test
	public void testCallNullConnection() throws Exception {
		connection = null;

		final Result result = testling.call();

		assertThat("Expect normal execution", result.isError(), is(TRUE));
		assertThat("Expect non-null value", result.getError(), is(notNullValue()));
	}

	@Test
	public void testNeedsContext() throws Exception {
		assertThat("This command creates his own FS connection therefore doesn't need one from outside",
				testling.needsContext(), is(FALSE));
	}

}
