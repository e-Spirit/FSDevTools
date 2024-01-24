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

package com.espirit.moddev.cli.results.logging;

import de.espirit.common.util.Pair;
import de.espirit.firstspirit.io.FileHandle;
import de.espirit.firstspirit.store.access.nexport.ElementExportInfo;
import de.espirit.firstspirit.store.access.nexport.ExportInfo;
import de.espirit.firstspirit.store.access.nexport.ExportStatus;
import de.espirit.firstspirit.store.access.nexport.io.ExportInfoFileHandle;
import de.espirit.firstspirit.store.access.nexport.operations.ExportOperation;
import de.espirit.firstspirit.transport.PropertiesTransportOptions;

import java.util.*;

public class MockedExportResult implements ExportOperation.Result {

	private Collection<ExportInfo> _createdElements, _updateElements, _deletedElements, _movedElements;

	MockedExportResult() {
		this(true);
	}

	MockedExportResult(boolean fill) {
		if (fill) {
			fill();
		} else {
			_createdElements = Collections.emptyList();
			_updateElements = Collections.emptyList();
			_deletedElements = Collections.emptyList();
			_movedElements = Collections.emptyList();
		}
	}

	private void fill() {
		{
			_createdElements = fillCollection(MockedElementExportInfo.createMapWithStoreElements(ExportStatus.CREATED, true, false, false, true).values());
			_createdElements.add(new MockedPropertyTypeExportInfo(PropertiesTransportOptions.ProjectPropertyType.USERS));
			_createdElements.add(new MockedEntityTypeExportInfo("createdType", "createdSchema", 1));
		}
		{
			_updateElements = fillCollection(MockedElementExportInfo.createMapWithStoreElements(ExportStatus.UPDATED, false, true, true, false).values());
			_updateElements.add(new MockedPropertyTypeExportInfo(PropertiesTransportOptions.ProjectPropertyType.USERS));
			_updateElements.add(new MockedEntityTypeExportInfo("updatedType1", "updatedSchema1", 2));
			_updateElements.add(new MockedEntityTypeExportInfo("updatedType2", "updatedSchema1", 3));
			_updateElements.add(new MockedEntityTypeExportInfo("updatedType1", "updatedSchema2", 1));
			_updateElements.add(new MockedExportInfo(ExportInfo.Type.FS_META));
		}
		{
			_deletedElements = fillCollection(MockedElementExportInfo.createMapWithStoreElements(ExportStatus.DELETED, true, false, true, false).values());
			_deletedElements.add(new MockedPropertyTypeExportInfo(PropertiesTransportOptions.ProjectPropertyType.USERS));
			_deletedElements.add(new MockedEntityTypeExportInfo("deletedType1", "deletedSchema", 3));
			_deletedElements.add(new MockedEntityTypeExportInfo("deletedType2", "deletedSchema", 4));
		}
		{
			_movedElements = fillCollection(MockedElementExportInfo.createMapWithStoreElements(ExportStatus.MOVED, false, true, false, true).values());
			_movedElements.add(new MockedPropertyTypeExportInfo(PropertiesTransportOptions.ProjectPropertyType.USERS));
			_movedElements.add(new MockedEntityTypeExportInfo("movedType1", "movedSchema1", 1));
			_movedElements.add(new MockedEntityTypeExportInfo("movedType2", "movedSchema2", 2));
		}
	}

	private Collection<ExportInfo> fillCollection(final Collection<List<ElementExportInfo>> values) {
		final Collection<ExportInfo> result = new ArrayList<>();
		for (final List<ElementExportInfo> list : values) {
			result.addAll(list);
		}
		return result;
	}

	@Override
	public Set<FileHandle> getCreatedFiles() {
		return null;
	}

	@Override
	public Set<FileHandle> getUpdatedFiles() {
		return null;
	}

	@Override
	public Set<FileHandle> getDeletedFiles() {
		return null;
	}

	@Override
	public Set<ExportInfoFileHandle> getCreatedFileHandles() {
		return null;
	}

	@Override
	public Set<ExportInfoFileHandle> getUpdatedFileHandles() {
		return null;
	}

	@Override
	public Set<ExportInfoFileHandle> getDeletedFileHandles() {
		return null;
	}

	@Override
	public Collection<Pair<ExportInfoFileHandle, ExportInfoFileHandle>> getMovedFileHandles() {
		return null;
	}

	@Override
	public Collection<ExportInfo> getCreatedElements() {
		return _createdElements;
	}

	@Override
	public Collection<ExportInfo> getUpdatedElements() {
		return _updateElements;
	}

	@Override
	public Collection<ExportInfo> getDeletedElements() {
		return _deletedElements;
	}

	@Override
	public Collection<ExportInfo> getMovedElements() {
		return _movedElements;
	}
}
