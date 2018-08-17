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
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import org.apache.log4j.Logger;

import java.util.HashSet;
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
     * Ignores abstract classes.
     *
     * @param packagesToScan is a String with comma separated packages that should be scanned. Excluded packages
     *                       are prefixed with - (minus). If all packages should be scanned, just pass an empty String, or use
     *                       scanForCommandClasses without parameter.
     * @return a set of matching classes
     */
    public static Set<Class<?>> scanForGroupClasses(String packagesToScan) {
        Set<Class<?>> result = new HashSet<>();
        FastClasspathScanner scanner = new FastClasspathScanner(packagesToScan).matchClassesWithAnnotation(Group.class, result::add);
        scanner.scan();
        LOGGER.debug("Found " + result.size() + " commands. " + result.stream().map(it -> it.getSimpleName()).collect(Collectors.joining(",")));
        return result;
    }

    /**
     * Scans the whole classpath for classes that are annotated with airline's {@link Group} annotation.
     * Uses scanForGroupClasses
     *
     * @return a set of matching classes
     */
    public static Set<Class<?>> scanForGroupClasses() {
        return scanForGroupClasses("");
    }
}
