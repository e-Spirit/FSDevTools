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

import de.espirit.firstspirit.access.store.Store;
import de.espirit.firstspirit.store.access.nexport.ElementExportInfo;
import de.espirit.firstspirit.store.access.nexport.EntityTypeExportInfo;
import de.espirit.firstspirit.store.access.nexport.ExportInfo;
import de.espirit.firstspirit.store.access.nexport.PropertyTypeExportInfo;
import de.espirit.firstspirit.transport.PropertiesTransportOptions;

import java.util.*;

// [review] javadoc (sorting, ...)
class ReorganizedResult {

    private final Map<PropertiesTransportOptions.ProjectPropertyType, PropertyTypeExportInfo> _projectProperties;
    private final Map<Store.Type, List<ElementExportInfo>> _storeElements;
    private final Collection<EntityTypeExportInfo> _entityTypes;
    /**
     * Indicates whether an FS_META file is contained in this result set
     */
    private boolean _containsFsMeta;

    ReorganizedResult(final Collection<ExportInfo> elements) {
        _projectProperties = new HashMap<>();
        _storeElements = new TreeMap<>();
        _entityTypes = new ArrayList<>();
        reorganize(elements);
    }

    private void reorganize(final Collection<ExportInfo> elements) {
        for (final ExportInfo element : elements) {
            switch (element.getType()) {
                case PROJECT_PROPERTY: {
                    final PropertyTypeExportInfo exportInfo = (PropertyTypeExportInfo) element;
                    _projectProperties.put(exportInfo.getPropertyType(), exportInfo);
                    break;
                }
                case ELEMENT: {
                    final ElementExportInfo exportInfo = (ElementExportInfo) element;
                    final Store.Type storeType = exportInfo.getElementInfo().getStoreType();
                    List<ElementExportInfo> exportInfoList = _storeElements.get(storeType);
                    if (exportInfoList == null) {
                        exportInfoList = new ArrayList<>();
                        _storeElements.put(storeType, exportInfoList);
                    }
                    exportInfoList.add(exportInfo);
                    break;
                }
                case ENTITY_TYPE: {
                    final EntityTypeExportInfo exportInfo = (EntityTypeExportInfo) element;
                    _entityTypes.add(exportInfo);
                    break;
                }
                case FS_META:
                    _containsFsMeta = true;
                    break;
                default: {
                    // ignore FS-META
                    break;
                }
            }
        }
    }

    Collection<PropertyTypeExportInfo> getProjectProperties() {
        return _projectProperties.values();
    }

    Map<Store.Type, List<ElementExportInfo>> getStoreElements() {
        return _storeElements;
    }

    Collection<EntityTypeExportInfo> getEntityTypes() {
        return Collections.unmodifiableCollection(_entityTypes);
    }

    /**
     * Indicates whether an FS_META (e.g. .FirstSpirit/Import*.txt)  file is contained in this result set
     */
    public boolean containsFsMeta() {
        return _containsFsMeta;
    }
}
