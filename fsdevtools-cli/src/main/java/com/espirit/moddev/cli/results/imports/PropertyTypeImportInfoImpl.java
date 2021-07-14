/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2021 e-Spirit AG
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
import de.espirit.firstspirit.store.access.nexport.ExportStatus;
import de.espirit.firstspirit.store.access.nexport.PropertyTypeExportInfo;
import de.espirit.firstspirit.transport.PropertiesTransportOptions.ProjectPropertyType;

/**
 * ImportInfo for project properties. This class <b>may</b> be included in newer versions of FirstSpirit.
 * This class wraps the imported/updated project properties for the result logging.
 */
public class PropertyTypeImportInfoImpl extends ImportInfoImpl implements PropertyTypeExportInfo {

    private final ProjectPropertyType _propertyType;

    /**
     * Constructor
     * @param status the status
     * @param propertyType the property type
     */
    public PropertyTypeImportInfoImpl(final ExportStatus status, final ProjectPropertyType propertyType) {
        super(Type.PROJECT_PROPERTY, status);
        _propertyType = propertyType;
    }

    @Override
    public ProjectPropertyType getPropertyType() {
        return _propertyType;
    }

    @Override
    public String toString() {
        return "PropertyTypeImportInfo { " + getName() + " }";
    }

    @Override
    public String getName() {
        if (_propertyType == null) {
            // meta file in global directory
            return PROPERTY_FS_METADATA;
        }
        return _propertyType.name();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getType()).append(getName()).toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return obj != null && this.getClass().equals(obj.getClass()) && obj.hashCode() == hashCode();
    }
}
