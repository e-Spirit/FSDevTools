/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2026 Crownpeak Technology GmbH
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

package com.espirit.moddev.connection;

import de.espirit.firstspirit.access.ConnectionManager;

/**
 * Compression modes for the FirstSpirit connection, mapping CLI names to {@link ConnectionManager} byte constants.
 * Mirrors the pattern of {@link FsConnectionType}. {@code COMPRESSION_SNAPPY} is intentionally omitted as it is
 * deprecated for removal in the FirstSpirit API.
 */
public enum FsConnectionCompression {

	NONE(ConnectionManager.COMPRESSION_NONE),
	DEFLATE(ConnectionManager.COMPRESSION_DEFLATE),
	DEFLATE_SPEED(ConnectionManager.COMPRESSION_DEFLATE_SPEED),
	DEFLATE_BEST(ConnectionManager.COMPRESSION_DEFLATE_BEST),
	ZSTD(ConnectionManager.COMPRESSION_ZSTD);

	private final byte _compressionValue;

	FsConnectionCompression(final byte compressionValue) {
		_compressionValue = compressionValue;
	}

	/**
	 * Returns the {@link ConnectionManager} byte constant for this compression mode.
	 *
	 * @return the compression byte constant
	 */
	public byte getCompressionValue() {
		return _compressionValue;
	}

}
