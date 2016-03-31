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

package com.espirit.moddev.cli.reflection;

import com.espirit.moddev.cli.api.command.Command;
import org.apache.log4j.Logger;
import org.reflections.Reflections;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;


/**
 * This class is a utility class for command extraction. It offers some convenience
 * methods that use reflection to scan packages, read classes and annotations etc.
 */
public abstract class CommandUtils {
    private static final Logger LOGGER = Logger.getLogger(CommandUtils.class);

    /**
     * Scans the given package for classes that implement the {@link Command} interface.
     * Ignores abstract classes.
     *
     * @param packageToScan the package, that should be scanned recursively
     * @return a set of matching classes
     */
    public static Set<Class<? extends Command>> scanForCommandClasses(String packageToScan) {
        String packageToScanForCommands = packageToScan;
        LOGGER.debug("Scanning for command classes in package " + packageToScanForCommands);
        Reflections reflections = new Reflections(packageToScanForCommands);
        Set<Class<? extends Command>> commandClasses = reflections.getSubTypesOf(Command.class);
        commandClasses = commandClasses
                .stream()
                .filter(commandClass -> !Modifier.isAbstract(commandClass.getModifiers()))
                .collect(Collectors.toSet());

        String commaSeparatedCommands = commandClasses.stream()
                .map(commandClass -> commandClass.getSimpleName().toString())
                .collect(Collectors.joining(", "));
        LOGGER.debug("Found " + commandClasses.size() + " commands. " + commaSeparatedCommands);

        return commandClasses;
    }
}
