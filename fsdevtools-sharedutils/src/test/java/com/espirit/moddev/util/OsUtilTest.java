/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2021 e-Spirit GmbH
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

package com.espirit.moddev.util;


import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OsUtilTest {

	@Test
	public void asWindowsCommands() {
		final List<String> windowsCommands = OsUtil.asWindowsCommands(Arrays.asList("bin", "myFile.exe"));
		assertEquals(4, windowsCommands.size());
		assertEquals("cmd", windowsCommands.get(0));
		assertEquals("/c", windowsCommands.get(1));
		assertEquals("bin", windowsCommands.get(2));
		assertEquals("myFile.exe", windowsCommands.get(3));
	}

	@Test
	public void asUnixCommands() {
		final List<String> unixCommands = OsUtil.asUnixCommands(Arrays.asList("bin", "myFile.sh"));
		assertEquals(3, unixCommands.size());
		assertEquals("sh", unixCommands.get(0));
		assertEquals("bin", unixCommands.get(1));
		assertEquals("myFile.sh", unixCommands.get(2));
	}

}
