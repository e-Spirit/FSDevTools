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

import com.espirit.moddev.cli.documentation.DocumentationInfo;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommandDocumentationInfo implements DocumentationInfo {

	public static final String GLOBAL_GROUP_NAME = "GLOBAL";

	private final String _name;
	private final String _description;
	private final List<String> _groupNames;
	private final List<ExamplesInfo> _examples;
	private final List<CommandParameterInfo> _parameters;

	public CommandDocumentationInfo(@NotNull final String name, @NotNull final String description, @NotNull final List<String> groupNames, @NotNull final List<ExamplesInfo> examples, @NotNull final List<CommandParameterInfo> parameters) {
		_name = name;
		_description = description;
		_examples = examples;
		_groupNames = groupNames;
		_parameters = parameters;
		_parameters.sort((first, second) -> {
			if (first.isGlobal() != second.isGlobal()) {
				if (first.isGlobal()) {
					return -1;
				} else {
					return +1;
				}
			}
			return first.getNames().get(0).compareTo(second.getNames().get(0));
		});
	}

	@NotNull
	public String getName() {
		return _name;
	}

	@NotNull
	public String getDescription() {
		return _description;
	}

	@NotNull
	public List<String> getGroupNames() {
		if (_groupNames.isEmpty()) {
			final ArrayList<String> emptyGroups = new ArrayList<>();
			emptyGroups.add(GLOBAL_GROUP_NAME);
			return emptyGroups;
		}
		return _groupNames;
	}

	@NotNull
	public List<ExamplesInfo> getExamples() {
		return _examples;
	}

	@NotNull
	public List<CommandParameterInfo> getParameters(final boolean global) {
		return _parameters.stream().filter(commandParameterInfo -> global == commandParameterInfo.isGlobal()).collect(Collectors.toList());
	}

}
