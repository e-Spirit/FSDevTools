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

package com.espirit.moddev.shared.exception;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class MultiExceptionTest {

	@Test
	void getMessage() {
		// setup
		final List<Throwable> exceptions = new ArrayList<>();
		exceptions.add(new IllegalStateException("Just an IllegalStateException"));
		exceptions.add(new RuntimeException("Just an RuntimeException", exceptions.get(0)));
		final MultiException exception = new MultiException("Custom message!", exceptions);

		// test
		final String result = exception.getMessage();

		// verify
		assertThat(result).contains("Custom message!");
		assertThat(result).contains("#1: Just an IllegalStateException");
		assertThat(result).contains("#2: Just an RuntimeException");
		assertThat(result).contains("-- #1 : STACKTRACE --");
		assertThat(result).contains("java.lang.IllegalStateException: Just an IllegalStateException");
		assertThat(result).contains("-- #1 : END OF STACKTRACE --");
		assertThat(result).contains("-- #2 : STACKTRACE --");
		assertThat(result).contains("java.lang.RuntimeException: Just an RuntimeException");
		assertThat(result).contains("Caused by: java.lang.IllegalStateException: Just an IllegalStateException");
		assertThat(result).contains("-- #2 : END OF STACKTRACE --");
	}

}