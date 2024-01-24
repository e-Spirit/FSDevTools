/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2024 Crownpeak Technology GmbH
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

package com.espirit.moddev.cli.commands.module.uninstallCommand;

import com.espirit.moddev.cli.ConnectionBuilder;
import com.espirit.moddev.cli.commands.SimpleCommand;
import com.espirit.moddev.cli.commands.module.ModuleCommandGroup;
import com.espirit.moddev.cli.commands.module.ModuleCommandNames;
import com.espirit.moddev.cli.commands.module.utils.ModuleUninstaller;
import com.espirit.moddev.cli.results.SimpleResult;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.help.Examples;
import com.github.rvesse.airline.annotations.restrictions.Required;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.common.MaximumNumberOfSessionsExceededException;
import de.espirit.firstspirit.server.authentication.AuthenticationException;

import java.io.IOException;

/**
 * Uninstalls a module from a FirstSpirit server. Removes corresponding components and project-specific
 * components for the given project.
 */
@Command(name = ModuleCommandNames.UNINSTALL, groupNames = ModuleCommandGroup.NAME, description = "Uninstalls a FirstSpirit module from a FirstSpirit Server.")
@Examples(
		examples = {
				"module uninstall -h localhost -p 8000 --moduleName \"abtesting\" --projectName \"Mithras Energy\""
		},
		descriptions = {
				"Uninstalls the abtesting module and removes all components from the Mithras Energy project"
		}
)
public class UninstallModuleCommand extends SimpleCommand<SimpleResult<Boolean>> {

	@Option(type = OptionType.COMMAND, name = {"-m", "--moduleName"}, description = "Name of the module that should be deleted", title = "moduleName")
	@Required
	private String _moduleName;

	@Override
	public SimpleResult<Boolean> call() {
		try (Connection connection = create()) {
			connection.connect();
			new ModuleUninstaller().uninstall(connection, _moduleName);
			return new SimpleResult<>(true);
		} catch (IOException | AuthenticationException | MaximumNumberOfSessionsExceededException e) {
			return new SimpleResult<>(e);
		}
	}

	protected Connection create() {
		return ConnectionBuilder.with(this).build();
	}

	@Override
	public boolean needsContext() {
		return false;
	}

}
