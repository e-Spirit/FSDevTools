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

import com.espirit.moddev.cli.commands.module.configureCommand.json.components.Service;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_AUTO_START;
import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_FILES;
import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_RESTART;
import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_SERVICE_NAME;

public class ServiceSerializer extends StdSerializer<Service> {

	public ServiceSerializer() {
		this(null);
	}

	protected ServiceSerializer(final Class<Service> clazz) {
		super(clazz);
	}

	@Override
	public void serialize(@NotNull final Service value, @NotNull final JsonGenerator generator, @NotNull final SerializerProvider provider) throws IOException {
		generator.writeStartObject();
		generator.writeStringField(ATTR_SERVICE_NAME, value.getServiceName());
		if (value.getAutoStart()) {
			generator.writeBooleanField(ATTR_AUTO_START, value.getAutoStart());
		}
		if (value.getRestart()) {
			generator.writeBooleanField(ATTR_RESTART, value.getRestart());
		}
		if (!value.getFiles().isEmpty()) {
			generator.writeObjectField(ATTR_FILES, value.getFiles());
		}
		generator.writeEndObject();
	}

}
