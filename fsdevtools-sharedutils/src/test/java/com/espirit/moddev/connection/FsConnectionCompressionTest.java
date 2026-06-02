/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2025 Crownpeak Technology GmbH
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
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FsConnectionCompressionTest {

	@Test
	void none_mapsToCompressionNone() {
		assertThat(FsConnectionCompression.NONE.getCompressionValue()).isEqualTo(ConnectionManager.COMPRESSION_NONE);
	}

	@Test
	void deflate_mapsToCompressionDeflate() {
		assertThat(FsConnectionCompression.DEFLATE.getCompressionValue()).isEqualTo(ConnectionManager.COMPRESSION_DEFLATE);
	}

	@Test
	void deflateSpeed_mapsToCompressionDeflateSpeed() {
		assertThat(FsConnectionCompression.DEFLATE_SPEED.getCompressionValue()).isEqualTo(ConnectionManager.COMPRESSION_DEFLATE_SPEED);
	}

	@Test
	void deflateBest_mapsToCompressionDeflateBest() {
		assertThat(FsConnectionCompression.DEFLATE_BEST.getCompressionValue()).isEqualTo(ConnectionManager.COMPRESSION_DEFLATE_BEST);
	}

	@Test
	void zstd_mapsToCompressionZstd() {
		assertThat(FsConnectionCompression.ZSTD.getCompressionValue()).isEqualTo(ConnectionManager.COMPRESSION_ZSTD);
	}

	@Test
	void valueOf_isHandledCaseInsensitively() {
		assertThat(FsConnectionCompression.valueOf("deflate".toUpperCase(Locale.ROOT))).isEqualTo(FsConnectionCompression.DEFLATE);
		assertThat(FsConnectionCompression.valueOf("zstd".toUpperCase(Locale.ROOT))).isEqualTo(FsConnectionCompression.ZSTD);
	}

	@Test
	void valueOf_throwsOnUnknownValue() {
		assertThatThrownBy(() -> FsConnectionCompression.valueOf("BOGUS")).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void snappy_isNotAnAllowedConstant() {
		assertThatThrownBy(() -> FsConnectionCompression.valueOf("SNAPPY")).isInstanceOf(IllegalArgumentException.class);
	}

}
