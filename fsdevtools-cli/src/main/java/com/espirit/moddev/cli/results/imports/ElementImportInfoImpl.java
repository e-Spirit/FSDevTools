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

package com.espirit.moddev.cli.results.imports;

import de.espirit.common.util.HashCodeBuilder;
import de.espirit.firstspirit.access.store.BasicElementInfo;
import de.espirit.firstspirit.store.access.nexport.ElementExportInfo;
import de.espirit.firstspirit.store.access.nexport.ExportStatus;

/**
 * ImportInfo for store elements. This class <b>may</b> be included in newer versions of FirstSpirit.
 * This class wraps the imported/updated store elements for the result logging.
 */
public class ElementImportInfoImpl extends ImportInfoImpl implements ElementExportInfo {

	private final BasicElementInfo _elementInfo;

	/**
	 * Constructor
	 *
	 * @param status      the status
	 * @param elementInfo the element info
	 */
	public ElementImportInfoImpl(final ExportStatus status, final BasicElementInfo elementInfo) {
		super(Type.ELEMENT, status);
		_elementInfo = elementInfo;
	}

	@Override
	public BasicElementInfo getElementInfo() {
		return _elementInfo;
	}

	@Override
	public String getName() {
		return _elementInfo.getUid();
	}

	@Override
	public String toString() {
		return "ElementImportInfo { store=" + getElementInfo().getStoreType().getName() + " ; uid=" + getElementInfo().getUid() + " ; id=" + getElementInfo().getNodeId() + " }";
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(getType()).append(_elementInfo.getUid()).append(_elementInfo).toHashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		return obj != null && this.getClass().equals(obj.getClass()) && obj.hashCode() == hashCode();
	}
}
