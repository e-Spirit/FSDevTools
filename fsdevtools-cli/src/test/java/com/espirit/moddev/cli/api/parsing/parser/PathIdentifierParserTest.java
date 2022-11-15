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

import com.espirit.moddev.cli.api.parsing.identifier.PathIdentifier;
import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


public class PathIdentifierParserTest {

	private PathIdentifierParser testling;

	@NotNull
	private static Stream<List<String>> parameterSet() {
		return Stream.of(Collections.singletonList("path:/TemplateStore/PageTemplates/hurz"),
				Collections.singletonList("PATH:/TemplateStore/PageTemplates/hurz"),
				Collections.singletonList("path :/TemplateStore/PageTemplates/hurz"),
				Collections.singletonList("path : /TemplateStore/PageTemplates/hurz"));
	}

	@BeforeEach
	public void setUp() {
		testling = new PathIdentifierParser();
	}

	@ParameterizedTest
	@MethodSource("parameterSet")
	public void testParse(List<String> paths) {
		final List<PathIdentifier> list = testling.parse(paths);

		assertThat(list).hasSize(1);
		assertThat(list.get(0).getPath()).isEqualTo("/TemplateStore/PageTemplates/hurz");
	}

	@ParameterizedTest
	@MethodSource("parameterSet")
	public void testAppliesTo(List<String> paths) {
		for (String current : paths) {
			boolean appliesTo = testling.appliesTo(current);
			assertTrue(appliesTo, "Parser should apply to string " + current);
		}
	}

	@Test
	public void testDontApplyTo() {
		boolean appliesTo = testling.appliesTo("pathxyz :bla");
		assertFalse(appliesTo, "Parser should apply to string pathxyz :bla");
	}


	@Test
	public void testParseWithNonExistentPrefix() {
		assertThrows(IllegalArgumentException.class, () -> testling.parse(Collections.singletonList("xxxxx:myPath")));
	}


	@Test
	public void testParseMultiple() {
		final List<PathIdentifier> parse = testling.parse(Arrays.asList("path:/TemplateStore/PageTemplates/first", "path:/PageStore/folder"));
		Assertions.assertThat(parse).hasSize(2);
	}


	@Test
	public void testEmptyPath() {
		assertThrows(IllegalArgumentException.class, () -> testling.parse(Collections.singletonList("path:")));
	}


	@Test
	public void testEmptyPathWhitespaces() {
		assertThrows(IllegalArgumentException.class, () -> testling.parse(Collections.singletonList("path: ")));
	}


	@Test
	public void testNoLeadingSlash() {
		assertThrows(IllegalArgumentException.class, () -> testling.parse(Collections.singletonList("path:TemplateStore/PageTemplates/first")));
	}
}
