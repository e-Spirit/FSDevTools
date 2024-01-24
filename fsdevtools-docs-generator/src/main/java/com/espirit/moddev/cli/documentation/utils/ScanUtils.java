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

package com.espirit.moddev.cli.documentation.utils;

import com.espirit.moddev.shared.annotation.VisibleForTesting;
import io.github.classgraph.AnnotationInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Optional;

public class ScanUtils {

	private ScanUtils() {
		// NOP
	}

	@NotNull
	public static Optional<String> getValueAsString(@NotNull final AnnotationInfo annotationInfo, @NotNull final String variableName) {
		return getValueAsString(annotationInfo, variableName, null);
	}

	@NotNull
	public static Optional<String> getValueAsString(@NotNull final AnnotationInfo annotationInfo, @NotNull final String variableName, @Nullable final String defaultValue) {
		Object value = annotationInfo.getParameterValues(true).getValue(variableName);
		if (value != null) {
			String val = null;
			if (value.getClass().isArray()) {
				if (((Object[]) value).length > 0 && ((Object[]) value)[0] != null) {
					val = ((Object[]) value)[0].toString();
				}
			} else {
				val = value.toString();
			}
			if (val != null && !val.isEmpty()) {
				return Optional.of(val);
			}
		}
		return Optional.ofNullable(defaultValue);
	}

	@SuppressWarnings({"unchecked"})
	@NotNull
	public static <T> Optional<T[]> getValueAsArray(@NotNull final AnnotationInfo annotationInfo, @NotNull final String variableName) {
		Object value = annotationInfo.getParameterValues(true).getValue(variableName);
		if (value != null && value.getClass().isArray()) {
			return Optional.of((T[]) value);
		}
		return Optional.empty();
	}

	@NotNull
	public static Optional<Field> getField(@NotNull final Class<?> clazz, @NotNull final String fieldName) {
		Field field = null;
		try {
			field = clazz.getDeclaredField(fieldName);
		} catch (final NoSuchFieldException ignore) {
		}
		if (field == null) {
			try {
				field = clazz.getField(fieldName);
			} catch (final NoSuchFieldException ignore) {
			}
		}

		if (field == null && clazz.getSuperclass() != null) {
			final Optional<Field> optionalParentField = getField(clazz.getSuperclass(), fieldName);
			field = optionalParentField.orElse(null);
		}
		return Optional.ofNullable(field);
	}

	@VisibleForTesting
	@Nullable
	static Object getFieldValue(@NotNull final Object instance, @NotNull final Field field) {
		final boolean wasAccessible = field.isAccessible();
		try {
			field.setAccessible(true);
			return field.get(instance);
		} catch (final IllegalAccessException e) {
			return null;
		} finally {
			if (!wasAccessible) {
				field.setAccessible(false);
			}
		}
	}

	@Nullable
	public static String getFieldValueAsString(@NotNull final Object instance, @NotNull final String fieldName) {
		final Optional<Field> optionalField = getField(instance.getClass(), fieldName);
		if (optionalField.isPresent()) {
			final Object fieldValue = ScanUtils.getFieldValue(instance, optionalField.get());
			return fieldValue == null ? null : fieldValue.toString();
		}
		return null;
	}

}
