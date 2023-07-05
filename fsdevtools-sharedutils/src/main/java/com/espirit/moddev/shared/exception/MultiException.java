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

import java.io.PrintWriter;
import java.io.StringWriter;
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

	private final List<Throwable> _throwables;
	private String _message;

	public MultiException(@NotNull final String message, @NotNull final Collection<Throwable> throwables) {
		super(message);
		_throwables = new ArrayList<>();
		addThrowables(throwables);
	}

	@Override
	public String getMessage() {
		if (_message == null) {
			final StringWriter stringWriter = new StringWriter();
			try (final PrintWriter writer = new PrintWriter(stringWriter)) {
				writer.print(super.getMessage());
				writer.print("\n\n");
				// summary
				writer.print("-- SUMMARY --");
				writer.print("\n");
				for (int index = 0; index < _throwables.size(); index++) {
					final Throwable throwable = _throwables.get(index);
					final String numberSuffix = "#" + getNumberWithLeadingChar(index + 1, _throwables.size(), '0');
					if (throwable.getMessage() != null) {
						writer.print(numberSuffix + ": " + throwable.getMessage());
					} else {
						writer.print(numberSuffix + ": <Exception message is null - see stacktrace for details>");
					}
					writer.print("\n");
				}
				writer.print("-- END OF SUMMARY --\n");

				// stracktraces
				for (int index = 0; index < _throwables.size(); index++) {
					final Throwable throwable = _throwables.get(index);
					final String numberSuffix = "#" + getNumberWithLeadingChar(index + 1, _throwables.size(), '0');
					writer.print("\n\n-- " + numberSuffix + " : STACKTRACE --\n");
					throwable.printStackTrace(writer);
					writer.print("-- " + numberSuffix + " : END OF STACKTRACE --\n");
				}
			}
			_message = stringWriter.toString();
		}
		return _message;
	}

	private void addThrowables(@NotNull final Collection<Throwable> throwables) {
		for (final Throwable throwable : throwables) {
			if (throwable instanceof MultiException) {
				addThrowables(((MultiException) throwable)._throwables);
			} else {
				_throwables.add(throwable);
			}
		}
	}

	@NotNull
	private static String getNumberWithLeadingChar(final int number, final int max, final char leadingChar) {
		final int digits = String.valueOf(number).length();
		final int maxLength = String.valueOf(max).length();
		final int leadingZeros = maxLength - digits;
		final StringBuilder sb = new StringBuilder();
		if (leadingZeros > 0) {
			sb.append(String.valueOf(leadingChar).repeat(leadingZeros));
		}
		sb.append(number);
		return sb.toString();
	}

}
