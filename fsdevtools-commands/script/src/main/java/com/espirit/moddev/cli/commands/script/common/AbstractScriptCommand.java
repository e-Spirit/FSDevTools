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

package com.espirit.moddev.cli.commands.script.common;

import com.espirit.moddev.cli.ConnectionBuilder;
import com.espirit.moddev.cli.api.annotations.ParameterExamples;
import com.espirit.moddev.cli.api.script.ScriptEngine;
import com.espirit.moddev.cli.api.script.exception.ScriptExecutionException;
import com.espirit.moddev.cli.api.script.exception.ScriptParsingException;
import com.espirit.moddev.cli.commands.SimpleCommand;
import com.espirit.moddev.cli.commands.script.common.beanshell.BeanshellScriptEngine;
import com.espirit.moddev.cli.results.SimpleResult;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.restrictions.PathKind;
import com.github.rvesse.airline.annotations.restrictions.Required;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractScriptCommand extends SimpleCommand<SimpleResult<Boolean>> {

	@VisibleForTesting
	static final String ATTR_CONTEXT = "context";

	protected final Logger _logger;

	@Option(type = OptionType.COMMAND, name = {"-se", "--scriptEngine"}, description = "The name of the script engine to use. Default is " + BeanshellScriptEngine.NAME + ".", title = "scriptEngine")
	private String _engineName = BeanshellScriptEngine.NAME;

	@Arguments(title = "parameters", description = "A list of various parsable parameters in the form <key>=<value>")
	@ParameterExamples(examples = {
			"-- singleKey=mykey",
			"-- name=MyName \"text=my text edit\"",
	}, descriptions = {
			"A single additional parameter",
			"A list of additional parameters"
	})
	private final List<String> _parameterList = new ArrayList<>();

	@Option(type = OptionType.COMMAND, name = {"-sf", "--scriptFile"}, description = "Path to the script file.", title = "scriptFile")
	@com.github.rvesse.airline.annotations.restrictions.Path(mustExist = true, kind = PathKind.FILE, writable = false)
	@Required
	@ParameterExamples(
			examples = {
					"-sf \"path/to/file.bsh\"",
					"--scriptFile \"C:/path/to/file.bsh\"",
			},
			descriptions = {
					"Sets the script file to `path/to/file.bsh`.",
					"Sets the script file to `C:/path/to/file.bsh`.",
			}
	)
	private String _scriptFile;

	public AbstractScriptCommand() {
		_logger = LoggerFactory.getLogger(getClass());
	}

	protected abstract void executeCommand(@NotNull final Connection connection, @NotNull final ScriptEngine scriptEngine, @NotNull final Path scriptPath, @NotNull final String scriptSource) throws ScriptParsingException, ScriptExecutionException;

	@Override
	public final SimpleResult<Boolean> call() {
		final Path scriptPath = new java.io.File(_scriptFile).toPath();
		try (final Connection connection = ConnectionBuilder.with(this).build()) {
			connection.connect();
			// initialize script engines
			_logger.debug("Initializing script engines registry...");
			final ScriptEngineRegistry scriptEngineRegistry = new ScriptEngineRegistry();
			scriptEngineRegistry.initialize();
			// get script engine
			_logger.debug("Retrieving script engine '{}'...", _engineName);
			final ScriptEngine scriptEngine = scriptEngineRegistry.requireEngine(_engineName);
			// read script file
			_logger.debug("Reading script source from '{}'...", _scriptFile);
			final String scriptSource = Files.readString(scriptPath);

			executeCommand(connection, scriptEngine, scriptPath, scriptSource);
			return new SimpleResult<>(true);
		} catch (final Exception e) {
			_logger.error("Error during script command command '{}': {}", scriptPath.toAbsolutePath(), e.getMessage());
			return new SimpleResult<>(e);
		}
	}

	@Override
	public final boolean needsContext() {
		return false;
	}

	@NotNull
	protected Map<String, Object> createScriptContext(@NotNull final Connection connection) {
		final Map<String, Object> context = new HashMap<>();
		final String projectName = getProject();
		Project project = null;
		if (projectName != null) {
			project = connection.getProjectByName(projectName);
			if (project == null) {
				throw new IllegalStateException(String.format("Project '%s' not found (typo in the project name?).", projectName));
			}
		}
		final CLIScriptContext scriptContext = new CLIScriptContext(connection, project, parseParameterList(_parameterList));
		context.put(ATTR_CONTEXT, scriptContext);
		return context;
	}

	@VisibleForTesting
	@NotNull
	Map<String, String> parseParameterList(@NotNull final List<String> parameterList) {
		final Map<String, String> result = new HashMap<>();
		parameterList.forEach(parameter -> {
			final int firstIndex = parameter.indexOf("=");
			if (firstIndex == -1) {
				_logger.warn("Ignoring malformed parameter: {}", parameter);
				return;
			}
			final String name = parameter.substring(0, firstIndex);
			final String value = parameter.substring(firstIndex + 1);
			result.put(name, value);
		});
		return result;
	}

}
