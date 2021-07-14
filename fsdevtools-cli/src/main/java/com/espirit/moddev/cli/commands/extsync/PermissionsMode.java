/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2021 e-Spirit AG
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

package com.espirit.moddev.cli.commands.extsync;

import de.espirit.firstspirit.transport.PermissionMode;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public enum PermissionsMode {

	NONE(PermissionMode.NO_PERMISSIONS),

	ALL(PermissionMode.ALL_PERMISSIONS),

	STORE_ELEMENT(PermissionMode.STORE_ELEMENT_PERMISSIONS),

	WORKFLOW(PermissionMode.WORKFLOW_PERMISSIONS);

	private final PermissionMode _permissionMode;

	PermissionsMode(@NotNull final String name) {
		this(toPermissionsMode(name).getFirstSpiritPermissionMode());
	}

	PermissionsMode(@NotNull final PermissionMode permissionMode) {
		_permissionMode = permissionMode;
	}

	@NotNull
	public PermissionMode getFirstSpiritPermissionMode() {
		return _permissionMode;
	}

	@NotNull
	static PermissionsMode toPermissionsMode(@NotNull final String input) {
		for (final PermissionsMode permissionMode : PermissionsMode.values()) {
			final String name = permissionMode.name();
			// case insensitive lookup
			if (isInsensitiveEqual(name, input)) {
				return permissionMode;
			}
			// fallback to FirstSpirit PermissionMode
			final String fsModeName = permissionMode.getFirstSpiritPermissionMode().name();
			if (isInsensitiveEqual(fsModeName, input)) {
				return permissionMode;
			}
			// fallback to FirstSpirit PermissionMode - but without the _PERMISSIONS-Postfix
			final String fsModeShortName = fsModeName.replaceAll("_PERMISSIONS", "");
			if (isInsensitiveEqual(fsModeShortName, input)) {
				return permissionMode;
			}
		}
		throw new IllegalArgumentException(String.format("Permission mode '%s' is invalid, possible values are %s.", input, Arrays.toString(PermissionsMode.values())));
	}

	private static boolean isInsensitiveEqual(@NotNull final String name, @NotNull final String compareTo) {
		// case insensitive lookup ignoring '_' & '-'
		return compareTo.equalsIgnoreCase(name) || compareTo.replaceAll("[_-]", "").equalsIgnoreCase(name.replaceAll("[_-]", ""));
	}

}
