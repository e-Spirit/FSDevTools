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

package com.espirit.moddev.cli.api.parsing.parser;

import com.espirit.moddev.cli.api.parsing.identifier.ProjectPropertiesIdentifier;
import de.espirit.firstspirit.transport.PropertiesTransportOptions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;


public class ProjectPropertiesParserTest {

	@NotNull
	private static Stream<List<String>> parameterSet() {
		return Stream.of(List.of("Projectproperty:LANGUAGES"),
				List.of("projectproperty:languages"),
				List.of("projectproperty :LANGUAGES"),
				List.of("projectproperty : LANGUAGES"));
	}

	private ProjectPropertiesParser testling;

	@BeforeEach
	public void setUp() {
		testling = new ProjectPropertiesParser();
	}

	@ParameterizedTest
	@MethodSource("parameterSet")
	public void testAppliesTo(@NotNull final List<String> uids) {
		for (String current : uids) {
			boolean appliesTo = testling.appliesTo(current);
			assertTrue(appliesTo, "Parser should apply to string " + current);
		}
	}

	@Test
	public void parse() {
		List<ProjectPropertiesIdentifier> result = testling.parse(List.of("projectproperty:LANGUAGES"));
		assertEquals(1, result.size());
		EnumSet<PropertiesTransportOptions.ProjectPropertyType> enumSet = EnumSet.noneOf(PropertiesTransportOptions.ProjectPropertyType.class);
		enumSet.add(PropertiesTransportOptions.ProjectPropertyType.LANGUAGES);
		assertEquals(new ProjectPropertiesIdentifier(enumSet), result.get(0));
	}

	@Test
	public void parseAll() {
		List<ProjectPropertiesIdentifier> result = testling.parse(List.of("projectproperty:ALL"));
		assertEquals(1, result.size());
		EnumSet<PropertiesTransportOptions.ProjectPropertyType> enumSet = EnumSet.allOf(PropertiesTransportOptions.ProjectPropertyType.class);
		assertEquals(new ProjectPropertiesIdentifier(enumSet), result.get(0));
	}


	@Test
	public void parseAllWhitespace() {
		List<ProjectPropertiesIdentifier> result = testling.parse(List.of("projectproperty: ALL"));
		assertEquals(1, result.size());
		EnumSet<PropertiesTransportOptions.ProjectPropertyType> enumSet = EnumSet.allOf(PropertiesTransportOptions.ProjectPropertyType.class);
		assertEquals(new ProjectPropertiesIdentifier(enumSet), result.get(0));
	}


	@Test
	public void parseAllWithAdditionalProperty() {
		List<ProjectPropertiesIdentifier> result = testling.parse(List.of("projectproperty:LANGUAGES", "projectproperty:ALL"));
		assertEquals(1, result.size());
		EnumSet<PropertiesTransportOptions.ProjectPropertyType> enumSet = EnumSet.allOf(PropertiesTransportOptions.ProjectPropertyType.class);
		assertEquals(new ProjectPropertiesIdentifier(enumSet), result.get(0));
	}


	@Test
	public void parseAllWithAdditionalProperties() {
		List<ProjectPropertiesIdentifier> result = testling.parse(List.of("projectproperty:LANGUAGES", "projectproperty:ALL", "projectproperty:RESOLUTIONS"));
		assertEquals(1, result.size());
		EnumSet<PropertiesTransportOptions.ProjectPropertyType> enumSet = EnumSet.allOf(PropertiesTransportOptions.ProjectPropertyType.class);
		assertEquals(new ProjectPropertiesIdentifier(enumSet), result.get(0));
	}

	@Test
	public void testAppliesTo() {
		assertTrue(testling.appliesTo("projectproperty:languages"));
	}

	@Test
	public void testDontApplyTo() {
		assertFalse(testling.appliesTo("asdasd"));
	}

	@Test
	public void testDontApplyToStartsWithEntitiesIdentifier() {
		assertFalse(testling.appliesTo("entitiesaasd:asd"));
	}
}
