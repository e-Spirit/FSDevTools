package com.espirit.moddev.cli.commands.service;

import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.ServiceNotFoundException;
import de.espirit.firstspirit.access.project.ProjectScriptContext;
import de.espirit.firstspirit.agency.ModuleAdminAgent;
import de.espirit.firstspirit.agency.SpecialistsBroker;
import de.espirit.firstspirit.module.descriptor.ModuleDescriptor;
import de.espirit.firstspirit.module.descriptor.ServiceDescriptor;
import org.mockito.Answers;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class ServiceProcessCommandBaseTest<T extends ServiceProcessCommand> {

    protected T testling;
    protected Connection mockConnection;
    protected ProjectScriptContext mockContext;
    protected SpecialistsBroker mockBroker;
    protected ModuleAdminAgent mockModuleAdminAgent;

    public void setUp(Class<T> testlingClass) {
        setUp(mock(testlingClass, Answers.CALLS_REAL_METHODS));

    }
    public void setUp(T testling) {
        this.testling = testling;
        mockConnection = mock(Connection.class);
        mockContext = mock(ProjectScriptContext.class);
        mockBroker = mock(SpecialistsBroker.class);
        mockModuleAdminAgent = mock(ModuleAdminAgent.class);

        when(mockContext.getConnection()).thenReturn(mockConnection);
        when(mockConnection.getBroker()).thenReturn(mockBroker);
        when(mockBroker.requestSpecialist(ModuleAdminAgent.TYPE)).thenReturn(mockModuleAdminAgent);
        this.testling.setContext(mockContext);

        ModuleDescriptor mockModuleDescriptor = mock(ModuleDescriptor.class);
        ServiceDescriptor[] testServiceDescriptor = {
                new ServiceDescriptor("StoppedTestService", "TestModule", "1.0.0"),
                new ServiceDescriptor("StoppedTestService2", "TestModule", "1.0.0"),
                new ServiceDescriptor("RunningTestService", "TestModule", "1.0.0")
        };
        when(mockModuleDescriptor.getComponents()).thenReturn(testServiceDescriptor);
        List<ModuleDescriptor> moduleDescriptors = new ArrayList<>();
        moduleDescriptors.add(mockModuleDescriptor);
        when(mockModuleAdminAgent.getModules()).thenReturn(moduleDescriptors);

        when(mockModuleAdminAgent.isRunning("StoppedTestService")).thenReturn(false);
        when(mockModuleAdminAgent.isRunning("StoppedTestService2")).thenReturn(false);
        when(mockModuleAdminAgent.isRunning("RunningTestService")).thenReturn(true);

    }
}
