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

import com.espirit.moddev.cli.api.parsing.identifier.*;
import com.google.common.collect.Lists;
import de.espirit.firstspirit.access.store.IDProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

public class RegistryBasedParserTest {

	private RegistryBasedParser testling;

	@BeforeEach
	public void setUp() {
		testling = new RegistryBasedParser();
	}

	@Test
	public void registerNullParser() {
		assertThrows(IllegalArgumentException.class, () -> testling.registerParser(null));
	}

	@Test
	public void registerParser() {
		Parser parser = new Parser() {
			@Override
			public List parse(List input) {
				return null;
			}

			@Override
			public boolean appliesTo(String input) {
				return false;
			}
		};
		boolean registeredParser = testling.registerParser(parser);
		assertTrue(registeredParser, "Parser wasn't registered successfully");
		assertTrue(testling.unregisterParser(parser), "Parser wasn't unregistered successfully");
	}

	@Test
	public void appliesTo() {
		boolean registeredParser = testling.registerParser(new Parser() {
			@Override
			public List parse(List input) {
				return null;
			}

			@Override
			public boolean appliesTo(String input) {
				return input.startsWith("xxx");
			}
		});
		assertTrue(registeredParser, "Parser wasn't registered successfully");
		assertTrue(testling.appliesTo("xxxaaa"), "Parser should delegate appliesTo to registered parsers");
		assertFalse(testling.appliesTo("aaabbb"), "Parser should not apply to other input strings");
	}

	@Test
	public void testParseMultipleElements() throws Exception {
		testling.registerParser(new RootNodeIdentifierParser());
		testling.registerParser(new UidIdentifierParser());
		testling.registerParser(new EntitiesIdentifierParser());
		testling.registerParser(new SchemaIdentifierParser());
		List<String> testIdentifiers = Arrays.asList("root:templatestore", "mediafolder:layout", "entities:news", "schema:products");
		final List<Identifier> list = testling.parse(testIdentifiers);
		assertEquals(testIdentifiers.size(), list.size(), "List should contain " + testIdentifiers.size() + " elements.");
		assertThat(list.contains(new RootNodeIdentifier(IDProvider.UidType.TEMPLATESTORE)), equalTo(true));
		assertThat(list.contains(new UidIdentifier(UidMapping.MEDIAFOLDER, "layout")), equalTo(true));
		assertThat(list.contains(new EntitiesIdentifier("news")), equalTo(true));
		assertThat(list.contains(new SchemaIdentifier("products", Collections.emptyMap())), equalTo(true));
	}

	@Test
	public void testDEVEX69() {
		testling.registerParser(new Parser<Identifier>() {
			@Override
			public List<Identifier> parse(List<String> input) {
				return new ArrayList<Identifier>() {{
					add((storeAgent, useReleaseState, exportOperation) -> {
					});
				}};
			}

			@Override
			public boolean appliesTo(String input) {
				return input.startsWith("path:");
			}
		});
		testling.registerParser(new Parser<Identifier>() {
			@Override
			public List<Identifier> parse(List<String> input) {
				return new ArrayList<Identifier>() {{
					add((storeAgent, useReleaseState, exportOperation) -> {
					});
				}};
			}

			@Override
			public boolean appliesTo(String input) {
				return input.startsWith("entities:");
			}
		});
		testling.registerParser(new Parser<Identifier>() {
			@Override
			public List<Identifier> parse(List<String> input) {
				return new ArrayList<Identifier>() {{
					add((storeAgent, useReleaseState, exportOperation) -> {
					});
				}};
			}

			@Override
			public boolean appliesTo(String input) {
				return input.startsWith("projectprops:");
			}
		});
		testling.registerParser(new UidIdentifierParser());

		assertTrue(testling.appliesTo("pagetemplate:homepage"));
		assertTrue(testling.appliesTo("path:/TemplateStore/Pagetemplates/FOLDER_NAME/UID"));
		assertTrue(testling.appliesTo("entities:Produkte"));
		assertTrue(testling.appliesTo("projectprops:RESOLUTION"));

		List<Identifier> result = testling.parse(Lists.newArrayList("path:/TemplateStore/Pagetemplates/<FOLDER_NAME>/UID", "entities:Produkte", "projectprops:RESOLUTION", "pagetemplate:homepage", "projectprops:COMMON"));
		assertEquals(4, result.size());
	}
}
