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

import com.espirit.moddev.cli.reflection.GroupUtils;
import com.espirit.moddev.cli.reflection.ReflectionUtils;
import junit.framework.Assert;
import org.junit.Test;

import java.util.Set;

/**
 * @author e-Spirit AG
 */
public class GroupUtilsTest {

    public static final String DEFAULT_GROUP_TEST_PACKAGE_NAME = "com.espirit.moddev.cli.testgroups.reflectiontest";

    @Test
    public void packageScanRetrievesCorrectCommandClassCount() {
        Set<Class<?>> groupClassesInPackage = GroupUtils.scanForGroupClasses(DEFAULT_GROUP_TEST_PACKAGE_NAME);
        Assert.assertEquals(5, groupClassesInPackage.size());
    }

    /**
     * @author e-Spirit AG
     */
    public static class ReflectionTest {
        @Test
        public void readsGroupDescriptionFromAnnotatedMethodTest() {
            Assert.assertEquals("Description is expected to be xyz non null from getMyCustomDescription()",
                    "xyz", ReflectionUtils.getDescriptionFromClass(GroupWithDescriptionAnnotation.class));
        }
        @Test
        public void readsGroupDescriptionFromNonAnnotatedMethodWithNamingConventionTest() {
            Assert.assertEquals("Description is expected to be abc non null from getDescription()",
                    "abc", ReflectionUtils.getDescriptionFromClass(GroupWithDescriptionMethod.class));
        }
        @Test
        public void readsGroupDescriptionAsEmptyFromDescriptionMethodReturnsVoidTest() {
            Assert.assertTrue("Description is expected to be empty from getDescription()",
                    ReflectionUtils.getDescriptionFromClass(GroupWithVoidDescriptionMethod.class).isEmpty());
        }
        @Test
        public void readsGroupDescriptionAsEmptyFromDescriptionMethodReturnsNonStringTest() {
            Assert.assertTrue("Description is expected to be empty from getDescription()",
                    ReflectionUtils.getDescriptionFromClass(GroupWithNonStringDescriptionMethod.class).isEmpty());
        }
    }
}
