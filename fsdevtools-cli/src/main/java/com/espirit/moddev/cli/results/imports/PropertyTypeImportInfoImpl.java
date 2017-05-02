package com.espirit.moddev.cli.results.imports;

import de.espirit.common.util.HashCodeBuilder;
import de.espirit.firstspirit.store.access.nexport.ExportStatus;
import de.espirit.firstspirit.store.access.nexport.PropertyTypeExportInfo;
import de.espirit.firstspirit.transport.PropertiesTransportOptions.ProjectPropertyType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * ImportInfo for project properties. This class <b>may</b> be included in newer versions of FirstSpirit.
 * This class wraps the imported/updated project properties for the result logging.
 */
public class PropertyTypeImportInfoImpl extends ImportInfoImpl implements PropertyTypeExportInfo {

    private final ProjectPropertyType _propertyType;

    public PropertyTypeImportInfoImpl(@NotNull final ExportStatus exportStatus, @Nullable final ProjectPropertyType propertyType) {
        super(Type.PROJECT_PROPERTY, exportStatus);
        _propertyType = propertyType;
    }

    @Nullable
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
