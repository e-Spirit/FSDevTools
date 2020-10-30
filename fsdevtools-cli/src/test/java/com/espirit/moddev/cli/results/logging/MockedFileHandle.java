/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2020 e-Spirit AG
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
