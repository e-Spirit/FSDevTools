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

// Starts the real CLI distribution in a real JVM (no mocked classloaders) to catch problems that only exist in the
// shaded jar: relocated classes, META-INF/services files, plugin classloading. Not published.
//
//   ./gradlew :fsdevtools-classloading-tests:classloadingTest
//   ./gradlew :fsdevtools-classloading-tests:classloadingTest -PbaselineJar=<path to an older fsdevtools-cli jar>
//
// With -PbaselineJar the same suite runs against that jar instead of the one built here.

plugins {
    idea
}

project.afterEvaluate {
    disablePublishing()
}

// the fixture jars are read from these projects' tasks while this script is evaluated
evaluationDependsOn(":fsdevtools-scriptengines:Groovy")
evaluationDependsOn(":fsdevtools-scriptengines:Javascript")
evaluationDependsOn(":fsdevtools-commands:custom-command-example")
evaluationDependsOnChildren()

val baselineJar = providers.gradleProperty("baselineJar")

// fs-isolated-runtime comes from an internal repository; lenient so that a machine without access can still build
val fsRuntimeJar: Configuration by configurations.creating

dependencies {
    compileOnly(project(":fsdevtools-cli-api"))
    compileOnly(project(":fsdevtools-commands:script"))

    fsRuntimeJar(group = "de.espirit.firstspirit", name = "fs-isolated-runtime", version = rootProject.extra["fsRuntimeVersion"] as String, ext = "jar")

    testImplementation(libs.jackson.databind)
}

///////////////////////////////////////////////////////
// the distribution under test: lib/ (CLI jar, FirstSpirit runtime, probe main), conf/
///////////////////////////////////////////////////////
val testDist by tasks.registering(Sync::class) {
    into(layout.buildDirectory.dir("test-dist"))

    into("lib") {
        if (baselineJar.isPresent) {
            from(baselineJar.map { file(it) })
        } else {
            from(rootProject.layout.buildDirectory.file("shadowJAR/fsdevtools-cli-${rootProject.version}.jar"))
            dependsOn(":shadowJar", ":verifyShadowJar")
        }
        from(fsRuntimeJar.incoming.artifactView { isLenient = true }.files)
        from(tasks.jar)
    }
    into("conf") {
        from(rootProject.file("archive/conf/log4j2.xml"))
    }
}

///////////////////////////////////////////////////////
// fixture jars that the tests copy into lib/ or plugins/ of a fresh copy of the distribution
///////////////////////////////////////////////////////
fun jarOf(projectPath: String, taskName: String): Provider<File> =
    project(projectPath).tasks.named<AbstractArchiveTask>(taskName).flatMap { it.archiveFile }.map { it.asFile }

val fixtures = mapOf(
    "groovyEngine" to jarOf(":fsdevtools-scriptengines:Groovy", "shadowJar"),
    "javascriptEngine" to jarOf(":fsdevtools-scriptengines:Javascript", "shadowJar"),
    "exampleCommand" to jarOf(":fsdevtools-commands:custom-command-example", "jar"),
    "probeEngine" to jarOf(":fsdevtools-classloading-tests:probe-engine", "shadowJar"),
    "bundlingEngine" to jarOf(":fsdevtools-classloading-tests:probe-engine", "bundlingJar"),
    "ownJacksonEngine" to jarOf(":fsdevtools-classloading-tests:probe-engine", "ownJacksonJar"),
)

// slow (a JVM per case), so it runs with "check" and not with "test"
tasks.test {
    enabled = false
}

val classloadingTest by tasks.registering(Test::class) {
    group = "verification"
    description = "Runs the shaded CLI distribution in real JVMs and loads commands and script engines like a user does"

    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath

    dependsOn(testDist)
    fixtures.values.forEach { jar -> inputs.file(jar) }
    inputs.dir(testDist.map { it.destinationDir })

    // the tests start their own JVMs, so there is nothing to configure for this one
    doFirst {
        systemProperty("fscli.testDist", testDist.get().destinationDir.absolutePath)
        systemProperty("fscli.requireRuntime", (System.getProperty("isCI") != null).toString())
        fixtures.forEach { (name, jar) -> systemProperty("fscli.fixture.$name", jar.get().absolutePath) }
    }
}

tasks.check {
    dependsOn(classloadingTest)
}

// the fixtures must be built before the tests copy them
classloadingTest {
    dependsOn(
        ":fsdevtools-scriptengines:Groovy:shadowJar",
        ":fsdevtools-scriptengines:Javascript:shadowJar",
        ":fsdevtools-commands:custom-command-example:jar",
        ":fsdevtools-classloading-tests:probe-engine:shadowJar",
        ":fsdevtools-classloading-tests:probe-engine:bundlingJar",
        ":fsdevtools-classloading-tests:probe-engine:ownJacksonJar",
    )
}
