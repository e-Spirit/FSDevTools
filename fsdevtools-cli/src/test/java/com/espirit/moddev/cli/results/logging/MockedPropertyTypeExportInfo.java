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

package com.espirit.moddev.cli.results.logging;

import de.espirit.firstspirit.store.access.nexport.PropertyTypeExportInfo;
import de.espirit.firstspirit.transport.PropertiesTransportOptions;

public class MockedPropertyTypeExportInfo extends MockedExportInfo implements PropertyTypeExportInfo {

    private final PropertiesTransportOptions.ProjectPropertyType _propertyType;

    MockedPropertyTypeExportInfo(final PropertiesTransportOptions.ProjectPropertyType propertyType) {
        super(Type.PROJECT_PROPERTY);
        _propertyType = propertyType;
        setCreatedFileHandles(createFileHandleCollection(this, 1));
        setUpdatedFileHandles(createFileHandleCollection(this, 2));
        setDeletedFileHandles(createFileHandleCollection(this, 3));
        setMovedFileHandles(createMovedFileHandleCollection(this, 4));
    }

    @Override
    public String getName() {
        return _propertyType == null ? "Property FS metadata" : _propertyType.name();
    }

    @Override
    public PropertiesTransportOptions.ProjectPropertyType getPropertyType() {
        return _propertyType;
    }
}
