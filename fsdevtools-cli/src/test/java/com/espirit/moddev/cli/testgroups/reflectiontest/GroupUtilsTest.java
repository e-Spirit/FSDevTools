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

import com.espirit.moddev.cli.groups.example.ExampleCustomGroup;
import com.espirit.moddev.cli.reflection.GroupUtils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

/**
 * @author e-Spirit AG
 */
public class GroupUtilsTest {

    public static final String DEFAULT_GROUP_TEST_PACKAGE_NAME = "com.espirit.moddev.cli.testgroups.reflectiontest";

    @Test
    public void packageScanRetrievesCorrectCommandClassCount() {
        final Set<Class<?>> groupClassesInPackage = GroupUtils.scanForGroupClasses(DEFAULT_GROUP_TEST_PACKAGE_NAME);
        Assert.assertEquals(5, groupClassesInPackage.size());
    }
    @Test
    public void classpathScanRetrievesExampleGroup() {
        final Set<Class<?>> groupClassesInPackage = GroupUtils.scanForGroupClasses();
        Assert.assertTrue("Expected example group class to be found", groupClassesInPackage.contains(ExampleCustomGroup.class));
    }

}
