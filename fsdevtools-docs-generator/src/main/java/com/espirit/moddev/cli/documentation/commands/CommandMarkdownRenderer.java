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

package com.espirit.moddev.cli.documentation.commands;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CommandMarkdownRenderer implements CommandRenderer {

	protected static final char NEW_LINE = '\n';

	private final CommandDocumentationInfo _command;
	private final StringBuilder _builder;

	public CommandMarkdownRenderer(@NotNull final CommandDocumentationInfo command) {
		_command = command;
		_builder = new StringBuilder();
	}

	@Override
	public void renderName(@NotNull final String groupName) {
		_builder.append("# ");
		if (!groupName.equals("GLOBAL")) {
			_builder.append(groupName).append(" ");
		}
		_builder.append(_command.getName()).append(NEW_LINE);
	}

	@Override
	public void renderDescription() {
		_builder.append('*').append(_command.getDescription()).append('*').append(NEW_LINE);
	}

	@Override
	public void renderExamples(@NotNull final String groupName) {
		_builder.append(NEW_LINE).append("## Examples").append(NEW_LINE).append(NEW_LINE);
		for (final ExamplesInfo example : _command.getExamples()) {
			_builder.append("##### ").append(example.getDescription()).append(NEW_LINE);
			_builder.append("```").append(NEW_LINE).append(example.getExample()).append(NEW_LINE).append("```").append(NEW_LINE);
		}
	}

	@Override
	public void renderParametersTopic(final boolean global) {
		_builder.append(NEW_LINE).append("## ").append(global ? "Global" : "Command").append(" parameters").append(NEW_LINE).append(NEW_LINE);
	}

	@Override
	public void renderParameter(@NotNull final CommandParameterInfo parameter) {
		_builder.append("### ").append(String.join(" , ", parameter.getNames()));
		if (parameter.isRequired()) {
			_builder.append(" `REQUIRED`");
		}
		_builder.append(NEW_LINE);
		_builder.append('*').append(parameter.getDescription()).append('*').append(NEW_LINE);
		_builder.append("* Type: ").append('`').append(parameter.getClassName()).append('`').append(NEW_LINE);
		_builder.append("* Default value: ").append('`').append(parameter.getDefaultValue().orElse("<none>")).append('`').append(NEW_LINE);
		_builder.append("* Possible values: ").append('`').append(parameter.getPossibleValues()).append('`').append(NEW_LINE).append(NEW_LINE);
		renderParameterExamples(parameter.getExamples());
	}

	private void renderParameterExamples(@NotNull final List<ExamplesInfo> examples) {
		if (examples.isEmpty()) {
			return;
		}
		_builder.append("#### ").append("Examples").append(NEW_LINE).append(NEW_LINE);
		for (final ExamplesInfo example : examples) {
			_builder.append("##### ").append(example.getDescription()).append(NEW_LINE).append(NEW_LINE);
			_builder.append("```").append(NEW_LINE).append(example.getExample()).append(NEW_LINE).append("```").append(NEW_LINE);
		}
	}

	@NotNull
	@Override
	public String getText() {
		return _builder.toString();
	}
}
