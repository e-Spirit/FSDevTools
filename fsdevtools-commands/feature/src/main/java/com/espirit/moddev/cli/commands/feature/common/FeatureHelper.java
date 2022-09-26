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

package com.espirit.moddev.cli.commands.feature.common;

import com.espirit.moddev.shared.annotation.VisibleForTesting;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.ServerActionHandle;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.agency.BrokerAgent;
import de.espirit.firstspirit.agency.SpecialistsBroker;
import de.espirit.firstspirit.feature.FeatureAgent;
import de.espirit.firstspirit.feature.FeatureAnalyseResult;
import de.espirit.firstspirit.feature.FeatureDescriptor;
import de.espirit.firstspirit.feature.FeatureFile;
import de.espirit.firstspirit.feature.FeatureInstallAgent;
import de.espirit.firstspirit.feature.FeatureInstallResult;
import de.espirit.firstspirit.feature.FeatureProgress;
import de.espirit.firstspirit.storage.Revision;
import de.espirit.firstspirit.store.access.feature.FeatureInstallResultImpl;
import de.espirit.firstspirit.transport.LayerMapper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Feature related utility methods.
 */
public class FeatureHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(FeatureHelper.class);

	/**
	 * Returns a log string for the given {@link ExtendedFeatureAnalyseResult}.
	 *
	 * @param result the {@link ExtendedFeatureAnalyseResult}.
	 * @return a log string for the given {@link ExtendedFeatureAnalyseResult}.
	 */
	@NotNull
	public static String getFeatureLoggingString(@NotNull final ExtendedFeatureAnalyseResult result) {
		return getFeatureLoggingString(result.getFeatureName(), result.getAbsolutePathToFeatureFile(), result.getProjectName());
	}

	/**
	 * Returns a log string for the given parameters.
	 *
	 * @param featureName the name of the {@link de.espirit.firstspirit.store.access.feature.Feature}.
	 * @param filePath    the path to the {@link File feature file}.
	 * @param projectName the name of the {@link Project}.
	 * @return a log string for tshe given parameter.
	 */
	@NotNull
	public static String getFeatureLoggingString(@NotNull final String featureName, @NotNull final String filePath, @NotNull final String projectName) {
		return String.format(
				"feature '%s' (file: '%s', project: '%s')",
				featureName,
				filePath,
				projectName
		);
	}

	/**
	 * Uploads feature archive to the FirstSpirit server
	 * and tries to install it.
	 * Returns {@link FeatureInstallResultImpl}.
	 *
	 * @param featureInstallAgent {@link FeatureInstallAgent}.
	 * @param file                path to feature archive.
	 * @param layerMapper         {@link LayerMapper}.
	 * @return {@link FeatureInstallResultImpl}
	 * @throws Exception if server side operation fails.
	 */
	@NotNull
	public FeatureInstallResultImpl getFeatureInstallResult(@NotNull final FeatureInstallAgent featureInstallAgent, @NotNull final File file, @NotNull final LayerMapper layerMapper) throws Exception {
		final FeatureFile featureFile = uploadFeatureFile(featureInstallAgent, file);
		final ServerActionHandle<? extends FeatureProgress, FeatureInstallResult> serverActionHandle = featureInstallAgent.installFeature(featureFile, layerMapper);
		final FeatureInstallResultImpl featureInstallResult = (FeatureInstallResultImpl) serverActionHandle.getResult(true);
		if (featureInstallResult.hasInstallException()) {
			throw featureInstallResult.getInstallException().getKey();
		}
		return featureInstallResult;
	}

	/**
	 * Get {@link FeatureDescriptor}s from the FirstSpirit server,
	 * sort them by name and prune ones having duplicate names.
	 * The one with the more recent revision wins.
	 *
	 * @param featureAgent {@link FeatureAgent}.
	 * @return list of {@link FeatureDescriptor}s, deduplicated and sorted by name.
	 */
	@NotNull
	public List<FeatureDescriptor> getFeatureDescriptors(@NotNull final FeatureAgent featureAgent) {
		final Map<String, FeatureDescriptor> descriptors = featureAgent.getFeatureDescriptors()
				.stream()
				.collect(Collectors.toMap(
						FeatureDescriptor::getFeatureName,
						first -> first,
						(first, second) -> {
							final Revision firstRevision = first.getRevision();
							final Revision secondRevision = second.getRevision();
							// the one with the more recent revision wins
							if (firstRevision.compareTo(secondRevision) >= 0) {
								return first;
							}
							return second;
						}
				));
		return descriptors.entrySet()
				.stream()
				.sorted(Map.Entry.comparingByKey())
				.map(Map.Entry::getValue)
				.collect(Collectors.toList());
	}

	/**
	 * Upload feature archive to the FirstSpirit server and analyze the feature.
	 *
	 * @param connection {@link Connection} to the FirstSpirit server.
	 * @param project    {@link Project}.
	 * @param file       feature archive file.
	 * @return {@link ExtendedFeatureAnalyseResult}.
	 * @throws Exception if the FirstSpirit server failed to deliver analysis results.
	 */
	@NotNull
	public ExtendedFeatureAnalyseResult getFeatureAnalyseResult(@NotNull final Connection connection, @NotNull final Project project, @NotNull final File file) throws Exception {
		final FeatureInstallAgent featureInstallAgent = getFeatureInstallAgent(connection, project);
		final FeatureFile featureFile = uploadFeatureFile(featureInstallAgent, file);
		final String featureName = featureFile.getFeatureName();
		final String absolutePathToFeatureFile = file.getAbsolutePath();
		final String projectName = project.getName();
		final String loggingStringForExtendedFeatureAnalyseResult = getFeatureLoggingString(
				featureName,
				absolutePathToFeatureFile,
				projectName
		);
		LOGGER.info("Analyzing {}.", loggingStringForExtendedFeatureAnalyseResult);
		final Optional<FeatureAnalyseResult> featureAnalyseResult = getFeatureAnalyseResult(featureInstallAgent, featureFile);
		final ExtendedFeatureAnalyseResult result = featureAnalyseResult
				.map(analyseResult -> new ExtendedFeatureAnalyseResult(analyseResult, featureName, absolutePathToFeatureFile, projectName))
				.orElseThrow(() -> new IllegalStateException(String.format("Error analyzing %s.", loggingStringForExtendedFeatureAnalyseResult)));
		LOGGER.info("Analysis of {} has been completed.", getFeatureLoggingString(result));
		return result;
	}

	/**
	 * Get {@link FeatureAgent} for the given project from {@link Connection}.
	 *
	 * @param connection {@link Connection} to the FirstSpirit server.
	 * @param project    {@link Project}.
	 * @return {@link FeatureAgent} for the given project from {@link Connection}.
	 */
	@NotNull
	public FeatureAgent getFeatureAgent(@NotNull final Connection connection, @NotNull final Project project) {
		final SpecialistsBroker projectBroker = getProjectBroker(connection, project);
		return projectBroker.requireSpecialist(FeatureAgent.TYPE);
	}

	/**
	 * Get {@link FeatureInstallAgent} for the given project from {@link Connection}.
	 *
	 * @param connection {@link Connection} to the FirstSpirit server.
	 * @param project    {@link Project}.
	 * @return {@link FeatureInstallAgent} for the given project from {@link Connection}.
	 */
	@NotNull
	public FeatureInstallAgent getFeatureInstallAgent(@NotNull final Connection connection, @NotNull final Project project) {
		final SpecialistsBroker projectBroker = getProjectBroker(connection, project);
		return projectBroker.requireSpecialist(FeatureInstallAgent.TYPE);
	}

	/**
	 * Upload feature archive.
	 *
	 * @param featureInstallAgent {@link FeatureInstallAgent}.
	 * @param file                path to the feature archive which is going to be uploaded.
	 * @return {@link FeatureFile}
	 * @throws IOException when it's impossible to upload the file e.g. when it does not exist.
	 */
	@NotNull
	public FeatureFile uploadFeatureFile(@NotNull final FeatureInstallAgent featureInstallAgent, @NotNull final File file) throws IOException {
		try (final FileInputStream fis = new FileInputStream(file)) {
			return featureInstallAgent.uploadFeatureFile(fis);
		}
	}

	/**
	 * Delegates to {@link FeatureInstallAgent}
	 * to analyze the given {@link FeatureFile}.
	 *
	 * @param featureInstallAgent {@link FeatureInstallAgent}.
	 * @param featureFile         {@link FeatureFile}.
	 * @return {@link Optional#empty()} is server does not return analysis results
	 * or {@link FeatureAnalyseResult} wrapped into {@link Optional} otherwise.
	 * @throws Exception if server side operation fails.
	 */
	@VisibleForTesting
	@NotNull
	Optional<FeatureAnalyseResult> getFeatureAnalyseResult(@NotNull final FeatureInstallAgent featureInstallAgent, @NotNull final FeatureFile featureFile) throws Exception {
		final ServerActionHandle<? extends FeatureProgress, FeatureAnalyseResult> serverActionHandle = featureInstallAgent.analyzeFeature(featureFile);
		return Optional.ofNullable(serverActionHandle.getResult(true));
	}

	/**
	 * Get {@link SpecialistsBroker} for the given project from {@link Connection}.
	 *
	 * @param connection {@link Connection} to the FirstSpirit server.
	 * @param project    {@link Project}.
	 * @return {@link SpecialistsBroker} for the given project from {@link Connection}.
	 */
	@NotNull
	@VisibleForTesting
	SpecialistsBroker getProjectBroker(@NotNull final Connection connection, @NotNull final Project project) {
		final String projectName = project.getName();
		final SpecialistsBroker projectBroker = connection.getBroker().requireSpecialist(BrokerAgent.TYPE).getBrokerByProjectName(projectName);
		if (projectBroker == null) {
			throw new IllegalStateException(String.format("Project broker for project '%s' not found.", projectName));
		}
		return projectBroker;
	}

}
