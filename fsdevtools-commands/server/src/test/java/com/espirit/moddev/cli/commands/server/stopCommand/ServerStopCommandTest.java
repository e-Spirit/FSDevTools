/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2024 Crownpeak Technology GmbH
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

package com.espirit.moddev.cli.commands.server.stopCommand;

import com.espirit.moddev.cli.commands.server.common.AbstractServerCommandTest;
import com.espirit.moddev.cli.commands.server.utils.ServerRunner;
import com.espirit.moddev.cli.results.SimpleResult;
import com.espirit.moddev.connection.FsConnectionConfig;
import com.espirit.moddev.util.FsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServerStopCommandTest extends AbstractServerCommandTest<ServerStopCommand> {

	@NotNull
	@Override
	protected ServerStopCommand createTestling() {
		return new ServerStopCommand();
	}

	@Test
	public void getServerDir_default() {
		final ServerStopCommand instance = createTestling();
		assertNull(instance.getServerDir());
	}

	@Test
	public void getServerDir_customValue() {
		final ServerStopCommand instance = createTestling();
		final String expected = "myServerDir";
		instance.setServerDir(expected);
		assertEquals(expected, instance.getServerDir());
	}

	@Test
	public void getHost_default() {
		final ServerStopCommand instance = createTestling();
		assertNull(instance.getHost());
	}

	@Test
	public void getHost_customValue() {
		final ServerStopCommand instance = createTestling();
		final String expected = "myHost";
		instance.setHost(expected);
		assertEquals(expected, instance.getHost());
	}

	@Test
	public void getPort_default() {
		final ServerStopCommand instance = createTestling();
		assertEquals(0, instance.getPort());
	}

	@Test
	public void getPort_customValue() {
		final ServerStopCommand instance = createTestling();
		final int expected = 1337;
		instance.setPort(expected);
		assertEquals(expected, instance.getPort());
	}

	@Test
	public void call_remote_serverStop_successful() {
		final ServerStopCommand instance = new ServerStopCommand() {
			@NotNull
			@Override
			ServerRunner getServerRunner(@Nullable final String serverDir) {
				return new ServerRunner() {

					@Override
					public void stop(@NotNull final FsConnectionConfig config) throws IOException {
						// do nothing for this test (which means that the server has been stopped)
					}
				};
			}
		};
		final String host = FsUtil.VALUE_DEFAULT_HOST;
		final int port = 80;
		instance.setHost(host);
		instance.setPort(port);
		final SimpleResult<Boolean> result = instance.call();
		assertFalse(result.isError());
		assertTrue(result.get());
	}

	@Test
	public void call_local_serverStop_successful() {
		final ServerStopCommand instance = new ServerStopCommand() {
			@Override
			boolean updateLocalConfig(@NotNull final FsConnectionConfig config) {
				return true; // nothing to do in junit
			}

			@NotNull
			@Override
			ServerRunner getServerRunner(@Nullable final String serverDir) {
				return new ServerRunner() {
					@Override
					public void stop(@NotNull final FsConnectionConfig config) throws IOException {
						// do nothing for this test (which means that the server has been stopped)
					}
				};
			}
		};
		instance.setServerDir("/my/path/to/dir");
		final SimpleResult<Boolean> result = instance.call();
		assertFalse(result.isError());
		assertTrue(result.get());
	}

	@Test
	public void call_serverStop_wrongArguments() {
		final ServerStopCommand instance = new ServerStopCommand();
		final SimpleResult<Boolean> result = instance.call();
		assertTrue(result.isError());
		assertTrue(result.getError().getMessage().contains(ServerStopCommand.MSG_ERROR));
	}

	@Test
	public void call_local_serverStop_successful_noLockFile() {
		final ServerStopCommand instance = new ServerStopCommand() {
			@NotNull
			@Override
			ServerRunner getServerRunner(@Nullable final String serverDir) {
				return new ServerRunner() {
					@Override
					public void stop(@NotNull final FsConnectionConfig config) throws IOException {
						// do nothing for this test (which means that the server has been stopped)
					}
				};
			}
		};
		instance.setServerDir("/my/path/to/dir");
		final SimpleResult<Boolean> result = instance.call();
		assertFalse(result.isError());
		assertTrue(result.get());
	}

	@Test
	public void call_serverStop_failed() {
		final ServerStopCommand instance = new ServerStopCommand() {
			@NotNull
			@Override
			ServerRunner getServerRunner(@Nullable final String serverDir) {
				return new ServerRunner() {
					@Override
					public void stop(@NotNull final FsConnectionConfig config) throws IOException {
						throw new IOException();
					}
				};
			}
		};
		final SimpleResult<Boolean> result = instance.call();
		assertTrue(result.isError());
		assertEquals(ServerStopCommand.MSG_ERROR, result.getError().getMessage());
	}

}
