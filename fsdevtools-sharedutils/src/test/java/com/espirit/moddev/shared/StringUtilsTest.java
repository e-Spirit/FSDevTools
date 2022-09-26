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

package com.espirit.moddev.shared;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StringUtilsTest {

	@Test
	public void toPluralRespectingString_zero() {
		assertThat(StringUtils.toPluralRespectingString(0, "app")).isEqualTo("apps");
	}

	@Test
	public void toPluralRespectingString_one() {
		assertThat(StringUtils.toPluralRespectingString(1, "app")).isEqualTo("app");
	}

	@Test
	public void toPluralRespectingString_greater_one() {
		assertThat(StringUtils.toPluralRespectingString(2, "app")).isEqualTo("apps");
	}

	@Test
	public void toPluralRespectingString_minus_one() {
		assertThat(StringUtils.toPluralRespectingString(-1, "app")).isEqualTo("app");
	}

	@Test
	public void toPluralRespectingString_less_minus_one() {
		assertThat(StringUtils.toPluralRespectingString(-2, "app")).isEqualTo("apps");
	}

	@Test
	public void isNullOrEmpty() {
		assertThat(StringUtils.isNullOrEmpty(null)).isTrue();
		assertThat(StringUtils.isNullOrEmpty("")).isTrue();
		assertThat(StringUtils.isNullOrEmpty(" ")).isTrue();
		assertThat(StringUtils.isNullOrEmpty("Test")).isFalse();
	}

	@Test
	public void isEmpty() {
		assertThat(StringUtils.isNullOrEmpty("")).isTrue();
		assertThat(StringUtils.isNullOrEmpty(" ")).isTrue();
		assertThat(StringUtils.isNullOrEmpty("Test")).isFalse();
	}

	@Test
	public void whenExceptionThrown_thenExpectationSatisfied() {
		Assertions.assertThrows(NullPointerException.class, () -> StringUtils.isEmpty(null));
	}

}
