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

package com.espirit.moddev.cli.commands.feature.common;

import org.jetbrains.annotations.VisibleForTesting;
import com.google.common.collect.Streams;
import de.espirit.firstspirit.access.database.BasicEntityInfo;
import de.espirit.firstspirit.access.store.BasicElementInfo;
import de.espirit.firstspirit.access.store.Store;
import de.espirit.firstspirit.feature.FeatureAnalyseResult;
import de.espirit.firstspirit.feature.FeatureDescriptor;
import de.espirit.firstspirit.storage.Revision;
import de.espirit.firstspirit.store.access.feature.ElementReference;
import de.espirit.firstspirit.store.access.feature.FeatureError;
import de.espirit.firstspirit.transport.PropertiesTransportOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Utility methods to format FirstSpirit objects for logging.
 */
public class FsObjectsLoggingFormatHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(FsObjectsLoggingFormatHelper.class);

	/**
	 * Log {@link FeatureAnalyseResult}.
	 *
	 * @param analyseResult {@link ExtendedFeatureAnalyseResult} to log.
	 * @return {@code true} if errors have been logged or {@code false} otherwise.
	 */
	public boolean logFeatureAnalyseResult(@NotNull final ExtendedFeatureAnalyseResult analyseResult) {
		final FeatureAnalyseResult originalAnalyseResult = analyseResult.getFeatureAnalyseResult();
		final Map<FeatureError.Level, List<FeatureError>> errorLevelToFeatureErrors = Streams.concat(
				originalAnalyseResult.getGlobalErrors().stream(),
				originalAnalyseResult.getEntryErrors().values().stream().flatMap(Collection::stream)
		).collect(Collectors.groupingBy(FeatureError::getErrorLevel));
		final List<FeatureError> errors = errorLevelToFeatureErrors.getOrDefault(FeatureError.Level.ERROR, Collections.emptyList());
		final List<FeatureError> warnings = errorLevelToFeatureErrors.getOrDefault(FeatureError.Level.WARNING, Collections.emptyList());
		if (errors.isEmpty() && warnings.isEmpty()) {
			LOGGER.info("Analysis of {} has not revealed any errors or warnings.", FeatureHelper.getFeatureLoggingString(analyseResult));
			return false;
		}
		if (!errors.isEmpty()) {
			LOGGER.error(
					"There are {} error(s) in {}:\n{}",
					errors.size(),
					FeatureHelper.getFeatureLoggingString(analyseResult),
					formatFeatureErrors(errors)
			);
		}
		if (!warnings.isEmpty()) {
			LOGGER.warn(
					"There are {} warning(s) in {}:\n{}",
					warnings.size(),
					FeatureHelper.getFeatureLoggingString(analyseResult),
					formatFeatureErrors(warnings)
			);
		}
		final boolean featureCanBeInstalled = errors.isEmpty();
		final String installationDecisionMessage;
		if (featureCanBeInstalled) {
			installationDecisionMessage = String.format("Analysis of feature '%s' in project '%s' finished without errors. The feature is ready to be installed.", analyseResult.getFeatureName(), analyseResult.getProjectName());
		} else {
			installationDecisionMessage = String.format("The analysis found multiple errors. The feature '%s' cannot be installed in project '%s'.", analyseResult.getFeatureName(), analyseResult.getProjectName());
		}
		LOGGER.info(
				"Summary of analysis of {}: there are {} errors and {} warnings. {}",
				FeatureHelper.getFeatureLoggingString(analyseResult),
				errors.size(),
				warnings.size(),
				installationDecisionMessage
		);
		return !errors.isEmpty();
	}

	/**
	 * Formats {@link FeatureDescriptor}.
	 *
	 * @param descriptor {@link FeatureDescriptor}.
	 * @return formatted {@link FeatureDescriptor}.
	 */
	@NotNull
	public String formatFeatureDescriptor(@NotNull final FeatureDescriptor descriptor) {
		final StringBuilder result = new StringBuilder();
		result.append("Feature '").append(descriptor.getFeatureName()).append("':").append('\n');
		final String indent = " ".repeat(4);
		result.append(indent).append("Revision: ").append(formatRevision(descriptor.getRevision())).append('\n');
		result.append(indent).append("Release: ").append(descriptor.isRelease()).append('\n');
		result.append(indent).append("Elements: ").append(descriptor.getElementCount()).append('\n');
		final AtomicInteger datasetCount = new AtomicInteger();
		descriptor.getDatarecordsCount().values().forEach(datasetCount::addAndGet);
		result.append(indent).append("Datasets: ").append(datasetCount).append('\n');
		return result.toString();
	}

	/**
	 * Formats {@link FeatureDescriptor}.
	 *
	 * @param descriptor {@link FeatureDescriptor}.
	 * @return formatted {@link FeatureDescriptor}.
	 */
	@NotNull
	public String shortFormatFeatureDescriptor(@NotNull final FeatureDescriptor descriptor) {
		final StringBuilder result = new StringBuilder();
		result.append("  - ");
		result.append('"').append(descriptor.getFeatureName()).append('"');
		result.append(" ( ");
		result.append("revision=").append(descriptor.getRevision().getId());
		result.append(" , ");
		result.append("release=").append(descriptor.isRelease());
		result.append(" , ");
		result.append("elements=").append(descriptor.getElementCount());
		result.append(" , ");
		final AtomicInteger datasetCount = new AtomicInteger();
		descriptor.getDatarecordsCount().values().forEach(datasetCount::addAndGet);
		result.append("datasets=").append(datasetCount.get());
		result.append(" )");
		return result.toString();
	}

	/**
	 * Formats {@link Revision}.
	 *
	 * @param revision {@link Revision}.
	 * @return formatted {@link Revision}.
	 */
	@VisibleForTesting
	@NotNull
	String formatRevision(@NotNull final Revision revision) {
		return String.format("ID = %s, Comment = %s, Editor = %s, Time = %s", revision.getId(), revision.getComment(), revision.getEditor(), formatTimestamp(revision.getCommitOrCreationTime()));
	}

	/**
	 * Formats {@link FeatureError}s.
	 *
	 * @param featureErrors {@link FeatureError}s.
	 * @return formatted {@link FeatureError}s.
	 */
	@VisibleForTesting
	@NotNull
	String formatFeatureErrors(
			@NotNull final Collection<FeatureError> featureErrors
	) {
		final StringBuilder result = new StringBuilder();
		int index = 1;
		for (final FeatureError featureError : featureErrors) {
			result.append('#')
					.append(index++)
					.append(' ')
					.append(formatFeatureError(featureError))
					.append('\n');
		}
		return result.toString();
	}

	/**
	 * Formats {@link FeatureError}.
	 *
	 * @param error {@link FeatureError}.
	 * @return formatted {@link FeatureError}.
	 */
	@VisibleForTesting
	@NotNull
	String formatFeatureError(@NotNull final FeatureError error) {
		final String errorCode = Optional.ofNullable(error.getErrorCode()).map(Enum::name).orElse("unknown error code");
		return String.format("%s (%s)", error.getMessage(), errorCode);
	}

	/**
	 * Formats project properties.
	 *
	 * @param projectProperties project properties
	 * @return formatted project properties.
	 */
	@NotNull
	public String formatProjectProperties(@Nullable final EnumSet<PropertiesTransportOptions.ProjectPropertyType> projectProperties) {
		if (projectProperties == null) {
			return "<null>";
		}
		return projectProperties.stream().map(String::valueOf).collect(Collectors.joining(","));
	}

	/**
	 * Formats elements map.
	 *
	 * @param elements elements map.
	 * @return formatted elements map.
	 */
	@NotNull
	public String formatElementsMap(@Nullable final Map<BasicElementInfo, ElementReference> elements, @Nullable final String keyPrefix, @Nullable final String valuePrefix) {
		if (elements == null) {
			return "<null>";
		}
		final String actualKeyPrefix = Optional.ofNullable(keyPrefix).orElse("");
		final String actualValuePrefix = Optional.ofNullable(valuePrefix).orElse("");
		return elements.entrySet()
				.stream()
				.map(entry -> {
					final String key = formatBasicElementInfo(entry.getKey());
					final String value = formatElementReference(entry.getValue());
					return actualKeyPrefix + key + actualValuePrefix + value;
				})
				.collect(Collectors.joining("\n"));
	}

	/**
	 * Formats store nodes map.
	 *
	 * @param storeNodes store nodes map.
	 * @return formatted store nodes map.
	 */
	@NotNull
	public String formatStoreNodesMap(@Nullable final EnumMap<Store.Type, List<BasicElementInfo>> storeNodes) {
		if (storeNodes == null) {
			return "<null>";
		}
		final StringBuilder result = new StringBuilder();
		final String indent = " ".repeat(4);
		storeNodes.forEach((k, v) -> {
			result.append("Store ").append(k).append(':').append('\n');
			v.forEach(elem -> result.append(indent).append("Element: ").append(formatBasicElementInfo(elem)).append('\n'));
		});
		return result.toString();
	}

	/**
	 * Formats {@link BasicElementInfo}.
	 *
	 * @param elementInfo {@link BasicElementInfo}.
	 * @return formatted {@link BasicElementInfo}.
	 */
	@VisibleForTesting
	@NotNull
	String formatBasicElementInfo(@Nullable final BasicElementInfo elementInfo) {
		if (elementInfo == null) {
			return "<null>";
		}
		return String.format("StoreType = %s, NodeTag = %s, NodeId = %s, Uid = %s, RevisionId = %s", elementInfo.getStoreType(), elementInfo.getNodeTag(), elementInfo.getNodeId(), elementInfo.getUid(), elementInfo.getRevisionId());
	}

	/**
	 * Formats {@link ElementReference}.
	 *
	 * @param elementReference {@link ElementReference}.
	 * @return formatted {@link ElementReference}.
	 */
	@VisibleForTesting
	@NotNull
	String formatElementReference(@Nullable final ElementReference elementReference) {
		if (elementReference == null) {
			return "<null>";
		}
		return String.format("ID = %s, UID = %s, ElementClass = %s, ProjectId = %s", elementReference.getId(), elementReference.getUid(), elementReference.getElementClass(), elementReference.getProjectId());
	}

	/**
	 * Formats {@link BasicEntityInfo}.
	 *
	 * @param entityInfo {@link BasicEntityInfo}.
	 * @return formatted {@link BasicEntityInfo}.
	 */
	@NotNull
	public String formatBasicEntityInfo(@Nullable final BasicEntityInfo entityInfo) {
		if (entityInfo == null) {
			return "<null>";
		}
		return String.format("Gid = %s, EntityType = %s, SchemaUid = %s", entityInfo.getGid(), entityInfo.getEntityType(), entityInfo.getSchemaUid());
	}

	/**
	 * Formats timestamp in millis.
	 *
	 * @param milliseconds timestamp in milliseconds.
	 * @return the formatted timestamp.
	 */
	@VisibleForTesting
	@NotNull
	String formatTimestamp(final long milliseconds) {
		if (milliseconds <= 0) {
			return "<unknown>";
		}
		return Instant.ofEpochMilli(milliseconds).toString();
	}
}
