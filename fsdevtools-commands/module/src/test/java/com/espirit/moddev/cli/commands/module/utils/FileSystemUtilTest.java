/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2022 Crownpeak Technology GmbH
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

import com.espirit.moddev.cli.api.result.ExecutionResult;
import com.espirit.moddev.cli.api.result.ExecutionResults;
import com.google.common.collect.Lists;
import de.espirit.firstspirit.io.MemoryFileHandle;
import de.espirit.firstspirit.io.MemoryFileSystem;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class FileSystemUtilTest {

	@TempDir
	public File temporaryFolder;

	public static void verifyFile(@NotNull final MemoryFileSystem fileSystem, @NotNull final String fileName) {
		verifyFile(fileSystem, fileName, "");
	}

	public static void verifyFile(@NotNull final MemoryFileSystem fileSystem, @NotNull final String fileName, @NotNull final String targetDir) {
		try {
			final MemoryFileHandle handle = fileSystem.obtain(targetDir + fileName);
			assertThat(handle.exists()).describedAs("File must exist.").isTrue();
			assertThat(handle.isFile()).describedAs("Object must be a file.").isTrue();
			assertThat(fileSystem.getFileAsString(targetDir + fileName).replaceAll("\r\n", "\n").replaceAll("\n", "")).isEqualTo(handle.getName());
		} catch (final IOException exception) {
			fail("File '" + fileName + "' does not exist!", exception);
		}
	}

	public static void verifyDir(@NotNull final MemoryFileSystem fileSystem, @NotNull final String dirName) {
		verifyDir(fileSystem, dirName, "");
	}

	public static void verifyDir(@NotNull final MemoryFileSystem fileSystem, @NotNull final String dirName, @NotNull final String targetDir) {
		try {
			final MemoryFileHandle handle = fileSystem.obtain(targetDir + dirName);
			assertThat(handle.exists()).describedAs("Directory must exist.").isTrue();
			assertThat(handle.isDirectory()).describedAs("Object must be a directory.").isTrue();
		} catch (final IOException exception) {
			fail("File '" + dirName + "' does not exist!", exception);
		}
	}

	public static void verifyFileDoesNotExist(@NotNull final MemoryFileSystem fileSystem, @NotNull final String fileName) {
		verifyFileDoesNotExist(fileSystem, fileName, "");
	}

	public static void verifyFileDoesNotExist(@NotNull final MemoryFileSystem fileSystem, @NotNull final String fileName, @NotNull final String targetDir) {
		try {
			final MemoryFileHandle handle = fileSystem.obtain(targetDir + fileName);
			assertThat(handle.exists()).describedAs("File must not exist.").isFalse();
		} catch (final IOException ignore) {
			// we assume this exception as ignored (FileNotFoundException)
		}
	}

	public static void verifyDirDoesNotExist(@NotNull final MemoryFileSystem fileSystem, @NotNull final String dirName) {
		verifyDirDoesNotExist(fileSystem, dirName, "");
	}

	public static void verifyDirDoesNotExist(@NotNull final MemoryFileSystem fileSystem, @NotNull final String dirName, @NotNull final String targetDir) {
		try {
			final MemoryFileHandle handle = fileSystem.obtain(targetDir + dirName);
			assertThat(handle.exists()).describedAs("Directory must not exist.").isFalse();
		} catch (final IOException exception) {
			fail("Directory '" + dirName + "' must not exist!", exception);
		}
	}

	private final MemoryFileSystem _fileSystem = new MemoryFileSystem();

	@Test
	public void buildPath() {
		assertThat(FileSystemUtil.buildPath(new String[0])).isEqualTo("");
		assertThat(FileSystemUtil.buildPath(new String[]{"1"})).isEqualTo("1");
		assertThat(FileSystemUtil.buildPath(new String[]{"1", "2"})).isEqualTo("1/2");
	}

	@Test
	public void uploadFile() {
		final String file = "src/test/resources/1.json";
		final String targetDirectory = "";
		final ExecutionResult result = FileSystemUtil.uploadFile(_fileSystem, file, targetDirectory);
		assertThat(result).isInstanceOf(FileSystemUtil.FileUploadedResult.class);
		assertThat(((FileSystemUtil.FileUploadedResult) result).getFileName()).isEqualTo(file);
		assertThat(result.toString()).isEqualTo(String.format(FileSystemUtil.FileUploadedResult.MESSAGE, file));
		verifyFile(_fileSystem, "1.json");
	}

	@Test
	public void uploadFile_failed() throws IOException {
		final MemoryFileSystem fileSystem = mock(MemoryFileSystem.class);
		final String ioExceptionMessage = "IOException occurred.";
		doAnswer(invocation -> {
			throw new IOException(ioExceptionMessage);
		}).when(fileSystem).obtain(any());
		final String file = "src/test/resources/1.json";
		final String targetDirectory = "";
		final ExecutionResult result = FileSystemUtil.uploadFile(fileSystem, file, targetDirectory);
		assertThat(result).isInstanceOf(FileSystemUtil.FileUploadFailedResult.class);
		assertThat(((FileSystemUtil.FileUploadFailedResult) result).getException().getMessage()).isEqualTo(String.format(FileSystemUtil.FileUploadFailedResult.MESSAGE + "java.io.IOException: " + ioExceptionMessage, file, targetDirectory));
		verifyFileDoesNotExist(fileSystem, "1.json");
	}

	@Test
	public void uploadFile_into_non_existing_directory() {
		final String file = "src/test/resources/1.json";
		final ExecutionResult result = FileSystemUtil.uploadFile(_fileSystem, file, "newDir");
		assertThat(result).isInstanceOf(FileSystemUtil.FileUploadedResult.class);
		assertThat(result.toString()).isEqualTo(String.format(FileSystemUtil.FileUploadedResult.MESSAGE, file));
		verifyFile(_fileSystem, "1.json", "newDir/");
	}

	@Test
	public void uploadFile_file_does_not_exist() {
		final String file = "file/does/not/exist.json";
		final ExecutionResult result = FileSystemUtil.uploadFile(_fileSystem, file, "");
		assertThat(result).isInstanceOf(FileSystemUtil.FileNotFoundResult.class);
		assertThat(((FileSystemUtil.FileNotFoundResult) result).getException().getMessage()).isEqualTo(String.format(FileSystemUtil.FileNotFoundResult.MESSAGE, file));
	}

	@Test
	public void uploadFile_object_is_not_a_file() {
		final String file = "src/test/resources/dir";
		final ExecutionResult result = FileSystemUtil.uploadFile(_fileSystem, file, "");
		assertThat(result).isInstanceOf(FileSystemUtil.ObjectIsNotAFileResult.class);
		assertThat(((FileSystemUtil.ObjectIsNotAFileResult) result).getException().getMessage()).isEqualTo(String.format(FileSystemUtil.ObjectIsNotAFileResult.MESSAGE, file));
	}

	@Test
	public void uploadFiles_empty_directory() {
		final File emptyFolder = temporaryFolder.toPath().resolve("emptyDir").toFile();
		emptyFolder.mkdir();
		final ExecutionResult result = FileSystemUtil.uploadFiles(Lists.newArrayList(emptyFolder.getAbsolutePath()), _fileSystem);
		assertThat(result).isInstanceOf(ExecutionResults.class);
		final ExecutionResults results = (ExecutionResults) result;
		assertThat(results.isEmpty()).describedAs(results.stream().map(Object::toString).collect(Collectors.joining(", ", "[ ", " ]"))).isTrue();
		verifyDirDoesNotExist(_fileSystem, "emptyDir");
	}

	@Test
	public void uploadFiles_single_file() {
		final String file = "src/test/resources/1.json";
		final ExecutionResult result = FileSystemUtil.uploadFiles(Lists.newArrayList(file), _fileSystem);
		assertThat(result).isInstanceOf(ExecutionResults.class);
		final ExecutionResults results = (ExecutionResults) result;
		assertThat(results.hasError()).isFalse();
		assertThat(results.size()).isEqualTo(1);
		assertThat(results.get(0)).isInstanceOf(FileSystemUtil.FileUploadedResult.class);
		assertThat(results.get(0).toString()).isEqualTo(String.format(FileSystemUtil.FileUploadedResult.MESSAGE, file));
		verifyFile(_fileSystem, "1.json");
	}

	@Test
	public void uploadFiles_directory_with_file() {
		final String file = "src/test/resources/dir/subDir";
		final ExecutionResult result = FileSystemUtil.uploadFiles(Lists.newArrayList(file), _fileSystem);
		assertThat(result).isInstanceOf(ExecutionResults.class);
		final ExecutionResults results = (ExecutionResults) result;
		assertThat(results.hasError()).isFalse();
		assertThat(results.size()).isEqualTo(1);
		assertThat(results.get(0)).isInstanceOf(FileSystemUtil.FileUploadedResult.class);
		verifyDirDoesNotExist(_fileSystem, "subDir");
		verifyFile(_fileSystem, "3.json");
	}

	@Test
	public void uploadFiles_multiple_files_partly_fail() {
		final String file = "src/test/resources/1.json";
		final String nonExistingFile = "file/does/not/exist.json";
		final ExecutionResult result = FileSystemUtil.uploadFiles(Lists.newArrayList(file, nonExistingFile), _fileSystem);
		assertThat(result).isInstanceOf(ExecutionResults.class);
		final ExecutionResults results = (ExecutionResults) result;
		assertThat(results.hasError()).isTrue();
		assertThat(results.size()).isEqualTo(2);
		assertThat(results.get(0)).isInstanceOf(FileSystemUtil.FileUploadedResult.class);
		assertThat(results.get(1)).isInstanceOf(FileSystemUtil.FileNotFoundResult.class);
		verifyFile(_fileSystem, "1.json");
	}

	@Test
	public void uploadFiles_directory_with_sub_structure() {
		final ExecutionResult result = FileSystemUtil.uploadFiles(Lists.newArrayList("src/test/resources/dir"), _fileSystem);
		assertThat(result).isInstanceOf(ExecutionResults.class);
		final ExecutionResults results = (ExecutionResults) result;
		assertThat(results.hasError()).isFalse();
		assertThat(results.size()).isEqualTo(2);
		assertThat(results.get(0)).isInstanceOf(FileSystemUtil.FileUploadedResult.class);
		assertThat(results.get(1)).isInstanceOf(FileSystemUtil.FileUploadedResult.class);
		verifyDirDoesNotExist(_fileSystem, "dir");
		verifyFile(_fileSystem, "2.json");
		verifyDir(_fileSystem, "subDir");
		verifyFile(_fileSystem, "subDir/3.json");
	}

}
