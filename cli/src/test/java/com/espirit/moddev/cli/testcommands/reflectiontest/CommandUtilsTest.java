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

package com.espirit.moddev.cli.testcommands.reflectiontest;

import com.espirit.moddev.cli.api.command.Command;
import com.espirit.moddev.cli.reflection.CommandUtils;
import com.espirit.moddev.cli.reflection.ReflectionUtils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

/**
 * @author e-Spirit AG
 */
public class CommandUtilsTest {

    public static final String DEFAULT_COMMAND_TEST_PACKAGE_NAME = "com.espirit.moddev.cli.testcommands.reflectiontest";

    @Test
    public void packageScanRetrievesCorrectCommandClassCount() {
        Set<Class<? extends Command>> commandClassesInPackage = CommandUtils.scanForCommandClasses(DEFAULT_COMMAND_TEST_PACKAGE_NAME);
        Assert.assertEquals(5, commandClassesInPackage.size());
    }

    /**
     * @author e-Spirit AG
     */
    public static class ReflectionTest {
        @Test
        public void readsCommandDescriptionFromAnnotatedMethodTest() {
            Assert.assertEquals("Description is expected to be xyz non null from getMyCustomDescription()",
                    "xyz", ReflectionUtils.getDescriptionFromClass(CommandWithDescriptionAnnotation.class));
        }
        @Test
        public void readsCommandDescriptionFromNonAnnotatedMethodWithNamingConventionTest() {
            Assert.assertEquals("Description is expected to be abc non null from getDescription()",
                    "abc", ReflectionUtils.getDescriptionFromClass(CommandWithDescriptionMethod.class));
        }
        @Test
        public void readsCommandDescriptionAsEmptyFromDescriptionMethodReturnsVoidTest() {
            Assert.assertTrue("Description is expected to be empty from getDescription()",
                    ReflectionUtils.getDescriptionFromClass(CommandWithVoidDescriptionMethod.class).isEmpty());
        }
        @Test
        public void readsCommandDescriptionAsEmptyFromDescriptionMethodReturnsNonStringTest() {
            Assert.assertTrue("Description is expected to be empty from getDescription()",
                    ReflectionUtils.getDescriptionFromClass(CommandWithNonStringDescriptionMethod.class).isEmpty());
        }
    }
}
