/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2020 e-Spirit AG
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
import org.jetbrains.annotations.NotNull;

public enum FsConnectionType {

	HTTP(ConnectionManager.HTTP_MODE, "HTTP", 8000),
	HTTPS(ConnectionManager.HTTP_MODE, "HTTP", 8000),
	SOCKET(ConnectionManager.SOCKET_MODE, "SOCKET", 1088);

	private final int _fsMode;
	private final String _propertyName;
	private final int _defaultPort;

	FsConnectionType(final int fsMode, @NotNull final String propertyName, final int defaultPort) {
		_fsMode = fsMode;
		_propertyName = propertyName;
		_defaultPort = defaultPort;
	}

	public int getFsMode() {
		return _fsMode;
	}

	@NotNull
	public String getPropertyName() {
		return _propertyName;
	}

	public int getDefaultPort() {
		return _defaultPort;
	}

}
