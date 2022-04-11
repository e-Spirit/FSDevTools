/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2021 e-Spirit GmbH
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

package com.espirit.moddev.cli.commands.project.activateWebServerCommand;

import com.espirit.moddev.shared.webapp.WebAppIdentifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProjectWebServerActivationParameterBuilderTest {
	private ProjectWebServerActivationParameterBuilder testling;

	@BeforeEach
	public void setUp() {
		List<WebAppIdentifier> validScopes = new ArrayList<>();
		validScopes.add(WebAppIdentifier.LIVE);
		validScopes.add(WebAppIdentifier.FS5_ROOT);
		testling = ProjectWebServerActivationParameter.builder();
		testling.atProjectName("validProjectName").forScopes(validScopes).withServerName("ValidServerName").withForceActivation(true);
	}

	@Test
	public void testBuild() {
		final ProjectWebServerActivationParameter parameter = testling.withForceActivation(true).build();
		assertNotNull(parameter);
		assertTrue(parameter.isForceActivation());
		assertEquals("validProjectName", parameter.getProjectName());
		assertEquals("ValidServerName", parameter.getServerName());
		assertNotNull(parameter.getScopes());
		assertEquals(2, parameter.getScopes().size());
		assertSame(parameter.getScopes().get(0).getScope(), WebAppIdentifier.LIVE.getScope());
	}

	@Test
	public void testEmptyAtProjectName() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			testling.atProjectName("").build();
		});
	}

	@Test
	public void testNullForScopes() {
		List<WebAppIdentifier> invalidScopes = null;
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			testling.forScopes(invalidScopes).build();
		});
	}

	@Test
	public void withInvalidServerName() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			testling.atProjectName("").build();
		});
	}

	@Test
	public void withEmptyServerName() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			testling.atProjectName(null).build();
		});
	}

	@Test
	public void testWithoutForceActivation() {
		assertFalse(testling.withForceActivation(false).build().isForceActivation());
	}
}
