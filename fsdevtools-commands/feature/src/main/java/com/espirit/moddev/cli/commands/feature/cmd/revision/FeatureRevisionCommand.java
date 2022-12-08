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

package com.espirit.moddev.cli.commands.feature.cmd.revision;

import com.espirit.moddev.cli.api.annotations.ParameterExamples;
import com.espirit.moddev.cli.commands.feature.FeatureCommandGroup;
import com.espirit.moddev.cli.commands.feature.FeatureCommandNames;
import com.espirit.moddev.cli.commands.feature.common.AbstractFeatureCommand;
import com.espirit.moddev.cli.commands.feature.common.FeatureHelper;
import com.espirit.moddev.shared.annotation.VisibleForTesting;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.help.Examples;
import com.github.rvesse.airline.annotations.restrictions.Required;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.feature.FeatureAgent;
import de.espirit.firstspirit.feature.FeatureDescriptor;
import de.espirit.firstspirit.feature.FeatureModel;
import de.espirit.firstspirit.storage.HistoryProvider;
import de.espirit.firstspirit.storage.Revision;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@com.github.rvesse.airline.annotations.Command(
		groupNames = FeatureCommandGroup.NAME,
		name = FeatureCommandNames.REVISION,
		description = "Update the revision of the specified feature in the specified FirstSpirit project."
)
@Examples(
		examples = {
				FeatureCommandGroup.NAME + " " + FeatureCommandNames.REVISION + " --name \"My Feature\"",
				FeatureCommandGroup.NAME + " " + FeatureCommandNames.REVISION + " --name \"My Feature\" --revision 4211"
		},
		descriptions = {
				"Updates the feature named \"My Feature\" from the specified FirstSpirit project to the latest revision of the project.",
				"Updates the feature named \"My Feature\" from the specified FirstSpirit project to revision 4211."
		}
)
public class FeatureRevisionCommand extends AbstractFeatureCommand {

	private static final Logger LOGGER = LoggerFactory.getLogger(FeatureRevisionCommand.class);

	@NotNull
	private final FeatureHelper _featureHelper = new FeatureHelper();

	@Required
	@Option(
			type = OptionType.COMMAND,
			name = {"-n", "--name"},
			description = "Feature name in the specified FirstSpirit project as reported by the \"" + FeatureCommandGroup.NAME + ' ' + FeatureCommandNames.LIST + "\" command."
	)
	@ParameterExamples(
			examples = {
					"-n MyFeature",
					"--name \"My Corporate Content\"",
			},
			descriptions = {
					"Example of the feature named \"MyFeature\" in the specified FirstSpirit project",
					"Example of the feature named \"My Corporate Content\" in the specified FirstSpirit project",
			}
	)
	@NotNull // required CLI parameter so Airline should do the nullability check before setting it
	private String _featureName = "";

	@Option(
			type = OptionType.COMMAND,
			name = {"-r", "--revision"},
			description = "Set the project revision to use. If none is set or the revision is set to -1 (default), the latest revision of the project will be used."
	)
	@ParameterExamples(
			examples = {
					"--revision 4211",
					"-r -1",
			},
			descriptions = {
					"Set the revision for the specified feature to 4211.",
					"Set the revision for the specified feature to the latest project revision.",
			}
	)
	private long _revision = -1;

	@VisibleForTesting
	@NotNull
	FeatureHelper getFeatureHelper() {
		return _featureHelper;
	}

	@VisibleForTesting
	@NotNull
	String getFeatureName() {
		return _featureName;
	}

	@VisibleForTesting
	long getRevision() {
		return _revision;
	}

	@Override
	protected void execute(@NotNull final Connection connection, @NotNull final Project project) throws Exception {
		final FeatureHelper featureHelper = getFeatureHelper();
		final FeatureAgent featureAgent = featureHelper.getFeatureAgent(connection, project);
		final FeatureDescriptor featureDescriptor = featureHelper.getFeatureDescriptor(featureAgent, getFeatureName());
		final Revision currentProjectRevision = project.getRevision(HistoryProvider.UNTIL_NOW);
		final long revisionId = getRevision();
		final Optional<Revision> optionalRevision = determineRevision(project, revisionId);
		final Revision revisionToUse = optionalRevision.orElseThrow(() -> new IllegalArgumentException(String.format("Revision '%s' not found in project '%s'! Current project revision is '%s'.", revisionId, project.getName(), currentProjectRevision.getId())));
		updateFeatureRevision(featureAgent, featureDescriptor, revisionToUse);
	}

	@VisibleForTesting
	@NotNull
	Optional<Revision> determineRevision(@NotNull final Project project, final long revisionId) {
		if (revisionId < 0) {
			// use latest revision
			final Revision revision = project.getRevision(HistoryProvider.UNTIL_NOW);
			LOGGER.debug("Using latest revision of project '{}': '{}'.", project.getName(), revision.getId());
			return Optional.of(revision);
		} else {
			// use specific revision: verify that the revision exists
			try {
				LOGGER.debug("Determining current revision of project '{}'...", project.getName());
				final Revision revision = project.getRevision(revisionId);
				LOGGER.debug("Current revision of project '{}' is '{}'.", project.getName(), revision.getId());
				return Optional.of(revision);
			} catch (final IllegalArgumentException exception) {
				// IllegalArgumentException is thrown by FirstSpirit if the revision does not exist
				return Optional.empty();
			}
		}
	}

	@VisibleForTesting
	void updateFeatureRevision(@NotNull final FeatureAgent featureAgent, @NotNull final FeatureDescriptor featureDescriptor, @NotNull final Revision revision) {
		if (revision.getId() == featureDescriptor.getRevision().getId()) {
			LOGGER.info("Revision of feature '{}' is already set to '{}'. Nothing to do...", featureDescriptor.getFeatureName(), revision.getId());
			return;
		}
		LOGGER.info("Updating revision of feature '{}' to '{}' (current = '{}')...", featureDescriptor.getFeatureName(), revision.getId(), featureDescriptor.getRevision().getId());
		final FeatureDescriptor updatedFeatureDescriptor = featureAgent.createFeatureBuilder(featureDescriptor)
				.useRevision(revision)
				.useRelease(featureDescriptor.isRelease())
				.create();
		LOGGER.debug("Creating feature model for feature '{}' in revision '{}'...", updatedFeatureDescriptor.getFeatureName(), revision.getId());
		final FeatureModel featureModel = featureAgent.createFeatureModel(updatedFeatureDescriptor);
		LOGGER.debug("Saving feature '{}' in revision '{}'...", updatedFeatureDescriptor.getFeatureName(), revision.getId());
		featureAgent.saveFeature(featureModel);
		LOGGER.info("Revision of feature '{}' successfully set to '{}'.", updatedFeatureDescriptor.getFeatureName(), revision.getId());
	}

}
