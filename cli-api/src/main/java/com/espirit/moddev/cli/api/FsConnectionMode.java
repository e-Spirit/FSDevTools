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
 * The enum Fs connection mode.
 */
public enum FsConnectionMode {

    /**
     * The HTTP.
     */
    HTTP(ConnectionManager.HTTP_MODE, Constants.HTTP_PORT),

    /**
     * The SOCKET.
     */
    SOCKET(ConnectionManager.SOCKET_MODE, Constants.SOCKET_PORT);

    private final int code;
    private final Integer defaultPort;

    FsConnectionMode(int code, int defaultPort) {
        this.code = code;
        this.defaultPort = defaultPort;
    }

    /**
     * Gets code.
     *
     * @return the code
     */
    public int getCode() {
        return code;
    }

    /**
     * Gets default port.
     *
     * @return the default port
     */
    public Integer getDefaultPort() {
        return defaultPort;
    }

    /**
     * Is default port.
     *
     * @param port the port
     * @return the boolean
     */
    public boolean isDefaultPort(Integer port) {
        return defaultPort.equals(port);
    }

    @Override
    public String toString() {
        return name();
    }

    private static final class Constants {

        /**
         * The constant HTTP_PORT.
         */
        public static final int HTTP_PORT = 8000;
        /**
         * The constant SOCKET_PORT.
         */
        public static final int SOCKET_PORT = 1088;

        private Constants() {
        }
    }
}
