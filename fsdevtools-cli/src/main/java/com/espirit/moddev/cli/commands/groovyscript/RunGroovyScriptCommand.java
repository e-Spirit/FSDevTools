package com.espirit.moddev.cli.commands.groovyscript;

import com.espirit.moddev.cli.ConnectionBuilder;
import com.espirit.moddev.cli.commands.SimpleCommand;
import com.espirit.moddev.cli.results.SimpleResult;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.help.Examples;
import de.espirit.firstspirit.access.Connection;
import groovy.lang.GroovyShell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;

@Command(name = "run", groupNames = {"groovyscript"}, description = "Runs a given groovyscript.")
@Examples(
        examples = {"fs-cli -h localhost -p 8000 groovyscript run --scriptURI myscript.groovy"},
        descriptions = {"Runs given \"myscript.groovy\" scriptfile.\ncontext variables:\n" +
                "\tfsConnection : class de.espirit.firstspirit.access.Connection"})
public class RunGroovyScriptCommand extends SimpleCommand<SimpleResult> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(RunGroovyScriptCommand.class);

    @Option(type = OptionType.COMMAND, name = {"--scriptURI"}, description = "relative or absolute URI")
    private String scriptURI;

    @Override
    public SimpleResult call() {
        try (final Connection connection = createConnection()) {
            connection.connect();

            URI uri = URI.create(scriptURI);
            File file = new File(uri.getPath());
            if (!file.exists()) {
                throw new IllegalArgumentException("Your given scriptURI '" + scriptURI + "' does not exist. Resolved URI was: " + file);
            }

            GroovyShell shell = new GroovyShell(this.getClass().getClassLoader());
            shell.setVariable("log", LOGGER);
            shell.setVariable("fsConnection", connection);
            Object result = null;

            try {
                result = shell.evaluate(uri);
            } catch (Exception e) {
                return new SimpleResult<>(e);
            }
            return new SimpleResult(result);
        } catch (final Exception e) {
            return new SimpleResult<>(e);
        }
    }

    /**
     * Creates a connection to a FirstSpirit Server with this instance as config.
     *
     * @return A connection from a ConnectionBuild.
     * @see ConnectionBuilder
     */
    protected Connection createConnection() {
        return ConnectionBuilder.with(this).build();
    }

    @Override
    public boolean needsContext() {
        return false;
    }
}
