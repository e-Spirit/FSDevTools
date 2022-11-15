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

import com.espirit.moddev.cli.commands.module.configureCommand.json.components.ComponentWebApps;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_COMPONENT_NAME;
import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_DEPLOY;
import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_FILES;
import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_PROJECT_NAME;
import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_WEB_APPS;
import static com.espirit.moddev.cli.api.json.common.AttributeNames.ATTR_WEB_APP_NAME;

public class ComponentWebAppsSerializer extends StdSerializer<ComponentWebApps> {

	public ComponentWebAppsSerializer() {
		this(null);
	}

	protected ComponentWebAppsSerializer(final Class<ComponentWebApps> clazz) {
		super(clazz);
	}

	@Override
	public void serialize(@NotNull final ComponentWebApps value, @NotNull final JsonGenerator generator, @NotNull final SerializerProvider provider) throws IOException {
		generator.writeStartObject();
		generator.writeStringField(ATTR_COMPONENT_NAME, value.getComponentName());
		generator.writeObjectField(ATTR_WEB_APPS, value.getWebApps());
		generator.writeEndObject();
	}

	public static class WebAppSerializer extends StdSerializer<ComponentWebApps.WebApp> {

		public WebAppSerializer() {
			this(null);
		}

		protected WebAppSerializer(final Class<ComponentWebApps.WebApp> clazz) {
			super(clazz);
		}

		@Override
		public void serialize(@NotNull final ComponentWebApps.WebApp value, @NotNull final JsonGenerator generator, @NotNull final SerializerProvider provider) throws IOException {
			generator.writeStartObject();
			generator.writeStringField(ATTR_WEB_APP_NAME, value.getWebAppName().toString());
			if (value.getRawProjectName() != null) {
				generator.writeStringField(ATTR_PROJECT_NAME, value.getRawProjectName());
			}
			if (!value.getDeploy()) {
				generator.writeBooleanField(ATTR_DEPLOY, value.getDeploy());
			}
			generator.writeObjectField(ATTR_FILES, value.getFiles());
			generator.writeEndObject();
		}

	}
}
