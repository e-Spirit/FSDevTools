/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2016 e-Spirit AG
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

package com.espirit.moddev.cli.api;

import de.espirit.firstspirit.access.ConnectionManager;

/**
 * Connection modes that can be used to access a FirstSpirit server.
 *
 * @author e-Spirit AG
 */
public enum FsConnectionMode {

    /**
     * Http connection mode.
     * Uses {@link com.espirit.moddev.cli.api.FsConnectionMode.Constants#DEFAULT_HTTP_PORT} as default port.
     */
    HTTP(ConnectionManager.HTTP_MODE, Constants.DEFAULT_HTTP_PORT),

    /**
     * Https connection mode.
     * Uses {@link com.espirit.moddev.cli.api.FsConnectionMode.Constants#DEFAULT_HTTP_PORT} as default port.
     */
    HTTPS(ConnectionManager.HTTP_MODE, Constants.DEFAULT_HTTP_PORT),

    /**
     * Socket connection mode.
     * Uses {@link com.espirit.moddev.cli.api.FsConnectionMode.Constants#DEFAULT_SOCKET_PORT} as default port.
     */
    SOCKET(ConnectionManager.SOCKET_MODE, Constants.DEFAULT_SOCKET_PORT);

    private final int code;
    private final int defaultPort;

    FsConnectionMode(int code, int defaultPort) {
        this.code = code;
        this.defaultPort = defaultPort;
    }

    /**
     * Get the code that identifies this connection mode.
     *
     * @return the code that identifies this connection mode
     */
    public int getCode() {
        return code;
    }

    /**
     * Get the default port used by this connection mode.
     *
     * @return the default port used by this connection mode
     */
    public int getDefaultPort() {
        return defaultPort;
    }

    @Override
    public String toString() {
        return name();
    }

    public static final class Constants {

        /**
         * Default FirstSpirit http port (8000).
         */
        public static final int DEFAULT_HTTP_PORT = 8000;

        /**
         * Default FirstSpirit socket port (1088).
         */
        public static final int DEFAULT_SOCKET_PORT = 1088;

        private Constants() {
        }
    }
}
