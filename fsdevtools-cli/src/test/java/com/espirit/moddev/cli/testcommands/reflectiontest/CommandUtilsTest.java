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
import com.espirit.moddev.cli.commands.example.ExampleCustomCommand;
import com.espirit.moddev.cli.commands.export.ExportCommand;
import com.espirit.moddev.cli.reflection.CommandUtils;
import com.espirit.moddev.cli.reflection.ReflectionUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

public class CommandUtilsTest {

    private static final String DEFAULT_COMMAND_TEST_PACKAGE_NAME = "com.espirit.moddev.cli.testcommands.reflectiontest";

    private static final int EXPECTED_COMMANDCLASSES_IN_TESTCOMMANDSPACKAGE = 4;

    /**
     * If there are command class implementations added or removed from the test package, this test may fail. In
     * order to make it green, you should expect the correct count of command classes in the test package. Since
     * we test runtime behaviour (reflection) influenced by compile time changes (class removed/added) here,
     * this test fails at test runtime, even though the project compiles fine.
     */
    @Test
    public void packageScanRetrievesCorrectCommandClassCount() {
        Set<Class<? extends Command>> commandClassesInPackage = CommandUtils.scanForCommandClasses(DEFAULT_COMMAND_TEST_PACKAGE_NAME);
        Assert.assertEquals(EXPECTED_COMMANDCLASSES_IN_TESTCOMMANDSPACKAGE, commandClassesInPackage.size());
    }

    /**
     * This test ensures that the whole classpath is scanned for commands without specifying a package name.
     * Asserting the presence of an explicit class from an external dependency is probably the best Test for this.
     */
    @Test
    public void classpathScanRetrievesAllCommandClasses() {
        Set<Class<? extends Command>> commandClassesInClasspath = CommandUtils.scanForCommandClasses();
        Assert.assertTrue("Classpath scan should retrieve custom command class", commandClassesInClasspath.contains(ExampleCustomCommand.class));
    }

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
        @Test
        public void readsCommandDescriptionExportCommand() {
            final String expectedMsgPart = "ALL, COMMON, RESOLUTIONS, GROUPS, SCHEDULE_ENTRIES, TEMPLATE_SETS, FONTS, MODULE_CONFIGURATIONS, LANGUAGES, USERS";
            final String msg = ReflectionUtils.getDescriptionFromClass(ExportCommand.class);
            Assert.assertTrue("wrong project properties msg. was=\n" + msg, msg.contains(expectedMsgPart));
        }
    }
}
