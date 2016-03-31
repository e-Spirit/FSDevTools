# FSDevTools

Project to support developer experience (DX) with FirstSpirit template development by offering a connection between a VCS like Git and FirstSpirit.


## Versions

Here you can obtain information about versions from several sources.

### Version information from ticket system
See https://projects.e-spirit.de/browse/DEVEX/?selectedTab=com.atlassian.jira.jira-projects-plugin:versions-panel

### Binary artifacts
* *Release internal:* https://artifactory.e-spirit.de/artifactory/webapp/#/builds/PM%20-%20Developer%20Experience%20-%20FS%20CLI%20Release%20-%20Deploy%20to%20artifactory


## User Documentation
### Perquisites
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
See `fs-cli help` for more information on how to use the command.

In general, the tool offers a set of commands, that can be grouped. A command can be configured with options and
additional global options. The global options have to be stated first and before the command, while the
command's options have to be stated after the command itself. The order of options and global options within
their regions is arbitrary.
While groups behave mostly like commands - because a default command is executed
if specified for a group - the description of a group might give information about several commands.
In order to get information about all known commands or command groups, use `fs-cli help`. Information about a command
or a group can be retrieved with `fs-cli help commandorgroup`. Besides that, examples are provided per command.
Every command description also features information about the command's specific options and all global options.

### Logging

By default, every command gives you a more or less detailed, printed result. Use the logging properties
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

### Developer Documentation

## Dependencies
Only compiles with FirstSpirit 5.2.102 or newer.

## Compile with Maven
Simply type on command line at project root:

```
mvn clean package
```

If you need to provide a specific version and FirstSpirit version please type:

```
mvn clean package -Dci.version=VERSION -Dfirstspirit.version=FS_VERSION
```

## Project structure
This tool should be easily expandable with further commands, while the 'execution framework' should rarely
needed to be touched at all. For your convenience, you can add commands and groups. Our cli assumes, that
you place your commands in the existing command package and your new groups in the existing group package
in the cli module. Since those packages are configured to be scanned, there's no need to further register
commands or anything. Since our tool relies on the airline library (https://github.com/airlift/airline), you
have to annotate your class with a `@Command` annotation and implement our `Command` interface. By default,
our commands use a connection to a FirstSpirit server. A global configuration for commands, as well as a
context, is made available through the `Config` interface. A general implementation is provided by our
`GlobalConfig` class. If you implement a configuration, our execution environment uses the command itself
for the connection configuration and initializes the connection for you right before the command
execution. For your convenience, we provided the `SimpleCommand` class that can be extended
to prevent you from specifying standard connection logic for each command. The pure logic you want to program
can then be placed in the generic `call` method you know from java's `Callable` interface and you are all done.
For help configurations, take a look at existing commands and their annotations. If you really need it, you
can have dynamic descriptions via a `public static String getDescription()` method in your command class
(have a look at our ExportCommand class).
