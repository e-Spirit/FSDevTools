package com.espirit.moddev.cli.testcommands;

import com.espirit.moddev.cli.CliContextImpl;
import com.espirit.moddev.cli.commands.project.WebServerActivationCommand;
import com.espirit.moddev.cli.results.SimpleResult;
import org.junit.Assert;
import org.junit.Test;

public class WebServerActivationCommandIT extends AbstractIntegrationTest {

    @Test
    public void parameterLessCommandDoesntExport() throws Exception {
        WebServerActivationCommand command = new WebServerActivationCommand();
        command.setProject(PROJECT_NAME);
        initContextWithDefaultConfiguration(command);
        SimpleResult<?> result = command.call();

        Assert.assertNotNull(result);
        Assert.assertTrue("Result should be an error!", result.isError());
        Assert.assertTrue("Expect resulting error to be illegal state. Actual: " + result.getError(), result.getError() instanceof IllegalArgumentException);
        Assert.assertFalse(command.needsContext());
    }

    @Test
    public void non_existing_web_server_should_fail_with_exception() throws Exception {
        try {
            // setup the command
            final WebServerActivationCommand command = new WebServerActivationCommand();
            initContextWithDefaultConfiguration(command);
            command.setContext(new CliContextImpl(command));
            command.setProjectName(PROJECT_NAME);
            command.setWebAppScopes("WEBEDIT,LIVE");
            command.setWebServerName("non existent web server");

            // finally call the command
            command.call();
        } catch (Exception e) {
            Assert.assertTrue("Should be illegal state", e instanceof IllegalStateException);
        }
    }
}
