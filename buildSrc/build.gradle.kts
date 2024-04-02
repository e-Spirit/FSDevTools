plugins {
    `kotlin-dsl`
}

val useArtifactory = project.hasProperty("artifactory_username") && project.hasProperty("artifactory_password")

repositories {
    if (useArtifactory) {
        maven(url = "https://artifactory.e-spirit.de/artifactory/repo") {
            credentials {
                username = property("artifactory_username") as String
                password = property("artifactory_password") as String
            }
        }
    } else {
        mavenCentral()
    }
}