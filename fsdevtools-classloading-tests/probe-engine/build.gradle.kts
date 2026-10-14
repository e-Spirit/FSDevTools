/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2026 Crownpeak Technology GmbH
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
 */

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.gradleup.shadow")
}

// Fixture plugins for the classloading tests. Not published.
project.afterEvaluate {
    disablePublishing()
}

// bundled by the "bundling" variant only: other versions of libraries that the CLI also contains
val bundled: Configuration by configurations.creating

// bundled by the "own Jackson" variant only: an unrelocated Jackson, so that annotations of result classes can be loaded
val bundledJackson: Configuration by configurations.creating

dependencies {
    // everything the plugin needs from the CLI is provided at runtime, as for any real plugin
    compileOnly(project(":fsdevtools-cli-api"))
    compileOnly(project(":fsdevtools-common"))

    bundled("com.github.rvesse:airline:2.8.4")
    bundled("com.fasterxml.jackson.core:jackson-annotations:2.17.0")

    bundledJackson(libs.jackson.databind)
}

tasks.shadowJar {
    archiveFileName.set("probe-engine-${project.version}.jar")
    destinationDirectory.set(layout.buildDirectory.dir("shadowJAR"))
}

// same classes as probe-engine, plus own copy of Jackson (no Airline: the variant is put into lib/, where a second Airline clashes)
val ownJacksonJar by tasks.registering(ShadowJar::class) {
    archiveFileName.set("own-jackson-engine-${project.version}.jar")
    destinationDirectory.set(layout.buildDirectory.dir("shadowJAR"))
    from(sourceSets.main.get().output)
    configurations = listOf(bundledJackson)
    // Jackson's service files and module descriptors must stay intact
    mergeServiceFiles()
}

// same classes as probe-engine, plus own copies of Airline and jackson-annotations (upload-hook scenario of DEVEX-706)
val bundlingJar by tasks.registering(ShadowJar::class) {
    archiveFileName.set("bundling-engine-${project.version}.jar")
    destinationDirectory.set(layout.buildDirectory.dir("shadowJAR"))
    from(sourceSets.main.get().output)
    configurations = listOf(bundled)
}

tasks.assemble {
    dependsOn(bundlingJar, ownJacksonJar)
}
