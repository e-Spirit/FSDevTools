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
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FsConnectionEncryptionTest {

	@Test
	void none_mapsToEncryptionNone() {
		assertThat(FsConnectionEncryption.NONE.getEncryptionValue()).isEqualTo(ConnectionManager.ENCRYPTION_NONE);
	}

	@Test
	void tls_mapsToEncryptionTls() {
		assertThat(FsConnectionEncryption.TLS.getEncryptionValue()).isEqualTo(ConnectionManager.ENCRYPTION_TLS);
	}

	@Test
	void chacha20_mapsToEncryptionChacha20() {
		assertThat(FsConnectionEncryption.CHACHA20.getEncryptionValue()).isEqualTo(ConnectionManager.ENCRYPTION_CHACHA20);
	}

	@Test
	void valueOf_isHandledCaseInsensitively() {
		assertThat(FsConnectionEncryption.valueOf("tls".toUpperCase(Locale.ROOT))).isEqualTo(FsConnectionEncryption.TLS);
		assertThat(FsConnectionEncryption.valueOf("none".toUpperCase(Locale.ROOT))).isEqualTo(FsConnectionEncryption.NONE);
		assertThat(FsConnectionEncryption.valueOf("chacha20".toUpperCase(Locale.ROOT))).isEqualTo(FsConnectionEncryption.CHACHA20);
	}

	@Test
	void valueOf_throwsOnUnknownValue() {
		assertThatThrownBy(() -> FsConnectionEncryption.valueOf("BOGUS")).isInstanceOf(IllegalArgumentException.class);
	}

}
