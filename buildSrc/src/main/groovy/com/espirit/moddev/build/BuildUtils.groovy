package com.espirit.moddev.build

import java.util.regex.Pattern

class BuildUtils {

    private BuildUtils() {
        // private constructor ; utility class
    }

    /**
     * Converts the project.properties to a Hashtable<String, String>. Used to filter during the build process
     * @return the converted project.properties
     */
    static Hashtable<String, String> mapToHashtable(final Map<String, Object> properties) {
        final Hashtable<String, String> result = new Hashtable()
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            result.put(entry.getKey(), entry.getValue().toString())
        }
        return result;
    }

    /**
     * Returns whether the current version is a snapshot-version.
     *
     * @return {@code true} if the current version is a snapshot, otherwise {@code false}
     */
    static boolean isSnapshotVersion(final Object version) {
        return version.contains("-SNAPSHOT")
    }

    /**
     * Returns the target artifactory based on the version. The snapshot repository will be returned if the version ends
     * with "SNAPSHOT", otherwise the release repository will be returned.
     *
     * @return the target artifactory based on the version
     */
    static String getArtifactory(final Object version, final String snapshotRepository, final String releaseRepository) {
        return "https://artifactory.e-spirit.de/artifactory/${isSnapshotVersion(version) ? snapshotRepository : releaseRepository}"
    }

    /**
     * Tries to determine 2 free randomized ports 30000 <= x <= 65535
     *
     * @return a{@link List list} with 2 free ports
     */
    static List<Integer> guessFreePorts() {
        return guessFreePorts(2)
    }

    /**
     * Tries to determine a list of free randomized ports 30000 <= x <= 65535 with the given count of ports
     *
     * @return a{@link List list} with free ports
     */
    static List<Integer> guessFreePorts(final int count) {
        return guessFreePorts(count, 30000, 65535, 100)
    }

    /**
     * Tries to determine a list of free randomized ports lowerBound <= x <= upperBound with the given count of ports and the given maxRetries
     *
     * @return a{@link List list} with free ports
     */
    static List<Integer> guessFreePorts(final int count, int lowerBound, int upperBound, final int maxRetries) {
        final List<Integer> result = new ArrayList<>(count)
        int retries = 0;
        while (result.size() < count) {
            try {
                new ServerSocket(0).withCloseable { socket ->
                    final int port = socket.getLocalPort()
                    if (port >= lowerBound && port <= upperBound && !result.contains(port)) {
                        result.add(port);
                    }
                    return socket
                }
            } catch (final IOException e) {
                throw new IllegalStateException(e);
            }
            if (++retries > maxRetries) {
                throw new IllegalStateException("Could not determine randomized ports!")
            }
        }
        return result;
    }


}
