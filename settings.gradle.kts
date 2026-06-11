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
 *
 */

rootProject.name = "fs-cli"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("jetbrains-annotations", "org.jetbrains:annotations:26.0.2")

            version("log4j", "2.24.3")
            library("log4j-core", "org.apache.logging.log4j", "log4j-core").versionRef("log4j")
            library("log4j-slf4j", "org.apache.logging.log4j", "log4j-slf4j2-impl").versionRef("log4j")

            version("airline", "2.8.5")
            library("airline", "com.github.rvesse", "airline").versionRef("airline")
            library("airline-help-markdown", "com.github.rvesse", "airline-help-markdown").versionRef("airline")
            library("beanshell", "com.github.pejobo:beanshell2:2.1.9")
            library("classgraph", "io.github.classgraph:classgraph:4.8.179")
            library("commons-lang3", "org.apache.commons:commons-lang3:3.17.0")
            library("commons-compress", "org.apache.commons:commons-compress:1.27.1")
            library("guava", "com.google.guava:guava:33.4.7-jre")
            library("jackson-databind", "tools.jackson.core:jackson-databind:3.2.0")
            library("slf4j-api", "org.slf4j:slf4j-api:2.0.17")
        }
        create("testlibs") {
            library("assertj", "org.assertj:assertj-core:3.27.3")
            library("mockito", "org.mockito:mockito-junit-jupiter:4.4.0")
            library("archunit", "com.tngtech.archunit:archunit-junit5:1.4.0")
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