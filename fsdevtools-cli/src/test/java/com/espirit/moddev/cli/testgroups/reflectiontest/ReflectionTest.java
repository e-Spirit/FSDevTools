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

package com.espirit.moddev.cli.testgroups.reflectiontest;

import com.espirit.moddev.cli.reflection.ReflectionUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReflectionTest {

	@Test
	public void readsGroupDescriptionFromAnnotatedMethodTest() {
		assertEquals("xyz", ReflectionUtils.getDescriptionFromClass(GroupWithDescriptionAnnotation.class));
	}

	@Test
	public void readsGroupDescriptionFromNonAnnotatedMethodWithNamingConventionTest() {
		assertEquals("abc", ReflectionUtils.getDescriptionFromClass(GroupWithDescriptionMethod.class));
	}

	@Test
	public void readsGroupDescriptionAsEmptyFromDescriptionMethodReturnsVoidTest() {
		assertTrue(ReflectionUtils.getDescriptionFromClass(GroupWithVoidDescriptionMethod.class).isEmpty());
	}

	@Test
	public void readsGroupDescriptionAsEmptyFromDescriptionMethodReturnsNonStringTest() {
		assertTrue(ReflectionUtils.getDescriptionFromClass(GroupWithNonStringDescriptionMethod.class).isEmpty());
	}

	@Test
	public void readsGroupWithoutDescriptionAnnotationAndVoidDescriptionMethodTest() {
		assertTrue(ReflectionUtils.getDescriptionFromClass(GroupWithoutDescriptionAnnotationAndVoidDescriptionMethod.class).isEmpty());
	}
}
