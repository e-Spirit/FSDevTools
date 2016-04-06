# FSDevTools

Project to support developer experience (DX) with FirstSpirit template development by offering a connection between a VCS like Git and FirstSpirit.

## User Documentation

### Prequisites
Installation prerequisites are documented inside the zip or tar-gz file of the binary distribution.

In short, a correct `JAVA_HOME` environment variable must be set which points to a **Java 8** installation.

Some configurations are loaded from your system environment in first instance.
It is possible to override all default values from the environment by passing actual command line options for them.
The following values are read from your environment with the corresponding key.

- **fshost** - The FirstSpirit host address
- **fsport** - The FirstSpirit port number.
- **fsmode** - The FirstSpirit connection mode, either `HTTP` or `SOCKET`.
- **fsuser** - The FirstSpirit user account to authenticate the connection.
- **fspwd** - The FirstSpirit user's password.
- **fsproject** - The FirstSpirit project name.

### Usage
Most of the information you need in order to use the command line tool can be found via the integrated help command.
See `fs-cli help` for more information on how to use the help command.

For more information and examples how to use the command line interface see the [command line interface usage page](documentation/CLI_USAGE.md).

### Logging

By default, every command gives you a more or less detailed, printed result. Use the Log4J logging properties
file `/conf/log4j.properties`, to set a finer logging level as needed.

If you need an even more detailed error logging, you can use the global option **-e**. With **-e**, you get
an additional exception stacktrace in some cases.

By default no log file will be written  - To change this, configure the `log4j.properties` file in `/conf` directory.
You need to set `logfile` as additional parameter for `log4j.rootLogger` and add following lines to this file:

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

Information about how to compile and extend this project is described in the  [developer documentation](documentation/DEV_DOC.md).

## Disclaimer

FirstSpirit and this project are developed by the [e-Spirit AG](http://www.e-spirit.com).
The head office of the e-Spirit AG is in Dortmund, Germany.

Use this project and provided binaries at your own risk.
