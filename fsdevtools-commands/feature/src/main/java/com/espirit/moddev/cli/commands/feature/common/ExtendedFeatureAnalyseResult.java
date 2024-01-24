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

import de.espirit.firstspirit.feature.FeatureAnalyseResult;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Extends {@link FeatureAnalyseResult}
 * with additional information
 * such as e.g. feature name.
 */
public class ExtendedFeatureAnalyseResult {
	@NotNull
	private final FeatureAnalyseResult _featureAnalyseResult;
	@NotNull
	private final String _featureName;

	@NotNull
	private final String _absolutePathToFeatureFile;

	@NotNull
	private final String _projectName;

	public ExtendedFeatureAnalyseResult(
			@NotNull final FeatureAnalyseResult featureAnalyseResult,
			@NotNull final String featureName,
			@NotNull final String absolutePathToFeatureFile,
			@NotNull final String projectName
	) {
		_featureAnalyseResult = Objects.requireNonNull(featureAnalyseResult);
		_featureName = Objects.requireNonNull(featureName);
		_absolutePathToFeatureFile = Objects.requireNonNull(absolutePathToFeatureFile);
		_projectName = Objects.requireNonNull(projectName);
	}

	@NotNull
	public FeatureAnalyseResult getFeatureAnalyseResult() {
		return _featureAnalyseResult;
	}

	@NotNull
	public String getFeatureName() {
		return _featureName;
	}

	@NotNull
	public String getAbsolutePathToFeatureFile() {
		return _absolutePathToFeatureFile;
	}

	@NotNull
	public String getProjectName() {
		return _projectName;
	}

}
