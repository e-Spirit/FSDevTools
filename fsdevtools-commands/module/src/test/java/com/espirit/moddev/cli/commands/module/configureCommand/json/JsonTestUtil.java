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

package com.espirit.moddev.cli.commands.module.configureCommand.json;

import com.espirit.moddev.cli.commands.module.configureCommand.json.components.ComponentProjectApps;
import com.espirit.moddev.cli.commands.module.configureCommand.json.components.ComponentWebApps;
import com.espirit.moddev.cli.commands.module.configureCommand.json.components.Components;
import com.espirit.moddev.cli.commands.module.configureCommand.json.components.Service;
import com.espirit.moddev.cli.commands.module.configureCommand.json.serializer.ComponentProjectAppsSerializer;
import com.espirit.moddev.cli.commands.module.configureCommand.json.serializer.ComponentWebAppsSerializer;
import com.espirit.moddev.cli.commands.module.configureCommand.json.serializer.ComponentsSerializer;
import com.espirit.moddev.cli.commands.module.configureCommand.json.serializer.ModuleConfigurationSerializer;
import com.espirit.moddev.cli.commands.module.configureCommand.json.serializer.ServiceSerializer;
import com.espirit.moddev.cli.commands.module.configureCommand.json.serializer.WebAppIdentifierSerializer;
import com.espirit.moddev.shared.webapp.WebAppIdentifier;
import com.espirit.moddev.util.JacksonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class JsonTestUtil {

	protected static final ObjectMapper OBJECT_MAPPER = createMapper();

	@NotNull
	public static Map.Entry<String, Object> createEntry(@NotNull final String key, @Nullable final Object value) {
		return new Map.Entry<String, Object>() {

			private final String _key = key;
			private Object _value = value;

			@Override
			public String getKey() {
				return _key;
			}

			@Override
			public Object getValue() {
				return _value;
			}

			@Override
			public Object setValue(@Nullable final Object value) {
				final Object oldValue = _value;
				_value = value;
				return oldValue;
			}
		};
	}

	@SafeVarargs
	@NotNull
	public static Map<String, Object> createMap(@NotNull Map.Entry<String, Object>... entries) {
		final Map<String, Object> map = new HashMap<>();
		for (final Map.Entry<String, Object> entry : entries) {
			map.put(entry.getKey(), entry.getValue());
		}
		return map;
	}

	@NotNull
	public static String toJsonObject(@NotNull final Object object) {
		try {
			return OBJECT_MAPPER.writeValueAsString(object);
		} catch (final JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	@NotNull
	public static ObjectMapper createMapper() {
		final ObjectMapper objectMapper = JacksonUtil.createInputMapper();
		// register custom serializers for tests
		final SimpleModule module = new SimpleModule();
		module.addSerializer(WebAppIdentifier.class, new WebAppIdentifierSerializer());
		module.addSerializer(ModuleConfiguration.class, new ModuleConfigurationSerializer());
		module.addSerializer(Components.class, new ComponentsSerializer());
		module.addSerializer(ComponentWebApps.class, new ComponentWebAppsSerializer());
		module.addSerializer(ComponentWebApps.WebApp.class, new ComponentWebAppsSerializer.WebAppSerializer());
		module.addSerializer(ComponentProjectApps.class, new ComponentProjectAppsSerializer());
		module.addSerializer(ComponentProjectApps.ProjectApp.class, new ComponentProjectAppsSerializer.ProjectAppSerializer());
		module.addSerializer(Service.class, new ServiceSerializer());
		objectMapper.registerModule(module);
		// configure serialization
		objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
		return objectMapper;
	}

}
