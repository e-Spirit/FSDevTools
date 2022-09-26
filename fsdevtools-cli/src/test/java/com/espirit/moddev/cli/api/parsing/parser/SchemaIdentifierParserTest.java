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

import com.espirit.moddev.cli.api.parsing.identifier.SchemaIdentifier;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.espirit.moddev.cli.api.parsing.parser.SchemaIdentifierParser.CUSTOM_PREFIX_SCHEMA_OPTION;
import static org.junit.jupiter.api.Assertions.*;

public class SchemaIdentifierParserTest {

	@NotNull
	private static Stream<List<String>> parameterSet() {
		return Stream.of(List.of(CUSTOM_PREFIX_SCHEMA_OPTION + ":Products[exportallentities]"),
				List.of("schema: SimpleSchema"), // just using CUSTOM_PREFIX_SCHEMA_OPTION to keep this list more readable
				List.of("schema: Products[ExportAllEntities]"),
				List.of("schema: Products[ExportAllEntities=true]"),
				List.of("schema: names[ExportAllEntities=false]"),
				List.of("schema :names[ExportGidMapping]"),
				List.of("schema :names[ExportGidMapping=false]"),
				List.of("schema : names[addEntity=entityId]"));
	}

	private SchemaIdentifierParser testling;

	@BeforeEach
	public void setUp() {
		testling = new SchemaIdentifierParser();
	}

	@AfterEach
	public void resetValidOptions() {
		SchemaIdentifier.resetValidSchemaOptions();
	}

	@ParameterizedTest
	@MethodSource("parameterSet")
	public void testAppliesTo(List<String> uids) {
		for (String current : uids) {
			boolean appliesTo = testling.appliesTo(current);
			assertTrue(appliesTo);
		}
	}

	@Test
	public void parse() {
		List<SchemaIdentifier> result = testling.parse(Collections.singletonList("entities:xyz"));
		assertEquals(1, result.size());
		assertEquals(new SchemaIdentifier("xyz", Collections.emptyMap()), result.get(0));
	}

	@Test
	public void parseWithOptions() {
		SchemaIdentifier.VALID_SCHEMA_OPTIONS.addAll(List.of("option1", "option2", "option3"));
		List<SchemaIdentifier> result = testling.parse(Collections.singletonList("entities:xyz[option1=switch1|option2=switch2|option3=switch3]"));
		assertEquals(1, result.size());
		assertEquals(getExpectedSchemaIdentifier(), result.get(0));
	}

	@Test
	public void parseSchemaOptions() {
		SchemaIdentifier.VALID_SCHEMA_OPTIONS.addAll(List.of("option1", "option2", "option3WithoutEquals", "option4WithoutValue", "option5", "option6"));
		final String entitiesUidWithOptions = "[option1=switch1|option2=switch2|option3WithoutEquals|option4WithoutValue=|||option5=afterTriplePipe|option6=normal_again]";
		Map<String, String> schemaOptionsMap = SchemaIdentifierParser.parseSchemaOptions(entitiesUidWithOptions);
		assertEquals("switch1", schemaOptionsMap.get("option1"));
		assertEquals("switch2", schemaOptionsMap.get("option2"));
		assertNull(schemaOptionsMap.get("option3WithoutEquals"));
		assertNull(schemaOptionsMap.get("option4WithoutValue"));
		assertEquals("afterTriplePipe", schemaOptionsMap.get("option5"));
		assertEquals("normal_again", schemaOptionsMap.get("option6"));
	}

	@Test
	public void parseSchemaOptions_exportGidMapping() {
		{
			final Map<String, String> options = SchemaIdentifierParser.parseSchemaOptions("[exportgidMapping=true]");
			assertEquals(1, options.size());
			assertEquals("true", options.values().iterator().next());
		}
		{
			final Map<String, String> options = SchemaIdentifierParser.parseSchemaOptions("[exportGidMapping=false]");
			assertEquals(1, options.size());
			assertEquals("false", options.values().iterator().next());
		}
		{
			final Map<String, String> options = SchemaIdentifierParser.parseSchemaOptions("[EXPORTGIDMAPPING=unknown]");
			assertEquals(1, options.size());
			assertEquals("unknown", options.values().iterator().next());
		}
	}

	@Test
	public void parseSchemaOptions_invalidOption() {
		assertThrows(IllegalArgumentException.class, () -> SchemaIdentifierParser.parseSchemaOptions("[option1=switch1]"));
	}

	@Test
	public void parseSchemaOptions_illegalInputFormat_test1() {
		assertThrows(IllegalArgumentException.class, () -> SchemaIdentifierParser.parseSchemaOptions("option1=switch1"));
	}

	@Test
	public void parseSchemaOptions_illegalInputFormat_test2() {
		assertThrows(IllegalArgumentException.class, () -> SchemaIdentifierParser.parseSchemaOptions("[option1=switch1"));
	}

	@Test
	public void parseSchemaOptions_illegalInputFormat_test3() {
		assertThrows(IllegalArgumentException.class, () -> SchemaIdentifierParser.parseSchemaOptions("option1=switch1]"));
	}

	@Test
	public void parseSchemaOptions_illegalInputFormat_test4() {
		assertThrows(IllegalArgumentException.class, () -> SchemaIdentifierParser.parseSchemaOptions(" [option1=switch1]"));
	}

	@Test
	public void parseSchemaOptions_illegalInputFormat_test5() {
		assertThrows(IllegalArgumentException.class, () -> SchemaIdentifierParser.parseSchemaOptions("[option1=switch1] "));
	}

	@Test
	public void parseSchemaOptions_illegalInputFormat_test6() {
		assertThrows(IllegalArgumentException.class, () -> SchemaIdentifierParser.parseSchemaOptions(" [option1=switch1] "));
	}

	@Test
	public void getSchemaIdentifier() {
		SchemaIdentifier.VALID_SCHEMA_OPTIONS.addAll(List.of("option1", "option2", "option3"));
		SchemaIdentifier identifierWithOptions = SchemaIdentifierParser.getSchemaIdentifier("xyz[option1=switch1|option2=switch2|option3=switch3]");
		assertEquals(getExpectedSchemaIdentifier(), identifierWithOptions);

		SchemaIdentifier identifierWithoutOptions = SchemaIdentifierParser.getSchemaIdentifier("entitiesUidNoDelimiterBracket");
		assertEquals(new SchemaIdentifier("entitiesUidNoDelimiterBracket", Collections.emptyMap()), identifierWithoutOptions);
	}

	private SchemaIdentifier getExpectedSchemaIdentifier() {
		HashMap<String, String> expectedSchemaOptions = new HashMap<>();
		expectedSchemaOptions.put("option1", "switch1");
		expectedSchemaOptions.put("option2", "switch2");
		expectedSchemaOptions.put("option3", "switch3");
		return new SchemaIdentifier("xyz", expectedSchemaOptions);
	}

	@Test
	public void testDontApplyTo() {
		assertFalse(testling.appliesTo("asdasd"));
	}

	@Test
	public void testDontApplyToStartsWithSchemaIdentifier() {
		assertFalse(testling.appliesTo("schemaaasd:asd"));
	}

}
