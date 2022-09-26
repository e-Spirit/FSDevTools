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

package com.espirit.moddev.cli.commands.module.installer;

import com.espirit.moddev.cli.commands.module.installCommand.ModuleInstallationParameters;
import com.espirit.moddev.shared.webapp.WebAppIdentifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModuleInstallationParametersBuilderTest {

	private ModuleInstallationParameters.ModuleInstallationParametersBuilder builder;

	@BeforeEach
	public void setUp() throws Exception {
		builder = ModuleInstallationParameters.builder();
	}

	@Test
	public void getWebScopeFileMap() throws Exception {
		String testWebAppConfigurationFiles = "staging=temp/myConfig.ini,preview=temp/myConfig2.ini";
		assertThat(builder.getWebScopeFileMap(testWebAppConfigurationFiles).get(WebAppIdentifier.STAGING), is(new File("temp/myConfig.ini")));
		assertThat(builder.getWebScopeFileMap(testWebAppConfigurationFiles).get(WebAppIdentifier.PREVIEW), is(new File("temp/myConfig2.ini")));
	}

	@Test
	public void getWebScopeFileMapWithNonExistentWebScope() throws Exception {
		String testWebAppConfigurationFiles = "staging=temp/myConfig.ini, XXX=temp/myConfig2.ini";
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			builder.getWebScopeFileMap(testWebAppConfigurationFiles).get(WebAppIdentifier.STAGING);
		});
	}

	@Test
	public void getOptionalProjectAppConfigurationFile() throws Exception {
		String testProjectAppConfigurationFile = "staging=temp/myConfig.ini";
		assertNotNull(builder.createOptionalProjectAppConfigurationFile(testProjectAppConfigurationFile));
	}

	@Test
	public void getStringFilesMap() throws Exception {
		Map<String, File> stringFilesMap = builder.getStringFilesMap("staging=temp/myConfig.ini,preview=temp/myConfig2.ini");
		assertThat(stringFilesMap.get("staging"), is(new File("temp/myConfig.ini")));
		assertThat(stringFilesMap.get("preview"), is(new File("temp/myConfig2.ini")));
	}

	@Test
	public void getDeploy() throws Exception {
		assertTrue(builder.shouldDeploy(), "Default value mismatch");
		assertTrue(builder.deploy("true").shouldDeploy(), "Value mismatch");
		assertTrue(builder.deploy("trUE").shouldDeploy(), "Value mismatch");
		assertTrue(builder.deploy("TRUE").shouldDeploy(), "Value mismatch");
		assertFalse(builder.deploy("false").shouldDeploy(), "Value mismatch");
		assertFalse(builder.deploy("falSE").shouldDeploy(), "Value mismatch");
		assertFalse(builder.deploy("FALSE").shouldDeploy(), "Value mismatch");
		assertTrue(builder.deploy("illegalValue").shouldDeploy(), "Value mismatch");
	}

}
