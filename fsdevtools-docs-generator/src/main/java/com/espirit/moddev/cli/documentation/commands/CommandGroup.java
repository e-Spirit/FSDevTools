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

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CommandGroup {

	private final String _name;
	private final List<CommandDocumentationInfo> _commands;

	public CommandGroup(@NotNull final String name) {
		_name = name;
		_commands = new ArrayList<>();
	}

	@NotNull
	public String getName() {
		return _name;
	}

	@NotNull
	public Collection<CommandDocumentationInfo> getCommands() {
		return Collections.unmodifiableCollection(_commands);
	}

	public void addCommand(@NotNull final CommandDocumentationInfo command) {
		_commands.add(command);
	}

}
