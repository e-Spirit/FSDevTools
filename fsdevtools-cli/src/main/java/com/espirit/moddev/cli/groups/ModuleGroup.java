package com.espirit.moddev.cli.groups;

import com.espirit.moddev.cli.commands.module.InstallModuleCommand;
import com.github.rvesse.airline.annotations.Group;

/**
 * {@link com.github.rvesse.airline.annotations.Group} that contains commands to handle FirstSpirit modules.
 * For example for installing, uninstalling them;
 */
@Group(name = "module", description = "Install and uninstall modules.", defaultCommand = InstallModuleCommand.class)
public class ModuleGroup {
}
