package com.espirit.moddev.cli.commands.service;

import com.espirit.moddev.cli.results.SimpleResult;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.manager.ServiceManager;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ServiceProcessCommand.
 */
public class ServiceProcessCommandTest {

    private ServiceProcessCommand testling;

    @Before
    public void setUp() {
        testling = new ServiceProcessCommand() {
            @Override
            protected void processService(ServiceManager serviceManager, String serviceName) {
            }
        };
    }

    /**
     * Test that the default constructor has no dependencies or exceptions.
     */
    @Test
    public void testDefaultConstructor() {
        assertThat("Expect not null", testling, is(notNullValue()));
    }

    /**
     * Test if call() throws exception when createConnection() fails.
     */
    @Test
    public void testCallHandlesExceptionAndReturnsSimpleResultWithError() {
        // Arrange
        final ServiceProcessCommand spyTestling = spy(testling);

        doThrow(Exception.class).when(spyTestling).createConnection();

        // Act
        final SimpleResult<Boolean> simpleResult = spyTestling.call();

        // Assert
        assertThat("Expected instance of Exception", simpleResult.getError(), instanceOf(Exception.class));
    }


    /**
     * Test if call() requires an instance of ServerConnection.
     */
    @Test
    public void testCallNoServerConnectionReturnsSimpleResultWithError() {
        // Arrange
        final Connection mockConnection = mock(Connection.class);

        final ServiceProcessCommand spyTestling = spy(testling);

        doReturn(mockConnection).when(spyTestling).createConnection();

        // Act
        final SimpleResult<Boolean> simpleResult = spyTestling.call();

        // Assert
        assertThat("Expected instance of IllegalStateException.", simpleResult.getError(), instanceOf(IllegalStateException.class));
        assertThat("Expected equal.", simpleResult.getError().getMessage(), equalTo("Connection is not a ManagerProvider implementation."));
    }

    /**
     * Test if successful restart of services with empty --serviceName parameter returns a SimpleResult containing true.
     */
    @Test
    public void testCallWithEmptyServiceNamesAndNoAutoStartServicesReturnsTrue() {
        // Arrange
        final Connection mockConnection = mock(ConnectionDummy.class);
        final ServiceManager mockServiceManager = mock(ServiceManager.class);
        final ServiceProcessCommand spyTestling = spy(testling);
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
     * Test to retrieve the ServiceManager with wrong connection instance. Should fail with Exception.
     */
    @Test(expected = IllegalStateException.class)
    public void testGetServiceManagerThrowErrorWithWrongManagerProvider() {
        // Arrange
        final ConnectionDummy mockConnection = mock(ConnectionDummy.class);
        final ServiceProcessCommand spyTestling = spy(testling);
        when(spyTestling.createConnection()).thenReturn(mockConnection);

        // Act
        spyTestling.getServiceManager(mockConnection);

        // Assert
        fail("Should trow exception");
    }

    /**
     * Test of getting optional service names from null
     */
    @Test
    public void testGetOptionalServiceNamesWithEmptyValue() {
        // Arrange

        final ServiceProcessCommand spyTestling = spy(testling);

        // Act
        List<String> serviceNames = spyTestling.getOptionalServiceNames();

        // Assert
        assertThat(serviceNames, is(emptyIterable()));
    }

    /**
     * Test of getting optional names from "ServiceA,ServiceB"
     */
    @Test
    public void testGetOptionalServiceNamesWithProvidedValue() {
        // Arrange

        final ServiceProcessCommand spyTestling = spy(testling);
        //noinspection ResultOfMethodCallIgnored
        doReturn("ServiceA,ServiceB").when(spyTestling).getServiceNames();

        // Act
        List<String> serviceNames = spyTestling.getOptionalServiceNames();

        // Assert
        assertThat(serviceNames, hasSize(2));
        assertThat(serviceNames, contains("ServiceA", "ServiceB"));
    }

    /**
     * Test of getting optional names from "ServiceA, ServiceB, Service C with Blanks in Name"
     */
    @Test
    public void testGetOptionalServiceNamesWithProvidedValueWith3Items() {
        // Arrange

        final ServiceProcessCommand spyTestling = spy(testling);
        //noinspection ResultOfMethodCallIgnored
        doReturn("ServiceA, ServiceB, Service C with Blanks in Name").when(spyTestling).getServiceNames();

        // Act
        List<String> serviceNames = spyTestling.getOptionalServiceNames();

        // Assert
        assertThat(serviceNames, hasSize(3));
        assertThat(serviceNames, contains("ServiceA", "ServiceB", "Service C with Blanks in Name"));
    }

    /**
     * Test of getting optional names from "ServiceA, ServiceB, Service C with Blanks in Name"
     */
    @Test
    public void testGetOptionalServiceNamesWithProvidedValueWithOneItem() {
        // Arrange

        final ServiceProcessCommand spyTestling = spy(testling);
        //noinspection ResultOfMethodCallIgnored
        doReturn(" ServiceA ").when(spyTestling).getServiceNames();

        // Act
        List<String> serviceNames = spyTestling.getOptionalServiceNames();

        // Assert
        assertThat(serviceNames, hasSize(1));
        assertThat(serviceNames, hasItem("ServiceA"));
    }


    /**
     * Test get optional service names from param
     */
    @Test(expected = IllegalStateException.class)
    public void testGetServiceManagerThrowErrorWithWrongConnection() {
        // Arrange
        final Connection mockConnection = mock(Connection.class);
        final ServiceProcessCommand spyTestling = spy(testling);
        when(spyTestling.createConnection()).thenReturn(mockConnection);

        // Act
        spyTestling.getServiceManager(mockConnection);

        // Assert
        fail("Should trow exception");
    }

    /**
     * Test if successful retrieve of ServiceManager from Connection instance
     */
    @Test
    public void testGetServiceManager() {
        // Arrange
        final ConnectionDummy mockConnection = mock(ConnectionDummy.class);
        final ServiceProcessCommand spyTestling = spy(testling);
        final ServiceManager mockServiceManger = mock(ServiceManager.class);

        when(spyTestling.createConnection()).thenReturn(mockConnection);
        when(mockConnection.getManager(ServiceManager.class)).thenReturn(mockServiceManger);

        // Act
        final ServiceManager serviceManager = spyTestling.getServiceManager(mockConnection);

        // Assert
        assertThat(serviceManager, is(mockServiceManger));
        verify(mockConnection, times(1)).getManager(ServiceManager.class);
    }

    /**
     * Test if createConnection returns an instance of Connection.class
     */
    @Test
    public void testCreateConnectionReturnsInstanceOfConnection() {
        // Arrange

        // Act
        final Connection connection = testling.createConnection();

        // Assert
        assertThat("Expect instance of Connection.class", connection, instanceOf(Connection.class));
    }

    /**
     * Test if needsContext() is false.
     */
    @Test
    public void testNeedsContextReturnsFalse() {
        // Arrange

        // Act

        // Assert
        assertThat("Expected equal", testling.needsContext(), equalTo(false));
    }
}

