# Release notes

## Version 4.5.0

* added new parameter "*--include-feature-model*" to "*feature install*" command

For more information about the new parameter, please use "*fs-cli help feature install*" or open the included documentation under *docs/index.html*.

## Version 4.4.2

* fixed java.lang.Error not getting handled in general
* commands "*module install*" & "*module installBulk*":
  * fixed java.lang.Error aborting the complete process

## Version 4.4.1

* updated FirstSpirit compile dependency to 5.2.230409 (was 5.2.220907)
* implemented missing methods in FsLoggingBridge

## Version 4.4.0

* added new parameter "*--run-level/-rl*" to "*server start*" command

## Version 4.3.0

* new command for Content Transport:
  * "*feature revision*": updates the revision of the specified feature 
  
For more information about the new command, please use "*fs-cli help feature revision*" or open the included documentation under *docs/index.html*.

## Version 4.2.0

* updated default *log4J2.xml* to reduce the default logging for some packages, this was a regression of v4.0.0
* fixed exceptions being logged twice under some circumstances
* commands "*module install*" & "*module installBulk*":
  * project app components will now be updated by the CLI instead of the FirstSpirit server
  * web app components will now be updated by the CLI instead of the FirstSpirit server
  * fixed parameter "*--deployWebApps/-dwa*" not working properly

## Version 4.1.0

* new commands for Content Transport:
    * "*feature list*": lists all features for the specified project
    * "*feature download*": downloads the specified feature
    * "*feature analyze*": analyzes the specified feature in the project and logs the result
    * "*feature install*": analyzes & installs the specified feature in the project

For more information about the new commands, please use "*fs-cli help feature*" or open the included documentation under *docs/index.html*.

## Version 4.0.2

* fixed logging for MultiExceptions in "*module installBulk*" command

## Version 4.0.1

* added missing add-opens for JDK 17
* updated Gradle to 7.5.1 (was 7.1.1)
* updated minimal required FirstSpirit version to 5.2.220907 (was 5.2.210710)

## Version 4.0.0

* fixed error in start script (WIN only)
* moved to Log4J 2.17.2
* moved to JUnit 5
* updated used libraries to latest versions

## Version 3.0.2

* updated Gradle to 7.1.1 (was 5.5.1)
* updated dependent gradle plugins to latest versions
* updated used libraries to latest versions

## Version 3.0.1

* command "*import*": clarified error messages if project exists and is deactivated or maximum projects on server are exceeded

## Version 3.0.0

* switched to [semantic versioning 2.0.0](https://semver.org/#semantic-versioning-200)
* updated minimal required FirstSpirit version to 5.2.210710 (was 5.2.200807)
* command "*export*": added support for permission transport (new parameter: *--permissionMode*)
* command "*import*": added support for permission transport (new parameters: *--permissionMode*, *--updateExistingPermissions*)
* added RELEASENOTES.md

## Version 2.6.10

* fixed compatibility for JDK16
* WIN only: fixed whitespaces in execution path not working
* added link to ExternalSync documentation in [README](README.md)

## Version 2.6.9

* enhanced logging for module installation

## Version 2.6.8

* UNIX only: fixed file permissions (regression in 2.6.7)

## Version 2.6.7

* added support for new FirstSpirit wrapper scripts

## Version 2.6.6

* inactive web apps or web apps without an active webserver will no longer get re-deployed
* fixed and enhanced logging for web app deployment process

## Version 2.6.5

* added option to disable wrapper restarts

## Version 2.6.4

* NEW command: "*module configure*"
* refactoring of internal project structure

## 2.6.3 and older

* please see the [commit history](https://github.com/e-Spirit/FSDevTools/commits/master) for details
