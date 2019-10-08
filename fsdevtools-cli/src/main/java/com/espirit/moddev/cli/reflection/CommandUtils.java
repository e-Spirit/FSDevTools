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
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * This class is a utility class for command extraction. It offers some convenience methods that use reflection to scan packages, read classes and
 * annotations etc.
 *
 * @author e-Spirit AG
 */
public final class CommandUtils {

    private static final Logger LOGGER = Logger.getLogger(CommandUtils.class);

    private CommandUtils() {
    }

    /**
     * Scans the whole classpath for classes that implement the {@link Command} interface. Ignores abstract classes.
     *
     * Calls scanForCommandClasses(String packageToScanForCommands) under the hood.
     *
     * @return a set of matching classes
     */
    public static Set<Class<? extends Command>> scanForCommandClasses() {
        return scanForCommandClasses("");
    }

    /**
     * Scans the given package for classes that implement the {@link Command} interface. Ignores abstract classes.
     * packageToScanForCommands is a String with comma separated packages that should be scanned. Excluded packages
     * are prefixed with - (minus). If all packages should be scanned, just pass an empty String, or use
     * scanForCommandClasses without parameter.
     *
     * @param packagesToScanForCommands the package, that should be scanned recursively
     * @return a set of matching classes
     * @throws IllegalArgumentException if null or empty package string is passed
     */
    public static Set<Class<? extends Command>> scanForCommandClasses(String packagesToScanForCommands) {
        final Set<Class<? extends Command>> matchingClasses = new HashSet<>();
        final ClassGraph classGraph = new ClassGraph().enableClassInfo().whitelistPackages(packagesToScanForCommands);
        try (final ScanResult scanResult = classGraph.scan()) {
            for (final ClassInfo classInfo : scanResult.getClassesImplementing(Command.class.getName())) {
                if (!classInfo.isAbstract()) {
                    //noinspection unchecked --> we already know that this command implements the Command.class
                    matchingClasses.add((Class<? extends Command>) classInfo.loadClass());
                } else {
                    LOGGER.debug("Found command " + classInfo.getSimpleName() + ", which is abstract, so it is ignored.");
                }
            }
        }
        LOGGER.debug("Found " + matchingClasses.size() + " commands. " + matchingClasses.stream().map(Class::getSimpleName).collect(Collectors.joining(",")));
        return matchingClasses;
    }
}
