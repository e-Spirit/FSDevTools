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

package com.espirit.moddev.util;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class ArchiveUtilTest {

	@TempDir
	public File _temp;

	@Test
	public void decompressJarEntry() throws IOException {
		// decompress
		final Path archivePath = new File(getClass().getResource("/test_archive.jar").getFile()).toPath();
		final Path targetDir = _temp.toPath();
		final Path jarFile = targetDir.resolve("jarFile.jar");
		Files.copy(archivePath, jarFile);
		final Path targetFile = targetDir.resolve("jarEntry.txt");
		ArchiveUtil.decompressJarEntry(jarFile, "sub/dir/file.txt", targetFile);
		// verify
		assertTrue(targetFile.toFile().exists());
		final List<String> lines = Files.readAllLines(targetFile);
		assertEquals(1, lines.size());
		assertEquals("sub/dir/file.txt", lines.get(0));
		// verify that the jar stream is closed (by deleting the file)
		assertTrue(jarFile.toFile().delete());
	}

	@Test
	public void decompressTarGz() throws IOException {
		// decompress
		final Path archivePath = new File(getClass().getResource("/test_archive.tar.gz").getFile()).toPath();
		final Path targetDir = _temp.toPath();
		ArchiveUtil.decompressTarGz(archivePath, targetDir);
		// verify
		try (final ArchiveInputStream inputStream = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(archivePath.toFile())))) {
			ArchiveEntry entry;
			while ((entry = inputStream.getNextEntry()) != null) {
				final File file = targetDir.resolve(entry.getName()).toFile();
				assertTrue(file.exists());
			}
		}
	}

}
