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

package com.espirit.moddev.shared.exception;

import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * {@link Exception} class that works as a container for a list of {@link Exception exceptions}.
 *
 * @see Exception
 * @see RuntimeException
 */
@SuppressWarnings("Duplicates")
public class MultiException extends RuntimeException {

	private final List<Exception> _exceptions;

	public MultiException(@NotNull final String message, @NotNull final Collection<Exception> exceptions) {
		super(message);
		_exceptions = new ArrayList<>();
		addExceptions(exceptions);
	}

	private void addExceptions(final Collection<Exception> exceptions) {
		for (final Exception exception : exceptions) {
			if (exception instanceof MultiException) {
				addExceptions(((MultiException) exception)._exceptions);
			} else {
				_exceptions.add(exception);
			}
		}
	}

	public List<Exception> getExceptions() {
		return _exceptions;
	}

	@Override
	public void printStackTrace(final PrintStream writer) {
		writer.println(getMessage());
		for (int index = 0; index < _exceptions.size(); index++) {
            final Exception exception = _exceptions.get(index);
            final String numberSuffix = "#" + getNumberWithLeadingChar(index + 1, _exceptions.size(), '0');
            writer.print(numberSuffix + ": " + exception.getMessage());
            writer.print("\n\n-- STACKTRACE " + numberSuffix + " --\n");
            exception.printStackTrace(writer);
            writer.print("-- END OF STACKTRACE --\n\n");
		}
		super.printStackTrace(writer);
	}

	@Override
	public void printStackTrace(final PrintWriter writer) {
		writer.println(getMessage());
		for (int index = 0; index < _exceptions.size(); index++) {
			final Exception exception = _exceptions.get(index);
			final String numberSuffix = "#" + getNumberWithLeadingChar(index + 1, _exceptions.size(), '0');
			writer.print(numberSuffix + ": " + exception.getMessage());
			writer.print("\n\n-- STACKTRACE " + numberSuffix + " --\n");
			exception.printStackTrace(writer);
			writer.print("-- END OF STACKTRACE --\n\n");
		}
	}

	private String getNumberWithLeadingChar(final int number, final int max, final char leadingChar) {
		final int digits = String.valueOf(number).length();
		final int maxLength = String.valueOf(max).length();
		final int leadingZeros = maxLength - digits;
		final StringBuilder sb = new StringBuilder();
		if (leadingZeros > 0) {
			for (int i = 0; i < leadingZeros; i++) {
				sb.append(leadingChar);
			}
		}
		sb.append(number);
		return sb.toString();
	}

}
