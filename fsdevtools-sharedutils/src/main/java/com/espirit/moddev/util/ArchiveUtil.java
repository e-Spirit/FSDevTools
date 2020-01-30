package com.espirit.moddev.util;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Utility class for archive operations.
 */
public enum ArchiveUtil {

	;

	private static final int BUFFER_SIZE = 4096;

	@NotNull
	private static BufferedInputStream createBufferedInputStream(@NotNull final Path path) throws FileNotFoundException {
		return new BufferedInputStream(new FileInputStream(path.toFile()), BUFFER_SIZE);
	}

	@NotNull
	private static BufferedInputStream createBufferedInputStream(final int bufferSize, @NotNull final Path path) throws FileNotFoundException {
		return new BufferedInputStream(new FileInputStream(path.toFile()), bufferSize);
	}

	@NotNull
	private static BufferedOutputStream createBufferedOutputStream(final int bufferSize, @NotNull final Path path) throws FileNotFoundException {
		return new BufferedOutputStream(new FileOutputStream(path.toFile()), bufferSize);
	}

	/**
	 * Decompresses all {@link ArchiveEntry entries} of the given {@link ArchiveInputStream input stream} and saves them inside the given {@link Path target root}.
	 *
	 * @param targetRoot  the {@link Path target root}
	 * @param inputStream the {@link ArchiveInputStream input stream} to read from
	 * @throws IOException if an I/O error occurs
	 */
	private static void decompressArchiveEntries(@NotNull final Path targetRoot, @NotNull final ArchiveInputStream inputStream) throws IOException {
		ArchiveEntry entry;
		while ((entry = inputStream.getNextEntry()) != null) {
			decompressArchiveEntry(inputStream, entry, targetRoot);
		}
	}

	/**
	 * Decompresses a single archive entry and saves it to the equal {@link Path path} inside of the given {@link Path target root}.
	 *
	 * @param inputStream the {@link ArchiveInputStream input stream} to read from
	 * @param entry       the {@link ArchiveEntry entry} to decompress
	 * @param targetRoot  the {@link Path target root}
	 * @throws IOException if an I/O error occurs
	 */
	private static void decompressArchiveEntry(@NotNull final ArchiveInputStream inputStream, @NotNull final ArchiveEntry entry, @NotNull final Path targetRoot) throws IOException {
		// create the directory
		final Path target = targetRoot.resolve(entry.getName());
		if (entry.isDirectory()) {
			FileUtil.mkDirs(target);
		} else {
			FileUtil.mkDirs(target.getParent());
			// extract the file, if we have a file
			int count;
			final byte[] data = new byte[BUFFER_SIZE];
			try (final OutputStream dest = createBufferedOutputStream(BUFFER_SIZE, target)) {
				while ((count = inputStream.read(data, 0, BUFFER_SIZE)) != -1) {
					dest.write(data, 0, count);
				}
			}
		}
	}

	/**
	 * Decompresses a single {@link Path entry} from the given {@link Path jar file} to the given {@link Path target file}.
	 *
	 * @param jarFile    the {@link Path path} to the jar file
	 * @param entryPath  the {@link Path path} to the entry to decompress
	 * @param targetFile the {@link Path path} to the target file
	 * @throws IOException if an I/O error occurs
	 */
	public static void decompressJarEntry(@NotNull final Path jarFile, @NotNull final String entryPath, @NotNull final Path targetFile) throws IOException {
		final URL url = new URL("jar:file:" + jarFile.toAbsolutePath() + "!/" + entryPath);
		final JarURLConnection connection = (JarURLConnection) url.openConnection();
		// turn off caching, this will result in closing the jar file after decompression
		connection.setDefaultUseCaches(false);
		connection.setUseCaches(false);
		final JarFile openedJarFile = connection.getJarFile();
		// get the entry and decompress it
		final JarEntry jarEntry = connection.getJarEntry();
		if (jarEntry == null) {
			throw new FileNotFoundException("Entry '" + targetFile + "' not found in archive!");
		}
		try (final BufferedInputStream inputStream = new BufferedInputStream(openedJarFile.getInputStream(jarEntry), BUFFER_SIZE)) {
			try (final BufferedOutputStream outputStream = createBufferedOutputStream(BUFFER_SIZE, targetFile)) {
				int count;
				final byte[] data = new byte[BUFFER_SIZE];
				while ((count = inputStream.read(data, 0, BUFFER_SIZE)) != -1) {
					outputStream.write(data, 0, count);
				}
			}
		}
		openedJarFile.close();
	}

	/**
	 * Decompresses the contents of the given file into the given target directory.
	 * <b>NOTE:</b> This method will {@code NOT} delete the target directory. To delete the target directory use {@link FileUtil#deleteDirectory(Path)}.
	 *
	 * @param sourceFile the tar.gz-file to decompress
	 * @param targetDir  the target directory for the decompressed content
	 * @throws IOException if an I/O error occurs
	 * @see FileUtil#deleteDirectory(Path)
	 */
	public static void decompressTarGz(@NotNull final Path sourceFile, @NotNull final Path targetDir) throws IOException {
		FileUtil.mkDirs(targetDir);
		try (final ArchiveInputStream inputStream = new TarArchiveInputStream(new GzipCompressorInputStream(createBufferedInputStream(sourceFile)))) {
			decompressArchiveEntries(targetDir, inputStream);
		}
	}

}
