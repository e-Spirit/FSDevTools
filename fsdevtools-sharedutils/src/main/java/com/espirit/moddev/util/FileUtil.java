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

package com.espirit.moddev.util;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;

/**
 * Utility class for file operations.
 */
public enum FileUtil {
	;

	/**
	 * Moves the content of the given {@link Path path} on directory up.
	 * If the given path is a directory, all contents of the directory will be moved - but the given directory will stay at its current location.
	 *
	 * @param path the file or directory to move
	 * @throws IOException If move operation could not be performed
	 */
	public static void moveContentsUp(@NotNull final Path path) throws IOException {
		final File file = path.toFile();
		final Path newParent = path.toAbsolutePath().getParent();
		if (newParent == null) {
			throw new IllegalStateException("Parent of '" + path.toAbsolutePath() + "' is null!");
		}
		if (file.isDirectory()) {
			// move all contents of the directory
			final File[] children = file.listFiles();
			if (children != null) {
				for (final File child : children) {
					final Path newPath = newParent.resolve(child.getName());
					if (!child.renameTo(newPath.toFile())) {
						throw new IOException("Renaming file '" + child.getAbsolutePath() + "' to '" + newPath.toAbsolutePath() + "' failed!");
					}
				}
			}
		} else {
			// directly move the file
			Files.move(path, newParent.resolve(path.getFileName()));
		}
	}

	/**
	 * Deletes the file (or directory) at the given {@link Path path}.
	 * If the given path is a directory, all contents of the directory will be deleted recursively.
	 *
	 * @param path the file or directory to delete
	 * @throws IOException if an I/O error occurs
	 */
	public static void deleteDirectory(@NotNull final Path path) throws IOException {
		if (!path.toFile().exists()) {
			return;
		}
		Files.walkFileTree(path,
				new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult postVisitDirectory(@NotNull final Path dir, @NotNull final IOException exc) throws IOException {
						Files.delete(dir);
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFile(@NotNull final Path file, @NotNull final BasicFileAttributes attrs) throws IOException {
						Files.delete(file);
						return FileVisitResult.CONTINUE;
					}
				});
	}

	/**
	 * Marks the {@link File file} at the given {@link Path path} as executable files.
	 *
	 * @param path the path of the {@link File file} to modify
	 * @throws IOException           if the {@link File file} could not be marked as executable
	 * @throws FileNotFoundException if the file at the given {@link Path path} does not exist
	 */
	public static void setExecutable(@NotNull final Path path) throws FileNotFoundException, IOException {
		final File file = path.toFile();
		if (!file.exists()) {
			throw new FileNotFoundException("File '" + path.toAbsolutePath() + "' does not exist!");
		}
		if (!file.setExecutable(true)) {
			throw new IOException("Could not set file '" + path.toAbsolutePath() + "' executable!");
		}
	}

	/**
	 * Marks the {@link File executable} at the given {@link Path paths} as executable files.
	 *
	 * @param basePath    the base path for the given executables
	 * @param executables the relative paths of the executable to update
	 * @throws IOException           if one (or more) {@link File files} could not be marked as executable
	 * @throws FileNotFoundException if one (or more) files at the given {@link Path path} does not exist
	 */
	public static void setExecutable(@NotNull final Path basePath, @NotNull final Collection<Path> executables) throws IOException {
		for (final Path executable : executables) {
			final Path path = basePath.resolve(executable);
			final File file = path.toFile();
			if (!file.exists()) {
				// ignore non existing files
				continue;
			}
			FileUtil.setExecutable(path);
		}
	}

	/**
	 * Creates the directory named by the given {@link Path path}, including any necessary but nonexistent parent directories.
	 * If the given path represents a file, only the parent directories will be created.
	 * Note that if this operation fails it may have succeeded in creating some of the necessary parent directories.
	 *
	 * @param path the path to create
	 * @throws IOException if the directory for the given {@link Path path} could not be created
	 */
	public static void mkDirs(@NotNull final Path path) throws IOException {
		final File file = path.toFile();
		if (!file.exists()) {
			if (!file.mkdirs()) {
				throw new IOException("Could not create directories for '" + file.toPath().toAbsolutePath() + "'!");
			}
		}
	}

}
