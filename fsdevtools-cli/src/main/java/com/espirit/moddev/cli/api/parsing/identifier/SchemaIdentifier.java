/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2020 e-Spirit AG
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

package com.espirit.moddev.cli.api.parsing.identifier;

import com.espirit.moddev.shared.StringUtils;
import com.espirit.moddev.shared.annotation.VisibleForTesting;
import de.espirit.firstspirit.access.store.Store;
import de.espirit.firstspirit.access.store.templatestore.Schema;
import de.espirit.firstspirit.access.store.templatestore.TemplateStoreRoot;
import de.espirit.firstspirit.agency.StoreAgent;
import de.espirit.firstspirit.store.access.nexport.operations.ExportOperation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Identifier for FirstSpirit schemas.
 */
public class SchemaIdentifier implements Identifier {

	protected static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SchemaIdentifier.class);

	static final String OPTION_EXPORT_GID_MAPPING = "exportGidMapping";

	@VisibleForTesting
	public static Set<String> VALID_SCHEMA_OPTIONS = new HashSet<>();

	static {
		resetValidSchemaOptions();
	}

	private final String uid;
	private final Map<String, String> schemaOptions;

	/**
	 * Instantiates a new identifier for schemas.
	 *
	 * @param uid           the uid of the Schema object
	 * @param schemaOptions A map of {@link de.espirit.firstspirit.store.access.nexport.operations.ExportOperation.SchemaOptions}, can be an empty map
	 * @throws IllegalArgumentException if a null or empty string is passed as uid
	 */
	public SchemaIdentifier(@Nullable final String uid, @NotNull final Map<String, String> schemaOptions) {
		if (StringUtils.isNullOrEmpty(uid)) {
			throw new IllegalArgumentException("Don't pass an empty or null uid to schema identifier!");
		}
		this.uid = uid;
		this.schemaOptions = schemaOptions;
	}

	@Override
	public void addToExportOperation(@NotNull final StoreAgent storeAgent, final boolean useReleaseState, @NotNull final ExportOperation exportOperation) {
		LOGGER.debug("Adding Schema: {}", uid);
		final ExportOperation.SchemaOptions exportSchemaOptions = exportOperation.addSchema(getSchema(storeAgent, useReleaseState));
		setSchemaOptions(exportSchemaOptions);
	}

	void setSchemaOptions(@NotNull final ExportOperation.SchemaOptions exportSchemaOptions) {
		for (final Map.Entry<String, String> entry : schemaOptions.entrySet()) {
			if (OPTION_EXPORT_GID_MAPPING.equalsIgnoreCase(entry.getKey())) {
				exportSchemaOptions.setExportGidMapping(Boolean.parseBoolean(entry.getValue()));
			}
		}
	}

	@NotNull
	private Schema getSchema(@NotNull final StoreAgent storeAgent, final boolean useReleaseState) {
		final TemplateStoreRoot store = (TemplateStoreRoot) storeAgent.getStore(Store.Type.TEMPLATESTORE, useReleaseState);

		final Schema schema = store.getSchemes().getSchemaByName(uid);

		if (schema == null) {
			throw new IllegalStateException("Schema for content2 object with uid " + uid + " couldn't be found.");
		}
		return schema;
	}

	@Override
	public boolean equals(@Nullable final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final SchemaIdentifier that = (SchemaIdentifier) o;
		return Objects.equals(uid, that.uid) && Objects.equals(schemaOptions, that.schemaOptions);
	}

	@Override
	public int hashCode() {
		return Objects.hash(uid, schemaOptions);
	}

	public static boolean isSchemaOptionValid(@NotNull final String optionName) {
		return VALID_SCHEMA_OPTIONS.contains(optionName.trim().toLowerCase());
	}

	@VisibleForTesting
	public static void resetValidSchemaOptions() {
		VALID_SCHEMA_OPTIONS.clear();
		VALID_SCHEMA_OPTIONS.add(OPTION_EXPORT_GID_MAPPING.toLowerCase());
	}

}
