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