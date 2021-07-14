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

package com.espirit.moddev.cli.commands.module.configureCommand.json.serializer;

import com.espirit.moddev.cli.commands.module.configureCommand.json.components.Components;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

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
	public void serialize(@NotNull final Components value, @NotNull final JsonGenerator generator, @NotNull final SerializerProvider provider) throws IOException {
		generator.writeStartObject();
		generator.writeObjectField(ATTR_WEB_COMPONENTS, value.getWebComponents());
		generator.writeObjectField(ATTR_PROJECT_COMPONENTS, value.getProjectComponents());
		generator.writeObjectField(ATTR_SERVICES, value.getServices());
		generator.writeEndObject();
	}

}
