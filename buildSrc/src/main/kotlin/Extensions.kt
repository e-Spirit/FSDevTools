import org.gradle.api.Project
import java.net.ServerSocket

/**
 * Returns whether the current version is a snapshot-version.
 *
 * @return `true` if the current version is a snapshot, otherwise `false`
 */
fun Project.isSnapshotVersion() : Boolean {
    return version.toString().contains("-SNAPSHOT")
}

/**
 * Returns the target artifactory based on the version. The snapshot repository will be returned if the version ends
 * with "SNAPSHOT", otherwise the release repository will be returned.
 *
 * @return the target artifactory based on the version
 */
fun Project.getArtifactory(snapshotRepository: String, releaseRepository: String) : String {
    return if (isSnapshotVersion()) {
        "https://artifactory.e-spirit.de/artifactory/$snapshotRepository"
    } else {
        "https://artifactory.e-spirit.de/artifactory/$releaseRepository"
    }
}

fun Project.disablePublishing() {
    tasks.getByName("generatePomFileForMavenPublication").enabled = false
    tasks.getByName("publish").enabled = false
    tasks.getByName("publishToMavenLocal").enabled = false
    tasks.getByName("publishMavenPublicationToMavenRepository").enabled = false
    tasks.getByName("publishMavenPublicationToMavenLocal").enabled = false
    tasks.getByName("publishAllPublicationsToMavenRepository").enabled = false
}

/**
 * Tries to determine 2 free randomized ports 30000 <= x <= 65535
 *
 * @return a [List] with 2 free ports
 */
fun guessFreePorts() : List<Int> {
    return guessFreePorts(2)
}

/**
 * Tries to determine a list of free randomized ports 30000 <= x <= 65535 with the given count of ports
 *
 * @return a [List] with free ports
 */
fun guessFreePorts(count: Int) : List<Int> {
    return guessFreePorts(count, 30000, 65535, 100)
}

/**
 * Tries to determine a list of free randomized ports lowerBound <= x <= upperBound with
 * the given count of ports and the given maxRetries
 *
 * @return a [List] with free ports
 */
fun guessFreePorts(count: Int, lowerBound: Int, upperBound: Int, maxRetries: Int) : List<Int> {
    val result = mutableListOf<Int>()
    var retries = 0

    while (result.size < count) {
        ServerSocket(0).use { socket ->
            val port = socket.getLocalPort()
            if (port in lowerBound..upperBound && !result.contains(port)) {
                result.add(port);
            }
        }
        if (++retries > maxRetries) {
            throw IllegalStateException("Could not determine randomized ports!")
        }
    }

    return result
}