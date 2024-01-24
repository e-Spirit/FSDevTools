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

package com.espirit.moddev.shared.webapp;

import org.junit.jupiter.api.Test;

import static com.espirit.moddev.shared.webapp.WebAppIdentifier.forGlobalWebApp;
import static com.espirit.moddev.shared.webapp.WebAppIdentifier.forScope;
import static de.espirit.firstspirit.module.WebEnvironment.WebScope.GLOBAL;
import static de.espirit.firstspirit.module.WebEnvironment.WebScope.PREVIEW;
import static de.espirit.firstspirit.module.WebEnvironment.WebScope.STAGING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WebAppIdentifierTest {

	@Test
	public void testEqualIdentifiers() {
		assertEquals(forGlobalWebApp("fs5root"), forGlobalWebApp("fs5root"));

		assertNotEquals(forGlobalWebApp("xyz"), forGlobalWebApp("abc"));

		assertEquals(forScope(PREVIEW), forScope(PREVIEW));

		assertNotEquals(forScope(PREVIEW), forScope(STAGING));
	}

	@Test
	public void testFactoryForSimpleWebAppIdentifier() {
		WebAppIdentifier identifier = forScope(PREVIEW);
		assertNotNull(identifier);
		assertEquals("preview", identifier.toString());
	}

	@Test
	public void testFactoryWebAppNameForGlobalWebAppIdentifier() {
		WebAppIdentifier parsed = forGlobalWebApp("fs5root");
		assertTrue(parsed instanceof GlobalWebAppIdentifier);
		assertEquals(GLOBAL, parsed.getScope());
		assertEquals("fs5root", ((GlobalWebAppIdentifier) parsed).getGlobalWebAppId());
	}

	@Test
	public void testFactoryForPreviewWebAppIdentifier() {
		WebAppIdentifier identifier = forScope(PREVIEW);
		assertNotNull(identifier);
		assertEquals("preview", identifier.toString());
	}
}
