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

import com.github.rvesse.airline.annotations.Group;
import org.apache.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.net.URL;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class is a utility class for group extraction. It offers some convenience methods that use reflection to scan packages, read classes and
 * annotations etc.
 *
 * @author e-Spirit AG
 */
public final class GroupUtils {

    private static final Logger LOGGER = Logger.getLogger(GroupUtils.class);

    private GroupUtils() {
        // Not used
    }

    /**
     * Scans the classpath for classes that are annotated with airline's {@link Group} annotation.
     * Excludes the package com.github.rvesse.airline.annotations, because it contains a malformed
     * command class that causes an exception when loaded.
     *
     * @return a set of matching classes
     */
    public static Set<Class<?>> scanForGroupClasses() {
        FilterBuilder filter = new FilterBuilder().add(input -> input.endsWith(".class")).excludePackage("com.github.rvesse.airline.annotations");
        Collection<URL> classPathUrls = ClasspathHelper.forJavaClassPath();
        Collection<URL> classPathUrlsExceptJre = classPathUrls.stream().filter(url -> !url.toString().contains("/jre/lib")).collect(Collectors.toList());
        ConfigurationBuilder configuration = new ConfigurationBuilder()
                .addUrls(classPathUrlsExceptJre)
                .filterInputsBy(filter);

        return scanForGroupClasses(new Reflections(configuration));
    }

    /**
     * Scans the given package for classes that are annotated with airline's {@link Group} annotation.
     *
     * @param packageToScan the package, that should be scanned recursively
     * @return a set of matching classes
     */
    public static Set<Class<?>> scanForGroupClasses(String packageToScan) {
        LOGGER.debug("Scanning for group classes in package " + packageToScan);
        return scanForGroupClasses(new Reflections(packageToScan));
    }

    private static Set<Class<?>> scanForGroupClasses(Reflections reflections) {
        Set<Class<?>> groupClasses = reflections.getTypesAnnotatedWith(Group.class);

        String commaSeparatedGroups = groupClasses.stream()
            .map(groupClass -> groupClass.getSimpleName())
            .collect(Collectors.joining(", "));
        LOGGER.debug("Found " + groupClasses.size() + " groups. " + commaSeparatedGroups);

        return groupClasses;
    }

}
