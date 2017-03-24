package com.espirit.moddev.cli.api.parsing.identifier;

import java.util.EnumSet;

import de.espirit.firstspirit.agency.StoreAgent;
import de.espirit.firstspirit.store.access.nexport.operations.ExportOperation;
import de.espirit.firstspirit.transport.PropertiesTransportOptions;

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
        if(o == null || o.getClass() != this.getClass()) {
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
