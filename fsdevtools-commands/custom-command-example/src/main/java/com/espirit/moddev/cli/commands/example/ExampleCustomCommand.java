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

package com.espirit.moddev.cli.commands.example;

import com.espirit.moddev.cli.api.command.Command;
import com.espirit.moddev.cli.api.result.Result;
import com.github.rvesse.airline.annotations.help.Examples;

/**
 * This is an example command, that shows how custom command implementations can be done.
 */
@com.github.rvesse.airline.annotations.Command(groupNames = ExampleCustomGroup.NAME, name = "example-custom-command", description = "This is a custom command example implementation.")
@Examples(
		examples = {
				"example-custom-command"
		},
		descriptions = {
				"Executes the example command"
		}
)
public class ExampleCustomCommand implements Command<ExampleCustomCommand.MyResult> {

	@Override
	public MyResult call() throws Exception {
		return new MyResult();
	}

	/**
	 * This is an example implementation for a custom result class that does pretty much nothing.
	 */
	public static class MyResult implements Result<Boolean> {

		@Override
		public boolean isError() {
			return false;
		}

		@Override
		public Exception getError() {
			return null;
		}

		@Override
		public Boolean get() {
			return Boolean.TRUE;
		}

	}

}
