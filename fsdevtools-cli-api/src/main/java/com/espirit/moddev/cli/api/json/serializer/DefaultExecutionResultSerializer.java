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

package com.espirit.moddev.cli.api.json.serializer;

import com.espirit.moddev.cli.api.result.ExecutionResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_MESSAGE;

public class DefaultExecutionResultSerializer extends StdSerializer<ExecutionResult> {

	public DefaultExecutionResultSerializer() {
		this(null);
	}

	protected DefaultExecutionResultSerializer(@Nullable final Class<ExecutionResult> clazz) {
		super(clazz);
	}

	@Override
	public void serialize(@NotNull final ExecutionResult value, @NotNull final JsonGenerator generator, @NotNull final SerializationContext ctxt) {
		generator.writeStartObject();
		{
			generator.writeStringProperty(ATTR_MESSAGE, value.toString());
		}
		generator.writeEndObject();
	}

}
