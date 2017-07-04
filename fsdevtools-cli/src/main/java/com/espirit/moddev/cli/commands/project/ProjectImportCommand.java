package com.espirit.moddev.cli.commands.project;

import com.espirit.moddev.cli.ConnectionBuilder;
import com.espirit.moddev.cli.api.result.Result;
import com.espirit.moddev.cli.commands.SimpleCommand;
import com.espirit.moddev.cli.results.SimpleResult;
import com.espirit.moddev.cli.results.TestResult;
import com.espirit.moddev.core.StringPropertiesMap;
import com.espirit.moddev.projectservice.projectimport.ProjectImportParametersBuilder;
import com.espirit.moddev.projectservice.projectimport.ProjectImporter;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.help.Examples;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.io.ServerConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(name = "import", groupNames = {"project"}, description = "Imports a FirstSpirit project export into a FirstSpirit Server as a new project.")
@Examples(
        examples = {"fs-cli project import -h localhost -p 8000 project import --importProjectName \"newProjectName\" --projectFile \"D:\\my-project-export.tar.gz\""},
        descriptions = {"Imports the project export into a new project that is named newProjectName"})
public class ProjectImportCommand extends SimpleCommand<SimpleResult<Boolean>>{
    protected static final Logger LOGGER = LoggerFactory.getLogger(ProjectImportCommand.class);

    @Option(type = OptionType.COMMAND, name = {"-ipn", "--importProjectName"}, description = "Name of the FirstSpirit target project where the import should go")
    private String projectName;
    @Option(type = OptionType.COMMAND, name = {"-ipd", "--importProjectDescription"}, description = "Description of the FirstSpirit target project")
    private String projectDescription;
    @Option(type = OptionType.COMMAND, name = {"-pf", "--projectFile"}, description = "Path to the project export file that should be imported")
    private String projectFile;
    @Option(type = OptionType.COMMAND, name = {"-fpa", "--forceProjectActivation"}, description = "Whether to force the project activation if the project is deactivated after import somehow. Default is false.")
    private boolean forceProjectActivation;
    @Option(type = OptionType.COMMAND, name = {"-dlm", "--databaseLayerMapping"}, description = "Define a map-like layerMapping with comma-separated key-value pairs by : or =; . Use layer names.")
    private String databaseLayerMapping;

    @Override
    public SimpleResult<Boolean> call() {
        try(final Connection connection = create()) {
            connection.connect();

            if(connection instanceof ServerConnection) {
                ProjectImportParametersBuilder importParametersBuilder = new ProjectImportParametersBuilder()
                        .setProjectName(projectName)
                        .setProjectFile(projectFile)
                        .setProjectDescription(projectDescription)
                        .setForceProjectActivation(forceProjectActivation)
                        .setDatabaseLayerMapping(new StringPropertiesMap(databaseLayerMapping));

                boolean imported = new ProjectImporter().importProject((ServerConnection) connection, importParametersBuilder.create());

                return new SimpleResult(imported ? imported : new IllegalStateException("Import was not successful"));
            } else {
                return new SimpleResult<>(new IllegalStateException("Connection is not a ServerConnection implementation."));
            }

        } catch (final Exception e) {
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
