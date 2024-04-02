/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2024 Crownpeak Technology GmbH
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


import org.apache.tools.ant.filters.FixCrLfFilter
import org.apache.tools.ant.filters.ReplaceTokens
import java.util.*

var firstSpiritVersion = "5.2.231105"


/**
 * plugins
 */

plugins {
    application
    id("com.github.johnrengelman.shadow") version("7.0.0")
    id("org.ajoberstar.grgit") version("5.0.0")
    `maven-publish`
    id("com.github.breadmoirai.github-release") version("2.5.2")
    id("net.researchgate.release") version("3.0.2")
    idea
}

///////////////////////////////////////////////////////
// configure gradle wrapper
///////////////////////////////////////////////////////

tasks.wrapper {
    gradleVersion = "8.7"
    distributionType = Wrapper.DistributionType.ALL
}


///////////////////////////////////////////////////////
// configure application plugin
///////////////////////////////////////////////////////

application {
    applicationName = "fs-devtools"
    mainClass = "com.espirit.moddev.cli.Main"
}

/**
 * project configuration
 */

///////////////////////////////////////////////////////
// configure global variables
///////////////////////////////////////////////////////

/**
 * Uses {@code grgit} to get the name of the current branch
 * @return
 */
// set version to {{issue.key}}-SNAPSHOT for feature branches
val branchName: String = grgit.branch.current().name
Regex("(?:.*/)?[^A-Z]*([A-Z]+-[0-9]+).*").matchEntire(branchName)?.let {
    project.version = "${it.groupValues[1]}-SNAPSHOT"
}

if (project.hasProperty("useLatestFirstSpiritBuild")) {
    firstSpiritVersion = "EAP-SNAPSHOT"
    logger.info("Using FirstSpirit server version 'EAP-SNAPSHOT'")
} else {
    logger.info("Using hard-coded FirstSpirit server version: {}", firstSpiritVersion)
}

// setup some variables
extra["fsRuntimeVersion"] = firstSpiritVersion
val javaVersion = JavaVersion.VERSION_17
val projectName = "FirstSpirit CLI"


// setup repositories
val releaseRepository = "core-platform-mvn-release"
val snapshotRepository = "core-platform-mvn-snapshot"

val useArtifactory = project.hasProperty("artifactory_username") && project.hasProperty("artifactory_password")

///////////////////////////////////////////////////////
// additional scopes
///////////////////////////////////////////////////////

val testRuntimeJar: Configuration by configurations.creating

/**
 * CONFIGURE PROJECTS
 */

///////////////////////////////////////////////////////
// dependencies for root project
///////////////////////////////////////////////////////

dependencies {
    implementation(project(":fsdevtools-cli"))

    // tests
    testRuntimeJar(group = "de.espirit.firstspirit", name = "fs-isolated-runtime", version = firstSpiritVersion, ext = "jar")
}

///////////////////////////////////////////////////////
// randomize port for integration tests
//
// NOTE: port should be in range 49152 <= port <= 65535
//
// see links for detailed description:
// - https://en.wikipedia.org/wiki/List_of_TCP_and_UDP_port_numbers#Dynamic,_private_or_ephemeral_ports
// - https://en.wikipedia.org/wiki/Ephemeral_port
//
///////////////////////////////////////////////////////

val ports = guessFreePorts()
extra["serverHttpPort"] = ports[0]
extra["serverSocketPort"] = ports[1]

///////////////////////////////////////////////////////
// configure all projects
///////////////////////////////////////////////////////

allprojects {
    repositories {
        if (useArtifactory) {
            maven(url = "https://artifactory.e-spirit.de/artifactory/repo") {
                credentials {
                    username = property("artifactory_username") as String
                    password = property("artifactory_password") as String
                }
            }
        } else {
            mavenLocal()
            mavenCentral()
        }
    }

    plugins.apply("maven-publish")

    group = "com.espirit.moddev.fsdevtools"
    version = rootProject.version

    publishing {
        if (useArtifactory) {
            repositories {
                maven(url = getArtifactory(snapshotRepository, releaseRepository)) {
                    credentials {
                        username = property("artifactory_username") as String
                        password = property("artifactory_password") as String
                    }
                }
            }
        }
    }
}

///////////////////////////////////////////////////////
// configure sub projects
///////////////////////////////////////////////////////

subprojects {
    plugins.apply("java-library")
    plugins.apply("idea")
    idea.module.isDownloadJavadoc = true
    idea.module.isDownloadSources = true

    // setup java
    tasks.compileJava {
        options.encoding = "UTF-8"
    }

    tasks.compileTestJava {
        options.encoding = "UTF-8"
    }

    java.sourceCompatibility = javaVersion
    java.targetCompatibility = javaVersion

    extra["firstSpiritVersion"] = firstSpiritVersion
    extra["gitHash"] = rootProject.grgit.head().id

    ///////////////////////////////////////////////////////
    // common dependencies for every sub project
    ///////////////////////////////////////////////////////

    dependencies {
        compileOnly(group = "de.espirit.firstspirit", name = "fs-isolated-runtime", version = firstSpiritVersion)
        implementation(rootProject.libs.slf4j.api)

        testRuntimeOnly(rootProject.testlibs.junit.jupiter.engine)
        testImplementation(rootProject.testlibs.junit.jupiter.api)
        testImplementation(rootProject.testlibs.junit.jupiter.params)
        testImplementation(rootProject.libs.hamcrest)
        testImplementation(rootProject.testlibs.mockito)
        testImplementation(rootProject.testlibs.assertj)
        testImplementation(rootProject.libs.jetbrains.annotations)
        testImplementation(
            group = "de.espirit.firstspirit",
            name = "fs-isolated-runtime",
            version = firstSpiritVersion,
            ext = "jar"
        )
    }

    java.withSourcesJar()
    java.withJavadocJar()

    ///////////////////////////////////////////////////////
    // publishing
    ///////////////////////////////////////////////////////

    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
            }
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

/**
 * DISTRIBUTION
 */

///////////////////////////////////////////////////////
// creates the fat jar (which is included in the distribution in "/lib/...")
///////////////////////////////////////////////////////
tasks.jar {
    manifest {
        attributes(
                "Manifest-Version" to "1.0",
                "Main-Class" to "com.espirit.moddev.cli.Main",
                "Multi-Release" to "true",
        )
    }
}

tasks.shadowJar {
    archiveFileName.set("fsdevtools-cli-${project.version}.jar")
    destinationDirectory.set(layout.buildDirectory.dir("shadowJAR"))
}

///////////////////////////////////////////////////////
// disable unneeded tasks
///////////////////////////////////////////////////////
tasks.shadowDistTar {
    enabled = false
}

tasks.shadowDistZip {
    enabled = false
}

tasks.distZip {
    enabled = false
}

tasks.distTar {
    enabled = false
}

///////////////////////////////////////////////////////
// assemble utility methods
///////////////////////////////////////////////////////

fun getJsonSchemas(project : Project) : List<File> {
    val files = mutableListOf<File>()
    val dir = File("${project.projectDir}/json-schema")
    if (dir.exists()) {
        files.add(dir)
    }
    project.subprojects.stream().forEach { subProject ->
        val subFiles = getJsonSchemas(subProject)
        for (file in subFiles) {
            if (!files.contains(file)) {
                files.add(file)
            }
        }
    }
    return files
}

val createDocumentationJson by tasks.creating(JavaExec::class) {
    dependsOn(tasks.classes, "writeArtifactInfo")
    group = "documentation"
    description = "Run the main class with JavaExecTask"
    classpath = project(":fsdevtools-docs-generator").sourceSets.main.get().runtimeClasspath
    mainClass = "com.espirit.moddev.cli.documentation.commands.CommandDocumentationGenerator"
    args = listOf("--file", "${project(":fsdevtools-docs").projectDir}/build/assets/data.json")
}

val writeArtifactInfo: Task by tasks.creating {
    group = "documentation"
    doLast {
        val branchName = grgit.branch.current().name
        val commit = grgit.head().abbreviatedId
        var version = rootProject.version.toString()
        if (System.getProperty("isCI") == null) {
            version = "$version (DEV)"
        }
        mkdir("${project(":fsdevtools-docs").projectDir}/build/assets/")
        File("${project(":fsdevtools-docs").projectDir}/build/assets/", "application.json").writeText("""
{
  "version": "$version",
  "branch": "$branchName",
  "commit": "$commit"
}
""")
    }
}

val tokens = Properties()
tokens.setProperty("projectName", projectName)
tokens.setProperty("projectVersion", project.version.toString())
tokens.setProperty("firstSpiritVersion", firstSpiritVersion)
tokens.setProperty("javaVersion", javaVersion.toString())

///////////////////////////////////////////////////////
// assembles the tar.gz file
///////////////////////////////////////////////////////

val assembleTarGz by tasks.creating(Tar::class) {
    dependsOn(tasks.shadowJar, "fsdevtools-docs:buildVueApp")

    group = "build"
    archiveBaseName = "fs-cli"
    archiveVersion.set(project.version as String)
    archiveExtension = "tar.gz"
    compression = Compression.GZIP
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))

    // copy README.txt
    from("${project.projectDir}/archive/README.txt") {
        into("fs-cli")
        filter<ReplaceTokens>("tokens" to tokens)
    }
    // copy conf-dir with filtering
    from("${project.projectDir}/archive/conf") {
        into("fs-cli/conf")
    }
    // copy fs-cli.sh with execution rights & lf
    from("${project.projectDir}/archive/bin/fs-cli.sh") {
        into("fs-cli/bin")
        fileMode = "755".toInt(8)
        filter<FixCrLfFilter>("eol" to FixCrLfFilter.CrLf.newInstance("lf"))
    }
    // copy fs-cli.cmd with execution rights & crlf
    from("${project.projectDir}/archive/bin/fs-cli.cmd") {
        into("fs-cli/bin")
        fileMode = "755".toInt(8)
        filter<FixCrLfFilter>("eol" to FixCrLfFilter.CrLf.newInstance("crlf"))
    }
    // copy fat jar
    into("fs-cli/lib") {
        from(tasks.shadowJar.map { it.archiveFile })
    }
    // include json schemas
    into("fs-cli/docs/json-schema") {
        from(getJsonSchemas(project));
    }
    // include documentation
    from("fsdevtools-docs/dist/") {
        into("fs-cli/docs")
    }
}

///////////////////////////////////////////////////////
// assembles the zip file
///////////////////////////////////////////////////////

val assembleZip by tasks.creating(Zip::class) {
    dependsOn(tasks.shadowJar, "fsdevtools-docs:buildVueApp")

    group = "build"

    archiveBaseName = "fs-cli"
    //archiveVersion = project.version
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))

    // copy README.txt
    from("${project.projectDir}/archive/README.txt") {
        into("fs-cli")
        filter<ReplaceTokens>("tokens" to tokens)
    }
    // copy conf-dir with filtering
    from("${project.projectDir}/archive/conf") {
        into("fs-cli/conf")
    }
    // copy fs-cli.sh with execution rights & lf
    from("${project.projectDir}/archive/bin/fs-cli.sh") {
        into("fs-cli/bin")
        fileMode = "755".toInt(8)
        filter<FixCrLfFilter>("eol" to FixCrLfFilter.CrLf.newInstance("lf"))
    }
    // copy fs-cli.cmd with execution rights & crlf
    from("${project.projectDir}/archive/bin/fs-cli.cmd") {
        into("fs-cli/bin")
        fileMode = "755".toInt(8)
        filter<FixCrLfFilter>("eol" to FixCrLfFilter.CrLf.newInstance("crlf"))
    }
    // copy fat jar
    into("fs-cli/lib") {
        from(tasks.shadowJar.map { it.archiveFile })
    }
    // include json schemas
    into("fs-cli/docs/json-schema") {
        from(getJsonSchemas(project));
    }
    // include documentation
    from("fsdevtools-docs/dist/") {
        into("fs-cli/docs")
    }
}

///////////////////////////////////////////////////////
// assembles the zip & tar.gz file
///////////////////////////////////////////////////////

tasks.assemble {
    dependsOn(assembleTarGz, assembleZip)
}

/**
 *  GITHUB RELEASE MANAGEMENT
 */

///////////////////////////////////////////////////////
// setup the githubRelease plugin
///////////////////////////////////////////////////////

val assets = mutableListOf(assembleTarGz.outputs.files, assembleZip.outputs.files)
for (project in project.project(":fsdevtools-scriptengines").subprojects) {
    val name = project.name
    val task = tasks.create(name, org.gradle.jvm.tasks.Jar::class)
    task.group = "script-engines"
    task.archiveFileName.set("fs-cli-scriptengine-${name}-${project.version}.jar")
    task.dependsOn("fsdevtools-scriptengines:${name}:build")
    task.dependsOn("fsdevtools-scriptengines:${name}:shadowJar")
    task.from(zipTree(project.layout.buildDirectory.file("shadowJAR/${name}-${project.version}.jar")))
    task.destinationDirectory.set(layout.buildDirectory.dir("distributions"))
    assets.add(task.outputs.files)
    tasks.build.get().dependsOn(task)
    tasks.githubRelease.get().dependsOn(task)
}

val releaseText = """
The zip and the tar.gz files are our windows and linux/mac distributions. This binary release does **not** contain FirstSpirit api jars (*fs-isolated-runtime.jar*). You must add it to the *"lib"* directory. 

For further information about the prerequisites, please take a look at the [README](https://github.com/e-Spirit/FSDevTools/blob/master/README.md#prerequisites). 
"""

if (project.hasProperty("github_fsdevtools_api_token")) {
    githubRelease {
        token(property("github_fsdevtools_api_token") as String)
        owner = "e-Spirit"
        tagName = "${project.version}"
        targetCommitish = "master"
        repo = "FSDevTools"
        body = releaseText
        releaseName = "Release " + project.version
        setReleaseAssets(assets)
    }
}

// release is only allowed for master branch & no snapshots
if (!isSnapshotVersion()) {
    tasks.githubRelease {
        dependsOn(tasks.assemble)
    }
    tasks.publish {
        dependsOn(tasks.githubRelease)
    }
} else {
    // disable githubRelease for non-master-branches/non-snapshots
    tasks.githubRelease {
        enabled = false
        doLast {
            // github release is not allowed for snapshots
            logger.error("GitHub Release is only allowed on master branch!")
        }
    }
}


/**
 * PUBLISHING
 */

publishing {
    publications {
        create<MavenPublication>("distributeArchives") {
            artifact(assembleZip)
            artifact(assembleTarGz)
        }
    }
}

/**
 * RELEASE MANAGEMENT
 */

///////////////////////////////////////////////////////
// setup the release plugin
///////////////////////////////////////////////////////

release {
    ignoredSnapshotDependencies = listOf("de.espirit.firstspirit:fs-license")

    git {
        requireBranch.set("master")
    }
}

subprojects {
    rootProject.tasks.afterReleaseBuild {
        dependsOn(tasks.publish)
    }
}