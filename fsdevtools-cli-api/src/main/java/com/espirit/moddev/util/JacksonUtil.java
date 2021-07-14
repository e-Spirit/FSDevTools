/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2021 e-Spirit AG
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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class for things related to json.
 */
public class JacksonUtil {

	/**
	 * Creates a preconfigured {@link ObjectMapper} and returns it. The returned mapper should only be used for deserialization purposes.<br/>
	 * <br/>
	 * The returned mapper will be configured as following:<br/>
	 * <b>Enabled features:</b>
	 * <ul>
	 *     <li>{@link DeserializationFeature#FAIL_ON_MISSING_CREATOR_PROPERTIES}</li>
	 *     <li>{@link DeserializationFeature#FAIL_ON_UNKNOWN_PROPERTIES}</li>
	 * </ul>
	 *
	 * @return a pre-configured {@link ObjectMapper} for deserialization purposes
	 */
	@NotNull
	public static ObjectMapper createInputMapper() {
		final ObjectMapper objectMapper = new ObjectMapper();
		// configure deserialization
		objectMapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, true);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return objectMapper;
	}

	/**
	 * Creates a preconfigured {@link ObjectMapper} and returns it. The returned mapper should only be used for serialization purposes.<br/>
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
	 *     <li>{@link MapperFeature#AUTO_DETECT_CREATORS}</li>
	 *     <li>{@link MapperFeature#AUTO_DETECT_FIELDS}</li>
	 *     <li>{@link MapperFeature#AUTO_DETECT_GETTERS}</li>
	 *     <li>{@link MapperFeature#AUTO_DETECT_IS_GETTERS}</li>
	 * </ul>
	 * <b>Enabled features:</b>
	 * <ul>
	 *     <li>{@link SerializationFeature#INDENT_OUTPUT}</li>
	 *     <li>{@link SerializationFeature#ORDER_MAP_ENTRIES_BY_KEYS}</li>
	 * </ul>
	 *
	 * @return a pre-configured {@link ObjectMapper} for serialization purposes
	 */
	@NotNull
	public static ObjectMapper createOutputMapper() {
		final ObjectMapper objectMapper = new ObjectMapper();
		// register custom serializers
		final SimpleModule module = new SimpleModule();
		module.addSerializer(Exception.class, new ExceptionSerializer());
		objectMapper.registerModule(module);
		// configure serialization
		objectMapper.disable(MapperFeature.AUTO_DETECT_CREATORS,
				MapperFeature.AUTO_DETECT_FIELDS,
				MapperFeature.AUTO_DETECT_GETTERS,
				MapperFeature.AUTO_DETECT_IS_GETTERS);
		// configure serialization
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
		return objectMapper;
	}

}
