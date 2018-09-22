package com.espirit.moddev.cli.groups;

import com.espirit.moddev.cli.commands.groovyscript.RunGroovyScriptCommand;
import com.github.rvesse.airline.annotations.Group;

/**
 * {@link com.github.rvesse.airline.annotations.Group} that contains commands to handle FirstSpirit groovy scripts.
 */
@Group(name = "groovyscript", description = "interact with groovy scripts", defaultCommand = RunGroovyScriptCommand.class)
public class GroovyScriptGroup {
}
