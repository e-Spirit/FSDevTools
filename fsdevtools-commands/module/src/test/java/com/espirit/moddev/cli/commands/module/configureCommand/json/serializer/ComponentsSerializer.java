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

package com.espirit.moddev.cli.commands.module.configureCommand.json.serializer;

import com.espirit.moddev.cli.commands.module.configureCommand.json.components.Components;
import org.jetbrains.annotations.NotNull;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_PROJECT_COMPONENTS;
import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_SERVICES;
import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_WEB_COMPONENTS;

public class ComponentsSerializer extends StdSerializer<Components> {

	public ComponentsSerializer() {
		this(null);
	}

	protected ComponentsSerializer(final Class<Components> clazz) {
		super(clazz);
	}

	@Override
	public void serialize(@NotNull final Components value, @NotNull final JsonGenerator generator, @NotNull final SerializationContext ctxt) {
		generator.writeStartObject();
		generator.writePOJOProperty(ATTR_WEB_COMPONENTS, value.getWebComponents());
		generator.writePOJOProperty(ATTR_PROJECT_COMPONENTS, value.getProjectComponents());
		generator.writePOJOProperty(ATTR_SERVICES, value.getServices());
		generator.writeEndObject();
	}

}
