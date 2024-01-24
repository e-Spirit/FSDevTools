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

package com.espirit.moddev.cli.commands.feature.cmd.download;

import com.espirit.moddev.cli.api.annotations.ParameterExamples;
import com.espirit.moddev.cli.commands.feature.FeatureCommandGroup;
import com.espirit.moddev.cli.commands.feature.FeatureCommandNames;
import com.espirit.moddev.cli.commands.feature.common.AbstractFeatureCommand;
import com.espirit.moddev.cli.commands.feature.common.FeatureHelper;
import com.espirit.moddev.cli.commands.feature.common.FsObjectsLoggingFormatHelper;
import com.espirit.moddev.shared.annotation.VisibleForTesting;
import com.espirit.moddev.util.FileUtil;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.help.Examples;
import com.github.rvesse.airline.annotations.restrictions.PathKind;
import com.github.rvesse.airline.annotations.restrictions.Required;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.ServerActionHandle;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.feature.FeatureAgent;
import de.espirit.firstspirit.feature.FeatureDescriptor;
import de.espirit.firstspirit.feature.FeatureFile;
import de.espirit.firstspirit.feature.FeatureProgress;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

@com.github.rvesse.airline.annotations.Command(
		groupNames = FeatureCommandGroup.NAME,
		name = FeatureCommandNames.DOWNLOAD,
		description = "Download feature file of the specified feature from the specified FirstSpirit project."
)
@Examples(
		examples = {
				FeatureCommandGroup.NAME + " " + FeatureCommandNames.DOWNLOAD + " --name \"My Feature\" --file feature.zip --useLatestRevision --useRelease true",
				FeatureCommandGroup.NAME + " " + FeatureCommandNames.DOWNLOAD + " --name \"My Feature\" --file feature.zip --useLatestRevision",
				FeatureCommandGroup.NAME + " " + FeatureCommandNames.DOWNLOAD + " --name \"My Feature\" --file feature.zip --useRelease false",
				FeatureCommandGroup.NAME + " " + FeatureCommandNames.DOWNLOAD + " --name \"My Feature\" --file feature.zip",
		},
		descriptions = {
				"Download feature archive for the feature named \"My Feature\" from the specified FirstSpirit project and store it locally to the \"feature.zip\" file." +
						" Instruct the FirstSpirit server to use the latest revision when creating the feature archive." +
						" Additionally, require that feature's objects are taken from the release FirstSpirit stores.",
				"Download feature archive for the feature named \"My Feature\" from the specified FirstSpirit project and store it locally to the \"feature.zip\" file." +
						" Instruct the FirstSpirit server to use the latest revision when creating the feature archive." +
						" Release FirstSpirit store flag will be taken from the feature settings.",
				"Download feature archive for the feature named \"My Feature\" from the specified FirstSpirit project and store it locally to the \"feature.zip\" file." +
						" When creating the feature archive, the FirstSpirit server will use the revision configured in the feature settings." +
						" Additionally, require that feature's objects are taken from the non-release FirstSpirit stores.",
				"Download feature archive for the feature named \"My Feature\" from the specified FirstSpirit project and store it locally to the \"feature.zip\" file." +
						" Both revision and release FirstSpirit store flag will be taken from the feature settings.",
		}
)
public class FeatureDownloadCommand extends AbstractFeatureCommand {

	private static final Logger LOGGER = LoggerFactory.getLogger(FeatureDownloadCommand.class);

	@NotNull
	private final FeatureHelper _featureHelper = new FeatureHelper();

	@NotNull
	private final FsObjectsLoggingFormatHelper _fsObjectsLoggingFormatHelper = new FsObjectsLoggingFormatHelper();

	@Required
	@Option(
			type = OptionType.COMMAND,
			arity = 1,
			name = {"-n", "--name"},
			description = "Feature name in the specified FirstSpirit project as reported by" +
					" the \"" + FeatureCommandGroup.NAME + ' ' + FeatureCommandNames.LIST + "\" command."
	)
	@ParameterExamples(
			examples = {
					"-n MyFeatureA",
					"--name \"My Corporate Content\"",
			},
			descriptions = {
					"Example of the feature named \"MyFeatureA\" in the specified FirstSpirit project",
					"Example of the feature named \"My Corporate Content\" in the specified FirstSpirit project",
			}
	)
	@NotNull // required CLI parameter so Airline should do the nullability check before setting it
	private String _featureName = "";

	@Required
	@Option(
			type = OptionType.COMMAND,
			arity = 1,
			name = {"-f", "--file"},
			description = "Path to output feature archive. The path to the file will be created (including the parent path) and existing files will be overwritten."
	)
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
	@com.github.rvesse.airline.annotations.restrictions.Path(kind = PathKind.FILE)
	@NotNull // required CLI parameter so Airline should do the nullability check before setting it
	private String _pathToFeatureZip = "";

	@Option(
			type = OptionType.COMMAND,
			name = {"--useLatestRevision"},
			description = "Use the latest revision for the specified feature before creating the feature archive. If this option is not specified, the revision from the feature settings will be used."
	)
	@ParameterExamples(
			examples = {
					"--useLatestRevision",
			},
			descriptions = {
					"Use the latest revision for the specified feature before creating the feature archive.",
			}
	)
	private Boolean _useLatestRevision;

	@Option(
			type = OptionType.COMMAND,
			arity = 1,
			name = {"--useRelease"},
			description = "Use release (or don't use it). If this option is not specified, the release FirstSpirit store flag will be taken from the feature settings."
	)
	@ParameterExamples(
			examples = {
					"--useRelease true",
					"--useRelease false",
			},
			descriptions = {
					"Use release store when creating the feature file for the specified feature.",
					"Use current store when creating the feature file for the specified feature.",
			}
	)
	@Nullable
	private Boolean _useRelease;

	@VisibleForTesting
	@NotNull
	FeatureHelper getFeatureHelper() {
		return _featureHelper;
	}

	@NotNull
	@VisibleForTesting
	FsObjectsLoggingFormatHelper getFsObjectsLoggingFormatHelper() {
		return _fsObjectsLoggingFormatHelper;
	}

	@VisibleForTesting
	@NotNull
	String getFeatureName() {
		return _featureName;
	}

	@VisibleForTesting
	@NotNull
	String getPathToFeatureZip() {
		return _pathToFeatureZip;
	}

	@VisibleForTesting
	Boolean useLatestRevision() {
		return _useLatestRevision;
	}

	@VisibleForTesting
	@Nullable
	Boolean useRelease() {
		return _useRelease;
	}

	@Override
	protected void execute(@NotNull final Connection connection, @NotNull final Project project) throws Exception {
		final File outputFile = ensureOutputFileExists();
		final FeatureAgent featureAgent = getFeatureAgent(connection, project);
		final FeatureDescriptor originalFeatureDescriptor = getFeatureHelper().getFeatureDescriptor(featureAgent, getFeatureName());
		final FsObjectsLoggingFormatHelper fsObjectsLoggingFormatHelper = getFsObjectsLoggingFormatHelper();
		final FeatureDescriptor featureDescriptor = getFeatureDescriptor(featureAgent, originalFeatureDescriptor);
		final boolean sameFeatureDescriptor = featureDescriptor == originalFeatureDescriptor;
		if (sameFeatureDescriptor) {
			LOGGER.info("Using original feature parameters:\n{}", fsObjectsLoggingFormatHelper.formatFeatureDescriptor(originalFeatureDescriptor));
		} else {
			LOGGER.info("Original feature parameters:\n{}", fsObjectsLoggingFormatHelper.formatFeatureDescriptor(originalFeatureDescriptor));
			LOGGER.info("Adjusted feature parameters:\n{}", fsObjectsLoggingFormatHelper.formatFeatureDescriptor(featureDescriptor));
		}
		downloadFeature(outputFile, featureAgent, featureDescriptor);
	}

	@VisibleForTesting
	@NotNull
	File ensureOutputFileExists() throws IOException {
		final Path pathToOutputFile = Paths.get(getPathToFeatureZip());
		final File outputFile = pathToOutputFile.toFile();
		if (outputFile.isDirectory()) {
			throw new IllegalArgumentException(String.format("specified output path \"%s\" is a directory", pathToOutputFile.toAbsolutePath()));
		}
		outputFile.getAbsoluteFile().getParentFile().mkdirs();
		if (!outputFile.createNewFile()) {
			LOGGER.warn("File \"{}\" exists. It is going to be overwritten.", pathToOutputFile.toAbsolutePath());
		}
		return outputFile;
	}

	@VisibleForTesting
	@NotNull
	FeatureAgent getFeatureAgent(@NotNull final Connection connection, @NotNull final Project project) {
		return getFeatureHelper().getFeatureAgent(connection, project);
	}

	@VisibleForTesting
	@NotNull
	FeatureDescriptor getFeatureDescriptor(@NotNull final FeatureAgent featureAgent, @NotNull final FeatureDescriptor originalFeatureDescriptor) {
		final Boolean useLatestRevision = useLatestRevision();
		final Boolean useRelease = useRelease();
		final boolean overrideUseLatestRevision = useLatestRevision != null && useLatestRevision;
		final boolean overrideUseRelease = useRelease != null && originalFeatureDescriptor.isRelease() != useRelease;
		final boolean overrideFeatureParameters = overrideUseLatestRevision || overrideUseRelease;
		if (!overrideFeatureParameters) {
			return originalFeatureDescriptor;
		}
		// we have changed parameters: build a new feature descriptor
		final FeatureAgent.FeatureBuilder featureBuilder = featureAgent.createFeatureBuilder(originalFeatureDescriptor);
		if (useLatestRevision != null) {
			featureBuilder.useLatestRevision();
			LOGGER.info("Using latest revision...");
		}
		if (useRelease != null) {
			featureBuilder.useRelease(useRelease);
			LOGGER.info("Setting release state to \"{}\" (was \"{}\").", useRelease, originalFeatureDescriptor.isRelease());
		}
		return featureBuilder.create();
	}

	@VisibleForTesting
	void downloadFeature(@NotNull final File outputFile, @NotNull final FeatureAgent featureAgent, @NotNull final FeatureDescriptor featureDescriptor) throws Exception {
		LOGGER.info(String.format("Starting download of feature '%s'...", featureDescriptor.getFeatureName()));
		final ServerActionHandle<? extends FeatureProgress, Boolean> handle = featureAgent.createFeatureTransportFile(featureDescriptor);
		final Boolean result = handle.getResult(false);
		if (!Boolean.TRUE.equals(result)) {
			throw new RuntimeException(String.format("Error downloading feature '%s' to file '%s'!", featureDescriptor.getFeatureName(), outputFile.getAbsolutePath()));
		}
		final FeatureProgress featureProgress = handle.getProgress(true);
		LOGGER.info(String.format("Download of feature '%s' completed. Writing to file '%s'...", featureDescriptor.getFeatureName(), outputFile.getAbsolutePath()));
		final FeatureFile featureFile = featureProgress.getFeatureFile();
		try (final InputStream inputStream = featureAgent.downloadFeatureFile(featureFile)) {
			writeInputStreamIntoFile(inputStream, outputFile);
		}
		LOGGER.info(String.format("Feature file '%s' successfully written.", outputFile.getAbsolutePath()));
	}

	@VisibleForTesting
	void writeInputStreamIntoFile(@NotNull final InputStream inputStream, @NotNull final File outputFile) throws IOException {
		FileUtil.writeIntoFile(inputStream, outputFile);
	}

}
