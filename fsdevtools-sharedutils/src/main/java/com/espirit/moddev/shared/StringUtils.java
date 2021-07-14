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

package com.espirit.moddev.shared;

import org.jetbrains.annotations.NotNull;

public class StringUtils {

	/**
	 * Useless default constructor
	 */
	public StringUtils() {
	}

	/**
	 * Converts the given input text to a plural respecting string by adding a "s" to it, if needed. The "s" will only be added in the following cases:
	 * <ul>
	 *     <li>amount < -1</li>
	 *     <li>amount == 0</li>
	 *     <li>amount > 1</li>
	 * </ul>
	 * <p>
	 * In all other cases, the unmodified input will be returned.
	 *
	 * @param amount the amount to check against
	 * @param input  the input text
	 * @return the plural respecting input text
	 */
	@NotNull
	public static String toPluralRespectingString(final int amount, @NotNull final String input) {
		if (amount == 1 || amount == -1) {
			return input;
		}
		return input + "s";
	}

	/**
	 * @param string String to check
	 * @return true if the String is null or empty, false otherwise
	 */
	public static boolean isNullOrEmpty(final String string) {
		return string == null || string.trim().isEmpty();
	}

	/**
	 * @param string String to check (may not be null!)
	 * @return true if the String is empty, false otherwise
	 */
	public static boolean isEmpty(final String string) {
		return string.trim().isEmpty();
	}
}
