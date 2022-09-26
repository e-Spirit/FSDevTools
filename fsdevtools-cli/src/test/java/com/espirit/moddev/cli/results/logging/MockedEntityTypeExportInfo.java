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

package com.espirit.moddev.cli.results.logging;

import de.espirit.firstspirit.access.database.BasicEntityInfo;
import de.espirit.firstspirit.access.database.BasicEntityInfoImpl;
import de.espirit.firstspirit.access.store.BasicElementInfo;
import de.espirit.firstspirit.access.store.Store;
import de.espirit.firstspirit.store.access.BasicElementInfoImpl;
import de.espirit.firstspirit.store.access.nexport.EntityTypeExportInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class MockedEntityTypeExportInfo extends MockedExportInfo implements EntityTypeExportInfo {

	private final String _entityType;
	private final BasicElementInfoImpl _schemaElementInfo;
	private Collection<BasicEntityInfo> _entities;

	MockedEntityTypeExportInfo(final String entityType) {
		this(entityType, "mockSchema");
	}

	MockedEntityTypeExportInfo(final String entityType, final String schemaName) {
		this(entityType, schemaName, 0);
	}

	MockedEntityTypeExportInfo(final String entityType, final String schemaName, final int entityCount) {
		super(Type.ENTITY_TYPE);
		_entityType = entityType;
		_schemaElementInfo = new BasicElementInfoImpl(Store.Type.TEMPLATESTORE, schemaName, -1, schemaName, -1);
		_entities = new ArrayList<>();
		for (int index = 0; index < entityCount; index++) {
			_entities.add(new BasicEntityInfoImpl(UUID.nameUUIDFromBytes(("entity_" + index).getBytes()), entityType, schemaName));
		}
		setCreatedFileHandles(createFileHandleCollection(this, 1));
		setUpdatedFileHandles(createFileHandleCollection(this, 2));
		setDeletedFileHandles(createFileHandleCollection(this, 3));
		setMovedFileHandles(createMovedFileHandleCollection(this, 4));
	}

	@Override
	public String getName() {
		return _schemaElementInfo.getUid() + '#' + getEntityType();
	}

	@Override
	public String getEntityType() {
		return _entityType;
	}

	@Override
	public BasicElementInfo getSchema() {
		return _schemaElementInfo;
	}

	@Override
	public Collection<BasicEntityInfo> getEntities() {
		return _entities;
	}

	@Override
	public boolean allEntitiesExported() {
		return false;
	}
}
