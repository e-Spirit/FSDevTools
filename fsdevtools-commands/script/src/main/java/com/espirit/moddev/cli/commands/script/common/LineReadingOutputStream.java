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

package com.espirit.moddev.cli.commands.script.common;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Custom implementation of {@link OutputStream} that reads input bytes and processes them line by line.
 * It buffers incoming bytes, detects line breaks, and invokes a consumer for each complete line.
 * This class is designed to handle various line break conventions (CR, LF, CRLF).
 */
public class LineReadingOutputStream extends OutputStream {

	private static final byte CR = '\r';
	private static final byte LF = '\n';

	private final Consumer<String> _consumer;
	private final StringBuilder _currentLine = new StringBuilder();
	private boolean _wasCR = false;

	/**
	 * Constructs a {@code LineReadingOutputStream} with the specified consumer.
	 *
	 * @param consumer The consumer to be invoked with each complete line.
	 * @throws NullPointerException If the specified consumer is null.
	 */
	public LineReadingOutputStream(@NotNull final Consumer<String> consumer) {
		this._consumer = Objects.requireNonNull(consumer);
	}

	/**
	 * Writes a byte to the output stream.
	 * This method delegates to the {@link #write(byte[], int, int)} method.
	 *
	 * @param b The byte to be written.
	 * @throws IOException If an I/O error occurs.
	 */
	@Override
	public void write(final int b) throws IOException {
		write(new byte[]{(byte) b});
	}

	/**
	 * Writes a portion of a byte array to the output stream.
	 * This method processes the incoming bytes, detects line breaks, and invokes the consumer for each complete line.
	 *
	 * @param b     The data.
	 * @param start The start offset in the data.
	 * @param len   The number of bytes to write.
	 */
	@Override
	public void write(final byte[] b, int start, final int len) {
		if (b == null) {
			throw new NullPointerException();
		}
		if (len < 0) {
			throw new IndexOutOfBoundsException();
		}
		final int end = start + len;
		if ((start < 0) || (start > b.length) || (end < 0) || (end > b.length)) {
			throw new IndexOutOfBoundsException();
		}

		if (this._wasCR && start < end && b[start] == LF) {
			start++;
			this._wasCR = false;
		} else if (start < end) {
			this._wasCR = b[end - 1] == CR;
		}

		int lineStart = start;
		for (int i = start; i < end; i++) {
			if (b[i] == LF || b[i] == CR) {
				this._currentLine.append(asString(b, lineStart, i));
				consume();
			}
			if (b[i] == LF) {
				lineStart = i + 1;
			} else if (b[i] == CR) {
				if (i < end - 1 && b[i + 1] == LF) {
					lineStart = i + 2;
					i++;
				} else {
					lineStart = i + 1;
				}
			}
		}
		this._currentLine.append(asString(b, lineStart, end));
	}

	/**
	 * Closes this output stream.
	 * If there are any remaining bytes in the current line buffer, it invokes the consumer for the last line.
	 */
	@Override
	public void close() {
		if (this._currentLine.length() != 0) {
			consume();
		}
	}

	/**
	 * Converts a portion of a byte array to a string using UTF-8 encoding.
	 *
	 * @param bytes The byte array.
	 * @param start The start index of the portion to convert.
	 * @param end   The end index of the portion to convert.
	 * @return The string representation of the specified portion of the byte array.
	 * @throws IllegalArgumentException If the start index is greater than the end index.
	 */
	@NotNull
	private static String asString(final byte[] bytes, final int start, final int end) {
		if (start > end) {
			throw new IllegalArgumentException();
		}
		if (start == end) {
			return "";
		}
		return new String(bytes, start, end - start, StandardCharsets.UTF_8);
	}

	/**
	 * Invokes the consumer with the current line content and clears the line buffer.
	 */
	private void consume() {
		this._consumer.accept(this._currentLine.toString());
		this._currentLine.setLength(0);
	}

}