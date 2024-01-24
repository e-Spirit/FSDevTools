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

package com.espirit.moddev.cli.commands.script.common.beanshell;

import com.espirit.moddev.cli.api.script.ScriptEngine;
import com.espirit.moddev.cli.api.script.ScriptExecutable;
import org.jetbrains.annotations.NotNull;

/**
 * {@link ScriptEngine} implementation for Beanshell scripts.
 */
public class BeanshellScriptEngine implements ScriptEngine {

	/**
	 * The name of the Beanshell script engine.
	 */
	public static final String NAME = "BEANSHELL";

	private final ClassLoader _classLoader;

	public BeanshellScriptEngine() {
		_classLoader = BeanshellScriptEngine.class.getClassLoader();
	}

	@NotNull
	@Override
	public String getName() {
		return NAME;
	}

	@NotNull
	@Override
	public ScriptExecutable getExecutable(@NotNull final String scriptSource) {
		return new BeanshellScriptExecutable(_classLoader, scriptSource);
	}

}
