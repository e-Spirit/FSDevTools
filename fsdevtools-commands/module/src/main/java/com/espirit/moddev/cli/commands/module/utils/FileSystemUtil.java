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

package com.espirit.moddev.cli.commands.module.utils;

import com.espirit.moddev.cli.api.result.ExecutionErrorResult;
import com.espirit.moddev.cli.api.result.ExecutionResult;
import com.espirit.moddev.cli.api.result.ExecutionResults;
import com.espirit.moddev.shared.annotation.VisibleForTesting;
import de.espirit.firstspirit.io.FileHandle;
import de.espirit.firstspirit.io.FileSystem;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;

public class FileSystemUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemUtil.class);

	@VisibleForTesting
	@NotNull
	static String buildPath(@NotNull final String[] childPath) {
		return String.join("/", childPath);
	}

	/**
	 * Uploads a collection of files/directories to the {@link FileSystem} returned by the given {@link Supplier}.
	 * Uploading a directory will also upload the contents of the directory, keeping the current directory structure.
	 *
	 * @param files      the collection of files/directories to upload
	 * @param fileSystem the {@link FileSystem}
	 * @return a {@link ExecutionResults result} containing information about the uploads
	 */
	@NotNull
	public static ExecutionResults uploadFiles(@NotNull final Collection<String> files, @NotNull final FileSystem<?> fileSystem) {
		final ExecutionResults fileResults = new ExecutionResults();
		if (!files.isEmpty()) {
			LOGGER.debug("Uploading files [ {} ]...", String.join(", ", files));
			for (final String file : files) {
				fileResults.add(uploadFiles(fileSystem, file, new String[0], true));
			}
		}
		return fileResults;
	}

	/**
	 * Uploads a file or directory to the given {@link FileSystem}. Uploading a directory will also upload the contents of the directory,
	 * keeping the current directory structure.
	 *
	 * @param fileSystem the target {@link FileSystem}
	 * @param path       the local path of the file or directory to upload
	 * @param parentPath the parent path of the current path
	 * @param isTopLevel whether the current element is on the top level or not
	 * @return a {@link ExecutionResult result} containing all information about the upload
	 */
	@NotNull
	private static ExecutionResult uploadFiles(@NotNull final FileSystem<?> fileSystem, @NotNull final String path, @NotNull final String[] parentPath, final boolean isTopLevel) {
		final File file = new File(path);
		if (file.isDirectory()) {
			// handle directory
			final ExecutionResults results = new ExecutionResults();
			final String[] files = file.list();
			if (files != null) {
				final String[] childPath;
				if (isTopLevel) {
					// top level directories are not created, otherwise it would be impossible to upload the contents of a directory directly to the root
					childPath = parentPath;
				} else {
					// copy the current path and append the current directory
					childPath = new String[parentPath.length + 1];
					System.arraycopy(parentPath, 0, childPath, 0, parentPath.length);
					childPath[childPath.length - 1] = file.getName();
				}
				// upload all inner directories & files
				Arrays.stream(files).forEach(fileName -> results.add(uploadFiles(fileSystem, file.toPath().toAbsolutePath().resolve(fileName).toString(), childPath, false)));
			}
			return results;
		} else {
			// upload single file
			return uploadFile(fileSystem, path, buildPath(parentPath));
		}
	}

	/**
	 * Uploads a single file directory to the given {@link FileSystem}.
	 *
	 * @param fileSystem the target {@link FileSystem}
	 * @param pathToFile the local path of the file to upload
	 * @return a {@link ExecutionResult result} containing all information about the upload
	 */
	@VisibleForTesting
	@NotNull
	static ExecutionResult uploadFile(@NotNull final FileSystem<?> fileSystem, @NotNull final String pathToFile, @NotNull final String targetDirectory) {
		final File file = new File(pathToFile);
		if (!file.exists()) {
			return new FileNotFoundResult(pathToFile);
		}
		if (!file.isFile()) {
			return new ObjectIsNotAFileResult(pathToFile);
		}
		try {
			final String targetFile = targetDirectory + FileSystem.SEPARATOR + file.getName();
			LOGGER.debug("Uploading file '{}' to '{}'...", pathToFile, targetFile);
			final FileHandle handle = fileSystem.obtain(targetFile);
			handle.getParent().mkDirs();
			handle.save(new BufferedInputStream(new FileInputStream(file)));
			return new FileUploadedResult(pathToFile);
		} catch (final IOException exception) {
			return new FileUploadFailedResult(pathToFile, exception);
		}
	}

	@VisibleForTesting
	public static abstract class FileResult {

		private final String _fileName;
		private final String _message;

		protected FileResult(@NotNull final String fileName, @NotNull final String message) {
			_message = message;
			_fileName = fileName;
		}

		@NotNull
		public String getFileName() {
			return _fileName;
		}

		@Override
		public String toString() {
			return _message;
		}

	}

	@VisibleForTesting
	static class FileUploadedResult extends FileResult implements ExecutionResult {

		@VisibleForTesting
		static final String MESSAGE = "File '%s' successfully uploaded.";

		private FileUploadedResult(@NotNull final String fileName) {
			super(fileName, String.format(MESSAGE, fileName));
		}

	}

	@VisibleForTesting
	static class FileNotFoundResult extends FileResult implements ExecutionErrorResult<IOException> {

		@VisibleForTesting
		static final String MESSAGE = "File '%s' not found!";

		private FileNotFoundResult(@NotNull final String fileName) {
			super(fileName, String.format(MESSAGE, fileName));
		}

		@NotNull
		@Override
		public IOException getException() {
			return new IOException(toString());
		}

	}

	@VisibleForTesting
	static class ObjectIsNotAFileResult extends FileResult implements ExecutionErrorResult<IOException> {

		@VisibleForTesting
		static final String MESSAGE = "Object '%s' is not a file!";

		private ObjectIsNotAFileResult(@NotNull final String fileName) {
			super(fileName, String.format(MESSAGE, fileName));
		}

		@NotNull
		@Override
		public IOException getException() {
			return new IOException(toString());
		}

	}

	@VisibleForTesting
	static class FileUploadFailedResult extends FileResult implements ExecutionErrorResult<IOException> {

		@VisibleForTesting
		static final String MESSAGE = "Error uploading file '%s':\n%s";

		private final IOException _exception;

		private FileUploadFailedResult(@NotNull final String fileName, @NotNull final IOException exception) {
			super(fileName, String.format(MESSAGE, fileName, exception));
			_exception = new IOException(toString());
		}

		@NotNull
		@Override
		public IOException getException() {
			return _exception;
		}

	}

}
