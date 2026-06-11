/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2026 Crownpeak Technology GmbH
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

package com.espirit.moddev.util;

import com.espirit.moddev.util.serializer.ExceptionSerializer;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.jetbrains.annotations.NotNull;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

/**
 * Utility class for things related to json.
 */
public class JacksonUtil {

	/**
	 * Creates a preconfigured {@link JsonMapper} and returns it. The returned mapper should only be used for deserialization purposes.<br/>
	 * <br/>
	 * The returned mapper will be configured as following:<br/>
	 * <b>Enabled features:</b>
	 * <ul>
	 *     <li>{@link DeserializationFeature#FAIL_ON_MISSING_CREATOR_PROPERTIES}</li>
	 *     <li>{@link DeserializationFeature#FAIL_ON_UNKNOWN_PROPERTIES}</li>
	 * </ul>
	 *
	 * @return a pre-configured {@link JsonMapper} for deserialization purposes
	 */
	@NotNull
	public static JsonMapper createInputMapper() {
		return JsonMapper.builder()
				.enable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES)
				.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
				.build();
	}

	/**
	 * Creates a preconfigured {@link JsonMapper} and returns it. The returned mapper should only be used for serialization purposes.<br/>
	 * <br/>
	 * The returned mapper will be configured as following:<br/>
	 * <br/>
	 * <b>Serializers:</b>
	 * <ul>
	 *     <li>{@link ExceptionSerializer}</li>
	 * </ul>
	 * <b>Disabled features:</b>
	 * <ul>
	 *     <li>{@link JsonInclude.Include#NON_NULL Serialization of null-values}</li>
	 *     <li>Auto-detection of creators, fields, getters and is-getters (all set to {@code NONE} visibility)</li>
	 * </ul>
	 * <b>Enabled features:</b>
	 * <ul>
	 *     <li>{@link SerializationFeature#INDENT_OUTPUT}</li>
	 *     <li>{@link SerializationFeature#ORDER_MAP_ENTRIES_BY_KEYS}</li>
	 * </ul>
	 *
	 * @return a pre-configured {@link JsonMapper} for serialization purposes
	 */
	@NotNull
	public static JsonMapper createOutputMapper() {
		final SimpleModule module = new SimpleModule();
		module.addSerializer(Exception.class, new ExceptionSerializer());
		return JsonMapper.builder()
				.changeDefaultVisibility(vc -> vc
						.withCreatorVisibility(JsonAutoDetect.Visibility.NONE)
						.withFieldVisibility(JsonAutoDetect.Visibility.NONE)
						.withGetterVisibility(JsonAutoDetect.Visibility.NONE)
						.withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
				)
				.changeDefaultPropertyInclusion(v -> v.withValueInclusion(JsonInclude.Include.NON_NULL))
				.enable(SerializationFeature.INDENT_OUTPUT)
				.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
				.addModule(module)
				.build();
	}

	/**
	 * Returns {@code true} if the given class carries an annotation whose fully-qualified type name ends with
	 * {@code ".jackson.databind.annotation.JsonSerialize"}.
	 *
	 * <p>Matching by FQN suffix rather than by class identity makes the check relocation-robust: Shadow rewrites the
	 * {@code JsonSerialize.class} literal to the shaded type, so a plain {@code isAnnotationPresent(JsonSerialize.class)}
	 * call would miss externally-built command JARs (compiled against the non-shaded cli-api) after relocation. The
	 * string suffix is not rewritten by Shadow and therefore matches all three variants:
	 * <ul>
	 *   <li>{@code tools.jackson.databind.annotation.JsonSerialize} (post-upgrade, pre-shade)</li>
	 *   <li>{@code com.fasterxml.jackson.databind.annotation.JsonSerialize} (legacy, pre-upgrade)</li>
	 *   <li>{@code com.espirit.moddev.cli.shaded.jackson.annotation.JsonSerialize} (post-shade)</li>
	 * </ul>
	 *
	 * @param clazz the class to inspect; must not be {@code null}
	 * @return {@code true} if any present annotation's type name ends with
	 *         {@code ".jackson.databind.annotation.JsonSerialize"}, {@code false} otherwise
	 */
	public static boolean hasJsonSerializeAnnotation(@NotNull final Class<?> clazz) {
		for (final java.lang.annotation.Annotation annotation : clazz.getAnnotations()) {
			if (annotation.annotationType().getName().endsWith(".jackson.databind.annotation.JsonSerialize")) {
				return true;
			}
		}
		return false;
	}

}
