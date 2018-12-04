package com.espirit.moddev.cli.results.logging;

import de.espirit.firstspirit.access.store.BasicElementInfo;
import de.espirit.firstspirit.access.store.Store;
import de.espirit.firstspirit.store.access.BasicElementInfoImpl;
import de.espirit.firstspirit.store.access.nexport.ElementExportInfo;
import de.espirit.firstspirit.store.access.nexport.ExportStatus;

public class MockedElementExportInfo extends MockedExportInfo implements ElementExportInfo {

    private final BasicElementInfoImpl _basicElementInfo;

    MockedElementExportInfo(final Store.Type storeType, final String name) {
        this(storeType, name, ExportStatus.CREATED);
    }

    MockedElementExportInfo(final Store.Type storeType, final String name, final TagNames nodeTag) {
        this(storeType, name, nodeTag, ExportStatus.CREATED);
    }

    MockedElementExportInfo(final Store.Type storeType, final String name, final ExportStatus exportStatus) {
        this(storeType, name, TagNames.PAGE, exportStatus);
    }

    MockedElementExportInfo(final Store.Type storeType, final String name, final TagNames nodeTag, final ExportStatus exportStatus) {
        super(Type.ELEMENT, name, exportStatus);
        _basicElementInfo = new BasicElementInfoImpl(storeType, nodeTag.getName(), -1, name, -1);
        setCreatedFileHandles(createFileHandleCollection(this, 1));
        setUpdatedFileHandles(createFileHandleCollection(this, 2));
        setDeletedFileHandles(createFileHandleCollection(this, 3));
        setMovedFileHandles(createMovedFileHandleCollection(this, 4));
    }

    @Override
    public BasicElementInfo getElementInfo() {
        return _basicElementInfo;
    }
}