/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2021 e-Spirit GmbH
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

import com.espirit.moddev.cli.api.result.ExecutionErrorResult;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_EXCEPTION;
import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_MESSAGE;

public class DefaultExecutionErrorResultSerializer extends StdSerializer<ExecutionErrorResult<?>> {

	public DefaultExecutionErrorResultSerializer() {
		this(null);
	}

	protected DefaultExecutionErrorResultSerializer(@Nullable final Class<ExecutionErrorResult<?>> clazz) {
		super(clazz);
	}

	@Override
	public void serialize(@NotNull final ExecutionErrorResult<?> value, @NotNull final JsonGenerator generator, @NotNull final SerializerProvider provider) throws IOException {
		generator.writeStartObject();
		{
			generator.writeObjectField(ATTR_MESSAGE, value.toString());
			generator.writeObjectField(ATTR_EXCEPTION, value.getException());
		}
		generator.writeEndObject();
	}

}
