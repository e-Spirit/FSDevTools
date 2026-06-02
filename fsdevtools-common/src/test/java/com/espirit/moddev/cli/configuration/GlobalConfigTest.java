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

package com.espirit.moddev.cli.configuration;

import com.espirit.moddev.cli.CliConstants;
import com.espirit.moddev.connection.FsConnectionCompression;
import com.espirit.moddev.connection.FsConnectionEncryption;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GlobalConfigTest {

	@Test
	public void userIsNotFetchedFromEnvironmentIfConfigured() {
		final GlobalConfig config = new GlobalConfig();
		config.setUser("abc");
		config.getEnvironment().clear();
		config.getEnvironment().put(CliConstants.KEY_FS_USER.value(), "xyz");

		assertEquals("abc", config.getUser());
	}

	@Test
	public void userIsFetchedFromEnvironmentIfNotConfigured() {
		final GlobalConfig config = new GlobalConfig();
		config.getEnvironment().clear();
		config.getEnvironment().put(CliConstants.KEY_FS_USER.value(), "xyz");

		assertEquals("xyz", config.getUser());
	}

	@Test
	public void defaultUserIsReturnedIfNoUserIsConfigured() {
		final GlobalConfig config = new GlobalConfig();
		config.getEnvironment().clear();

		assertEquals(CliConstants.DEFAULT_USER.value(), config.getUser());
	}

	@Test
	public void nullProjectIsReturnedIfNoProjectIsConfigured() {
		final GlobalConfig config = new GlobalConfig();

		config.getEnvironment().clear();
		assertNull(config.getProject());
	}

	@Test
	public void defaultHTTPProxySettings() {
		final GlobalConfig config = new GlobalConfig();
		assertEquals("", config.getHttpProxyHost());
		assertEquals(Integer.valueOf(8080), config.getHttpProxyPort());
	}

	@Test
	public void nullHTTPProxySettings() {
		final GlobalConfig config = new GlobalConfig();
		config.setHttpProxyHost(null);
		config.setHttpProxyPort(null);
		assertEquals("", config.getHttpProxyHost());
		assertEquals(Integer.valueOf(8080), config.getHttpProxyPort());
	}

	@Test
	public void customHTTPProxySettings() {
		final String customHost = "myHost";
		final Integer customPort = Integer.valueOf(1337);
		final GlobalConfig config = new GlobalConfig();
		config.setHttpProxyHost(customHost);
		config.setHttpProxyPort(customPort);
		assertEquals(customHost, config.getHttpProxyHost());
		assertEquals(customPort, config.getHttpProxyPort());
	}

	@Test
	public void defaultServletZone() {
		final GlobalConfig config = new GlobalConfig();
		assertEquals(CliConstants.DEFAULT_SERVLET_ZONE.value(), config.getServletZone());
	}

	@Test
	public void customServletZone() {
		final String customServletZone = "customServletZone";
		final GlobalConfig config = new GlobalConfig();
		config.setServletZone(customServletZone);
		assertEquals(customServletZone, config.getServletZone());
	}

	@Test
	public void encryptionIsNotFetchedFromEnvironmentIfConfigured() {
		final GlobalConfig config = new GlobalConfig();
		config.setConnectionEncryption(FsConnectionEncryption.TLS);
		config.getEnvironment().clear();
		config.getEnvironment().put(CliConstants.KEY_FS_ENCRYPTION.value(), "NONE");

		assertEquals(FsConnectionEncryption.TLS, config.getConnectionEncryption());
	}

	@Test
	public void encryptionIsFetchedFromEnvironmentIfNotConfigured() {
		final GlobalConfig config = new GlobalConfig();
		config.getEnvironment().clear();
		config.getEnvironment().put(CliConstants.KEY_FS_ENCRYPTION.value(), "tls");

		assertEquals(FsConnectionEncryption.TLS, config.getConnectionEncryption());
	}

	@Test
	public void nullEncryptionIsReturnedIfNothingIsConfigured() {
		final GlobalConfig config = new GlobalConfig();
		config.getEnvironment().clear();

		assertNull(config.getConnectionEncryption());
	}

	@Test
	public void invalidEncryptionEnvValueThrowsIllegalArgumentException() {
		final GlobalConfig config = new GlobalConfig();
		config.getEnvironment().clear();
		config.getEnvironment().put(CliConstants.KEY_FS_ENCRYPTION.value(), "BOGUS");

		assertThrows(IllegalArgumentException.class, config::getConnectionEncryption);
	}

	@Test
	public void compressionIsNotFetchedFromEnvironmentIfConfigured() {
		final GlobalConfig config = new GlobalConfig();
		config.setConnectionCompression(FsConnectionCompression.ZSTD);
		config.getEnvironment().clear();
		config.getEnvironment().put(CliConstants.KEY_FS_COMPRESSION.value(), "NONE");

		assertEquals(FsConnectionCompression.ZSTD, config.getConnectionCompression());
	}

	@Test
	public void compressionIsFetchedFromEnvironmentIfNotConfigured() {
		final GlobalConfig config = new GlobalConfig();
		config.getEnvironment().clear();
		config.getEnvironment().put(CliConstants.KEY_FS_COMPRESSION.value(), "deflate");

		assertEquals(FsConnectionCompression.DEFLATE, config.getConnectionCompression());
	}

	@Test
	public void nullCompressionIsReturnedIfNothingIsConfigured() {
		final GlobalConfig config = new GlobalConfig();
		config.getEnvironment().clear();

		assertNull(config.getConnectionCompression());
	}

	@Test
	public void invalidCompressionEnvValueThrowsIllegalArgumentException() {
		final GlobalConfig config = new GlobalConfig();
		config.getEnvironment().clear();
		config.getEnvironment().put(CliConstants.KEY_FS_COMPRESSION.value(), "BOGUS");

		assertThrows(IllegalArgumentException.class, config::getConnectionCompression);
	}

}
