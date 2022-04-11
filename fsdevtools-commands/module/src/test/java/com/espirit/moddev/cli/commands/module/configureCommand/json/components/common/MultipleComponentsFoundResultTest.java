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

package com.espirit.moddev.cli.commands.module.configureCommand.json.components.common;

import com.google.common.collect.Lists;
import de.espirit.firstspirit.module.descriptor.ComponentDescriptor;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MultipleComponentsFoundResultTest {

	private static final String MODULE_NAME = "testModule";
	private static final String DISPLAY_NAME = "testDisplayModule";
	private static final ComponentDescriptor.Type TYPE = ComponentDescriptor.Type.WEBAPP;

	@NotNull
	private static ComponentDescriptor mockDescriptor(@NotNull final String name, @NotNull final String displayName) {
		final ComponentDescriptor descriptor = mock(ComponentDescriptor.class);
		when(descriptor.getName()).thenReturn(name);
		when(descriptor.getDisplayName()).thenReturn(displayName);
		return descriptor;
	}

	@Test
	public void getException() {
		// setup
		final ComponentDescriptor descriptor1 = mockDescriptor(MODULE_NAME + "#1", DISPLAY_NAME + "#1");
		final ComponentDescriptor descriptor2 = mockDescriptor(MODULE_NAME + "#2", DISPLAY_NAME + "#2");
		final MultipleComponentsFoundResult result = new MultipleComponentsFoundResult(MODULE_NAME, DISPLAY_NAME, TYPE, Lists.newArrayList(descriptor1, descriptor2));
		// test
		final IllegalStateException exception = result.getException();
		// verify
		assertThat(exception.getMessage()).isEqualTo(String.format(MultipleComponentsFoundResult.MESSAGE, TYPE, DISPLAY_NAME, MODULE_NAME, "[ testModule#1, testModule#2 ]"));
	}

	@Test
	public void _toString() {
		// setup
		final ComponentDescriptor descriptor1 = mockDescriptor(MODULE_NAME + "#1", DISPLAY_NAME + "#1");
		final ComponentDescriptor descriptor2 = mockDescriptor(MODULE_NAME + "#2", DISPLAY_NAME + "#2");
		final MultipleComponentsFoundResult result = new MultipleComponentsFoundResult(MODULE_NAME, DISPLAY_NAME, TYPE, Lists.newArrayList(descriptor1, descriptor2));
		// test & verify
		assertThat(result.toString()).isEqualTo(String.format(MultipleComponentsFoundResult.MESSAGE, TYPE, DISPLAY_NAME, MODULE_NAME, "[ testModule#1, testModule#2 ]"));
	}

}
