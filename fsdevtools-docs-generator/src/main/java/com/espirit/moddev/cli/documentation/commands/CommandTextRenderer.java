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

package com.espirit.moddev.cli.documentation.commands;

import com.espirit.moddev.cli.documentation.pojos.JsonCommandExample;
import com.espirit.moddev.cli.documentation.pojos.JsonCommandText;
import com.espirit.moddev.cli.documentation.pojos.JsonElement;
import com.espirit.moddev.cli.documentation.pojos.JsonElementHolder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CommandTextRenderer implements CommandRenderer, JsonElementHolder {

	private final CommandDocumentationInfo _command;
	private final List<JsonElement> _content = new ArrayList<>();

	public CommandTextRenderer(@NotNull final CommandDocumentationInfo command) {
		_command = command;
	}

	@Override
	public void renderName(@NotNull final String groupName) {
		_content.add(new JsonCommandText("COMMAND_HEADER", !groupName.equals("GLOBAL") ? groupName + " " + _command.getName() : _command.getName()));
	}

	@Override
	public void renderDescription() {
		_content.add(new JsonCommandText("COMMAND_DESCRIPTION",_command.getDescription()));
	}

	@Override
	public void renderExamples(@NotNull final String groupName) {
		for (final ExamplesInfo example : _command.getExamples()) {
			_content.add(new JsonCommandExample(example.getDescription(), example.getExample()));
		}
	}

	@Override
	public void renderParametersTopic(final boolean global) {
	}

	@Override
	public void renderParameter(@NotNull final CommandParameterInfo parameter) {
		_content.add(parameter);
	}

	@NotNull
	@Override
	public String getText() {
		return "";
	}

	@NotNull
	@Override
	public Collection<JsonElement> getJsonElements() {
		return _content;
	}
}
