package com.espirit.moddev.cli.commands.module;

import com.espirit.moddev.cli.ConnectionBuilder;
import com.espirit.moddev.cli.commands.SimpleCommand;
import com.espirit.moddev.cli.results.SimpleResult;
import com.espirit.moddev.moduleuninstaller.ModuleUninstaller;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.help.Examples;
import com.github.rvesse.airline.annotations.restrictions.Required;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.common.MaximumNumberOfSessionsExceededException;
import de.espirit.firstspirit.server.authentication.AuthenticationException;

import java.io.IOException;

/**
 * Uninstalls a module from a FirstSpirit server. Removes corresponding components and project-specific
 * components for the given project.
 */
@Command(name = "uninstall", groupNames = {"module"}, description = "Uninstalls a FirstSpirit module from a FirstSpirit Server.")
@Examples(
        examples = {"fs-cli module uninstall -h localhost -p 8000 --moduleName \"abtesting\" --projectName \"Mithras Energy\""},
        descriptions = {"Uninstalls the abtesting module and removes all components from the Mithras Energy project"})
public class UninstallModuleCommand extends SimpleCommand<SimpleResult<Boolean>> {

    @Option(type = OptionType.COMMAND, name = {"-m", "--moduleName"}, description = "Name of the module that should be deleted", title = "moduleName")
    @Required
    private String _moduleName;

    @Override
    public SimpleResult<Boolean> call() {
        try(Connection connection = create()) {
            connection.connect();
            new ModuleUninstaller().uninstall(connection, _moduleName);
            return new SimpleResult<>(true);
        } catch (IOException | AuthenticationException | MaximumNumberOfSessionsExceededException e) {
            return new SimpleResult<>(e);
        }
    }

    protected Connection create() {
        return ConnectionBuilder.with(this).build();
    }

    @Override
    public boolean needsContext() {
        return false;
    }

}