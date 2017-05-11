package com.espirit.moddev.cli.results.logging;

import de.espirit.firstspirit.store.access.nexport.ExportInfo;
import de.espirit.firstspirit.store.access.nexport.io.ExportInfoFileHandleImpl;

public class MockedFileHandle extends ExportInfoFileHandleImpl {

    private final String _path;
    private final String _fileName;

    MockedFileHandle(final ExportInfo exportInfo, final String path, final String fileName) {
        super(null, exportInfo);
        _path = path;
        _fileName = fileName;
    }

    @Override
    public String getName() {
        return _fileName;
    }

    @Override
    public String getPath() {
        return _path;
    }
}