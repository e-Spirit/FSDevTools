# FSDevTools - User Documentation

FSDevTools is a project to optimize the developer experience (DX) with FirstSpirit.

## Versioning scheme

Since version **3.0.0**, the `FSDevTools` are using [semantic versioning 2.0.0](https://semver.org/#semantic-versioning-200) (MAJOR.MINOR.PATCH) as the version pattern.
By using this scheme, users can now see if the `FSDevTools` has any breaking changes compared to the latest release, just by looking at the version number.

Changes in the different parts of the version will have different meanings

- MAJOR: we made some incompatible changes. The major version will also if the minimal required FirstSpirit version is changed. Please be careful if you update and take a close look at the [RELEASE NOTES](RELEASENOTES.md). 
- MINOR: we added functionality in a backwards compatible manner
- PATCH: we made backwards compatible bug fixes

For the list of changes in a version, please take a look at the [RELEASE NOTES](RELEASENOTES.md).

## Prerequisites

In order to use FSDevTools various prerequisites must be considered.
They are described in detail inside the zip or tar.gz file of the binary distribution.
The following list is just a short overview.

Since version **3.0.0** `FSDevTools` requires at least FirstSpirit **2021-07**.

Furthermore a correct `JAVA_HOME` environment variable, which points to a **Java 8+** installation, must be set.

In first instance the following configurations are loaded from your system environment with the corresponding key.

- **fshost** - The FirstSpirit host address
- **fsport** - The FirstSpirit port number.
- **fsmode** - The FirstSpirit connection mode, either `HTTP`, `HTTPS` or `SOCKET`.
- **fsuser** - The FirstSpirit user account to authenticate the connection.
- **fspwd** - The FirstSpirit user's password.
- **fsservletzone** - The FirstSpirit servlet zone.
- **fsproject** - The FirstSpirit project name.

In contrast to the other properties only the project property does not have a default value.
Due to this it has to be configured somehow to avoid exceptions with project context specific operations.
The default values of the other properties are used if neither an environment variable nor an option is given.

It is possible to override all default values from the environment by passing actual command line options for them.

## Additional Documentation

Further information about how to use the FSDevTools in combination with Git can be found in the [FirstSpirit online documentation](https://docs.e-spirit.com/odfs/edocs/sync/introduction/index.html).

## Usage
Most of the information you need in order to use the command line tool can be found via the integrated help command `fs-cli help`.

Additional information and examples are provided via the [command line interface usage page](documentation/CLI_USAGE.md).

## Logging

By default no log file will be written.
Instead every command provides a more or less detailed, printed result in the command line.
Both, a finer logging level and the generation of a log file can be configured in the Log4J logging properties file `/conf/log4j2.xml`.

If you need an even more detailed error logging, you can use the global option **-e**. 
With **-e**, you get an additional exception stacktrace in some cases.

For more information about the Log4J configuration please consult the [Log4J manual](https://logging.apache.org/log4j/2.x/manual/).

## Developer Documentation

Information about how to compile and extend this project is described in the [developer documentation](documentation/DEV_DOC.md).

## Legal Notices

FSDevTools is a product of [e-Spirit GmbH](https://www.e-spirit.com), Dortmund, Germany.

Only a license agreed upon with e-Spirit GmbH is valid with respect to the user for using FSDevTools.

## Disclaimer

This document is provided for information purposes only. 
e-Spirit may change the contents hereof without notice. 
This document is not warranted to be error-free, nor subject to any other warranties or conditions, whether expressed orally or implied in law, including implied warranties and conditions of merchantability or fitness for a particular purpose.
e-Spirit specifically disclaims any liability with respect to this document and no contractual obligations are formed either directly or indirectly by this document.
The technologies, functionality, services, and processes described herein are subject to change without notice.
