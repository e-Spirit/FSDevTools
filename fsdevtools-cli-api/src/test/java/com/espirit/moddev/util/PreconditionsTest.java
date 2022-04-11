/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2021 e-Spirit GmbH
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

package com.espirit.moddev.util;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

public class PreconditionsTest {

	@Test
	public void checkNotNull_with_object() {
		final int input = 42;
		final int output = Preconditions.checkNotNull(input);
		assertThat(output).isEqualTo(input);
	}

	@Test
	public void checkNotNull_with_null_object_and_null_message() {
		try {
			Preconditions.checkNotNull(null);
			failBecauseExceptionWasNotThrown(NullPointerException.class);
		} catch (final NullPointerException e) {
			assertThat(e.getMessage()).isNull();
		}
	}

	@Test
	public void checkNotNull_with_null_object_and_custom_message() {
		final String errorMessage = "my custom error message";
		try {
			Preconditions.checkNotNull(null, errorMessage);
			failBecauseExceptionWasNotThrown(NullPointerException.class);
		} catch (final NullPointerException e) {
			assertThat(e.getMessage()).isEqualTo(errorMessage);
		}
	}

	@Test
	public void checkNotEmpty_for_string() {
		final String input = "my custom error message";
		final String output = Preconditions.checkNotEmpty(input);
		assertThat(output).isEqualTo(input);
	}

	@Test
	public void checkNotEmpty_for_null_string_and_null_message() {
		try {
			Preconditions.checkNotEmpty((String) null);
			failBecauseExceptionWasNotThrown(NullPointerException.class);
		} catch (final NullPointerException e) {
			assertThat(e.getMessage()).isNull();
		}
	}

	@Test
	public void checkNotEmpty_for_null_string_and_custom_message() {
		final String errorMessage = "my custom error message";
		try {
			Preconditions.checkNotEmpty((String) null, errorMessage);
			failBecauseExceptionWasNotThrown(NullPointerException.class);
		} catch (final NullPointerException e) {
			assertThat(e.getMessage()).isEqualTo(errorMessage);
		}
	}

	@Test
	public void checkNotEmpty_for_empty_string_and_null_message() {
		try {
			Preconditions.checkNotEmpty("");
			failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage()).isNull();
		}
	}

	@Test
	public void checkNotEmpty_for_empty_string_and_custom_message() {
		final String errorMessage = "my custom error message";
		try {
			Preconditions.checkNotEmpty("", errorMessage);
			failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage()).isEqualTo(errorMessage);
		}
	}

	@Test
	public void checkNotEmpty_for_trimmed_empty_string_and_null_message() {
		try {
			Preconditions.checkNotEmpty(" ");
			failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage()).isNull();
		}
	}

	@Test
	public void checkNotEmpty_for_trimmed_empty_string_and_custom_message() {
		final String errorMessage = "my custom error message";
		try {
			Preconditions.checkNotEmpty(" ", errorMessage);
			failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage()).isEqualTo(errorMessage);
		}
	}

	@Test
	public void checkNotEmpty_for_collection() {
		final Collection<Integer> input = Arrays.asList(2, 4);
		final Collection<Integer> output = Preconditions.checkNotEmpty(input);
		assertThat(output).isEqualTo(input);
	}

	@Test
	public void checkNotEmpty_for_null_collection_and_null_message() {
		try {
			Preconditions.checkNotEmpty((Collection<?>) null);
			failBecauseExceptionWasNotThrown(NullPointerException.class);
		} catch (final NullPointerException e) {
			assertThat(e.getMessage()).isNull();
		}
	}

	@Test
	public void checkNotEmpty_for_null_collection_and_custom_message() {
		final String errorMessage = "my custom error message";
		try {
			Preconditions.checkNotEmpty((Collection<?>) null, errorMessage);
			failBecauseExceptionWasNotThrown(NullPointerException.class);
		} catch (final NullPointerException e) {
			assertThat(e.getMessage()).isEqualTo(errorMessage);
		}
	}

	@Test
	public void checkNotEmpty_for_empty_collection_and_null_message() {
		try {
			Preconditions.checkNotEmpty(Collections.emptyList());
			failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage()).isNull();
		}
	}

	@Test
	public void checkNotEmpty_for_empty_collection_and_custom_message() {
		final String errorMessage = "my custom error message";
		try {
			Preconditions.checkNotEmpty(Collections.emptyList(), errorMessage);
			failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage()).isEqualTo(errorMessage);
		}
	}

	@Test
	public void getNonNullList() {
		assertThat(Preconditions.getNonNullList(null)).isEqualTo(Collections.emptyList());
		final ArrayList<String> emptyList = new ArrayList<>();
		assertThat(Preconditions.getNonNullList(emptyList)).isEqualTo(emptyList);
		final ArrayList<String> nonEmptyList = Lists.newArrayList("abc", "def");
		assertThat(Preconditions.getNonNullList(nonEmptyList)).isEqualTo(nonEmptyList);
	}

}
