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

package com.espirit.moddev.cli.api.parsing.identifier;

import de.espirit.firstspirit.agency.StoreAgent;
import de.espirit.firstspirit.store.access.nexport.operations.ExportOperation;
import de.espirit.firstspirit.transport.PropertiesTransportOptions;

import java.util.EnumSet;

public class ProjectPropertiesIdentifier implements Identifier {

	private final EnumSet<PropertiesTransportOptions.ProjectPropertyType> projectPropertyTypes;

	public ProjectPropertiesIdentifier(EnumSet<PropertiesTransportOptions.ProjectPropertyType> projectPropertyTypes) {
		if (projectPropertyTypes == null) {
			throw new IllegalArgumentException("projectPropertyType is null.");
		}
		this.projectPropertyTypes = EnumSet.copyOf(projectPropertyTypes);
	}

	@Override
	public void addToExportOperation(StoreAgent storeAgent, boolean useReleaseState, ExportOperation exportOperation) {
		final PropertiesTransportOptions options = exportOperation.configurePropertiesExport();
		options.setProjectPropertiesTransport(projectPropertyTypes);
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || o.getClass() != this.getClass()) {
			return false;
		} else if (this == o) {
			return true;
		} else {
			final ProjectPropertiesIdentifier that = (ProjectPropertiesIdentifier) o;
			return projectPropertyTypes.equals(that.projectPropertyTypes);
		}
	}

	@Override
	public int hashCode() {
		return projectPropertyTypes.hashCode();
	}

	@Override
	public String toString() {
		return projectPropertyTypes.toString();
	}
}
