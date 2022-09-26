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

package com.espirit.moddev.cli.commands.server.startCommand;

import com.espirit.moddev.cli.commands.server.common.AbstractServerCommandTest;
import com.espirit.moddev.cli.results.SimpleResult;
import com.espirit.moddev.cli.commands.server.utils.ServerRunner;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServerStartCommandTest extends AbstractServerCommandTest<ServerStartCommand> {

	@NotNull
	@Override
	protected ServerStartCommand createTestling() {
		return new ServerStartCommand();
	}

	@Test
	public void getWaitTime_default() {
		final ServerStartCommand instance = createTestling();
		assertEquals(ServerStartCommand.DEFAULT_WAIT_TIME, instance.getWaitTimeInSeconds());
	}

	@Test
	public void getWaitTime_customValue() {
		final ServerStartCommand instance = createTestling();
		final int expected = 10;
		instance.setWaitTimeInSeconds(expected);
		assertEquals(expected, instance.getWaitTimeInSeconds());
	}

	@Test
	public void getServerDir_default() {
		final ServerStartCommand instance = createTestling();
		assertNull(instance.getServerDir());
	}

	@Test
	public void getServerDir_customValue() {
		final ServerStartCommand instance = createTestling();
		final String expected = "myServerDir";
		instance.setServerDir(expected);
		assertEquals(expected, instance.getServerDir());
	}

	@Test
	public void call_serverStart_successful() throws Exception {
		final ServerStartCommand instance = new ServerStartCommand() {
			@NotNull
			@Override
			synchronized ServerRunner getServerRunner() {
				return new ServerRunner() {
					@Override
					public void start() throws IOException {
						// do nothing for this test (which means that the server has been started)
					}
				};
			}
		};
		final SimpleResult<String> result = instance.call();
		assertFalse(result.isError());
		assertEquals(ServerStartCommand.MSG_SUCCESS, result.get());
	}

	@Test
	public void call_serverStart_failed() throws Exception {
		final ServerStartCommand instance = new ServerStartCommand() {
			@NotNull
			@Override
			synchronized ServerRunner getServerRunner() {
				return new ServerRunner() {
					@Override
					public void start() throws IOException {
						throw new IOException();
					}
				};
			}
		};
		final SimpleResult<String> result = instance.call();
		assertTrue(result.isError());
		assertEquals(ServerStartCommand.MSG_ERROR, result.getError().getMessage());
	}

}
