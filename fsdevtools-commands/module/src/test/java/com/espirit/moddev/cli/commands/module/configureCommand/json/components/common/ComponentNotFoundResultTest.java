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

package com.espirit.moddev.cli.commands.module.configureCommand.json.components.common;

import de.espirit.firstspirit.module.descriptor.ComponentDescriptor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ComponentNotFoundResultTest {

	private static final String MODULE_NAME = "testModule";
	private static final ComponentDescriptor.Type TYPE = ComponentDescriptor.Type.PROJECTAPP;
	private static final String COMPONENT_NAME = "testComponent";

	@Test
	public void getException() {
		final ComponentNotFoundResult result = new ComponentNotFoundResult(MODULE_NAME, TYPE, COMPONENT_NAME);
		final IllegalStateException exception = result.getThrowable();
		assertThat(exception.getMessage()).isEqualTo(String.format(ComponentNotFoundResult.MESSAGE, COMPONENT_NAME, TYPE, MODULE_NAME));
	}

	@Test
	public void _toString() {
		final ComponentNotFoundResult result = new ComponentNotFoundResult(MODULE_NAME, TYPE, COMPONENT_NAME);
		assertThat(result.toString()).isEqualTo(String.format(ComponentNotFoundResult.MESSAGE, COMPONENT_NAME, TYPE, MODULE_NAME));
	}

}
