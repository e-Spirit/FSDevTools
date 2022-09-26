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

package com.espirit.moddev.cli.commands.module.installCommand;

import com.espirit.moddev.cli.results.SimpleResult;

/**
 * Specialization of the generic {@link SimpleResult} class that holds a module name.
 *
 * @author e-Spirit GmbH
 */
public class InstallModuleResult extends SimpleResult<String> {

	public InstallModuleResult(String result) {
		super(result);
	}

	public InstallModuleResult(final String pathToFsm, Exception exception) {
		super(pathToFsm, exception);
	}

	public String getModuleName() {
		return result;
	}

	@Override
	public void log() {
		if (isError()) {
			SimpleResult.LOGGER.error("There was an error installing the module '" + getModuleName() + "'");
		} else {
			SimpleResult.LOGGER.info("Module installed successfully. ModuleName: '" + getModuleName() + "'");
		}
	}
}
