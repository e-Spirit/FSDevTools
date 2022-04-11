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

package com.espirit.moddev.cli.api.parsing.identifier;

import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.access.store.Store;
import de.espirit.firstspirit.access.store.contentstore.Content2;
import de.espirit.firstspirit.access.store.mediastore.Media;
import de.espirit.firstspirit.access.store.mediastore.MediaFolder;
import de.espirit.firstspirit.access.store.pagestore.Page;
import de.espirit.firstspirit.access.store.pagestore.PageFolder;
import de.espirit.firstspirit.access.store.sitestore.DocumentGroup;
import de.espirit.firstspirit.access.store.sitestore.PageRef;
import de.espirit.firstspirit.access.store.sitestore.PageRefFolder;
import de.espirit.firstspirit.access.store.templatestore.*;

import java.util.Locale;

import de.espirit.firstspirit.access.store.globalstore.GCAPage;

/**
 * This enum specifies all valid prefixes for {@link UidIdentifier}. A prefix
 * always determines in which store to search for an element, which FirstSpirit
 * {@link de.espirit.firstspirit.access.store.IDProvider.UidType} is addressed by
 * the prefix and by which class the found result should be filtered.
 */
public enum UidMapping {
    CONTENT2(Store.Type.CONTENTSTORE, IDProvider.UidType.CONTENTSTORE, Content2.class),
    GCAPAGE(Store.Type.GLOBALSTORE, IDProvider.UidType.GLOBALSTORE, GCAPage.class),
    MEDIAFOLDER(Store.Type.MEDIASTORE, IDProvider.UidType.MEDIASTORE_FOLDER, MediaFolder.class),
    MEDIA(Store.Type.MEDIASTORE, IDProvider.UidType.MEDIASTORE_LEAF, Media.class),
    PAGE(Store.Type.PAGESTORE, IDProvider.UidType.PAGESTORE, Page.class),
    PAGEFOLDER(Store.Type.PAGESTORE, IDProvider.UidType.PAGESTORE, PageFolder.class),
    PAGEREFFOLDER(Store.Type.SITESTORE, IDProvider.UidType.SITESTORE_FOLDER, PageRefFolder.class),
    DOCUMENTGROUP(Store.Type.SITESTORE, IDProvider.UidType.SITESTORE_LEAF, DocumentGroup.class),
    PAGEREF(Store.Type.SITESTORE, IDProvider.UidType.SITESTORE_LEAF, PageRef.class),
    PAGETEMPLATE(Store.Type.TEMPLATESTORE, IDProvider.UidType.TEMPLATESTORE, PageTemplate.class),
    SCRIPT(Store.Type.TEMPLATESTORE, IDProvider.UidType.TEMPLATESTORE, Script.class),
    SECTIONTEMPLATE(Store.Type.TEMPLATESTORE, IDProvider.UidType.TEMPLATESTORE, SectionTemplate.class),
    WORKFLOW(Store.Type.TEMPLATESTORE, IDProvider.UidType.TEMPLATESTORE, Workflow.class),
    FORMATTEMPLATE(Store.Type.TEMPLATESTORE, IDProvider.UidType.TEMPLATESTORE_FORMATTEMPLATE, FormatTemplate.class),
    LINKTEMPLATE(Store.Type.TEMPLATESTORE, IDProvider.UidType.TEMPLATESTORE_LINKTEMPLATE, LinkTemplate.class),
    QUERY(Store.Type.TEMPLATESTORE, IDProvider.UidType.TEMPLATESTORE_SCHEMA, Query.class),
    TABLETEMPLATE(Store.Type.TEMPLATESTORE, IDProvider.UidType.TEMPLATESTORE_SCHEMA, TableTemplate.class),
    STYLETEMPLATE(Store.Type.TEMPLATESTORE, IDProvider.UidType.TEMPLATESTORE_STYLETEMPLATE, StyleTemplate.class),
    TABLEFORMATTEMPLATE(Store.Type.TEMPLATESTORE, IDProvider.UidType.TEMPLATESTORE_TABLEFORMATTEMPLATE, TableFormatTemplate.class);


    private IDProvider.UidType uidType;
    private Class<?> correspondingType;
    private Store.Type storeType;

    UidMapping(Store.Type storeType, IDProvider.UidType uidType, Class correspondingType) {
        this.storeType = storeType;
        this.uidType = uidType;
        this.correspondingType = correspondingType;
    }

    /**
     * Getter for the prefix string, that identifies a mapping.
     * @return the prefix string that corresponds to this mapping
     */
    public String getPrefix() {
        return this.name().toLowerCase(Locale.UK);
    }

    /**
     * Getter for the {@link de.espirit.firstspirit.access.store.IDProvider.UidType} the
     * uid mapping corresponds to.
     * @return the {@link de.espirit.firstspirit.access.store.IDProvider.UidType} for this mapping
     */
    public IDProvider.UidType getUidType() {
        return uidType;
    }

    /**
     * Getter for the class, a uid mapping uses for filtering.
     * @return the class of which corresponding elements should be
     */
    public Class<?> getCorrespondingType() {
        return correspondingType;
    }

    /**
     * Getter for the {@link de.espirit.firstspirit.base.store.StoreType}
     * @return the {@link de.espirit.firstspirit.base.store.StoreType} used by this mapping
     */
    public Store.Type getStoreType() {
        return storeType;
    }
}
