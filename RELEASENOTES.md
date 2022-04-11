# Release notes

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

* command "import": clarified error messages if project exists and is deactivated or maximum projects on server are exceeded

## Version 3.0.0

* switched to [semantic versioning 2.0.0](https://semver.org/#semantic-versioning-200)
* updated minimal required FirstSpirit version to 5.2.2107 (was 5.2.2008)
* command "export": added support for permission transport (new parameter: --permissionMode)
* command "import": added support for permission transport (new parameters: --permissionMode, --updateExistingPermissions)
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

* NEW command: "module configure"
* refactoring of internal project structure

## 2.6.3 and older

* please see the [commit history](https://github.com/e-Spirit/FSDevTools/commits/master) for details