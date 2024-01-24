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

package com.espirit.moddev.cli.api.script;

import org.jetbrains.annotations.NotNull;

/**
 * Interface representing a generic script engine, providing methods for parsing script sources
 * and obtaining executable representations of scripts.
 */
public interface ScriptEngine {

	/**
	 * Gets the name of this {@link ScriptEngine}.
	 *
	 * @return The name of this {@link ScriptEngine}.
	 */
	@NotNull
	String getName();

	/**
	 * Gets a {@link ScriptExecutable} instance for the given script source.
	 *
	 * @param scriptSource The source code of the script.
	 * @return A {@link ScriptExecutable} instance representing the executable form of the script.
	 */
	@NotNull ScriptExecutable getExecutable(@NotNull String scriptSource);

}
