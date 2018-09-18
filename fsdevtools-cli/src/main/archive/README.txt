-----------------------------------
${project.name} version ${project.version}
-----------------------------------

This tool is made to import and export FirstSpirit project content.

It is built for for FirstSpirit version ${firstspirit.version} (fs-access.jar in lib directory).
To use with an other FirstSpirit version simply exchange the fs-access.jar in the lib directory.
Keep in mind that '${project.name}' requires at least FirstSpirit Version 5.2.1403 since major version 2.1.


Prerequisites
-------------

- Java ${java.version} or better. Correct JAVA_HOME environment variable must be set.
- FirstSpirit 5.2.1403 or higher.


Help
----

For help about the usage please type 'fs-cli help' at command line.

There is a possibility to override default FirstSpirit connection parameters with environment variables:
- fshost:    The FirstSpirit host address. Default is localhost.
- fsport:    The FirstSpirit port number. Default is 8000.
- fsmode:    The FirstSpirit connection mode, either HTTP, HTTPS or SOCKET. Default is HTTP.
- fsuser:    The FirstSpirit user account to authenticate the connection.
- fspwd:     The FirstSpirit user's password.
- fsproject: The FirstSpirit project name.

