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

package com.espirit.moddev.cli.commands.feature.cmd.install;

import com.espirit.moddev.cli.api.annotations.ParameterExamples;
import com.espirit.moddev.cli.commands.feature.FeatureCommandGroup;
import com.espirit.moddev.cli.commands.feature.FeatureCommandNames;
import com.espirit.moddev.cli.commands.feature.common.AbstractFeatureCommand;
import com.espirit.moddev.cli.commands.feature.common.ExtendedFeatureAnalyseResult;
import com.espirit.moddev.cli.commands.feature.common.FeatureHelper;
import com.espirit.moddev.cli.commands.feature.common.FsObjectsLoggingFormatHelper;
import com.espirit.moddev.cli.common.StringPropertiesMap;
import org.jetbrains.annotations.VisibleForTesting;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.help.Examples;
import com.github.rvesse.airline.annotations.restrictions.Path;
import com.github.rvesse.airline.annotations.restrictions.PathKind;
import com.github.rvesse.airline.annotations.restrictions.Required;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.database.BasicEntityInfo;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.access.store.BasicElementInfo;
import de.espirit.firstspirit.access.store.Store;
import de.espirit.firstspirit.feature.FeatureAnalyseResult;
import de.espirit.firstspirit.feature.FeatureInstallAgent;
import de.espirit.firstspirit.feature.FeatureInstallResult;
import de.espirit.firstspirit.store.access.feature.ElementReference;
import de.espirit.firstspirit.store.access.feature.FeatureInstallResultImpl;
import de.espirit.firstspirit.transport.LayerMapper;
import de.espirit.firstspirit.transport.PropertiesTransportOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@com.github.rvesse.airline.annotations.Command(
		groupNames = FeatureCommandGroup.NAME,
		name = FeatureCommandNames.INSTALL,
		description = "Upload the specified feature archive to the FirstSpirit server" +
				" and import the feature to the specified FirstSpirit project" +
				" with the specified name-based FirstSpirit database layer mapping."
)
@Examples(
		examples = {
				FeatureCommandGroup.NAME + " " + FeatureCommandNames.INSTALL + " --file feature.zip --layerMapping \"*:CREATE_NEW\" --force",
				FeatureCommandGroup.NAME + " " + FeatureCommandNames.INSTALL + " --file feature.zip --layerMapping \"*:FirstSpiritDBA\"",
				FeatureCommandGroup.NAME + " " + FeatureCommandNames.INSTALL + " --file feature.zip --layerMapping \"featureLayerA=projectLayer1,featureLayerB=projectLayer2,*=FirstSpiritDBA\"",
				FeatureCommandGroup.NAME + " " + FeatureCommandNames.INSTALL + " --file feature.zip --layerMapping \"featureLayerA=projectLayer1,featureLayerB=projectLayer2,featureLayerC=CREATE_NEW\"",
				FeatureCommandGroup.NAME + " " + FeatureCommandNames.INSTALL + " --file feature.zip",
		},
		descriptions = {
				"Upload the feature archive \"feature.zip\" to the FirstSpirit server and import the corresponding feature to the specified FirstSpirit project." +
						" Create a new FirstSpirit database layer in the specified FirstSpirit project for each FirstSpirit database layer defined in the feature." +
						" If there are any errors reported by the server side feature analysis, ignore them and proceed with the feature import operation.",
				"Upload the feature archive \"feature.zip\" to the FirstSpirit server and import the corresponding feature to the specified FirstSpirit project." +
						" Map each FirstSpirit database layer defined in the feature onto the existing FirstSpirit database layer named \"FirstSpiritDBA\" defined in the specified FirstSpirit project." +
						" If there are any errors reported by the server side feature analysis, abort the import operation.",
				"Upload the feature archive \"feature.zip\" to the FirstSpirit server and import the corresponding feature to the specified FirstSpirit project." +
						" Map the FirstSpirit database layers defined in the feature as follows." +
						" The feature's FirstSpirit database layer named \"featureLayerA\" is going to be mapped onto the existing FirstSpirit database layer named \"projectLayer1\" defined in the specified FirstSpirit project." +
						" The feature's FirstSpirit database layer named \"featureLayerB\" is going to be mapped onto the existing FirstSpirit database layer named \"projectLayer2\" defined in the specified FirstSpirit project." +
						" All the other feature's FirstSpirit database layers are going to be mapped onto the existing FirstSpirit database layer named \"FirstSpiritDBA\" defined in the specified FirstSpirit project." +
						" If there are any errors reported by the server side feature analysis, abort the import operation.",
				"Upload the feature archive \"feature.zip\" to the FirstSpirit server and import the corresponding feature to the specified FirstSpirit project." +
						" Map the FirstSpirit database layers defined in the feature as follows." +
						" The feature's FirstSpirit database layer named \"featureLayerA\" is going to be mapped onto the existing FirstSpirit database layer named \"projectLayer1\" defined in the specified FirstSpirit project." +
						" The feature's FirstSpirit database layer named \"featureLayerB\" is going to be mapped onto the existing FirstSpirit database layer named \"projectLayer2\" defined in the specified FirstSpirit project." +
						" The feature's FirstSpirit database layer named \"featureLayerC\" is going to be mapped onto a new FirstSpirit database layer in the specified FirstSpirit project." +
						" If there are further feature's FirstSpirit database layers, error is going to be raised." +
						" If there are any errors reported by the server side feature analysis, abort the import operation.",
				"Upload the feature archive \"feature.zip\" to the FirstSpirit server and import the corresponding feature to the specified FirstSpirit project." +
						" Empty name-based database layer mapping is going to be used (same behavior as in the external sync import command)." +
						" If there are any errors reported by the server side feature analysis, abort the import operation.",
		}
)
public class FeatureInstallCommand extends AbstractFeatureCommand {

	private static final Logger LOGGER = LoggerFactory.getLogger(FeatureInstallCommand.class);

	/**
	 * Special FirstSpirit database layer mapping parameter,
	 * which can be used as the key in
	 * the FirstSpirit database layer mapping option
	 * in order to capture all source FirstSpirit database layers.
	 */
	static final String WILDCARD = "*";

	/**
	 * Special FirstSpirit database layer mapping parameter,
	 * which can be used as the value in
	 * the FirstSpirit database layer mapping option
	 * in order to create a new target FirstSpirit database layer
	 * for the corresponding source FirstSpirit database layer.
	 */
	static final String CREATE_NEW = "CREATE_NEW";

	/**
	 * Pre-parsed database layer mapping for creating
	 * a new target FirstSpirit database layer
	 * for any source FirstSpirit database layer.
	 */
	private static final Map<String, String> CREATE_NEW_LAYER_MAPPING = Map.of(WILDCARD, CREATE_NEW);

	@NotNull
	private final FeatureHelper _featureHelper = new FeatureHelper();

	@NotNull
	private final FsObjectsLoggingFormatHelper _fsObjectsLoggingFormatHelper = new FsObjectsLoggingFormatHelper();

	@Required
	@Option(
			type = OptionType.COMMAND,
			arity = 1,
			name = {"-f", "--file"},
			description = "Path to feature archive. It must exist and be readable."
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

	@Option(
			type = OptionType.COMMAND,
			arity = 1,
			name = {"-lm", "--layerMapping"},
			description = "Define how the feature's FirstSpirit database layers should be mapped onto the project's ones." +
					" It is a single string containing a comma-separated list of key-value pairs delimited by \":\" or \"=\" like this string over here: \"srcLayerA=dstLayer1,srcLayerB=dstLayer2,*=dstLayer3\"." +
					" Key is the name of the feature's FirstSpirit database layer (the source layer)." +
					" Value is the name of the project's FirstSpirit database layer (the target layer)." +
					" See command's examples for more information."
	)
	@Nullable
	private String _layerMapping;

	@Option(
			type = OptionType.COMMAND,
			arity = 0,
			name = {"--force"},
			description = "Proceed with feature import operation even if server side feature analysis reported errors." +
					" If this option is not specified, command will be aborted in the case of any feature analysis errors."
	)
	private boolean _forceImport;

	@Option(
			type = OptionType.COMMAND,
			arity = 0,
			name = {"--include-feature-model"},
			description = "Create or update feature model in the target project." +
					" Disabled by default." +
					" Feature model is the feature's metadata about nodes, data sources, entities, schemas etc. comprising the given feature." +
					" If enabled, this metadata is going to be created or updated (overwritten) in the target project's feature storage." +
					" The FirstSpirit IDs and revision is going to be adjusted to point to the elements in the target project." +
					" This option is useful for propagating the feature's content from the target instance further to other instances e.g. DEV -> QA -> PROD."
	)
	private boolean _includeFeatureModel;

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

	@VisibleForTesting
	@NotNull
	String getPathToFeatureZip() {
		return _pathToFeatureZip;
	}

	@VisibleForTesting
	@NotNull
	Optional<String> getLayerMapping() {
		return Optional.ofNullable(_layerMapping);
	}

	@VisibleForTesting
	boolean isForceImport() {
		return _forceImport;
	}

	@Override
	protected void execute(@NotNull final Connection connection, @NotNull final Project project) throws Exception {
		final FeatureHelper featureHelper = getFeatureHelper();
		final File featureFile = new File(getPathToFeatureZip());
		final ExtendedFeatureAnalyseResult analyseResult = featureHelper.getFeatureAnalyseResult(connection, project, featureFile);
		checkForErrors(analyseResult);
		final LayerMapper layerMapper = getLayerMapper(analyseResult.getFeatureAnalyseResult());
		final FeatureInstallAgent featureInstallAgent = featureHelper.getFeatureInstallAgent(connection, project);
		LOGGER.info("Installing {}...", FeatureHelper.getFeatureLoggingString(analyseResult));
		final FeatureInstallResultImpl installResult = featureHelper.getFeatureInstallResult(featureInstallAgent, featureFile, layerMapper, _includeFeatureModel);
		LOGGER.info("Installation of {} has been successfully completed.", FeatureHelper.getFeatureLoggingString(analyseResult));
		LOGGER.debug("Installation details of {}:", FeatureHelper.getFeatureLoggingString(analyseResult));
		logFeatureInstallResult(installResult);
	}

	@VisibleForTesting
	void checkForErrors(@NotNull final ExtendedFeatureAnalyseResult analyseResult) {
		final boolean errorsLogged = getFsObjectsLoggingFormatHelper().logFeatureAnalyseResult(analyseResult);
		if (!errorsLogged) {
			return;
		}
		if (isForceImport()) {
			LOGGER.warn("Proceeding with import of {}, because force flag is specified.", FeatureHelper.getFeatureLoggingString(analyseResult));
			return;
		}
		throw new IllegalStateException(String.format("Errors have been found during analysis of %s.", FeatureHelper.getFeatureLoggingString(analyseResult)));
	}

	@VisibleForTesting
	@NotNull
	LayerMapper getLayerMapper(@NotNull final FeatureAnalyseResult featureAnalyseResult) {
		final String rawLayerMappingOption = getLayerMapping().orElse("").trim();
		if (rawLayerMappingOption.isBlank()) {
			return mimicDefaultLayerMapperFromExternalSyncImportCommand();
		}
		final Map<String, String> layerMappingsFromOption = new StringPropertiesMap(rawLayerMappingOption).toHashMap();
		final boolean isCreateNewLayerMapping = layerMappingsFromOption.equals(CREATE_NEW_LAYER_MAPPING);
		if (isCreateNewLayerMapping) {
			LOGGER.debug("Layer mapping option: for each source layer a new target layer will be created.");
			return LayerMapper.CREATE_NEW_DEFAULT_LAYER_MAPPER;
		}
		useSpecialNameForCreatingNewDestinationLayer(layerMappingsFromOption);
		final Map<String, String> nameBasedLayerMapping = getNameBasedLayerMapping(layerMappingsFromOption, featureAnalyseResult.getMappedLayers(), featureAnalyseResult.getUnmappedLayers());
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Following name-based layer mappings are going to be used:\n{}", nameBasedLayerMapping.entrySet()
					.stream()
					.map(kv -> String.format("    %s -> %s", kv.getKey(), kv.getValue()))
					.collect(Collectors.joining("\n"))
			);
		}
		return getFirstSpiritLayerMapper(nameBasedLayerMapping);
	}

	/**
	 * For consistency mimic the logic of
	 * {@code com.espirit.moddev.cli.commands.ImportCommand#configureLayerMapper()}
	 * which returns a layer mapper backed by an empty map.
	 *
	 * @return {@link LayerMapper.LayerNameBasedLayerMapper} backed by an empty map.
	 */
	@VisibleForTesting
	@NotNull
	LayerMapper.LayerNameBasedLayerMapper mimicDefaultLayerMapperFromExternalSyncImportCommand() {
		LOGGER.debug("Layer mapping is empty!");
		return LayerMapper.LayerNameBasedLayerMapper.from(new HashMap<>());
	}

	/**
	 * Replace {@link this#CREATE_NEW}
	 * with {@link LayerMapper#CREATE_NEW_DEFAULT_LAYER}
	 * in destination layers specified by user.
	 *
	 * @param layerMappingsFromOption user defined layer mappings.
	 */
	@VisibleForTesting
	void useSpecialNameForCreatingNewDestinationLayer(
			@NotNull final Map<String, String> layerMappingsFromOption
	) {
		final Set<String> srcLayers = layerMappingsFromOption.keySet();
		for (final String srcLayer : srcLayers) {
			final String dstLayer = layerMappingsFromOption.get(srcLayer);
			if (CREATE_NEW.equals(dstLayer)) {
				layerMappingsFromOption.put(srcLayer, LayerMapper.CREATE_NEW_DEFAULT_LAYER);
			}
		}
	}

	@VisibleForTesting
	@NotNull
	Map<String, String> getNameBasedLayerMapping(@NotNull final Map<String, String> layerMappingsFromOption, @NotNull final Map<String, String> mappedLayers, @NotNull final List<String> unmappedLayers) {
		if (layerMappingsFromOption.containsKey(WILDCARD)) {
			return getNameBasedLayerMappingWithWildcardFallback(layerMappingsFromOption, mappedLayers, unmappedLayers);
		}
		return getNameBasedLayerMappingWithoutWildcardFallback(layerMappingsFromOption, mappedLayers, unmappedLayers);
	}

	@VisibleForTesting
	@NotNull
	LayerMapper getFirstSpiritLayerMapper(@NotNull final Map<String, String> nameBasedLayerMapping) {
		return LayerMapper.LayerNameBasedLayerMapper.from(nameBasedLayerMapping);
	}

	@VisibleForTesting
	@NotNull
	Map<String, String> getNameBasedLayerMappingWithWildcardFallback(@NotNull final Map<String, String> layerMappingsFromOption, @NotNull final Map<String, String> mappedLayers, @NotNull final List<String> unmappedLayers) {
		final String fallbackLayer = getFallbackLayer(layerMappingsFromOption);
		final Map<String, String> result = new HashMap<>();
		// ensure every mapped layer will be mapped onto at least the fallback layer
		mappedLayers.forEach((srcLayer, dstLayer) -> result.put(srcLayer, fallbackLayer));
		// ensure every unmapped layer will be mapped onto at least the fallback layer
		unmappedLayers.forEach(unmappedLayer -> result.put(unmappedLayer, fallbackLayer));
		// now everything is mapped onto the fallback layer
		// override these mappings with the user supplied layer mappings
		layerMappingsFromOption.forEach((srcLayer, dstLayer) -> {
			if (!WILDCARD.equals(srcLayer)) {
				result.put(srcLayer, dstLayer);
			}
		});
		return result;
	}

	@VisibleForTesting
	@NotNull
	String getFallbackLayer(@NotNull final Map<String, String> layerMappingsFromOption) {
		final String layerName = layerMappingsFromOption.get(WILDCARD);
		if (layerName == null) {
			throw new IllegalStateException("no fallback layer");
		}
		return layerName;
	}

	@VisibleForTesting
	@NotNull
	Map<String, String> getNameBasedLayerMappingWithoutWildcardFallback(@NotNull final Map<String, String> layerMappingsFromOption, @NotNull final Map<String, String> mappedLayers, @NotNull final List<String> unmappedLayers) {
		if (layerMappingsFromOption.containsKey(WILDCARD)) {
			throw new IllegalStateException("wildcard must not be here");
		}
		final Map<String, String> result = new HashMap<>();
		for (final String layer : unmappedLayers) {
			result.put(layer, layer);
		}
		result.putAll(mappedLayers);
		result.putAll(layerMappingsFromOption);
		return result;
	}

	@VisibleForTesting
	void logFeatureInstallResult(@NotNull final FeatureInstallResultImpl featureInstallResult) {
		logElementsMap(featureInstallResult.getNewElements(), "new elements");
		logElementsMap(featureInstallResult.getUpdatedElementsMap(), "updated elements");
		final EnumSet<PropertiesTransportOptions.ProjectPropertyType> modifiedProjectProperties = featureInstallResult.getModifiedProjectProperties();
		if (!modifiedProjectProperties.isEmpty() && LOGGER.isDebugEnabled()) {
			LOGGER.debug("{} modified project properties: {}", modifiedProjectProperties.size(), getFsObjectsLoggingFormatHelper().formatProjectProperties(modifiedProjectProperties));
		}
		logElementsMap(featureInstallResult.getMovedElementsMap(), "moved elements");
		logElementsMap(featureInstallResult.getModifiedSchemes(), "modified schemes");
		logEntities(featureInstallResult.getNewEntities(), "new entities");
		logEntities(featureInstallResult.getUpdatedEntities(), "updated entities");
		logStoreNodes(featureInstallResult.getLostAndFoundStoreNodes(), "lost and found store nodes");
		logStoreNodes(featureInstallResult.getDeletedStoreNodes(), "deleted store nodes");
	}

	@VisibleForTesting
	void logElementsMap(@NotNull final Map<BasicElementInfo, ElementReference> elementsMap, @NotNull final String message) {
		if (elementsMap.isEmpty() || !LOGGER.isDebugEnabled()) {
			return;
		}
		LOGGER.debug("{} {}:\n{}", elementsMap.size(), message, getFsObjectsLoggingFormatHelper().formatElementsMap(elementsMap, "SOURCE ELEMENT: ", "\n    TARGET ELEMENT: "));
	}

	@VisibleForTesting
	void logEntities(@NotNull final Set<BasicEntityInfo> entities, @NotNull final String message) {
		if (entities.isEmpty() || !LOGGER.isDebugEnabled()) {
			return;
		}
		LOGGER.debug("{} {}:\n{}", entities.size(), message, entities.stream()
				.map(getFsObjectsLoggingFormatHelper()::formatBasicEntityInfo)
				.collect(Collectors.joining("\n"))
		);
	}

	@VisibleForTesting
	void logStoreNodes(@NotNull final EnumMap<Store.Type, List<BasicElementInfo>> storeNodes, @NotNull final String message) {
		if (storeNodes.isEmpty() || !LOGGER.isDebugEnabled()) {
			return;
		}
		LOGGER.debug(
				"{} {}:\n{}",
				storeNodes.size(),
				message,
				getFsObjectsLoggingFormatHelper().formatStoreNodesMap(storeNodes)
		);
	}
}
