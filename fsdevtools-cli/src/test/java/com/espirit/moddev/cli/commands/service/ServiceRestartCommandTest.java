package com.espirit.moddev.cli.commands.service;

import com.espirit.moddev.cli.results.ServiceProcessResult;

import de.espirit.firstspirit.access.ServiceNotFoundException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.espirit.moddev.cli.commands.service.ProcessServiceInfo.ServiceStatus.RUNNING;
import static com.espirit.moddev.cli.commands.service.ProcessServiceInfo.ServiceStatus.STOPPED;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ServiceRestartCommandTest extends ServiceProcessCommandBaseTest<ServiceRestartCommand> {
    private ServiceProcessResult result;

    @Before
    public void setUp() {
        super.setUp(ServiceRestartCommand.class);

        result = testling.call();
    }

    @Test
    public void noParams_allStoppedServices_neverCallStop() {
        verify(mockModuleAdminAgent, never()).stopService("StoppedTestService");
        verify(mockModuleAdminAgent, never()).stopService("StoppedTestService2");
    }
    @Test
    public void noParams_allStoppedServices_callStartOnce() {
        verify(mockModuleAdminAgent, times(1)).startService("StoppedTestService");
        verify(mockModuleAdminAgent, times(1)).startService("StoppedTestService2");
    }
    @Test
    public void allStoppedServicesAreReturned() {
        assertThat(result.get(), hasItem(new ProcessServiceInfo("StoppedTestService", STOPPED, RUNNING)));
        assertThat(result.get(), hasItem(new ProcessServiceInfo("StoppedTestService2", STOPPED, RUNNING)));
    }

    @Test
    public void noParams_allRunningServices_callStopStartOnce() {
        verify(mockModuleAdminAgent, times(1)).stopService("RunningTestService");
        verify(mockModuleAdminAgent, times(1)).startService("RunningTestService");
    }
    @Test
    public void allRunningServicesAreReturned() {
        assertThat(result.get(), hasItem(new ProcessServiceInfo("RunningTestService", RUNNING, RUNNING)));
    }

    @Test
    public void noParams_totalNumberOfResults_equalsNumberOfServices() {
        assertEquals("Exactly three service results should be found!", 3, result.get().size());
    }
    @Test
    public void multipleParams_nonExistingServices_resultIsException() {
        when(mockModuleAdminAgent.isRunning("NonExistentService")).thenReturn(true);

        doThrow(new ServiceNotFoundException("unidentified service")).when(mockModuleAdminAgent).stopService("NonExistentService");
        //no need to mock start service since the exception is thrown before we reach that line

        testling.setServiceNames("NonExistentService");
        result = testling.call();

        //if there is a better way to test this, be sure to tell me izgoel@e-spirit.com
        assert(result.isError());
        assertEquals(result.getError().getClass(), ServiceNotFoundException.class);
    }
}

