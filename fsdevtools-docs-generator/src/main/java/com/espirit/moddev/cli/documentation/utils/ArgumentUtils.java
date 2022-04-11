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

package com.espirit.moddev.cli.documentation.utils;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class ArgumentUtils {

	public static final String ARG_FILE = "--file";

	private ArgumentUtils() {
		// NOP
	}

	@NotNull
	public static Optional<Path> findPath(@NotNull final String[] arguments) {
		int pathIndex = -1;
		for (int index = 1; index < arguments.length; index++) {
			final String previousArgument = arguments[index - 1];
			if (ARG_FILE.equals(previousArgument)) {
				pathIndex = index;
				break;
			}
		}
		return Optional.ofNullable(pathIndex == -1 ? null : Paths.get(arguments[pathIndex]));
	}

}
