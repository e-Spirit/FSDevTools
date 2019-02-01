package com.espirit.moddev.projectservice.projectactivatewebserver;

import com.espirit.moddev.shared.webapp.WebAppIdentifier;

import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.project.Project;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProjectWebServerActivatorTest {
    private ProjectWebServerActivator testling;

    @Before
    public void setUp() {
        testling = new ProjectWebServerActivator();
    }

    @Test
    public void activateWebServer() {
        List<WebAppIdentifier> scopes = new ArrayList<>();
        scopes.add(WebAppIdentifier.WEBEDIT);
        ProjectWebServerActivationParameterBuilder builder = new ProjectWebServerActivationParameterBuilder();
        builder.withServerName("serverName")
            .atProjectName("dummyProjectName")
            .forScopes(scopes)
            .withForceActivation(true);
        Connection connectionMock = mock(Connection.class);
        when(connectionMock.isConnected()).thenReturn(false);

        testling.activateWebServer(connectionMock, builder.build());
    }

    @Test
    public void testPreconditionsWithUnconnectedConnection() {
        ProjectWebServerActivationParameter parameter = getValidParameters().build();
        Connection mockedConnection;
        mockedConnection = mock(Connection.class);
        when(mockedConnection.isConnected()).thenReturn(false);
        Assert.assertFalse(testling.arePreconditionsFulfilled(mockedConnection, parameter));
    }

    @Test
    public void testPreconditionsWithInvalidProject() {
        ProjectWebServerActivationParameter parameter = getValidParameters().build();
        final Connection mockedConnection = mock(Connection.class);
        when(mockedConnection.isConnected()).thenReturn(true);
        when(mockedConnection.getProjects()).thenReturn(new Project[1]);
        when(mockedConnection.getProjectByName(ArgumentMatchers.anyString())).thenReturn(null);

        Assert.assertFalse(testling.arePreconditionsFulfilled(mockedConnection, parameter));
    }

    @Test
    public void testPreconditionsWithNullScope() {
        List<WebAppIdentifier> invalidScopes = new ArrayList<>();
        invalidScopes.add(null);
        ProjectWebServerActivationParameter parameter = getValidParameters().forScopes(invalidScopes).build();
        final Project mockedProject = mock(Project.class);
        when(mockedProject.getName()).thenReturn("validProjectName");

        final Connection mockedConnection = mock(Connection.class);
        when(mockedConnection.isConnected()).thenReturn(true);
        when(mockedConnection.getProjects()).thenReturn(new Project[1]);
        when(mockedConnection.getProjectByName(ArgumentMatchers.anyString())).thenReturn(mockedProject);

        Assert.assertFalse(testling.arePreconditionsFulfilled(mockedConnection, parameter));
    }

    private ProjectWebServerActivationParameterBuilder getValidParameters() {
        final ProjectWebServerActivationParameterBuilder parameterBuilder = ProjectWebServerActivationParameter.builder();
        List<WebAppIdentifier> validScopes = new ArrayList<>();
        validScopes.add(WebAppIdentifier.LIVE);
        validScopes.add(WebAppIdentifier.FS5_ROOT);
        parameterBuilder.atProjectName("validProjectName").forScopes(validScopes).withServerName("ValidServerName").withForceActivation(true);
        return  parameterBuilder;
    }

    @Test
    public void testPreconditionsWithValidParameters() {
        ProjectWebServerActivationParameter parameter = getValidParameters().build();
        final Project mockedProject = mock(Project.class);
        when(mockedProject.getName()).thenReturn("validProjectName");

        final Connection mockedConnection = mock(Connection.class);
        when(mockedConnection.isConnected()).thenReturn(true);
        when(mockedConnection.getProjects()).thenReturn(new Project[1]);
        when(mockedConnection.getProjectByName(ArgumentMatchers.anyString())).thenReturn(mockedProject);

        Assert.assertTrue(testling.arePreconditionsFulfilled(mockedConnection, parameter));
    }
}