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

import com.espirit.moddev.cli.api.annotations.Description;
import com.espirit.moddev.cli.api.command.Command;
import org.apache.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for various reflection-based techniques.
 *
 * @author e-Spirit AG
 */
public final class ReflectionUtils {

    private static final Logger LOGGER = Logger.getLogger(ReflectionUtils.class);

    private ReflectionUtils() {
        // Not used
    }

    /**
     * Changes the value attribute of a corresponding key for the given annotation. Note, that the value is replaced in place, so the state of the
     * annotation is manipualted.
     *
     * @param annotation the annotation the new value should be set
     * @param key        the attribute's name, for which the new value should be set
     * @param newValue   the new value that should be set for the given key
     * @return the old value of the annotation's field
     * @throws IllegalStateException    if the memberValues field can not be accessed
     * @throws IllegalArgumentException if the attribute isn't defined or the new value has a wrong type
     */
    @SuppressWarnings("unchecked")
    public static Object changeAnnotationValue(Annotation annotation, String key, Object newValue) {
        if (annotation == null) {
            LOGGER.debug("changeAnnotationValue called with null annotation, not changing anything");
            return null;
        }
        Object handler = Proxy.getInvocationHandler(annotation);
        Field f;
        try {
            f = handler.getClass().getDeclaredField("memberValues");
        } catch (NoSuchFieldException | SecurityException e) {
            throw new IllegalStateException(e);
        }
        f.setAccessible(true);
        Map<String, Object> memberValues;
        try {
            memberValues = (Map<String, Object>) f.get(handler);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
        Object oldValue = memberValues.get(key);
        if (oldValue == null || oldValue.getClass() != newValue.getClass()) {
            throw new IllegalArgumentException();
        }
        memberValues.put(key, newValue);
        return oldValue;
    }

    /**
     * A helper method that tries to retrieve potentially dynamic description information for {@code commandClass}. The description is not retrieved
     * from the airline annotation's description attribute, but instead from methods annotated with {@link Description}, or static methods that follow
     * the naming convention "getDescription()" and return a String value.
     *
     * @param commandClass the class the description should be retrieved for
     * @return the description if it is retrievable somehow, or an empty String, if no description can be found via a {@link Description} or a
     * getDescription method - both with a String return value.
     */
    public static String getDescriptionFromClass(Class commandClass) {
        String description = "";
        try {
            Method staticDescriptionMethod;
            staticDescriptionMethod = ReflectionUtils.getStaticDescriptionMethod(commandClass);

            if (staticDescriptionMethod != null) {
                if (!staticDescriptionMethod.isAccessible()) {
                    staticDescriptionMethod.setAccessible(true);
                }

                Object result = staticDescriptionMethod.invoke(null);

                if (result == null) {
                    LOGGER.debug("Dynamic description found for " + commandClass
                                 + ", but the return value of the annotated method is void or null. Change it.");
                } else if (result instanceof String) {
                    description = (String) result;
                } else {
                    LOGGER.debug(
                        "Dynamic description found for " + commandClass + ", but the return value of the annotated method is not String. Change it.");
                }
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("error getting description for command '" + commandClass + "'", e);
            } else {
                LOGGER.info("error getting description for command '" + commandClass + "'");
            }
            description = "<error reading description of command>";
        }

        return description;
    }

    /**
     * Retrieves a static method with a String return value that is either annotated with {@link Description}, or using the naming convention
     * getDescription().
     *
     * @param commandClass the class to retrieve the method from
     * @return the description method, or null if none is found
     */
    static Method getStaticDescriptionMethod(Class<? extends Command> commandClass) {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                                                      .setUrls(forClass(commandClass))
                                                      .filterInputsBy(in -> in.endsWith("java") || in.endsWith("class"))
                                                      .setScanners(new MethodAnnotationsScanner())
        );

        Set<Method> methodsWithDescriptionAnnotation = reflections.getMethodsAnnotatedWith(Description.class).stream()
            .filter(x -> x.getDeclaringClass().getCanonicalName().equals(commandClass.getCanonicalName()))
            .collect(Collectors.toSet());

        Method staticDescriptionMethod = null;
        if (!methodsWithDescriptionAnnotation.isEmpty()) {
            LOGGER.debug("Found annotated method for description for " + commandClass);
            staticDescriptionMethod = methodsWithDescriptionAnnotation.iterator().next();
        } else {
            try {
                staticDescriptionMethod = commandClass.getMethod("getDescription");
            } catch (NoSuchMethodException e) {
                // This case doesn't have to be handled, because it is perfectly fine, when such a method
                // does not exist
                LOGGER.trace(e);
            }
        }

        if (staticDescriptionMethod != null) {
            LOGGER.debug("Dynamic description found and used for " + commandClass);
        }
        return staticDescriptionMethod;
    }

    /**
     * This is copied from {@link ClasspathHelper}, but there's a bug when using default packages, so the implementation is changed.
     *
     * @param aClass
     * @param classLoaders
     * @return
     */
    public static URL forClass(Class<?> aClass, ClassLoader... classLoaders) {
        final ClassLoader[] loaders = ClasspathHelper.classLoaders(classLoaders);
        final String resourceName = aClass.getName().replace(".", "/") + ".class";
        for (ClassLoader classLoader : loaders) {
            try {
                final URL url = classLoader.getResource(resourceName);
                if (url != null) {
                    Package aPackage = aClass.getPackage();
                    String externalForm = url.toExternalForm();
                    if(aPackage != null) {
                        String convertedPackageName = aPackage.getName().replace(".", "/");
                        final String normalizedUrl = externalForm.substring(0, externalForm.lastIndexOf(convertedPackageName));
                        return new URL(normalizedUrl);
                    }
                    return url;
                }
            } catch (MalformedURLException e) {
                if (Reflections.log != null) {
                    Reflections.log.warn("Could not get URL", e);
                }
            }
        }
        return null;
    }
}
