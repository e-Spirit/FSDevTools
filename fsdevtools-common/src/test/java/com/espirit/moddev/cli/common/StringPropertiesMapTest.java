/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2026 Crownpeak Technology GmbH
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

package com.espirit.moddev.cli.common;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class StringPropertiesMapTest {

	@NotNull
	private static Stream<String> parameterSet_testValidStringConstructor() {
		return Stream.of("abc=123, def=456", "abc=123,def=456", "abc=123,def=456", "abc=123,def=456",
				"abc=123" + System.lineSeparator() + "def=456", "abc=123 , def=456", " abc = 123 , def = 456 ", "abc:123,def:456",
				" abc : 123 , def : 456 ");
	}

	@ParameterizedTest
	@MethodSource("parameterSet_testValidStringConstructor")
	public void testValidStringConstructor(@NotNull final String property) {
		StringPropertiesMap constructed = new StringPropertiesMap(property);

		final Collection<String> values = constructed.values();
		assertThat(values).as("Wrong count of parsed entries: " + property).hasSize(2);
		assertThat(values).as("Wrong parsed values " + values + " for " + property).containsExactlyInAnyOrder("123", "456");

		final Set<String> keys = constructed.keySet();
		assertThat(keys).as("Wrong parsed keys:  " + keys + " for " + property).containsExactlyInAnyOrder("abc", "def");
	}

	@ParameterizedTest
	@ValueSource(strings = {"abc=123;def=456", "abc=123 def=456"})
	public void testInvalidStringConstructor(@NotNull final String property) {
		StringPropertiesMap constructed = new StringPropertiesMap(property);
		assertThat(constructed.values()).as("Wrong count of parsed entries!").hasSize(1);
	}

	@Test
	public void testParameterlessConstructor() {
		StringPropertiesMap map = new StringPropertiesMap();
		assertThat(map.values()).as("Parameterless constructor should create empty map!").isEmpty();
	}

	@ParameterizedTest
	@ValueSource(strings = {"", " ", "\t"})
	public void testConstructorWithIllegalArgument(@NotNull final String source) {
		StringPropertiesMap constructed = new StringPropertiesMap(source);
		assertThat(constructed.values()).as("Illegal constructor parameter should create empty map!").isEmpty();
	}

}
