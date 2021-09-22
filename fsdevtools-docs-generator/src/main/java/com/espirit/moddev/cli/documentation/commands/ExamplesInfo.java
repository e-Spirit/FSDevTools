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

package com.espirit.moddev.cli.documentation.commands;

import com.espirit.moddev.cli.documentation.pojos.JsonElement;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

public class ExamplesInfo implements JsonElement {

	@JsonProperty("description")
	private final String _description;

	@JsonProperty("text")
	private final String _example;

	public ExamplesInfo(@NotNull final String description, @NotNull final String example) {
		_description = description;
		_example = example;
	}

	@NotNull
	public String getDescription() {
		return _description;
	}

	@NotNull
	public String getExample() {
		return _example;
	}

}
