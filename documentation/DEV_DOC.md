# FSDevTools - Developer Documentation

FSDevTools is a project to optimize the developer experience (DX) with FirstSpirit.

## Dependencies
As mentioned in the [README.md](../README.md) FSDevTools only compiles with **Java 8** and FirstSpirit **2018-12**. 
Since the required FirstSpirit artifacts are not publicly available the steps in the next section are absolutely necessary.

### Use FirstSpirit Access API as maven dependency

First, the **fs-isolated-runtime.jar** file of the FirstSpirit server used needs to be installed in the local Maven repository to enable a successful compilation of the amended source code.

The **fs-isolated-runtime.jar** file is located in the directory:

```
<FirstSpirit Server directory>/data/fslib
```

It is installed by entering the following command into the command line, in which the path to **fs-isolated-runtime.jar** file and the FirstSpirit version used have to be substituted accordingly:

```
mvn install:install-file -Dfile=<path-to-fs-isolated-runtime.jar> -DgroupId=de.espirit.firstspirit -DartifactId=fs-isolated-runtime -Dversion=<fs version e.g. '5.2.181108'> -Dpackaging=jar
```
**Note:** *Running the installation command within the directory in which the **pom.xml** file has been saved leads to an error. The installation must therefore be performed outside this directory.*

During installation the local Maven repository has been created automatically in the user directory under **<user home>.m2/repository**
After the **fs-isolated-runtime.jar** file has been installed successfully, it should be located in this directory (see figure below):

![Local Maven repository with installed fs-access.jar file](images/local_maven.gif)

Install the `fs-isolated-server.jar`, `fs-isolated-runtime.jar` and `wrapper-$VERSION.jar` files, too, by following the steps described above. 
They enable the execution of the integration tests for the module `fsdevtools-serverrunner`.
The files can be found in the following directories:

```
<FirstSpirit Server directory>/server/lib
<FirstSpirit Server directory>/server/lib-isolated
```

Note that the `fs-server.jar` and the `fs-isolated-server.jar` files need to be installed with the same version as the `fs-access.jar` or `fs-isolated-runtime.jar` files, while the version of the `wrapper-$VERSION.jar` file can be found in the file name.

## Compile with Maven

To compile the project with Maven simply use the following command in your command line, while you are at the project root:

```
mvn clean package
```

If you need to provide a specific version and FirstSpirit version please use the command:

```
mvn clean package -Dfirstspirit.version=FS_VERSION
```

To build a custom version of the FSDevTools use:

```
mvn clean package -Dci.version=VERSION
```


## Extending

This tool should be easily expandable with further commands, while the *execution framework* should rarely needed to be touched at all. 
You can either place your code right into this repository and compile it all together (hence have your own distribution), or you can build your own little software module and use FSDevTools as a dependency. 
Take a look at the chapters below for further details

You can add commands and groups. 
For command and group implementations, arbitrary packages can be used. 
There is no need to further register commands or use special package names.

Since our tool relies on the [airline library](https://github.com/airlift/airline) here on GitHub, you have to annotate your command class with a `@Command` annotation and implement our 
[`Command`](https://github.com/e-Spirit/FSDevTools/blob/master/fsdevtools-cli-api/src/main/java/com/espirit/moddev/cli/api/command/Command.java) interface 
(or extend other command classes, as the [`SimpleCommand`](https://github.com/e-Spirit/FSDevTools/blob/master/fsdevtools-cli/src/main/java/com/espirit/moddev/cli/commands/SimpleCommand.java)).
Take a look at the `fsdevtools-customcommand-example` submodule, to get a short impression of what you have to do. 
Keep in mind, that you have to provide a default command, if you add a custom group class implementation.

By default, our commands use a connection to a FirstSpirit server. 
A global configuration for commands, as well as a context, is made available through the [`Config`](https://github.com/e-Spirit/FSDevTools/blob/master/fsdevtools-cli-api/src/main/java/com/espirit/moddev/cli/api/configuration/Config.java) interface. 
A general implementation is provided by our [`GlobalConfig`](https://github.com/e-Spirit/FSDevTools/blob/master/fsdevtools-cli/src/main/java/com/espirit/moddev/cli/configuration/GlobalConfig.java) class. 
If you implement a configuration, our execution environment uses the command itself for the connection configuration and initializes the connection for you right before the command execution.

For your convenience, we provided the [`SimpleCommand`](https://github.com/e-Spirit/FSDevTools/blob/master/fsdevtools-cli/src/main/java/com/espirit/moddev/cli/commands/SimpleCommand.java) class that can be extended to prevent you from specifying standard connection logic for each command. 
The pure logic you want to program can then be placed in the generic `call` method you know from Java's `Callable` interface and you are all done.

For help configurations, take a look at existing commands and their annotations. 
If you really need it, you can have dynamic descriptions via a `public static String getDescription()` method in your command class 
(have a look at our [ExportCommand class](https://github.com/e-Spirit/FSDevTools/blob/master/fsdevtools-cli/src/main/java/com/espirit/moddev/cli/commands/export/ExportCommand.java)).

### Build your own distribution
You can clone this repository, extend the codebase and give us a pull request or maintain it as your own. 
The compilation and unit test stages can be executed with the dependencies installed, which are mentioned above.

### Use FSDevTools as a dependency in your own project
The command `mvn install` enables you to install the modules' artifacts of this repository to your maven repository and use them as dependencies.
After creating your own (e.g. maven) project, the dependencies can be defined to the api package (fsdevtools-api) as in the `fsdevtools-customcommand-example` module.
Alternatively it is possible to have a dependency on our implementations (fsdevtools-cli) - e.g. if you want to subclass our SimpleCommand class.
(Please keep in mind, that implementation classes are less stable than api interfaces.) 

Those libraries are part of the cli runtime, so they are defined with a *provided* scope.
After packaging your library as a jar file, it needs to be placed the *libs* folder of the cli distribution.
All libraries in this folder are added automatically to the classpath, and hence your command implementations can be found by our classpath scanner.
