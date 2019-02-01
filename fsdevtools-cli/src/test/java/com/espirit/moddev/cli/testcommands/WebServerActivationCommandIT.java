package com.espirit.moddev.cli.testcommands;

import com.espirit.moddev.IntegrationTest;
import com.espirit.moddev.cli.Cli;
import com.espirit.moddev.cli.commands.project.WebServerActivationCommand;
import com.espirit.moddev.cli.results.SimpleResult;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static com.espirit.moddev.IntegrationTest.PROJECT_NAME;

@Category(IntegrationTest.class)
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
    public void testExecution() throws Exception {
        Cli cli = new Cli();
        final String[] args = {"project", "activatewebserver", "-was", "WEBEDIT,LIVE", "-wsn", "some valid web server"};
        try {
            cli.execute(args);
        } catch (Exception e) {
            Assert.assertTrue("Should be illegal state", e instanceof IllegalStateException);
        }
    }
}
