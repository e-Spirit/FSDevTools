/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2016 e-Spirit AG
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * *********************************************************************
 *
 */

package com.espirit.moddev.cli;

import com.espirit.moddev.cli.api.CliContext;
import com.espirit.moddev.cli.api.FsConnectionMode;
import com.espirit.moddev.cli.api.parsing.identifier.UidIdentifier;
import com.espirit.moddev.cli.api.configuration.Config;
import com.espirit.moddev.cli.api.configuration.ImportConfig;

import com.espirit.moddev.cli.api.parsing.identifier.UidMapping;
import de.espirit.firstspirit.access.AdminService;
import de.espirit.firstspirit.access.BaseContext;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.admin.ProjectStorage;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.agency.BrokerAgent;
import de.espirit.firstspirit.agency.LanguageAgent;
import de.espirit.firstspirit.agency.SpecialistsBroker;

import org.apache.log4j.Appender;
import org.apache.log4j.Category;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.varia.FallbackErrorHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;


/**
 * @author e-Spirit AG
 */
@RunWith(Theories.class)
public class CliContextImplTest {

    @DataPoints
    public static BaseContext.Env[]
        testcases =
        {null, BaseContext.Env.PREVIEW, BaseContext.Env.WEBEDIT, BaseContext.Env.DROP, BaseContext.Env.FS_BUTTON};

    private ImportConfig clientConfig;
    private CliContext testling;
    private SpecialistsBroker specialistsBroker;
    private Connection connection;
    private AssertAppender assertAppender;

    @Before
    public void setUp() throws Exception {
        clientConfig = mock(ImportConfig.class);
        when(clientConfig.getHost()).thenReturn("host");
        when(clientConfig.getPort()).thenReturn(1234);
        when(clientConfig.getUser()).thenReturn("horst");
        when(clientConfig.getProject()).thenReturn("myProject");
        when(clientConfig.getSynchronizationDirectoryString()).thenReturn("dir");
        when(clientConfig.getConnectionMode()).thenReturn(FsConnectionMode.HTTP);
        final List<UidIdentifier> uidList = new ArrayList<>();
        uidList.add(new UidIdentifier(UidMapping.PAGE, "yourUID"));
        uidList.add(new UidIdentifier(UidMapping.PAGE, "yourSecondUID"));
        when(clientConfig.isCreatingProjectIfMissing()).thenReturn(true);

        connection = mock(Connection.class);
        specialistsBroker = mock(SpecialistsBroker.class);
        when(connection.getBroker()).thenReturn(specialistsBroker);

        final AdminService adminService = mock(AdminService.class);
        when(connection.getService(AdminService.class)).thenReturn(adminService);
        final ProjectStorage projectStorage = mock(ProjectStorage.class);
        when(adminService.getProjectStorage()).thenReturn(projectStorage);

        final Project project = mock(Project.class);
        final String projectName = clientConfig.getProject();
        when(connection.getProjectByName(projectName)).thenReturn(project);
        when(project.getName()).thenReturn(projectName);
        when(projectStorage.createProject(projectName, projectName + " created by fs-filesync")).thenReturn(project);

        final BrokerAgent brokerAgent = mock(BrokerAgent.class);
        when(specialistsBroker.requireSpecialist(BrokerAgent.TYPE)).thenReturn(brokerAgent);
        when(brokerAgent.getBrokerByProjectName(any())).thenReturn(specialistsBroker);

        final LanguageAgent agent = mock(LanguageAgent.class);
        when(specialistsBroker.requireSpecialist(LanguageAgent.TYPE)).thenReturn(agent);
        when(specialistsBroker.requestSpecialist(LanguageAgent.TYPE)).thenReturn(agent);

        testling = new TestContext(clientConfig);

        assertAppender = new AssertAppender();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor() throws Exception {
        new CliContextImpl(null);
    }

    @Test
    public void testObtainConnectionExceptionOnEmptyProject() throws Exception {
        when(clientConfig.getProject()).thenReturn(null);
        when(clientConfig.getHost()).thenReturn("localhost");
        new CliContextImpl(clientConfig) {
            @Override
            protected void openConnection() {}
        };
    }

    @Test
    public void testAutoCloseable() throws Exception {
        Connection firstSpiritConnection = null;
        try (TestContext context = new TestContext(clientConfig)) {
            firstSpiritConnection = context.getConnection();
        }
        assertThat("Expect a non-null value", firstSpiritConnection, is(notNullValue()));
        verify(firstSpiritConnection, times(2)).connect();
        verify(firstSpiritConnection, times(1)).close();
    }

    @Theory
    public void testIsRest(final BaseContext.Env environment) throws Exception {
        assertThat("Expected false", testling.is(environment), is(Boolean.FALSE));
    }

    @Test
    public void testIsHeadless() throws Exception {
        assertThat("Expected true", testling.is(BaseContext.Env.HEADLESS), is(Boolean.TRUE));
    }

    @Test
    public void testRequireSpecialist() throws Exception {
        when(testling.getSpecialistsBroker()).thenReturn(specialistsBroker);
        final LanguageAgent languageAgent = testling.requireSpecialist(LanguageAgent.TYPE);
        assertThat("Expected a non-null value", languageAgent, is(notNullValue()));
        verify(specialistsBroker, times(1)).requireSpecialist(LanguageAgent.TYPE);
    }
    @Test(expected = IllegalStateException.class)
    public void testRequireSpecialistWithNullBroker() throws Exception {
        testling = spy(new TestContext(clientConfig));
        when(testling.getSpecialistsBroker()).thenReturn(null);
        final LanguageAgent languageAgent = testling.requireSpecialist(LanguageAgent.TYPE);
        assertThat("Expected a null value for a null specialistBroker", languageAgent, is(nullValue()));
    }

    @Test
    public void testRequestSpecialist() throws Exception {
        when(clientConfig.getProject()).thenReturn(null);
        testling = new TestContext(clientConfig);
        Assert.assertNull(testling.getSpecialistsBroker());
        testling.requestSpecialist(LanguageAgent.TYPE);
    }

    @Test
    public void testLoggingDebug() throws Exception {
        //No NullPointerExceptions should be thrown
        Logger.getLogger(CliContextImpl.class).addAppender(assertAppender);
        testling.logDebug("debug");
        assertThat("Expected a specific value: " + assertAppender.getMessage(), assertAppender.getMessage(), containsString("debug"));
    }

    @Test
    public void testLoggingInfo() throws Exception {
        //No NullPointerExceptions should be thrown
        Logger.getLogger(CliContextImpl.class).addAppender(assertAppender);
        testling.logInfo("info");
        assertThat("Expected a specific value: " + assertAppender.getMessage(), assertAppender.getMessage(), containsString("info"));
    }

    @Test
    public void testLoggingWarning() throws Exception {
        //No NullPointerExceptions should be thrown
        Logger.getLogger(CliContextImpl.class).addAppender(assertAppender);
        testling.logWarning("warning");
        assertThat("Expected a specific value: " + assertAppender.getMessage(), assertAppender.getMessage(), containsString("warning"));
    }

    @Test
    public void testLoggingError() throws Exception {
        //No NullPointerExceptions should be thrown
        Logger.getLogger(CliContextImpl.class).addAppender(assertAppender);
        testling.logError("error");
        assertThat("Expected a specific value: " + assertAppender.getMessage(), assertAppender.getMessage(), containsString("error"));
    }

    @Test
    public void testLoggingErrorWithException() throws Exception {
        //No NullPointerExceptions should be thrown
        Logger.getLogger(CliContextImpl.class).addAppender(assertAppender);
        testling.logError("error with exception", new Exception("JUnit"));
        assertThat("Expected a specific value: " + assertAppender.getMessage(), assertAppender.getMessage(), containsString("error with exception"));
    }

    private static class AssertAppender implements Appender {

        private final FallbackErrorHandler fallbackErrorHandler = new FallbackErrorHandler();
        private StringBuilder message = new StringBuilder();

        public String getMessage() {
            return message.toString();
        }

        @Override
        public void addFilter(final Filter filter) {

        }

        @Override
        public Filter getFilter() {
            return null;
        }

        @Override
        public void clearFilters() {

        }

        @Override
        public void close() {

        }

        @Override
        public void doAppend(final LoggingEvent loggingEvent) {
            if (loggingEvent.getLogger() == Category.getInstance(CliContextImpl.class)) {
                message.append(loggingEvent.getMessage().toString());
            }
        }

        @Override
        public String getName() {
            return getClass().getSimpleName();
        }

        @Override
        public void setErrorHandler(final ErrorHandler errorHandler) {

        }

        @Override
        public ErrorHandler getErrorHandler() {
            return fallbackErrorHandler;
        }

        @Override
        public void setLayout(final Layout layout) {

        }

        @Override
        public Layout getLayout() {
            return null;
        }

        @Override
        public void setName(final String s) {

        }

        @Override
        public boolean requiresLayout() {
            return false;
        }
    }

    private class TestContext extends CliContextImpl {

        /**
         * Instantiates a new Vcs connect context.
         *
         * @param clientConfig the client config
         */
        public TestContext(final Config clientConfig) {
            super(clientConfig);
        }

        @Override
        protected Connection obtainConnection() {
            return connection;
        }

    }
}
