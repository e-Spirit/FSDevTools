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

package com.espirit.moddev.cli.commands.feature.cmd.analyze;

import com.espirit.moddev.cli.api.annotations.ParameterExamples;
import com.espirit.moddev.cli.commands.feature.FeatureCommandGroup;
import com.espirit.moddev.cli.commands.feature.FeatureCommandNames;
import com.espirit.moddev.cli.commands.feature.common.AbstractFeatureCommand;
import com.espirit.moddev.cli.commands.feature.common.ExtendedFeatureAnalyseResult;
import com.espirit.moddev.cli.commands.feature.common.FeatureHelper;
import com.espirit.moddev.cli.commands.feature.common.FsObjectsLoggingFormatHelper;
import com.espirit.moddev.shared.annotation.VisibleForTesting;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.help.Examples;
import com.github.rvesse.airline.annotations.restrictions.Path;
import com.github.rvesse.airline.annotations.restrictions.PathKind;
import com.github.rvesse.airline.annotations.restrictions.Required;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.project.Project;
import org.jetbrains.annotations.NotNull;

import java.io.File;

@com.github.rvesse.airline.annotations.Command(
		groupNames = FeatureCommandGroup.NAME,
		name = FeatureCommandNames.ANALYZE,
		description = "Analyze feature archive server side in the context of the specified FirstSpirit project and report errors e.g. missing store objects."
)
@Examples(
		examples = {
				FeatureCommandGroup.NAME + " " + FeatureCommandNames.ANALYZE + " --file \"C:/path/to/feature-file.zip\"",
		},
		descriptions = {
				"Send the \"C:/path/to/feature-file.zip\" feature archive to the FirstSpirit server, analyze it in the context of the specified FirstSpirit project and report any problems/errors/inconsistencies in it.",
		}
)
public class FeatureAnalyzeCommand extends AbstractFeatureCommand {

	@NotNull
	private final FeatureHelper _featureHelper = new FeatureHelper();

	@NotNull
	private final FsObjectsLoggingFormatHelper _fsObjectsLoggingFormatHelper = new FsObjectsLoggingFormatHelper();

	@Required
	@Option(
			type = OptionType.COMMAND,
			arity = 1,
			name = {"-f", "--file"},
			description = "Path to feature archive which will be sent to the FirstSpirit server for analysis in the context of the specified FirstSpirit project."
	)
	@Path(kind = PathKind.FILE, mustExist = true, writable = false)
	@ParameterExamples(
			examples = {
					"-f \"path/to/feature-file.zip\"",
					"--file \"C:/path/to/feature-file.zip\"",
			},
			descriptions = {
					"Example of a relative file path to a feature archive",
					"Example of an absolute file path to a feature archive",
			}
	)
	@NotNull // required CLI parameter so Airline should do the nullability check before setting it
	private String _pathToFeatureZip = "";

	@NotNull
	@VisibleForTesting
	FeatureHelper getFeatureHelper() {
		return _featureHelper;
	}

	@NotNull
	@VisibleForTesting
	FsObjectsLoggingFormatHelper getFsObjectsLoggingFormatHelper() {
		return _fsObjectsLoggingFormatHelper;
	}

	@NotNull
	@VisibleForTesting
	String getPathToFeatureZip() {
		return _pathToFeatureZip;
	}

	@Override
	public void execute(@NotNull final Connection connection, @NotNull final Project project) throws Exception {
		final File file = new File(getPathToFeatureZip());
		final ExtendedFeatureAnalyseResult featureAnalyseResult = getFeatureHelper().getFeatureAnalyseResult(connection, project, file);
		getFsObjectsLoggingFormatHelper().logFeatureAnalyseResult(featureAnalyseResult);
	}

}
