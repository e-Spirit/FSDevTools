package com.espirit.moddev.cli.commands.service;

import com.espirit.moddev.cli.results.SimpleResult;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.manager.ServiceManager;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ServiceStartCommand.
 */
public class ServiceStartCommandTest {

    private ServiceStartCommand testling;

    @Before
    public void setUp() {
        testling = new ServiceStartCommand();
    }

    /**
     * Test that the default constructor has no dependencies or exceptions.
     */
    @Test
    public void testDefaultConstructor() {
        assertThat("Expect not null", testling, is(notNullValue()));
    }

    /**
     * Test if successful restart of services returns a SimpleResult containing true.
     */
    @Test
    public void testCallWithEmptyServiceNamesReturnsTrue() {
        // Arrange
        final Connection mockConnection = mock(ConnectionDummy.class);
        final ServiceManager mockServiceManager = mock(ServiceManager.class);
        final ServiceStartCommand spyTestling = spy(testling);
        final String serviceName = "TestService";

        when(spyTestling.createConnection()).thenReturn(mockConnection);
        doReturn(mockServiceManager).when(spyTestling).getServiceManager(mockConnection);
        when(mockServiceManager.getServices()).thenReturn(Collections.singleton(serviceName));
        when(mockServiceManager.isAutostartEnabled(serviceName)).thenReturn(true);
        when(mockServiceManager.isServiceRunning(serviceName)).thenReturn(true);

        // Act
        final SimpleResult<Boolean> simpleResult = spyTestling.call();

        // Assert
        assertThat("Expected equal.", simpleResult.get(), equalTo(Boolean.TRUE));

        verify(mockServiceManager, times(1)).getServices();
        verify(mockServiceManager, times(1)).isServiceRunning(serviceName);
        verify(mockServiceManager, times(1)).isAutostartEnabled(serviceName);
        verify(mockServiceManager, never()).stopService(serviceName);
        verify(mockServiceManager, never()).startService(serviceName);

    }

    /**
     * Test if successful restart of services with empty --serviceName parameter returns a SimpleResult containing true.
     */
    @Test
    public void testCallWithEmptyServiceNamesAndNoAutoStartServicesReturnsTrue() {
        // Arrange
        final Connection mockConnection = mock(ConnectionDummy.class);
        final ServiceManager mockServiceManager = mock(ServiceManager.class);
        final ServiceStartCommand spyTestling = spy(testling);
        final String serviceName = "TestService";

        when(spyTestling.createConnection()).thenReturn(mockConnection);
        doReturn(mockServiceManager).when(spyTestling).getServiceManager(mockConnection);
        when(mockServiceManager.getServices()).thenReturn(Collections.singleton(serviceName));
        when(mockServiceManager.isAutostartEnabled(serviceName)).thenReturn(false);
        when(mockServiceManager.isServiceRunning(serviceName)).thenReturn(true);

        // Act
        final SimpleResult<Boolean> simpleResult = spyTestling.call();

        // Assert
        assertThat("Expected equal.", simpleResult.get(), equalTo(Boolean.TRUE));

        verify(mockServiceManager, times(1)).getServices();
        verify(mockServiceManager, times(1)).isAutostartEnabled(serviceName);
        verify(mockServiceManager, never()).isServiceRunning(serviceName);
        verify(mockServiceManager, never()).stopService(serviceName);
        verify(mockServiceManager, never()).startService(serviceName);

    }

    /**
     * Test if successful restart of services with provided --serviceName parameter returns a SimpleResult containing true.
     */
    @Test
    public void testCallWithProvidedServiceNamesServicesReturnsTrue() {
        // Arrange
        final Connection mockConnection = mock(ConnectionDummy.class);
        final ServiceManager mockServiceManager = mock(ServiceManager.class);
        final ServiceStartCommand spyTestling = spy(testling);
        final List<String> serviceNames = Arrays.asList("TestService", "TestService2");

        when(spyTestling.createConnection()).thenReturn(mockConnection);

        doReturn(mockServiceManager).when(spyTestling).getServiceManager(mockConnection);
        doReturn(serviceNames).when(spyTestling).getOptionalServiceNames();

        when(mockServiceManager.isAutostartEnabled(anyString())).thenReturn(true);
        when(mockServiceManager.isServiceRunning(anyString())).thenReturn(false);

        // Act
        final SimpleResult<Boolean> simpleResult = spyTestling.call();

        // Assert
        assertThat("Expected equal.", simpleResult.get(), equalTo(Boolean.TRUE));

        verify(mockServiceManager, never()).getServices();
        verify(mockServiceManager, never()).isAutostartEnabled(anyString());
        verify(mockServiceManager, times(serviceNames.size())).isServiceRunning(anyString());
        verify(mockServiceManager, never()).stopService(anyString());
        verify(mockServiceManager, times(serviceNames.size())).startService(anyString());

    }
}

