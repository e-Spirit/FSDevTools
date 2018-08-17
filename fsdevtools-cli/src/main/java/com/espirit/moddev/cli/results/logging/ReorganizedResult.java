package com.espirit.moddev.cli.results.logging;

import de.espirit.firstspirit.access.store.Store;
import de.espirit.firstspirit.store.access.nexport.ElementExportInfo;
import de.espirit.firstspirit.store.access.nexport.EntityTypeExportInfo;
import de.espirit.firstspirit.store.access.nexport.ExportInfo;
import de.espirit.firstspirit.store.access.nexport.PropertyTypeExportInfo;
import de.espirit.firstspirit.transport.PropertiesTransportOptions;
import org.jetbrains.annotations.NotNull;

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

    ReorganizedResult(@NotNull final Collection<ExportInfo> elements) {
        _projectProperties = new HashMap<>();
        _storeElements = new TreeMap<>();
        _entityTypes = new ArrayList<>();
        reorganize(elements);
    }

    private void reorganize(@NotNull final Collection<ExportInfo> elements) {
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

    @NotNull
    Collection<PropertyTypeExportInfo> getProjectProperties() {
        return _projectProperties.values();
    }

    @NotNull
    Map<Store.Type, List<ElementExportInfo>> getStoreElements() {
        return _storeElements;
    }

    @NotNull
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