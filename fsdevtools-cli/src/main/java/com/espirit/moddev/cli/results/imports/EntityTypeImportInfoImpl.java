/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2021 e-Spirit GmbH
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

package com.espirit.moddev.cli.results.imports;

import de.espirit.common.util.HashCodeBuilder;
import de.espirit.firstspirit.access.database.BasicEntityInfo;
import de.espirit.firstspirit.access.store.BasicElementInfo;
import de.espirit.firstspirit.store.access.nexport.EntityTypeExportInfo;
import de.espirit.firstspirit.store.access.nexport.ExportStatus;

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

    /**
     * Constructor
     * @param status the status
     * @param schema the schema
     * @param entityType the entity type
     * @param entities the collection of entities
     */
    public EntityTypeImportInfoImpl(final ExportStatus status, final BasicElementInfo schema, final String entityType, final Collection<BasicEntityInfo> entities) {
        super(Type.ENTITY_TYPE, status);
        _schema = schema;
        _entityType = entityType;
        _entities = Collections.unmodifiableCollection(entities);
    }

    @Override
    public String getName() {
        return _schema.getUid() + '#' + getEntityType();
    }

    @Override
    public BasicElementInfo getSchema() {
        return _schema;
    }

    @Override
    public String getEntityType() {
        return _entityType;
    }

    @Override
    @SuppressWarnings("squid:S2384")
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
