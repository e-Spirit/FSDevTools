/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2026 Crownpeak Technology GmbH
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

package com.espirit.moddev.cli.classloading.probe;

import com.espirit.moddev.cli.api.command.Command;
import com.espirit.moddev.cli.api.result.Result;
import com.espirit.moddev.cli.configuration.GlobalConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.rvesse.airline.annotations.help.Examples;
import tools.jackson.databind.annotation.JsonSerialize;

/**
 * Fixture plugin command with a result class annotated with the unrelocated Jackson annotations, as a plugin author
 * might write it. The CLI's Jackson is relocated, so these annotations cannot be loaded and have no effect.
 */
@com.github.rvesse.airline.annotations.Command(name = "probe-annotated-result", description = "Returns a result that is annotated with Jackson annotations.")
@Examples(examples = {"probe-annotated-result"}, descriptions = {"Does not write the result file, logs a warning"})
public class ProbeAnnotatedResultCommand extends GlobalConfig implements Command<ProbeAnnotatedResultCommand.ProbeResult> {

	@Override
	public boolean needsContext() {
		return false;
	}

	@Override
	public ProbeResult call() {
		return new ProbeResult();
	}

	public static class ProbeResult implements Result<ProbeValue> {

		@Override
		public boolean isError() {
			return false;
		}

		@Override
		public Throwable getError() {
			return null;
		}

		@Override
		public ProbeValue get() {
			return new ProbeValue();
		}

	}

	@JsonSerialize
	public static class ProbeValue {

		@JsonProperty("renamed")
		private final String _renamed = "value";

		public String getPlain() {
			return "plain";
		}

	}

}
