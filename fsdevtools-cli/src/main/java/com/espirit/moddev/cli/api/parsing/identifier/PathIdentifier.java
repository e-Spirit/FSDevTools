package com.espirit.moddev.cli.api.parsing.identifier;

import com.espirit.moddev.cli.api.parsing.exceptions.IDProviderNotFoundException;
import de.espirit.common.tools.Objects;
import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.agency.StoreAgent;
import de.espirit.firstspirit.store.access.nexport.ExportUtil;
import de.espirit.firstspirit.store.access.nexport.operations.ExportOperation;
import org.slf4j.LoggerFactory;


/**
 * @author e-Spirit AG
 */
public class PathIdentifier implements Identifier {

    protected static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PathIdentifier.class);
    private String _path;

    /**
     * Instantiates a new path identifier
     *
     * @param path the path
     */
    public PathIdentifier(String path) {
        if (path == null) {
            throw new IllegalArgumentException("path is null");
        }
        _path = path;
    }


    @Override
    public void addToExportOperation(StoreAgent storeAgent, boolean useReleaseState, ExportOperation exportOperation) {
        final IDProvider element = ExportUtil.getElementByPath(storeAgent, useReleaseState, _path);
        if(element != null) {
            LOGGER.debug("Adding store element: {}", element);
            exportOperation.addElement(element);
        } else {
            throw new IDProviderNotFoundException("IDProvider cannot be retrieved via path '" + _path + '\'');
        }
    }

    public String getPath() {
        return _path;
    }


    @Override
    public boolean equals(final Object o) {
        if(o == null || o.getClass() != this.getClass()) {
            return false;
        } else if (this == o) {
            return true;
        } else {
            return Objects.equal(((PathIdentifier) o).getPath(), _path);
        }
    }


    @Override
    public int hashCode() {
        return _path.hashCode();
    }


    @Override
    public String toString() {
        return _path;
    }
}
