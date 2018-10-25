package com.espirit.moddev.cli.commands.service;

import com.espirit.moddev.cli.results.ServiceProcessResult;

import de.espirit.firstspirit.agency.ModuleAdminAgent;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;


import static com.espirit.moddev.cli.commands.service.ProcessServiceInfo.ServiceStatus.STOPPED;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class ServiceProcessCommandTest extends ServiceProcessCommandBaseTest<ServiceProcessCommand> {

    @Before
    public void setUp() {
        ServiceProcessCommand testling = new ServiceProcessCommand() {
            @Override
            protected ProcessServiceInfo processService(@NotNull ModuleAdminAgent moduleAdminAgent, @NotNull String serviceName) {
                return new ProcessServiceInfo(serviceName, STOPPED, STOPPED);
            }
        };
        super.setUp(spy(testling));

    }

    @Test
    public void defaultConstructorDoesntFail() {
        assertThat("Expect not null", testling, is(notNullValue()));
    }

    @Test
    public void noParams_allServicesAreReturned() {

        ServiceProcessResult result = testling.call();

        assertThat(result.get().get(0).getServiceName(), is("RunningTestService"));
        assertThat(result.get().get(1).getServiceName(), is("StoppedTestService"));
        assertThat(result.get().get(2).getServiceName(), is("StoppedTestService2"));
    }

    @Test
    public void noParams_sameAmountIsReturned(){

        ServiceProcessResult result = testling.call();

        assertEquals("Exactly three service results should be found!", 3, result.get().size());
    }

    @Test
    public void oneParam_oneServiceIsReturned() {

        testling.setServiceNames("TestService");
        ServiceProcessResult result = testling.call();

        assertEquals("Exactly one service result should be found!", 1, result.get().size());
    }
    @Test
    public void oneParam_sameServiceIsReturned() {

        testling.setServiceNames("TestService");
        ServiceProcessResult result = testling.call();

        assertThat(result.get().get(0).getServiceName(), is("TestService"));
    }

    @Test
    public void multipleParams_sameAmountIsReturned() {

        testling.setServiceNames("TestService, TestService3");
        ServiceProcessResult result = testling.call();

        assertEquals("Exactly two services results should be found!", 2, result.get().size());
    }

    @Test
    public void multipleParams_sameServicesAreReturned() {

        testling.setServiceNames("TestService, TestService3");
        ServiceProcessResult result = testling.call();

        assertThat(result.get().get(0).getServiceName(), is("TestService"));
        assertThat(result.get().get(1).getServiceName(), is("TestService3"));
    }
}

