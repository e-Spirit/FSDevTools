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

package com.espirit.moddev.util.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_CAUSE;
import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_CLASS;
import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_LOCALIZED_MESSAGE;
import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_MESSAGE;

/**
 * Simple custom {@link JsonSerializer serializer} for {@link Exception exceptions}.<br/>
 * <br/>
 * The following attributes will be written:<br/>
 * <ul>
 *     <li>Class name of the exception</li>
 *     <li>Message of the exception</li>
 *     <li>Localized message of the exception</li>
 *     <li>The cause of the exception (which is another serialized exception)</li>
 * </ul>
 */
public class ExceptionSerializer extends StdSerializer<Exception> {

	public ExceptionSerializer() {
		this(null);
	}

	protected ExceptionSerializer(@Nullable final Class<Exception> clazz) {
		super(clazz);
	}

	@Override
	public void serialize(@NotNull final Exception value, @NotNull final JsonGenerator generator, @NotNull final SerializerProvider provider) throws IOException {
		generator.writeStartObject();
		generator.writeStringField(ATTR_CLASS, value.getClass().getName());
		final String message = value.getMessage();
		if (message != null) {
			generator.writeStringField(ATTR_MESSAGE, message);
		}
		final String localizedMessage = value.getLocalizedMessage();
		if (localizedMessage != null) {
			generator.writeStringField(ATTR_LOCALIZED_MESSAGE, localizedMessage);
		}
		final Throwable cause = value.getCause();
		if (cause != null) {
			generator.writeObjectField(ATTR_CAUSE, cause);
		}
		generator.writeEndObject();
	}

}
