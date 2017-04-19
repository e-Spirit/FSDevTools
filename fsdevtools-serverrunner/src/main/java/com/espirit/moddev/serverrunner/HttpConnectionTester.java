package com.espirit.moddev.serverrunner;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import lombok.extern.slf4j.Slf4j;

/**
 * Used to check whether an HTTP-Connection can be made to a given URL.
 */
@Slf4j
public class HttpConnectionTester {
    private HttpConnectionTester() {}

    private static final String PROBLEM_READING = "Problem reading data from FirstSpirit server process";

    /**
     * Tests if a URL can be reached and returns a success response code (2xx or 3xx)
     *
     * @param url the URL that should be tested
     * @return whether the URL can be reached and returns a success response code (2xx or 3xx)
     */
    static boolean testConnection(final URL url) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            connection.connect();
            //HTTP response code 2xx or 3xx
            return isSuccess(connection.getResponseCode());
        } catch (IOException | RuntimeException e) {
            log.trace(PROBLEM_READING, e);
        } finally {
            try {
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (final RuntimeException e) {
                log.trace(PROBLEM_READING, e);
            }
        }
        return false;
    }

    /**
     * Whether an http response code is success (2x or 3xx) or not
     *
     * @param httpResponseCode the HTTP response code
     * @return whether it was a success code
     */
    static boolean isSuccess(final int httpResponseCode) {
        return httpResponseCode >= 200 && httpResponseCode <= 399;
    }
}
