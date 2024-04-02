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

rootProject.name = "fs-cli"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("jetbrains-annotations", "org.jetbrains:annotations:24.0.1")

            version("log4j", "2.17.2")
            library("log4j-core", "org.apache.logging.log4j", "log4j-core").versionRef("log4j")
            library("log4j-slf4j", "org.apache.logging.log4j", "log4j-slf4j-impl").versionRef("log4j")

            version("airline", "2.8.5")
            library("airline", "com.github.rvesse", "airline").versionRef("airline")
            library("airline-help-markdown", "com.github.rvesse", "airline-help-markdown").versionRef("airline")
            library("beanshell", "com.github.pejobo:beanshell2:2.1.9")
            library("classgraph", "io.github.classgraph:classgraph:4.8.143")
            library("commons-lang3", "org.apache.commons:commons-lang3:3.12.0")
            library("commons-compress", "org.apache.commons:commons-compress:1.21")
            library("guava", "com.google.guava:guava:31.1-jre")
            library("hamcrest", "org.hamcrest:java-hamcrest:2.0.0.0")
            library("jackson-databind", "com.fasterxml.jackson.core:jackson-databind:2.13.2.2")
            library("lombok", "org.projectlombok:lombok:1.18.22")
            library("slf4j-api", "org.slf4j:slf4j-api:1.7.36")
        }
        create("testlibs") {
            version("junit-jupiter", "5.8.2")
            library("junit-jupiter-api", "org.junit.jupiter", "junit-jupiter-api").versionRef("junit-jupiter")
            library("junit-jupiter-engine", "org.junit.jupiter", "junit-jupiter-engine").versionRef("junit-jupiter")
            library("junit-vintage-engine", "org.junit.vintage", "junit-vintage-engine").versionRef("junit-jupiter")
            library("junit-jupiter-params", "org.junit.jupiter", "junit-jupiter-params").versionRef("junit-jupiter")
            library("assertj", "org.assertj:assertj-core:3.22.0")
            library("mockito", "org.mockito:mockito-junit-jupiter:4.4.0")
            library("archunit", "com.tngtech.archunit:archunit-junit5:1.2.1")
        }
    }
}

include("fsdevtools-cli")
include("fsdevtools-cli-api")
include("fsdevtools-common")
include("fsdevtools-sharedutils")
include("fsdevtools-serverrunner")
include("fsdevtools-commands")
include("fsdevtools-docs")
include("fsdevtools-docs-generator")
include("fsdevtools-commands:feature")
include("fsdevtools-commands:module")
include("fsdevtools-commands:project")
include("fsdevtools-commands:schedule")
include("fsdevtools-commands:script")
include("fsdevtools-commands:server")
include("fsdevtools-commands:service")
include("fsdevtools-commands:test")
include("fsdevtools-commands:custom-command-example")
include("fsdevtools-scriptengines")
include("fsdevtools-scriptengines:Groovy")
include("fsdevtools-scriptengines:Javascript")