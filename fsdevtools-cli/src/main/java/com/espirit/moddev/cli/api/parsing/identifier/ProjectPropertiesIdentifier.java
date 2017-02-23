package com.espirit.moddev.cli.api.parsing.identifier;

import java.util.EnumSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.espirit.firstspirit.agency.StoreAgent;
import de.espirit.firstspirit.store.access.nexport.operations.ExportOperation;
import de.espirit.firstspirit.transport.PropertiesTransportOptions;

/**
 *
 * @author kohlbrecher
 */
public class ProjectPropertiesIdentifier implements Identifier {
    
    protected static final Logger LOGGER = LoggerFactory.getLogger(ProjectPropertiesIdentifier.class);

    final EnumSet<PropertiesTransportOptions.ProjectPropertyType> projectPropertyTypes;

    public ProjectPropertiesIdentifier(EnumSet<PropertiesTransportOptions.ProjectPropertyType> projectPropertyTypes) {
        if (projectPropertyTypes == null) {
            throw new IllegalArgumentException("projectPropertyType is null.");
        }
        this.projectPropertyTypes = projectPropertyTypes;
    }

    @Override
    public void addToExportOperation(StoreAgent storeAgent, ExportOperation exportOperation) {
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
