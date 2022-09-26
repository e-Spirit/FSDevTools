/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2022 Crownpeak Technology GmbH
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

import com.espirit.moddev.shared.webapp.WebAppIdentifier;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class WebAppIdentifierSerializer extends StdSerializer<WebAppIdentifier> {

	public WebAppIdentifierSerializer() {
		this(null);
	}

	protected WebAppIdentifierSerializer(final Class<WebAppIdentifier> clazz) {
		super(clazz);
	}

	@Override
	public void serialize(@Nullable final WebAppIdentifier value, @NotNull final JsonGenerator generator, @NotNull final SerializerProvider provider) throws IOException {
		generator.writeString(value == null ? "null" : value.toString());
	}

}
