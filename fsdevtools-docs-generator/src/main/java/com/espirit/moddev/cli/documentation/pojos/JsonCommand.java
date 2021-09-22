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

package com.espirit.moddev.cli.documentation.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class JsonCommand implements JsonElement {

	@NotNull
	@JsonProperty("name")
	private final String name;

	@NotNull
	@JsonProperty("group")
	private final String group;

	@NotNull
	@JsonProperty("elements")
	private final List<JsonElement> elements;

	public JsonCommand(@NotNull final String name, @NotNull final String group, @NotNull final Collection<JsonElement> elements) {
		this.name = name;
		this.group = group;
		this.elements = new ArrayList<>(elements);
	}

}