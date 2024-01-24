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

package com.espirit.moddev.cli.commands.server.common;

import com.espirit.moddev.connection.FsConnectionType;
import com.espirit.moddev.util.FsUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class AbstractServerCommandTest<T extends AbstractServerCommand> {

	@NotNull
	protected abstract T createTestling();

	@Test
	public void getPassword_default() {
		final AbstractServerCommand instance = createTestling();
		final String expResult = FsUtil.VALUE_DEFAULT_USER;
		final String result = instance.getPassword();
		assertEquals(expResult, result);
	}

	@Test
	public void getConnectionMode_default() {
		final AbstractServerCommand instance = createTestling();
		assertEquals(FsConnectionType.HTTP, instance.getFsMode());
	}

	@Test
	public void getConnectionMode_http() {
		final AbstractServerCommand instance = createTestling();
		instance.setFsMode(FsConnectionType.HTTP);
		assertEquals(FsConnectionType.HTTP, instance.getFsMode());
	}

	@Test
	public void getConnectionMode_https() {
		final AbstractServerCommand instance = createTestling();
		instance.setFsMode(FsConnectionType.HTTPS);
		assertEquals(FsConnectionType.HTTPS, instance.getFsMode());
	}

	@Test
	public void getConnectionMode_socket() {
		final AbstractServerCommand instance = createTestling();
		instance.setFsMode(FsConnectionType.SOCKET);
		assertEquals(FsConnectionType.SOCKET, instance.getFsMode());
	}

}
