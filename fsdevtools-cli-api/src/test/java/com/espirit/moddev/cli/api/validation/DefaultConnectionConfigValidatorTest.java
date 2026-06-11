package com.espirit.moddev.cli.api.validation;

/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2026 Crownpeak Technology GmbH
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License"),
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

import com.espirit.moddev.cli.api.configuration.Config;
import com.espirit.moddev.connection.FsConnectionCompression;
import com.espirit.moddev.connection.FsConnectionEncryption;
import com.espirit.moddev.connection.FsConnectionType;
import com.espirit.moddev.util.FsUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultConnectionConfigValidatorTest {

	private DefaultConnectionConfigValidator _testling;
	private Config _config;

	@BeforeEach
	public void setUp() throws Exception {
		_testling = new DefaultConnectionConfigValidator();
		_config = mock(Config.class);
	}

	@Test
	public void validate() throws Exception {
		when(_config.getHost()).thenReturn("Localhost");
		when(_config.getUser()).thenReturn(FsUtil.VALUE_DEFAULT_USER);
		when(_config.getPassword()).thenReturn(FsUtil.VALUE_DEFAULT_USER);
		when(_config.getPort()).thenReturn(FsConnectionType.HTTP.getDefaultPort());
		when(_config.getConnectionMode()).thenReturn(FsConnectionType.HTTP);
		final Set<Violation> violation = _testling.validate(_config);

		assertThat(violation).as("Expect no violation").isEmpty();
	}

	@Test
	public void validateHost() throws Exception {
		when(_config.getHost()).thenReturn("");
		when(_config.getUser()).thenReturn(FsUtil.VALUE_DEFAULT_USER);
		when(_config.getPassword()).thenReturn(FsUtil.VALUE_DEFAULT_USER);
		when(_config.getPort()).thenReturn(FsConnectionType.HTTP.getDefaultPort());
		when(_config.getConnectionMode()).thenReturn(FsConnectionType.HTTP);

		final Set<Violation> violation = _testling.validate(_config);

		assertThat(violation).as("Expect a specific violation").containsExactly(new Violation("host", "is null or blank"));
	}

	@Test
	public void validateUser() throws Exception {
		when(_config.getHost()).thenReturn("Localhost");
		when(_config.getUser()).thenReturn(" ");
		when(_config.getPassword()).thenReturn(FsUtil.VALUE_DEFAULT_USER);
		when(_config.getPort()).thenReturn(FsConnectionType.HTTP.getDefaultPort());
		when(_config.getConnectionMode()).thenReturn(FsConnectionType.HTTP);

		final Set<Violation> violation = _testling.validate(_config);

		assertThat(violation).as("Expect one violation").hasSize(1);
		assertThat(violation).as("Expect a specific violation").containsExactly(new Violation("user", "is null or blank"));
	}

	@Test
	public void validatePassword() throws Exception {
		when(_config.getHost()).thenReturn("Localhost");
		when(_config.getUser()).thenReturn(FsUtil.VALUE_DEFAULT_USER);
		when(_config.getPassword()).thenReturn("\t");
		when(_config.getPort()).thenReturn(FsConnectionType.HTTP.getDefaultPort());
		when(_config.getConnectionMode()).thenReturn(FsConnectionType.HTTP);

		final Set<Violation> violation = _testling.validate(_config);

		assertThat(violation).as("Expect one violation").hasSize(1);
		assertThat(violation).as("Expect a specific violation").containsExactly(new Violation("password", "is null or blank"));
	}

	@Test
	public void validatePort() throws Exception {
		when(_config.getHost()).thenReturn("Localhost");
		when(_config.getUser()).thenReturn(FsUtil.VALUE_DEFAULT_USER);
		when(_config.getPassword()).thenReturn(FsUtil.VALUE_DEFAULT_USER);
		when(_config.getPort()).thenReturn(null);
		when(_config.getConnectionMode()).thenReturn(FsConnectionType.HTTP);

		final Set<Violation> violation = _testling.validate(_config);

		assertThat(violation).as("Expect one violation").hasSize(1);
		assertThat(violation).as("Expect a specific violation").containsExactly(new Violation("port", "is null"));
	}

	@Test
	public void validateConnectionMode() throws Exception {
		when(_config.getHost()).thenReturn("Localhost");
		when(_config.getUser()).thenReturn(FsUtil.VALUE_DEFAULT_USER);
		when(_config.getPassword()).thenReturn(FsUtil.VALUE_DEFAULT_USER);
		when(_config.getPort()).thenReturn(FsConnectionType.HTTP.getDefaultPort());
		when(_config.getConnectionMode()).thenReturn(null);

		final Set<Violation> violation = _testling.validate(_config);

		assertThat(violation).as("Expect one violation").hasSize(1);
		assertThat(violation).as("Expect a specific violation").containsExactly(new Violation("connectionMode", "is null"));
	}

	@Test
	public void validateEncryption_nullProducesNoViolation() {
		when(_config.getHost()).thenReturn("Localhost");
		when(_config.getUser()).thenReturn(FsUtil.VALUE_DEFAULT_USER);
		when(_config.getPassword()).thenReturn(FsUtil.VALUE_DEFAULT_USER);
		when(_config.getPort()).thenReturn(FsConnectionType.HTTP.getDefaultPort());
		when(_config.getConnectionMode()).thenReturn(FsConnectionType.HTTP);
		when(_config.getConnectionEncryption()).thenReturn(null);

		final Set<Violation> violations = _testling.validate(_config);

		assertThat(violations).as("Expect no violation for null encryption").isEmpty();
	}

	@Test
	public void validateEncryption_validEnumProducesNoViolation() {
		when(_config.getHost()).thenReturn("Localhost");
		when(_config.getUser()).thenReturn(FsUtil.VALUE_DEFAULT_USER);
		when(_config.getPassword()).thenReturn(FsUtil.VALUE_DEFAULT_USER);
		when(_config.getPort()).thenReturn(FsConnectionType.HTTP.getDefaultPort());
		when(_config.getConnectionMode()).thenReturn(FsConnectionType.HTTP);
		when(_config.getConnectionEncryption()).thenReturn(FsConnectionEncryption.TLS);

		final Set<Violation> violations = _testling.validate(_config);

		assertThat(violations).as("Expect no violation for valid encryption").isEmpty();
	}

	@Test
	public void validateEncryption_getterThrowingIllegalArgumentExceptionProducesViolation() {
		when(_config.getHost()).thenReturn("Localhost");
		when(_config.getUser()).thenReturn(FsUtil.VALUE_DEFAULT_USER);
		when(_config.getPassword()).thenReturn(FsUtil.VALUE_DEFAULT_USER);
		when(_config.getPort()).thenReturn(FsConnectionType.HTTP.getDefaultPort());
		when(_config.getConnectionMode()).thenReturn(FsConnectionType.HTTP);
		when(_config.getConnectionEncryption()).thenThrow(new IllegalArgumentException("bogus"));

		final Set<Violation> violations = _testling.validate(_config);

		assertThat(violations).as("Expect one violation for invalid encryption value").hasSize(1);
		assertThat(violations).as("Expect encryption violation").containsExactly(new Violation("encryption", "'bogus' is not a valid value"));
	}

	@Test
	public void validateCompression_nullProducesNoViolation() {
		when(_config.getHost()).thenReturn("Localhost");
		when(_config.getUser()).thenReturn(FsUtil.VALUE_DEFAULT_USER);
		when(_config.getPassword()).thenReturn(FsUtil.VALUE_DEFAULT_USER);
		when(_config.getPort()).thenReturn(FsConnectionType.HTTP.getDefaultPort());
		when(_config.getConnectionMode()).thenReturn(FsConnectionType.HTTP);
		when(_config.getConnectionCompression()).thenReturn(null);

		final Set<Violation> violations = _testling.validate(_config);

		assertThat(violations).as("Expect no violation for null compression").isEmpty();
	}

	@Test
	public void validateCompression_validEnumProducesNoViolation() {
		when(_config.getHost()).thenReturn("Localhost");
		when(_config.getUser()).thenReturn(FsUtil.VALUE_DEFAULT_USER);
		when(_config.getPassword()).thenReturn(FsUtil.VALUE_DEFAULT_USER);
		when(_config.getPort()).thenReturn(FsConnectionType.HTTP.getDefaultPort());
		when(_config.getConnectionMode()).thenReturn(FsConnectionType.HTTP);
		when(_config.getConnectionCompression()).thenReturn(FsConnectionCompression.ZSTD);

		final Set<Violation> violations = _testling.validate(_config);

		assertThat(violations).as("Expect no violation for valid compression").isEmpty();
	}

	@Test
	public void validateCompression_getterThrowingIllegalArgumentExceptionProducesViolation() {
		when(_config.getHost()).thenReturn("Localhost");
		when(_config.getUser()).thenReturn(FsUtil.VALUE_DEFAULT_USER);
		when(_config.getPassword()).thenReturn(FsUtil.VALUE_DEFAULT_USER);
		when(_config.getPort()).thenReturn(FsConnectionType.HTTP.getDefaultPort());
		when(_config.getConnectionMode()).thenReturn(FsConnectionType.HTTP);
		when(_config.getConnectionCompression()).thenThrow(new IllegalArgumentException("bogus"));

		final Set<Violation> violations = _testling.validate(_config);

		assertThat(violations).as("Expect one violation for invalid compression value").hasSize(1);
		assertThat(violations).as("Expect compression violation").containsExactly(new Violation("compression", "'bogus' is not a valid value"));
	}

}
