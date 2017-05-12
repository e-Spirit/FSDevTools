package com.espirit.moddev.cli.results.logging;

import de.espirit.firstspirit.access.database.BasicEntityInfo;
import de.espirit.firstspirit.access.database.BasicEntityInfoImpl;
import de.espirit.firstspirit.access.store.BasicElementInfo;
import de.espirit.firstspirit.access.store.Store;
import de.espirit.firstspirit.store.access.BasicElementInfoImpl;
import de.espirit.firstspirit.store.access.nexport.EntityTypeExportInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class MockedEntityTypeExportInfo extends MockedExportInfo implements EntityTypeExportInfo {

    private final String _entityType;
    private final BasicElementInfoImpl _schemaElementInfo;
    private Collection<BasicEntityInfo> _entities;

    MockedEntityTypeExportInfo(final String entityType) {
        this(entityType, "mockSchema");
    }

    MockedEntityTypeExportInfo(final String entityType, final String schemaName) {
        this(entityType, schemaName, 0);
    }

    MockedEntityTypeExportInfo(final String entityType, final String schemaName, final int entityCount) {
        super(Type.ENTITY_TYPE);
        _entityType = entityType;
        _schemaElementInfo = new BasicElementInfoImpl(Store.Type.TEMPLATESTORE, schemaName, -1, schemaName, -1);
        _entities = new ArrayList<>();
        for (int index = 0; index < entityCount; index++) {
            _entities.add(new BasicEntityInfoImpl(UUID.nameUUIDFromBytes(("entity_" + index).getBytes()), entityType, schemaName));
        }
        setCreatedFileHandles(createFileHandleCollection(this, 1));
        setUpdatedFileHandles(createFileHandleCollection(this, 2));
        setDeletedFileHandles(createFileHandleCollection(this, 3));
        setMovedFileHandles(createMovedFileHandleCollection(this, 4));
    }

    @Override
    public String getName() {
        return _schemaElementInfo.getUid() + '#' + getEntityType();
    }

    @Override
    public String getEntityType() {
        return _entityType;
    }

    @Override
    public BasicElementInfo getSchema() {
        return _schemaElementInfo;
    }

    @Override
    public Collection<BasicEntityInfo> getEntities() {
        return _entities;
    }

    @Override
    public boolean allEntitiesExported() {
        return false;
    }
}