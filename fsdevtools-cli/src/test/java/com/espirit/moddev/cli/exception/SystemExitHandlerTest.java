package com.espirit.moddev.cli.exception;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

public class SystemExitHandlerTest {
    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    @Test
    public void handle() throws Exception {
        exit.expectSystemExitWithStatus(1);
        new SystemExitHandler().handle(new Exception());
    }

    @Test
    public void afterExecution() throws Exception {
        exit.expectSystemExitWithStatus(0);
        new SystemExitHandler().afterExecution();
    }

}
