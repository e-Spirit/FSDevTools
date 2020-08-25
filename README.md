# FSDevTools - User Documentation

FSDevTools is a project to optimize the developer experience (DX) with FirstSpirit.

### Prerequisites

In order to use FSDevTools various prerequisites must be considered.
They are described in detail inside the zip or tar.gz file of the binary distribution.
The following list is just a short overview.

Since version **2.6.1** `FSDevTools` requires at least FirstSpirit **2020-08**.

Furthermore a correct `JAVA_HOME` environment variable, which points to a **Java 8** installation, must be set.

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

### Usage
Most of the information you need in order to use the command line tool can be found via the integrated help command `fs-cli help`.

Additional information and examples are provided via the [command line interface usage page](documentation/CLI_USAGE.md).

### Logging

By default no log file will be written.
Instead every command provides a more or less detailed, printed result in the command line.
Both, a finer logging level and the generation of a log file can be configured in the Log4J logging properties file `/conf/log4j.properties`.

If you need an even more detailed error logging, you can use the global option **-e**. 
With **-e**, you get an additional exception stacktrace in some cases.

To generate a log file you need to set `logfile` as an additional parameter for `log4j.rootLogger` and add the following lines to the properties file:

```
### Log to file
log4j.appender.logfile=org.apache.log4j.DailyRollingFileAppender
log4j.appender.logfile.File=fs-filesync.log
log4j.appender.logfile.DatePattern='_'yyyy-MM-dd'.log'
log4j.appender.logfile.layout=org.apache.log4j.PatternLayout
log4j.appender.logfile.layout.ConversionPattern=%d %5p %C{1} - %m%n
log4j.appender.logfile.Threshold=DEBUG
```

For more information about the Log4J configuration please consult the [Log4J manual](https://logging.apache.org/log4j/1.2/manual.html).

## Developer Documentation

Information about how to compile and extend this project is described in the [developer documentation](documentation/DEV_DOC.md).

## Legal Notices

FSDevTools is a product of [e-Spirit AG](http://www.e-spirit.com), Dortmund, Germany.

Only a license agreed upon with e-Spirit AG is valid with respect to the user for using FSDevTools.

## Disclaimer

This document is provided for information purposes only. 
e-Spirit may change the contents hereof without notice. 
This document is not warranted to be error-free, nor subject to any other warranties or conditions, whether expressed orally or implied in law, including implied warranties and conditions of merchantability or fitness for a particular purpose.
e-Spirit specifically disclaims any liability with respect to this document and no contractual obligations are formed either directly or indirectly by this document.
The technologies, functionality, services, and processes described herein are subject to change without notice.
