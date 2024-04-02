-----------------------------------
@projectName@ version @projectVersion@
-----------------------------------

This tool is made to import and export FirstSpirit project content.

It is built for FirstSpirit version @firstSpiritVersion@ (fs-isolated-runtime.jar in lib directory).
To use with another FirstSpirit version simply exchange the fs-isolated-runtime.jar in the lib directory.
Make sure you don't supply both files at the same time.
Keep in mind that '@projectName@' requires at least FirstSpirit Version @firstSpiritVersion@.


Prerequisites
-------------

- Java Runtime Environment (JRE) @javaVersion@ or better. Correct JAVA_HOME environment variable must be set.
- FirstSpirit @firstSpiritVersion@ or higher.


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

