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

package com.espirit.moddev.cli.api.parsing.parser;

import com.espirit.moddev.cli.api.parsing.exceptions.UnregisteredPrefixException;
import com.espirit.moddev.cli.api.parsing.identifier.UidIdentifier;
import com.espirit.moddev.cli.api.parsing.identifier.UidMapping;
import org.hamcrest.Matchers;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author e-Spirit GmbH
 */
public class UidIdentifierParserTest {

	private UidIdentifierParser testling;

	@NotNull
	private static Stream<List<String>> parameterSet() {
		return Stream.of(List.of("page:myuid"),
				List.of("PAGE:myuid"),
				List.of("PAGE :myuid"),
				List.of("PAGE : myuid"));
	}

	@ParameterizedTest
	@MethodSource("parameterSet")
	public void testAppliesTo(@NotNull final List<String> uids) {
		for (String current : uids) {
			boolean appliesTo = testling.appliesTo(current);
			assertTrue(appliesTo, "Parser should apply to string " + current);
		}
	}

	@ParameterizedTest
	@MethodSource("parameterSet")
	public void testParse(@NotNull final List<String> uids) {
		final List<UidIdentifier> list = testling.parse(uids);
		assertThat("Expected PAGE but got: " + uids, list.get(0).getUidMapping(), Matchers.is(UidMapping.PAGE));
		assertThat("Expected 'myuid' but got: " + uids, list.get(0).getUid(), is("myuid"));
	}

	@BeforeEach
	public void setUp() {
		testling = new UidIdentifierParser();
	}

	@Test
	public void testDontApplyTo() {
		boolean appliesTo = testling.appliesTo("pagexyz :bla");
		assertFalse(appliesTo, "Parser should apply to string pagexyz :bla");
	}

	@Test
	public void testParseWithNonExistentPrefix() {
		assertThrows(UnregisteredPrefixException.class, () -> testling.parse(Arrays.asList("xxxxx:myuid")));
	}

	@Test
	public void testParseWithNoStore() {
		assertThrows(IllegalArgumentException.class, () -> testling.parse(Arrays.asList("myuid")));
	}

}
