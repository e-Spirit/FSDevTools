package com.espirit.moddev.cli.results.imports;

import de.espirit.common.util.HashCodeBuilder;
import de.espirit.firstspirit.access.database.BasicEntityInfo;
import de.espirit.firstspirit.access.store.BasicElementInfo;
import de.espirit.firstspirit.store.access.nexport.EntityTypeExportInfo;
import de.espirit.firstspirit.store.access.nexport.ExportStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

/**
 * ImportInfo for entity types. This class <b>may</b> be included in newer versions of FirstSpirit.
 * This class wraps the imported/updated entity types for the result logging.
 */
public class EntityTypeImportInfoImpl extends ImportInfoImpl implements EntityTypeExportInfo {

    private final BasicElementInfo _schema;
    private final String _entityType;
    private final Collection<BasicEntityInfo> _entities;

    public EntityTypeImportInfoImpl(@NotNull final ExportStatus exportStatus, @NotNull final BasicElementInfo schema, @NotNull final String entityType, @NotNull final Collection<BasicEntityInfo> entities) {
        super(Type.ENTITY_TYPE, exportStatus);
        _schema = schema;
        _entityType = entityType;
        _entities = Collections.unmodifiableCollection(entities);
    }

    @Override
    public String getName() {
        return _schema.getUid() + '#' + getEntityType();
    }

    @NotNull
    @Override
    public BasicElementInfo getSchema() {
        return _schema;
    }

    @Override
    public String getEntityType() {
        return _entityType;
    }

    @NotNull
    @Override
    public Collection<BasicEntityInfo> getEntities() {
        return _entities;
    }

    @Override
    public boolean allEntitiesExported() {
        return false;
    }

    @Override
    public String toString() {
        return "EntityTypeImportInfo { schema=" + getSchema().getUid() + " ; entityType=" + getEntityType() + " }";
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getType()).append(_schema.getUid()).append(_schema).append(getEntityType()).append(_entities).append(allEntitiesExported()).toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return obj != null && this.getClass().equals(obj.getClass()) && obj.hashCode() == hashCode();
    }
}
