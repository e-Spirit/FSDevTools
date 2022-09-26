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

package com.espirit.moddev.cli.testcommands.reflectiontest;

import com.espirit.moddev.cli.api.command.Command;
import com.espirit.moddev.cli.commands.example.ExampleCustomCommand;
import com.espirit.moddev.cli.commands.export.ExportCommand;
import com.espirit.moddev.cli.reflection.CommandUtils;
import com.espirit.moddev.cli.reflection.ReflectionUtils;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
		assertEquals(EXPECTED_COMMANDCLASSES_IN_TESTCOMMANDSPACKAGE, commandClassesInPackage.size());
	}

	/**
	 * This test ensures that the whole classpath is scanned for commands without specifying a package name.
	 * Asserting the presence of an explicit class from an external dependency is probably the best Test for this.
	 */
	@Test
	public void classpathScanRetrievesAllCommandClasses() {
		Set<Class<? extends Command>> commandClassesInClasspath = CommandUtils.scanForCommandClasses();
		assertTrue(commandClassesInClasspath.contains(ExampleCustomCommand.class));
	}

	public static class ReflectionTest {
		@Test
		public void readsCommandDescriptionFromAnnotatedMethodTest() {
			assertEquals("xyz", ReflectionUtils.getDescriptionFromClass(CommandWithDescriptionAnnotation.class));
		}

		@Test
		public void readsCommandDescriptionFromNonAnnotatedMethodWithNamingConventionTest() {
			assertEquals("abc", ReflectionUtils.getDescriptionFromClass(CommandWithDescriptionMethod.class));
		}

		@Test
		public void readsCommandDescriptionAsEmptyFromDescriptionMethodReturnsVoidTest() {
			assertTrue(ReflectionUtils.getDescriptionFromClass(CommandWithVoidDescriptionMethod.class).isEmpty());
		}

		@Test
		public void readsCommandDescriptionAsEmptyFromDescriptionMethodReturnsNonStringTest() {
			assertTrue(ReflectionUtils.getDescriptionFromClass(CommandWithNonStringDescriptionMethod.class).isEmpty());
		}

		@Test
		public void readsCommandDescriptionExportCommand() {
			final String expectedMsgPart = "ALL, COMMON, CUSTOM_PROPERTIES, RESOLUTIONS, GROUPS, SCHEDULE_ENTRIES, TEMPLATE_SETS, FONTS, MODULE_CONFIGURATIONS, LANGUAGES, USERS";
			final String msg = ReflectionUtils.getDescriptionFromClass(ExportCommand.class);
			assertTrue(msg.contains(expectedMsgPart));
		}
	}
}
