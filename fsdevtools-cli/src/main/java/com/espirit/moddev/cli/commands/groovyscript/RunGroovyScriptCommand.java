package com.espirit.moddev.cli.commands.groovyscript;

import com.espirit.moddev.cli.ConnectionBuilder;
import com.espirit.moddev.cli.commands.SimpleCommand;
import com.espirit.moddev.cli.results.SimpleResult;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.help.Examples;
import com.github.rvesse.airline.annotations.restrictions.Required;
import de.espirit.firstspirit.access.Connection;
import groovy.json.JsonSlurper;
import groovy.lang.GroovyShell;
import org.apache.groovy.json.internal.LazyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.util.Map;

@Command(name = "run", groupNames = {"groovyscript"}, description = "Runs a given groovyscript.")
@Examples(
        examples = {"fs-cli -h localhost -p 8000 groovyscript run --scriptURI myscript.groovy --scriptParameters '{}"},
        descriptions = {
                "Runs given \"myscript.groovy\" scriptfile.\n" +
                        "\tcontext variables:\n" +
                        "\t\tfsConnection : class de.espirit.firstspirit.access.Connection\n" +
                        "\t\tfsContext : class ScriptContext\n" +
                        "\t\tscriptParameters : class Map<String, Object>"})
public class RunGroovyScriptCommand extends SimpleCommand<SimpleResult> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(RunGroovyScriptCommand.class);

    @Option(type = OptionType.COMMAND, name = {"--scriptURI"}, description = "relative or absolute URI")
    @Required
    private String scriptURI;

    @Option(type = OptionType.COMMAND, name = {"--scriptParameters"}, description = "parameters as json string, e.g.: \n" +
            "{\"hostname\":\"test\",\"domainname\":\"example.com\"}")
    private String scriptParameters;

    @Override
    public SimpleResult call() {
        try (final Connection connection = createConnection()) {
            connection.connect();

            URI uri = URI.create(scriptURI);
            File file = new File(uri.getPath());
            if (!file.exists()) {
                throw new IllegalArgumentException("Your given scriptURI '" + scriptURI + "' does not exist. Resolved URI was: " + file);
            }

            // checking for valid json input for scriptParameters
            if(scriptParameters == null || scriptParameters.length() <= 2) {
                scriptParameters = "{\"testParameter\": \"testValue\"}";
            }
            JsonSlurper jsonSlurper = new JsonSlurper();
            Object parsedParametersAsObject = jsonSlurper.parseText(scriptParameters);
            LazyMap parsedParameters;

            if(!(parsedParametersAsObject instanceof Map)) {
                return new SimpleResult<>(new IllegalArgumentException("--scriptParameters required to be of type HashMap<String, Object>"));
            }

            GroovyShell shell = new GroovyShell(this.getClass().getClassLoader());
            shell.setVariable("fsConnection", getContext().getConnection());
            shell.setVariable("fsContext", getContext());

            parsedParameters = (LazyMap)parsedParametersAsObject;
            shell.setVariable("scriptParameters", parsedParameters);

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
        return true;
    }
}
