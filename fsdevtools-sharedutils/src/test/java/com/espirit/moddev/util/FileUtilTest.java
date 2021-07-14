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

package com.espirit.moddev.util;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FileUtilTest {

	@Rule
	public TemporaryFolder _temp = new TemporaryFolder();

	@Test
	public void moveUp() throws IOException {
		final Path tempPath = _temp.getRoot().toPath();
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
		assertTrue("directory should still exist", subPath.toFile().exists());
		assertFalse("directory should have been moved (deleted)", subDirPath.toFile().exists());
		assertTrue("directory should have been moved (created)", tempPath.resolve(dirName).toFile().exists());
		assertFalse("file should have been moved (deleted)", filePath.toFile().exists());
		assertTrue("file should have been moved (created)", tempPath.resolve(fileName).toFile().exists());
	}

	@Test
	public void deleteDirectory() throws IOException {
		final Path tempPath = _temp.getRoot().toPath();
		final Path subPath = tempPath.resolve("sub");
		final Path subDirPath = subPath.resolve("dir");
		FileUtil.mkDirs(subDirPath);
		assertTrue(subDirPath.toFile().exists());
		// delete
		FileUtil.deleteDirectory(subPath);
		// verify
		assertFalse("directory should have been deleted", subDirPath.toFile().exists());
		assertFalse("directory should have been deleted", subPath.toFile().exists());
	}

	@Test
	public void mkDirs() throws IOException {
		final Path tempPath = _temp.getRoot().toPath();
		final Path pathToCreate = tempPath.resolve("sub").resolve("dir");
		assertFalse(pathToCreate.toFile().exists());
		// create
		FileUtil.mkDirs(pathToCreate);
		// verify
		assertTrue("file should have been created", pathToCreate.toFile().exists());
	}

}
