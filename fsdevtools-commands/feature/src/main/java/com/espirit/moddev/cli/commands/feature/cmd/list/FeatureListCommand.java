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

package com.espirit.moddev.cli.commands.feature.cmd.list;

import com.espirit.moddev.cli.commands.feature.FeatureCommandGroup;
import com.espirit.moddev.cli.commands.feature.FeatureCommandNames;
import com.espirit.moddev.cli.commands.feature.common.AbstractFeatureCommand;
import com.espirit.moddev.cli.commands.feature.common.FeatureHelper;
import com.espirit.moddev.cli.commands.feature.common.FsObjectsLoggingFormatHelper;
import org.jetbrains.annotations.VisibleForTesting;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.help.Examples;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.feature.FeatureAgent;
import de.espirit.firstspirit.feature.FeatureDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@com.github.rvesse.airline.annotations.Command(
		groupNames = FeatureCommandGroup.NAME,
		name = FeatureCommandNames.LIST,
		description = "List features available in the specified FirstSpirit project. The resulting list will be sorted by the name of the feature. Additionally, they are going to be deduplicated based on their revisions. Among multiple features having the same name, the one is going to be shown, which has the most recent revision."
)
@Examples(
		examples = {
				FeatureCommandGroup.NAME + " " + FeatureCommandNames.LIST,
				FeatureCommandGroup.NAME + " " + FeatureCommandNames.LIST + " --filter \"my test feature\"",
		},
		descriptions = {
				"List features available in the specified FirstSpirit project.",
				"List features available in the specified FirstSpirit project containing \"my test feature\" in their names (case is ignored).",
		}
)
public class FeatureListCommand extends AbstractFeatureCommand {

	private static final Logger LOGGER = LoggerFactory.getLogger(FeatureListCommand.class);

	@NotNull
	private final FeatureHelper _featureHelper = new FeatureHelper();

	@NotNull
	private final FsObjectsLoggingFormatHelper _fsObjectsLoggingFormatHelper = new FsObjectsLoggingFormatHelper();

	@Option(
			type = OptionType.COMMAND,
			arity = 1,
			name = {"-f", "--filter"},
			description = "Filter the results based on the given input string. It will check if a feature name contains the given string (ignoring the case)."
	)
	@Nullable
	private String _filter;

	@VisibleForTesting
	@NotNull
	FeatureHelper getFeatureHelper() {
		return _featureHelper;
	}

	@VisibleForTesting
	@NotNull
	FsObjectsLoggingFormatHelper getFsObjectsLoggingFormatHelper() {
		return _fsObjectsLoggingFormatHelper;
	}

	@Override
	protected void execute(@NotNull final Connection connection, @NotNull final Project project) {
		final FeatureHelper featureHelper = getFeatureHelper();
		final FeatureAgent featureAgent = featureHelper.getFeatureAgent(connection, project);
		List<FeatureDescriptor> featureDescriptors = featureHelper.getFeatureDescriptors(featureAgent);
		if (_filter != null) {
			final String filter = _filter.toLowerCase(Locale.ROOT);
			featureDescriptors = featureDescriptors.stream()
					.filter(featureDescriptor -> featureDescriptor.getFeatureName().toLowerCase(Locale.ROOT).contains(filter))
					.collect(Collectors.toList());
		}
		logFeatureDescriptors(project, featureDescriptors);
	}

	@VisibleForTesting
	void logFeatureDescriptors(@NotNull final Project project, @NotNull final List<FeatureDescriptor> featureDescriptors) {
		if (featureDescriptors.isEmpty()) {
			LOGGER.info("Project '{}' does not contain any features.", project.getName());
			return;
		}
		LOGGER.info("Found {} {} in project '{}':\n{}", featureDescriptors.size(), featureDescriptors.size() == 1 ? "feature" : "features", project.getName(), featureDescriptors.stream()
				.map(getFsObjectsLoggingFormatHelper()::shortFormatFeatureDescriptor)
				.collect(Collectors.joining("\n"))
		);
	}
}
