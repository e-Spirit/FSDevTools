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
import tools.jackson.databind.annotation.JsonSerialize;
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
	 * Returns {@code true} if the given class carries the {@link JsonSerialize} annotation of <em>this</em> Jackson,
	 * i.e. the one the output mapper of this class uses (after shading: the relocated one).
	 *
	 * <p>The check is by class identity on purpose: a Jackson annotation brought along by a custom command (same or
	 * different package, same suffix) is a different type, is not understood by the shaded output mapper and must
	 * therefore not count as serializable.
	 *
	 * @param clazz the class to inspect; must not be {@code null}
	 * @return {@code true} if the class is annotated with this Jackson's {@link JsonSerialize}
	 */
	public static boolean hasJsonSerializeAnnotation(@NotNull final Class<?> clazz) {
		return clazz.isAnnotationPresent(JsonSerialize.class);
	}

}
