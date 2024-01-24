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

package com.espirit.moddev.cli.commands.script.parseCommand;

import com.espirit.moddev.cli.api.script.ScriptEngine;
import com.espirit.moddev.cli.api.script.exception.ScriptExecutionException;
import com.espirit.moddev.cli.api.script.exception.ScriptParsingException;
import com.espirit.moddev.cli.commands.script.ScriptCommandGroup;
import com.espirit.moddev.cli.commands.script.ScriptCommandNames;
import com.espirit.moddev.cli.commands.script.common.AbstractScriptCommand;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.help.Examples;
import de.espirit.firstspirit.access.Connection;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

@Command(name = ScriptCommandNames.PARSE, groupNames = ScriptCommandGroup.NAME, description = "Parses a script by using the given script engine.\n\n**NOTE**\nThe reserved parameter *context* can be used to access the FirstSpirit API - the context is a [ProjectScriptContext](https://docs.e-spirit.com/odfs/access/de/espirit/firstspirit/access/project/ProjectScriptContext.html).\n\nSimple example beanshell script with access to the context:\n`context.logInfo(\"ProjectId: \" + context.getProject().getId());`")
@Examples(
		examples = {
				"script parse -sf \"script.bsh\"",
				"script parse -se \"beanshell\" -sf \"script.bsh\"",
		},
		descriptions = {
				"Parses the given script file with the default script engine.",
				"Parses the given script file with beanshell script engine.",
		}
)
public class ParseScriptCommand extends AbstractScriptCommand {

	@Override
	protected void executeCommand(@NotNull final Connection connection, @NotNull final ScriptEngine scriptEngine, @NotNull final Path scriptPath, @NotNull final String scriptSource) throws ScriptParsingException, ScriptExecutionException {
		// parse script
		_logger.info("Parsing script '{}'...", scriptPath.toAbsolutePath());
		scriptEngine.getExecutable(scriptSource).parse(createScriptContext(connection));
		_logger.info("Script '{}' successfully parsed without errors!", scriptPath.toAbsolutePath());
	}

}
