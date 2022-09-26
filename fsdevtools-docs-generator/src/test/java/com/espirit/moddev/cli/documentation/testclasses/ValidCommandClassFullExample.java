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

package com.espirit.moddev.cli.documentation.testclasses;

import com.espirit.moddev.cli.api.annotations.ParameterExamples;
import com.espirit.moddev.cli.api.annotations.ParameterType;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.help.Examples;
import com.github.rvesse.airline.annotations.restrictions.AllowedRawValues;
import com.github.rvesse.airline.annotations.restrictions.Required;

@Command(name = "ValidCommandClassFullExample", description = "ValidCommandClassFullExampleDescription", groupNames = {"first", "second"})
@Examples(examples =
		{
				"example#1",
				"example#2",
		},
		descriptions = {
				"description#1",
				"description#2",
		})
public class ValidCommandClassFullExample extends AbstractCommandClass {

	private String _nonParameter;

	@Option(type = OptionType.GLOBAL, name = {"--globalParameter"})
	private String _globalParameter;

	@Option(type = OptionType.GROUP, name = {"--invalid"})
	private String _invalidParameterType;

	@Option(type = OptionType.COMMAND, name = {})
	private String _emptyNamesParameter;

	@Option(type = OptionType.COMMAND, name = {"--parameterWithRawValues"})
	@AllowedRawValues(allowedValues = {"VALUE#1", "VALUE#2"})
	private String _parameterWithRawValues;

	@Option(type = OptionType.COMMAND, name = {"--parameterWithTitle"})
	@ParameterType(name = "Title of parameter")
	private String _parameterWithTitle;

	@Option(type = OptionType.COMMAND, name = {"--parameterWithDescription"}, description = "Description")
	private String _parameterWithDescription;

	@Option(type = OptionType.COMMAND, name = {"--parameterWithRequired"})
	@Required
	private String _parameterWithRequired;

	@Option(type = OptionType.COMMAND, name = {"--parameterWithDefaultValue"})
	private String _parameterWithDefaultValue = "defaultValue";

	@Option(type = OptionType.COMMAND, name = {"--parameterWithExamples"})
	@ParameterExamples(examples =
			{
					"example#1",
					"example#2",
			},
			descriptions = {
					"description#1",
					"description#2",
			})
	private Integer _parameterWithExamples;

	@Option(type = OptionType.COMMAND, name = {"--parameterWithMultipleNames", "-pwmn"})
	private Integer _parameterWithMultipleNames;

}

