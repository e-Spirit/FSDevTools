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
