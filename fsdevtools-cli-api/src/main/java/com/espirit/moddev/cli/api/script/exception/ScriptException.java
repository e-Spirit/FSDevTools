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
 * Base class for exceptions related to script processing, providing information about
 * the line and column numbers where the error occurred.
 */
public abstract class ScriptException extends Exception {

	private final int _lineNumber;
	private final int _columnNumber;

	/**
	 * Constructs a new ScriptException with the specified cause, setting line and column numbers to -1.
	 *
	 * @param cause The cause of the exception.
	 */
	public ScriptException(@NotNull final Throwable cause) {
		this(cause, -1, -1);
	}

	/**
	 * Constructs a new ScriptException with the specified cause, line, and column numbers.
	 *
	 * @param cause  The cause of the exception.
	 * @param line   The line number where the error occurred, or -1 if unknown.
	 * @param column The column number where the error occurred, or -1 if unknown.
	 */
	public ScriptException(@NotNull final Throwable cause, final int line, final int column) {
		super(cause);
		_lineNumber = line;
		_columnNumber = column;
	}

	/**
	 * Constructs a new ScriptException with the specified message, cause, line, and column numbers.
	 *
	 * @param message The detail message.
	 * @param cause   The cause of the exception.
	 * @param line    The line number where the error occurred, or -1 if unknown.
	 * @param column  The column number where the error occurred, or -1 if unknown.
	 */
	public ScriptException(@NotNull final String message, @Nullable final Throwable cause, final int line, final int column) {
		super(message, cause);
		_lineNumber = line;
		_columnNumber = column;
	}

	/**
	 * Get the line number on which an error occurred.
	 *
	 * @return The line number or -1 if a line number is unavailable.
	 */
	public final int getLineNumber() {
		return _lineNumber;
	}

	/**
	 * Get the column number on which an error occurred.
	 *
	 * @return The column number or -1 if a column number is unavailable.
	 */
	public final int getColumnNumber() {
		return _columnNumber;
	}

	/**
	 * Returns a message containing the String passed to a constructor as well as line and column numbers
	 * if any of these are known.
	 *
	 * @return The error message.
	 */
	@Override
	public final String getMessage() {
		final String msg = super.getMessage();
		if (_lineNumber != -1) {
			if (_columnNumber != -1) {
				return msg + " at line " + _lineNumber + ", column " + _columnNumber;
			}
			return msg + " at line " + _lineNumber;
		}
		return msg;
	}
}
