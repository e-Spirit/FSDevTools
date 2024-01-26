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

package com.espirit.moddev.cli.commands;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PermissionsModeTest {

	@Test
	public void toPermissionsMode_invalidValue() {
		assertThrows(IllegalArgumentException.class, () -> PermissionsMode.toPermissionsMode("4211_invalid-value"));
	}

	@Test
	public void toPermissionsMode_lowerCase() {
		for (final PermissionsMode mode : PermissionsMode.values()) {
			assertEquals(mode, PermissionsMode.toPermissionsMode(mode.name().toLowerCase(Locale.ROOT)));
		}
	}

	@Test
	public void toPermissionsMode_without_underscore() {
		for (final PermissionsMode mode : PermissionsMode.values()) {
			assertEquals(mode, PermissionsMode.toPermissionsMode(mode.name().replaceAll("_", "")));
		}
	}

	@Test
	public void toPermissionsMode_with_minus() {
		for (final PermissionsMode mode : PermissionsMode.values()) {
			assertEquals(mode, PermissionsMode.toPermissionsMode(mode.name().replaceAll("_", "-")));
		}
	}

}