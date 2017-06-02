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
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Collection;
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
     * Ignores furthermore everything that is not a .class file. Ignores every artifact from the Java runtime.
     * Ingores the package com.github.rvesse.airline.annotations, because it contains malformed classes.
     *
     * @return a set of matching classes
     */
    public static Set<Class<? extends Command>> scanForCommandClasses() {
        FilterBuilder filter = new FilterBuilder().add(input -> input.endsWith(".class")).excludePackage("com.github.rvesse.airline.annotations");
        Collection<URL> classPathUrls = ClasspathHelper.forJavaClassPath();
        Collection<URL> classPathUrlsExceptJre = classPathUrls.stream().filter(url -> !url.toString().contains("/jre/lib")).collect(Collectors.toList());
        ConfigurationBuilder configuration = new ConfigurationBuilder().addUrls(classPathUrlsExceptJre).filterInputsBy(filter);
        return scanForCommandClasses(new Reflections(configuration));
    }
    /**
     * Scans the given package for classes that implement the {@link Command} interface. Ignores abstract classes.
     *
     * @param packageToScanForCommands the package, that should be scanned recursively
     * @return a set of matching classes
     * @throws IllegalArgumentException if null or empty package string is passed
     */
    public static Set<Class<? extends Command>> scanForCommandClasses(String packageToScanForCommands) {
        if(packageToScanForCommands == null || packageToScanForCommands.isEmpty()) {
            throw new IllegalArgumentException("Don't pass a null or empty string! Use scanForCommandClasses() if you don't want to define a package");
        }
        FilterBuilder inputsFilter = new FilterBuilder().includePackage(packageToScanForCommands);
        ConfigurationBuilder configuration = new ConfigurationBuilder().forPackages(packageToScanForCommands).filterInputsBy(inputsFilter);
        Reflections reflections = new Reflections(configuration);
        return scanForCommandClasses(reflections);
    }

    private static Set<Class<? extends Command>> scanForCommandClasses(Reflections reflections) {
        Set<Class<? extends Command>> commandClasses = reflections.getSubTypesOf(Command.class);
        commandClasses = commandClasses
            .stream()
            .filter(commandClass -> !Modifier.isAbstract(commandClass.getModifiers()))
            .collect(Collectors.toSet());

        String commaSeparatedCommands = commandClasses.stream()
            .map(commandClass -> commandClass.getSimpleName())
            .collect(Collectors.joining(", "));
        LOGGER.debug("Found " + commandClasses.size() + " commands. " + commaSeparatedCommands);

        return commandClasses;
    }
}
