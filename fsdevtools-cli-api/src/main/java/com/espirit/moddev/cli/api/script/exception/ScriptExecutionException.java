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

package com.espirit.moddev.cli.api.script.exception;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Exception class representing errors that occur during script execution.
 * Extends the base {@link ScriptException} class and inherits its functionality.
 */
public class ScriptExecutionException extends ScriptException {

	/**
	 * Constructs a new ScriptExecutionException with the specified cause, setting line and column numbers to -1.
	 *
	 * @param cause The cause of the script execution error.
	 */
	public ScriptExecutionException(@NotNull final Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new ScriptExecutionException with the specified message, cause, line, and column numbers.
	 *
	 * @param message The detail message.
	 * @param cause   The cause of the script execution error.
	 * @param line    The line number where the error occurred, or -1 if unknown.
	 * @param column  The column number where the error occurred, or -1 if unknown.
	 */
	public ScriptExecutionException(@NotNull final String message, @Nullable final Throwable cause, final int line, final int column) {
		super(message, cause, line, column);
	}

}
