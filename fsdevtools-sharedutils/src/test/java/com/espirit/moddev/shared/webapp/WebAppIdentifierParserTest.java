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

package com.espirit.moddev.shared.webapp;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.espirit.moddev.shared.webapp.WebAppIdentifier.forGlobalWebApp;
import static com.espirit.moddev.shared.webapp.WebAppIdentifier.forScope;
import static de.espirit.firstspirit.module.WebEnvironment.WebScope.PREVIEW;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class WebAppIdentifierParserTest {

	private WebAppIdentifierParser parser;

	@BeforeEach
	public void setUp() {
		parser = new WebAppIdentifierParser();
	}

	@Test
	public void testSimpleScopeParsing() {
		assertEquals(forScope(PREVIEW), parser.parseSingle("preview"));
	}

	@Test
	public void testExceptionOnNonExistingWebAppScope() {
		assertThrows(IllegalArgumentException.class, () -> parser.parseSingle("previewXXX"));
	}

	@Test
	public void testGlobalWebAppNameParsing() {
		assertEquals(forGlobalWebApp("fs5root"), parser.parseSingle("global(fs5root)"));
	}

	@Test
	public void testExceptionOnNullWebAppScope() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			parser.parseSingle(null);
		});
	}

	@Test
	public void testExtractWebScopes() throws Exception {
		String testWebAppScopes = "preview,staging";
		List<WebAppIdentifier> extractedScopes = parser.extractWebScopes(testWebAppScopes);
		assertThat(extractedScopes, contains(WebAppIdentifier.PREVIEW, WebAppIdentifier.STAGING));
	}
}
