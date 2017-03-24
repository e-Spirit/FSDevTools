/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2016 e-Spirit AG
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

import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * @author e-Spirit AG
 */
public class ReflectionTest {

    @Test
    public void readsGroupDescriptionFromAnnotatedMethodTest() {
        assertEquals("Description is expected to be xyz non null from getMyCustomDescription()",
                     "xyz", ReflectionUtils.getDescriptionFromClass(GroupWithDescriptionAnnotation.class));
    }

    @Test
    public void readsGroupDescriptionFromNonAnnotatedMethodWithNamingConventionTest() {
        assertEquals("Description is expected to be abc non null from getDescription()",
                     "abc", ReflectionUtils.getDescriptionFromClass(GroupWithDescriptionMethod.class));
    }

    @Test
    public void readsGroupDescriptionAsEmptyFromDescriptionMethodReturnsVoidTest() {
        assertTrue("Description is expected to be empty from getDescription()",
                   ReflectionUtils.getDescriptionFromClass(GroupWithVoidDescriptionMethod.class).isEmpty());
    }

    @Test
    public void readsGroupDescriptionAsEmptyFromDescriptionMethodReturnsNonStringTest() {
        assertTrue("Description is expected to be empty from getDescription()",
                   ReflectionUtils.getDescriptionFromClass(GroupWithNonStringDescriptionMethod.class).isEmpty());
    }

    @Test
    public void readsGroupWithoutDescriptionAnnotationAndVoidDescriptionMethodTest() {
        assertTrue("Description is expected to be empty from getDescription()",
                   ReflectionUtils.getDescriptionFromClass(GroupWithoutDescriptionAnnotationAndVoidDescriptionMethod.class).isEmpty());
    }
}
