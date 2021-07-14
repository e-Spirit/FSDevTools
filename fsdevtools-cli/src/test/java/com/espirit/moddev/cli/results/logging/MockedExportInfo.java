/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2021 e-Spirit AG
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

package com.espirit.moddev.cli.results.logging;

import de.espirit.common.util.Pair;
import de.espirit.firstspirit.access.store.Store;
import de.espirit.firstspirit.store.access.nexport.ElementExportInfo;
import de.espirit.firstspirit.store.access.nexport.ExportInfo;
import de.espirit.firstspirit.store.access.nexport.ExportStatus;
import de.espirit.firstspirit.store.access.nexport.io.ExportInfoFileHandle;

import java.util.*;

public class MockedExportInfo implements ExportInfo {

    private final Type _type;
    private final String _name;
    private final ExportStatus _exportStatus;
    private final Set<ExportInfoFileHandle> _createdFileHandles, _updatedFileHandles, _deletedFileHandles;
    private final Collection<Pair<ExportInfoFileHandle, ExportInfoFileHandle>> _movedFileHandles;

    public MockedExportInfo() {
        this(Type.ELEMENT);
    }

    public MockedExportInfo(final Type type) {
        this(type, "testName", ExportStatus.CREATED);
    }

    public MockedExportInfo(final Type type, final String name, final ExportStatus exportStatus) {
        _type = type;
        _name = name;
        _exportStatus = exportStatus;
        _createdFileHandles = new HashSet<>();
        _updatedFileHandles = new HashSet<>();
        _deletedFileHandles = new HashSet<>();
        _movedFileHandles = new ArrayList<>();
    }

    @Override
    public Type getType() {
        return _type;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public ExportStatus getStatus() {
        return _exportStatus;
    }

    public void setCreatedFileHandles(final Collection<ExportInfoFileHandle> createdFileHandles) {
        _createdFileHandles.clear();
        _createdFileHandles.addAll(createdFileHandles);
    }

    @Override
    public Set<ExportInfoFileHandle> getCreatedFileHandles() {
        return _createdFileHandles;
    }

    public void setUpdatedFileHandles(final Collection<ExportInfoFileHandle> updatedFileHandles) {
        _updatedFileHandles.clear();
        _updatedFileHandles.addAll(updatedFileHandles);
    }

    @Override
    public Set<ExportInfoFileHandle> getUpdatedFileHandles() {
        return _updatedFileHandles;
    }

    public void setDeletedFileHandles(final Collection<ExportInfoFileHandle> deletedFileHandles) {
        _deletedFileHandles.clear();
        _deletedFileHandles.addAll(deletedFileHandles);
    }

    @Override
    public Set<ExportInfoFileHandle> getDeletedFileHandles() {
        return _deletedFileHandles;
    }

    public void setMovedFileHandles(final Collection<Pair<ExportInfoFileHandle, ExportInfoFileHandle>> movedFileHandles) {
        _movedFileHandles.clear();
        _movedFileHandles.addAll(movedFileHandles);
    }

    @Override
    public Collection<Pair<ExportInfoFileHandle, ExportInfoFileHandle>> getMovedFileHandles() {
        return _movedFileHandles;
    }

    public static Map<Store.Type, List<ElementExportInfo>> createMapWithStoreElements() {
        return createMapWithStoreElements(ExportStatus.CREATED, true, true, true, true);
    }

    public static Map<Store.Type, List<ElementExportInfo>> createMapWithStoreElements(final ExportStatus status, final boolean pageStore, final boolean mediaStore, final boolean siteStore, final boolean templateStore) {

        final Map<Store.Type, List<ElementExportInfo>> storeElements = new TreeMap<>();
        if (pageStore) {
            final Store.Type storeType = Store.Type.PAGESTORE;
            final List<ElementExportInfo> list = new ArrayList<>();
            list.add(new MockedElementExportInfo(storeType, "first", TagNames.PAGE, status));
            storeElements.put(storeType, list);
        }
        if (mediaStore) {
            final Store.Type storeType = Store.Type.MEDIASTORE;
            final List<ElementExportInfo> list = new ArrayList<>();
            list.add(new MockedElementExportInfo(storeType, "second", TagNames.MEDIUM, status));
            list.add(new MockedElementExportInfo(storeType, "first", TagNames.MEDIUM, status));
            list.add(new MockedElementExportInfo(storeType, "third", TagNames.MEDIANODE, status));
            storeElements.put(storeType, list);
        }
        if (siteStore) {
            final Store.Type storeType = Store.Type.SITESTORE;
            final List<ElementExportInfo> list = new ArrayList<>();
            list.add(new MockedElementExportInfo(storeType, "third", TagNames.PAGEREF, status));
            list.add(new MockedElementExportInfo(storeType, "first", TagNames.PAGEREF, status));
            list.add(new MockedElementExportInfo(storeType, "second", TagNames.PAGEREF, status));
            storeElements.put(storeType, list);
        }
        if (templateStore) {
            final Store.Type storeType = Store.Type.TEMPLATESTORE;
            final List<ElementExportInfo> list = new ArrayList<>();
            list.add(new MockedElementExportInfo(storeType, "second", TagNames.WORKFLOW, status));
            list.add(new MockedElementExportInfo(storeType, "fourth", TagNames.TEMPLATE, status));
            list.add(new MockedElementExportInfo(storeType, "third", TagNames.LINKTEMPLATE, status));
            list.add(new MockedElementExportInfo(storeType, "first", TagNames.TEMPLATE, status));
            storeElements.put(storeType, list);
        }
        return storeElements;
    }

    public static Collection<ExportInfoFileHandle> createFileHandleCollection(final ExportInfo exportInfo, final int amount) {
        final Collection<ExportInfoFileHandle> result = new ArrayList<>();
        for (int index = 0; index < amount; index++) {
            // reversed order for file names & paths (to check sorting)
            result.add(new MockedFileHandle(exportInfo, "/path/" + exportInfo.getName() + "/" + (amount - index - 1) + ".txt", (amount - index - 1) + ".txt"));
        }
        return result;
    }

    public static Collection<Pair<ExportInfoFileHandle, ExportInfoFileHandle>> createMovedFileHandleCollection(final ExportInfo exportInfo, final int amount) {
        final Collection<Pair<ExportInfoFileHandle, ExportInfoFileHandle>> result = new ArrayList<>();
        for (int index = 0; index < amount; index++) {
            // reversed order for file names & paths (to check sorting)
            result.add(new Pair<>(new MockedFileHandle(exportInfo, "/from/" + exportInfo.getName() + "/" + (amount - index - 1) + ".txt", (amount - index - 1) + ".txt"), new MockedFileHandle(exportInfo, "/to/" + exportInfo.getName() + "/" + (amount - index - 1) + ".txt", (amount - index - 1) + ".txt")));
        }
        return result;
    }
}
