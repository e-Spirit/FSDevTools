/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2024 Crownpeak Technology GmbH
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

import de.espirit.firstspirit.io.FileHandle;
import de.espirit.firstspirit.io.FileType;
import de.espirit.firstspirit.store.access.nexport.ExportInfo;
import de.espirit.firstspirit.store.access.nexport.io.ExportInfoFileHandle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class MockedFileHandle implements ExportInfoFileHandle {

	private final ExportInfo _exportInfo;
	private final String _path;
	private final String _fileName;

	MockedFileHandle(final ExportInfo exportInfo, final String path, final String fileName) {
		_exportInfo = exportInfo;
		_path = path;
		_fileName = fileName;
	}

	@Override
	public boolean exists() {
		return false;
	}

	@Override
	public @NotNull FileType getType() {
		return FileType.UNKNOWN;
	}

	@Override
	public boolean isFile() {
		return false;
	}

	@Override
	public boolean isDirectory() {
		return false;
	}

	@Override
	public @NotNull List<FileHandle> listFiles() {
		return List.of();
	}

	@Override
	public void mkDirs() {

	}

	@Override
	public @NotNull InputStream load() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void save(final InputStream inputStream) {

	}

	@Override
	public void append(final InputStream inputStream) {

	}

	@Override
	public @NotNull OutputStream getOutputStream() {
		throw new UnsupportedOperationException();
	}

	@Override
	public @NotNull OutputStream getOutputStream(final boolean b) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getName() {
		return _fileName;
	}

	@Override
	public String getPath() {
		return _path;
	}

	@Override
	public long getSize() {
		return 0;
	}

	@Override
	public boolean hasCrc() {
		return false;
	}

	@Override
	public long getCrc() {
		return 0;
	}

	@Override
	public void delete() {

	}

	@Override
	public void rename(final String s) {

	}

	@Override
	public void swapWith(final String s) {

	}

	@Override
	public long getLastModified() {
		return 0;
	}

	@Override
	public void setLastModified(final long l) {

	}

	@Override
	public @Nullable FileHandle getParent() {
		return null;
	}

	@Override
	public FileHandle getChild(final String s) {
		return null;
	}

	@Override
	public ExportInfo getExportInfo() {
		return _exportInfo;
	}

	@Override
	public int compareTo(@NotNull final FileHandle o) {
		return 0;
	}
}
