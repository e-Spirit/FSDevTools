/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2022 Crownpeak Technology GmbH
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

package com.espirit.moddev.shared.exception;

import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * {@link Exception} class that wraps a parent exception and has a special handling for writing the stacktrace.
 *
 * @see Exception
 */
public class WrappedException extends Exception {

	public WrappedException(@NotNull final String message, @NotNull final Exception exception) {
		super(message, exception);
	}

	@Override
	public void printStackTrace(final PrintStream stream) {
		stream.println(getMessage());
		getCause().printStackTrace(stream);
	}

	@Override
	public void printStackTrace(final PrintWriter writer) {
		writer.println(getMessage());
		getCause().printStackTrace(writer);
	}

}
