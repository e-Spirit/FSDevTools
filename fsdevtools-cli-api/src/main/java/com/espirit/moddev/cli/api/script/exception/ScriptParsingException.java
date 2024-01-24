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

/**
 * Exception class representing parsing errors that occur during script processing.
 * Extends the base {@link ScriptException} class and inherits its functionality.
 */
public class ScriptParsingException extends ScriptException {

	/**
	 * Constructs a new ScriptParsingException with the specified cause, setting line and column numbers to -1.
	 *
	 * @param cause The cause of the parsing error.
	 */
	public ScriptParsingException(@NotNull final Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new ScriptParsingException with the specified cause, line, and column numbers.
	 *
	 * @param cause  The cause of the parsing error.
	 * @param line   The line number where the parsing error occurred, or -1 if unknown.
	 * @param column The column number where the parsing error occurred, or -1 if unknown.
	 */
	public ScriptParsingException(@NotNull final Throwable cause, final int line, final int column) {
		super(cause, line, column);
	}

}
