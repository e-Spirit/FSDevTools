# FSDevTools - User Guide

Welcome to the FSDevTools User Documentation, a project dedicated to enhancing the developer experience (DX) with
FirstSpirit.

## Minimal FirstSpirit version & Prerequisites

Since version **4.4.1**, `FSDevTools` requires at least the FirstSpirit version **2023-04-09**.

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

## Developer Documentation

Extensive information on compiling and extending this project is available in
the [developer documentation](documentation/DEV_DOC.md).

## Legal Notices

FSDevTools is a product of [Crownpeak Technology GmbH](https://www.e-spirit.com), based in Dortmund, Germany.

Usage of FSDevTools requires a valid license agreement with Crownpeak Technology GmbH.

## Disclaimer

This document serves informational purposes only. Crownpeak reserves the right to modify its contents without prior
notice. This document is not guaranteed to be free from errors and is not subject to any other warranties or conditions,
whether implied in law or expressed orally. Crownpeak disclaims any liability associated with this document, and no
contractual obligations are established by it, either directly or indirectly. The technologies, functionality, services,
and processes described here are subject to change without notice.
