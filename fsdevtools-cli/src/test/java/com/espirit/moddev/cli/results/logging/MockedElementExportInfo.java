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

import de.espirit.firstspirit.access.store.BasicElementInfo;
import de.espirit.firstspirit.access.store.Store;
import de.espirit.firstspirit.store.access.BasicElementInfoImpl;
import de.espirit.firstspirit.store.access.nexport.ElementExportInfo;
import de.espirit.firstspirit.store.access.nexport.ExportStatus;

public class MockedElementExportInfo extends MockedExportInfo implements ElementExportInfo {

	private final BasicElementInfoImpl _basicElementInfo;

	MockedElementExportInfo(final Store.Type storeType, final String name) {
		this(storeType, name, ExportStatus.CREATED);
	}

	MockedElementExportInfo(final Store.Type storeType, final String name, final TagNames nodeTag) {
		this(storeType, name, nodeTag, ExportStatus.CREATED);
	}

	MockedElementExportInfo(final Store.Type storeType, final String name, final ExportStatus exportStatus) {
		this(storeType, name, TagNames.PAGE, exportStatus);
	}

	MockedElementExportInfo(final Store.Type storeType, final String name, final TagNames nodeTag, final ExportStatus exportStatus) {
		super(Type.ELEMENT, name, exportStatus);
		_basicElementInfo = new BasicElementInfoImpl(storeType, nodeTag.getName(), -1, name, -1);
		setCreatedFileHandles(createFileHandleCollection(this, 1));
		setUpdatedFileHandles(createFileHandleCollection(this, 2));
		setDeletedFileHandles(createFileHandleCollection(this, 3));
		setMovedFileHandles(createMovedFileHandleCollection(this, 4));
	}

	@Override
	public BasicElementInfo getElementInfo() {
		return _basicElementInfo;
	}
}
