/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2024 Crownpeak Technology GmbH
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility class for various reflection-based techniques.
 */
public final class ReflectionUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReflectionUtils.class);

	private ReflectionUtils() {
		// Not used
	}

	/**
	 * Creates a new instance of the specified class using its default constructor.
	 *
	 * @param clazz The {@link Class} object representing the class for which an instance is to be created.
	 * @param <T>   The type of the class.
	 * @return A new instance of the specified class.
	 * @throws NoSuchMethodException     If the default constructor of the class is not found.
	 * @throws InvocationTargetException If the underlying constructor throws an exception.
	 * @throws InstantiationException    If the class is abstract or an interface, or if the instantiation fails for some other reason.
	 * @throws IllegalAccessException    If the default constructor is inaccessible.
	 */
	@SuppressWarnings("unchecked")
	@NotNull
	public static <T> T createInstance(@NotNull final Class<T> clazz) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
		final String className = clazz.getName();

		// get default constructor
		LOGGER.trace("Getting default constructor of class '{}'...", className);
		final Constructor<?> constructor = clazz.getConstructor();

		// create engine instance
		LOGGER.trace("Creating instance '{}'...", className);
		return (T) constructor.newInstance();
	}

	/**
	 * Changes the value attribute of a corresponding key for the given annotation. Note, that the value is replaced in place, so the state of the
	 * annotation is manipulated.
	 *
	 * @param annotation the annotation the new value should be set
	 * @param key        the attribute's name, for which the new value should be set
	 * @param newValue   the new value that should be set for the given key
	 * @return the old value of the annotation's field
	 * @throws IllegalStateException    if the memberValues field can not be accessed
	 * @throws IllegalArgumentException if the attribute isn't defined or the new value has a wrong type
	 */
	@SuppressWarnings("unchecked")
	public static Object changeAnnotationValue(@Nullable final Annotation annotation, @NotNull final String key, @Nullable final Object newValue) {
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

		Method[] methods = commandClass.getMethods();
		List<Method> methodsWithDescriptionAnnotation = new ArrayList<>(methods.length);
		for (Method method : methods) {
			if (method.isAnnotationPresent(Description.class)) {
				methodsWithDescriptionAnnotation.add(method);
			}
		}

		Method staticDescriptionMethod = null;
		if (!methodsWithDescriptionAnnotation.isEmpty()) {
			LOGGER.debug("Found annotated method for description for " + commandClass);
			if (methodsWithDescriptionAnnotation.size() > 1) {
				LOGGER.warn("Found multiple annotated methods for description for " + commandClass + ". Using first one.");
			}
			staticDescriptionMethod = methodsWithDescriptionAnnotation.get(0);
		} else {
			try {
				staticDescriptionMethod = commandClass.getMethod("getDescription");
			} catch (NoSuchMethodException e) {
				// This case doesn't have to be handled, because it is perfectly fine, when such a method
				// does not exist
				LOGGER.trace(e.getMessage(), e);
			}
		}

		if (staticDescriptionMethod != null) {
			LOGGER.debug("Dynamic description found and used for " + commandClass);
		}
		return staticDescriptionMethod;
	}

}
