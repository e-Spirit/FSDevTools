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

package com.espirit.moddev.cli.commands.script.runCommand;

import com.espirit.moddev.cli.api.script.ScriptEngine;
import com.espirit.moddev.cli.api.script.ScriptExecutable;
import com.espirit.moddev.cli.api.script.exception.ScriptExecutionException;
import com.espirit.moddev.cli.api.script.exception.ScriptParsingException;
import com.espirit.moddev.cli.commands.script.ScriptCommandGroup;
import com.espirit.moddev.cli.commands.script.ScriptCommandNames;
import com.espirit.moddev.cli.commands.script.common.AbstractScriptCommand;
import com.espirit.moddev.cli.commands.script.common.LineReadingOutputStream;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.help.Examples;
import de.espirit.firstspirit.access.Connection;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Map;

@Command(name = ScriptCommandNames.RUN, groupNames = ScriptCommandGroup.NAME, description = "Execute a script by using the given script engine.\n\n**NOTE**\nThe reserved parameter *context* can be used to access the FirstSpirit API - the context is a [ProjectScriptContext](https://docs.e-spirit.com/odfs/access/de/espirit/firstspirit/access/project/ProjectScriptContext.html).\n\nSimple example beanshell script with access to the context:\n`context.logInfo(\"ProjectId: \" + context.getProject().getId());`")
@Examples(
		examples = {
				"script run -sf \"script.bsh\"",
				"script run -se \"beanshell\" -sf \"script.bsh\"",
		},
		descriptions = {
				"Executes the given script file with the default script engine.",
				"Executes the given script file with the beanshell script engine.",
		}
)
public class RunScriptCommand extends AbstractScriptCommand {

	private static final Logger LOGGER = LoggerFactory.getLogger(RunScriptCommand.class);

	@Override
	protected void executeCommand(@NotNull final Connection connection, @NotNull final ScriptEngine scriptEngine, final Path scriptPath, @NotNull final String scriptSource) throws ScriptParsingException, ScriptExecutionException {
		// create script context
		_logger.debug("Creating script context...");
		final Map<String, Object> scriptContext = createScriptContext(connection);
		_logger.debug("Creating script executable...");
		// execute script
		final ScriptExecutable executable = scriptEngine.getExecutable(scriptSource);
		_logger.info("Executing script '{}'...", scriptPath.toAbsolutePath());
		final PrintStream out = System.out;
		final PrintStream err = System.err;
		try {
			System.setOut(new PrintStream(new LineReadingOutputStream(LOGGER::info)));
			System.setErr(new PrintStream(new LineReadingOutputStream(LOGGER::error)));
			executable.execute(scriptContext);
		} finally {
			System.setOut(out);
			System.setErr(err);
		}
		_logger.info("Script '{}' successfully executed!", scriptPath.toAbsolutePath());
	}

}
