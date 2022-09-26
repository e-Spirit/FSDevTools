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

package com.espirit.moddev.util;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileUtilTest {

	@TempDir
	public File _temp;

	@Test
	public void moveUp() throws IOException {
		final Path tempPath = _temp.toPath();
		final Path subPath = tempPath.resolve("sub");
		final String dirName = "dir";
		final Path subDirPath = subPath.resolve(dirName);
		// create directories "sub/dir"
		FileUtil.mkDirs(subDirPath);
		// create test file "sub/myFile.txt"
		final String fileName = "myFile.txt";
		final Path filePath = subPath.resolve(fileName);
		try (final OutputStream outputStream = new FileOutputStream(filePath.toFile())) {
			outputStream.write("myText.txt".getBytes());
		}
		assertTrue(filePath.toFile().exists());
		assertTrue(subDirPath.toFile().exists());
		// move
		FileUtil.moveContentsUp(subPath);
		// verify
		assertTrue(subPath.toFile().exists(), "directory should still exist");
		assertFalse(subDirPath.toFile().exists(), "directory should have been moved (deleted)");
		assertTrue(tempPath.resolve(dirName).toFile().exists(), "directory should have been moved (created)");
		assertFalse(filePath.toFile().exists(), "file should have been moved (deleted)");
		assertTrue(tempPath.resolve(fileName).toFile().exists(), "file should have been moved (created)");
	}

	@Test
	public void deleteDirectory() throws IOException {
		final Path tempPath = _temp.toPath();
		final Path subPath = tempPath.resolve("sub");
		final Path subDirPath = subPath.resolve("dir");
		FileUtil.mkDirs(subDirPath);
		assertTrue(subDirPath.toFile().exists());
		// delete
		FileUtil.deleteDirectory(subPath);
		// verify
		assertFalse(subDirPath.toFile().exists(), "directory should have been deleted");
		assertFalse(subPath.toFile().exists(), "directory should have been deleted");
	}

	@Test
	public void mkDirs() throws IOException {
		final Path tempPath = _temp.toPath();
		final Path pathToCreate = tempPath.resolve("sub").resolve("dir");
		assertFalse(pathToCreate.toFile().exists());
		// create
		FileUtil.mkDirs(pathToCreate);
		// verify
		assertTrue(pathToCreate.toFile().exists(), "file should have been created");
	}

	@Test
	void writeIntoFile(@TempDir final Path tempDir) throws IOException {
		// GIVEN
		final Path tempFilePath = tempDir.resolve("test-file");
		final File testFile = tempFilePath.toFile();
		Assumptions.assumeThat(testFile.createNewFile()).isTrue();
		try (final ByteArrayInputStream is = new ByteArrayInputStream("test content".getBytes())) {
			// WHEN
			FileUtil.writeIntoFile(is, testFile);
		}
		// THEN
		final String fileContent = Files.readString(tempFilePath);
		Assertions.assertThat(fileContent).isEqualTo("test content");
	}
}
