-----------------------------------
@projectName@ version @projectVersion@
-----------------------------------

This tool is made to import and export FirstSpirit project content.

It is built for FirstSpirit version @firstSpiritVersion@ (fs-isolated-runtime.jar in lib directory).
To use with another FirstSpirit version simply exchange the fs-isolated-runtime.jar in the lib directory.
Make sure you don't supply both files at the same time.

The jar is not part of this distribution. Download the one that matches your FirstSpirit server with:

    fs-cli.sh  download [<url>] [<user>] [<password>] [--force]
    fs-cli.cmd download [<url>] [<user>] [<password>] [--force]

Example: fs-cli.sh download http://myserver:8000 Admin secret

The jar is saved as lib/fs-isolated-runtime-<server version>.jar. If the url, user or password is omitted, the environment
variables fsurl, fsuser and fspwd are used (preferred on shared machines, as arguments are visible in the process list).
If lib already contains the jar of the server's version, nothing is downloaded, unless --force is given, which always
downloads. If the version differs, the jar is downloaded and every other fs-isolated-runtime*.jar in lib is replaced.
The download needs curl and does not need JAVA_HOME. On Windows, put the password in double quotes if it contains
special characters such as & ^ % !, and do not start the script with 'call' (it would swallow ^).
Alternatively copy the fs-isolated-runtime.jar manually from your FirstSpirit server into the lib directory.
Keep in mind that '@projectName@' requires at least FirstSpirit Version @firstSpiritVersion@.


Prerequisites
-------------

- Java Runtime Environment (JRE) @javaVersion@ or better. Correct JAVA_HOME environment variable must be set.
- FirstSpirit @firstSpiritVersion@ or higher.


Help
----

For help about the usage please type 'fs-cli help' at command line.

There is a possibility to override default FirstSpirit connection parameters with environment variables:
- fsurl:     The FirstSpirit server base URL (only used by "download"; http:// is prepended if no scheme is given).
- fshost:    The FirstSpirit host address. Default is localhost.
- fsport:    The FirstSpirit port number. Default is 8000.
- fsmode:    The FirstSpirit connection mode, either HTTP, HTTPS or SOCKET. Default is HTTP.
- fsuser:    The FirstSpirit user account to authenticate the connection.
- fspwd:     The FirstSpirit user's password.
- fsproject: The FirstSpirit project name.

