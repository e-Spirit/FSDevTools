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

import com.espirit.moddev.cli.api.script.exception.ScriptExecutionException;
import com.espirit.moddev.cli.api.script.exception.ScriptParsingException;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Interface representing an executable form of a script, allowing the execution of the script
 * with a specified context.
 */
public interface ScriptExecutable {

	/**
	 * Parses the source with the given context.
	 *
	 * @param context A {@link Map} representing the context variables for the script parsing.
	 * @throws ScriptParsingException If an error occurs during script parsing.
	 */
	void parse(@NotNull Map<String, Object> context) throws ScriptParsingException;

	/**
	 * Executes the script with the given context.
	 *
	 * @param context A {@link Map} representing the context variables for the script execution.
	 * @return The result of the script execution, typically an {@link Object}.
	 * @throws ScriptExecutionException If an error occurs during script execution.
	 */
	Object execute(@NotNull Map<String, Object> context) throws ScriptExecutionException;

}