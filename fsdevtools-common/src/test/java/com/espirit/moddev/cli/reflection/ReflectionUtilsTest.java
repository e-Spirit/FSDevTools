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

package com.espirit.moddev.cli.reflection;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReflectionUtilsTest {

	public static class SimpleClass {
	}

	public static class SimpleClassNoDefaultConstructor {
		public SimpleClassNoDefaultConstructor(@NotNull final Object parameter) {
		}
	}

	@Test
	void createInstance() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
		// test & verify
		final SimpleClass instance = ReflectionUtils.createInstance(SimpleClass.class);
		assertThat(instance).isNotNull();
	}

	@Test
	void createInstance_noDefaultConstructor() {
		// test & verify
		assertThatThrownBy(() -> ReflectionUtils.createInstance(SimpleClassNoDefaultConstructor.class)).isInstanceOf(NoSuchMethodException.class);
	}

}