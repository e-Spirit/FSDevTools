# FSDevTools - User Guide

Welcome to the FSDevTools User Documentation, a project dedicated to enhancing the developer experience (DX) with
FirstSpirit.

## Minimal FirstSpirit version & Prerequisites

Since version **4.5.0**, `FSDevTools` requires at least the FirstSpirit version **2023-11-05**.

Before using FSDevTools, several prerequisites must be met. Detailed instructions can be found in the zip or tar.gz file
of the binary distribution. Below is a summary of the key requirements.

Additionally, ensure that the `JAVA_HOME` environment variable is correctly set to a **Java 11+** installation.

Upon initialization, the following configurations are sourced from your system environment using specific keys:

- **fshost**: FirstSpirit host address.
- **fsport**: FirstSpirit port number.
- **fsmode**: FirstSpirit connection mode (HTTP, HTTPS, or SOCKET).
- **fsuser**: FirstSpirit user account for authentication.
- **fspwd**: Password for the FirstSpirit user.
- **fsservletzone**: FirstSpirit servlet zone.
- **fsproject**: Name of the FirstSpirit project.

Among these properties, only the project property lacks a default value. It must be configured to avoid exceptions in
project-specific operations. Default values are utilized for other properties when neither an environment variable nor
an option is provided.

All default values can be overridden by supplying command line options during execution.

## Versioning Scheme

Starting from version **3.0.0**, the `FSDevTools` follows
the [semantic versioning 2.0.0](https://semver.org/#semantic-versioning-200) (MAJOR.MINOR.PATCH) format for version
numbering. This enables users to quickly determine whether there are any breaking changes compared to the previous
release, simply by examining the version number.

Here's what each part of the version number signifies:

- MAJOR: Includes incompatible changes. This version also indicates if the minimum required FirstSpirit version has been
  modified. Please exercise caution while updating and refer to the [RELEASE NOTES](RELEASENOTES.md) for details.
- MINOR: Adds new features in a backward-compatible manner.
- PATCH: Contains backward-compatible bug fixes.

For a comprehensive list of changes within each version, please consult the [RELEASE NOTES](RELEASENOTES.md).

## Additional Documentation

For information on using FSDevTools in conjunction with Git, refer to
the [FirstSpirit online documentation](https://docs.e-spirit.com/odfs/edocs/sync/introduction/index.html).

## Usage

Most of the information required to utilize the command line tool can be accessed through the integrated `fs-cli help`
command.

For detailed insights and examples, visit the [command line interface usage page](documentation/CLI_USAGE.md).

## Logging

By default, no log files are generated. Instead, each command displays varying levels of detail in the command line
output. To configure finer logging levels and enable log file creation, refer to the Log4J logging properties file
located at `/conf/log4j2.xml`.

For more comprehensive error logging, the global option **-e** can be used. This provides additional exception stack
traces in specific cases.

For detailed guidance on Log4J configuration, consult the [Log4J manual](https://logging.apache.org/log4j/2.x/manual/).

## External script engines

FSDevTools, by default, supports executing beanshell scripts in *"script"* commands. You can extend support for other
script engines by copying external script engine JAR files into the *"plugins"* directory.

For now, the **FSDevTools** bundle comes with additional support for Groovy and Javascript scripts. Simply download the
jar files and copy them into the *"plugins" directory.*

To use the script commands, use the following syntax.

- `fs-cli [-project ...] script parse [--scriptEngine|-se <NAME>] --scriptFile|-sf <PATH>`
- `fs-cli [-project ...] script run [--scriptEngine|-se <NAME>] --scriptFile|-sf <PATH>`

The currently supported script engines are:

- *beanshell* - default
    - uses [Beanshell fork by pejobo](https://github.com/pejobo/beanshell2)
- *groovy* - only available if installed (available with this release)
    - uses [Apache Groovy](https://github.com/apache/groovy)
- *javascript* - only available if installed (available with this release)
    - uses [OpenJDK Nashorn](https://github.com/openjdk/nashorn)

### Developing your own script engines

You can build your own script engine in just a few steps:

- create a clean and new project
- add the *fs-cli.jar* as a **compile only** dependency
- add the dependencies as **implementation** dependencies that are needed for your script engine
- implement the following interfaces:
    - [ScriptEngine](fsdevtools-cli-api/src/main/java/com/espirit/moddev/cli/api/script/ScriptEngine.java)
        - use a unique name for your script engine
    - [ScriptExecutable](fsdevtools-cli-api/src/main/java/com/espirit/moddev/cli/api/script/ScriptExecutable.java)
- create a fat jar that contains your implementations and the dependencies that are needed at runtime
    - you can use the [ShadowJAR](https://github.com/johnrengelman/shadow) plugin for gradle to do this easily
- copy the created fat jar to the `$fs-cli$/plugins` directory

Take a closer look at our external [script engines](fsdevtools-scriptengines), they were implemented in the described
way.

#### Classloading for external script engines

The CLI uses a parent-first classloader to load each external script engine jar from the `plugins` directory. This means
that each jar has its own classpath but all classes that are bundled with the CLI will be loaded from the classpath of
the CLI - not the jar file itself. This allows developers to bundle external jars with their own libraries without
polluting the classpath of the CLI. However, one big disadvantage is that you cannot override any classes that come with
the CLI or the `fs-isolated-runtime.jar`.

This is especially the case for the following classes/libraries:

- all classes from the CLI itself
- all classes from the `fs-isolated-runtime.jar`.
- [com.fasterxml.jackson.core:jackson-databind](https://github.com/FasterXML/jackson-databind)
- [org.slf4j:slf4j-api](https://github.com/qos-ch/slf4j)
- [com.google.guava:guava](https://github.com/google/guava)
- [org.apache.commons:commons-compress](https://github.com/apache/commons-compress)
- [org.apache.commons:commons-lang3](https://github.com/apache/commons-lang)
- [org.apache.logging.log4j:log4j-core](https://github.com/apache/logging-log4j2)
- [org.apache.logging.log4j:log4j-slf4j-impl](https://github.com/apache/logging-log4j2)
- [org.hamcrest:java-hamcrest](https://github.com/hamcrest/JavaHamcrest)
- [com.github.rvesse:airline](https://github.com/rvesse/airline)

## Legal Notices

FSDevTools is a product of [Crownpeak Technology GmbH](https://www.e-spirit.com), based in Dortmund, Germany.

Usage of FSDevTools requires a valid license agreement with Crownpeak Technology GmbH.

## Disclaimer

This document serves informational purposes only. Crownpeak reserves the right to modify its contents without prior
notice. This document is not guaranteed to be free from errors and is not subject to any other warranties or conditions,
whether implied in law or expressed orally. Crownpeak disclaims any liability associated with this document, and no
contractual obligations are established by it, either directly or indirectly. The technologies, functionality, services,
and processes described here are subject to change without notice.
