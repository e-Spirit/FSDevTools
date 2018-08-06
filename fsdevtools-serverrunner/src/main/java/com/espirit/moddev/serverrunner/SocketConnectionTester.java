package com.espirit.moddev.serverrunner;

import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.ConnectionManager;
import de.espirit.firstspirit.common.MaximumNumberOfSessionsExceededException;
import de.espirit.firstspirit.server.authentication.AuthenticationException;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

/**
 * Used to check whether a socket connection can be established to a given server.
 */
@Slf4j
class SocketConnectionTester {
    private SocketConnectionTester() {}

    private static final String PROBLEM_CONNECTING = "Problem connecting to FirstSpirit server process";

    /**
     * Tests if a FirstSpirit server can be reached
     *
     * @param hostname the hostname of the FirstSpirit server
     * @param socketPort the socket port to connect to
     * @return whether the server can be reached
     */
    static boolean testConnection(final String hostname, final int socketPort, final String adminPassword) {

        try (Connection connection = ConnectionManager.getConnection(hostname, socketPort, ConnectionManager.SOCKET_MODE, "Admin", adminPassword)) {
            connection.connect();
            return true;
        } catch (IOException | RuntimeException | AuthenticationException | MaximumNumberOfSessionsExceededException e) {
            log.trace(PROBLEM_CONNECTING, e);
            return false;
        }

    }

}
