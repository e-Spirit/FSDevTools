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
import com.espirit.moddev.cli.api.FullQualifiedUid;
import com.espirit.moddev.cli.api.configuration.Config;
import com.espirit.moddev.cli.api.configuration.ImportConfig;
import com.espirit.moddev.test.rules.logging.InitLog4jLoggingRule;
import de.espirit.firstspirit.access.AdminService;
import de.espirit.firstspirit.access.BaseContext;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.admin.ProjectStorage;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.agency.BrokerAgent;
import de.espirit.firstspirit.agency.LanguageAgent;
import de.espirit.firstspirit.agency.SpecialistsBroker;
import org.apache.log4j.*;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.varia.FallbackErrorHandler;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author e-Spirit AG
 */
@RunWith(Theories.class)
public class CliContextImplTest {

    @ClassRule
    public static InitLog4jLoggingRule initLog4jLoggingRule = new InitLog4jLoggingRule(Level.DEBUG);

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
        List<FullQualifiedUid> uidList = new ArrayList<>();
        uidList.add(new FullQualifiedUid(IDProvider.UidType.PAGESTORE, "yourUID"));
        uidList.add(new FullQualifiedUid(IDProvider.UidType.PAGESTORE, "yourSecondUID"));
        when(clientConfig.isCreatingProjectIfMissing()).thenReturn(true);
        /**when(clientConfig.isDeleteObsoleteFiles()).thenReturn(true);
        when(clientConfig.isExportChildElements()).thenReturn(true);*/

        connection = mock(Connection.class);
        specialistsBroker = mock(SpecialistsBroker.class);
        when(connection.getBroker()).thenReturn(specialistsBroker);


        AdminService adminService = mock(AdminService.class);
        when(connection.getService(AdminService.class)).thenReturn(adminService);
        final ProjectStorage projectStorage = mock(ProjectStorage.class);
        when(adminService.getProjectStorage()).thenReturn(projectStorage);

        Project project = mock(Project.class);
        final String projectName = clientConfig.getProject();
        when(connection.getProjectByName(projectName)).thenReturn(project);
        when(project.getName()).thenReturn(projectName);
        when(projectStorage.createProject(projectName, projectName + " created by fs-filesync")).thenReturn(project);

        BrokerAgent brokerAgent = mock(BrokerAgent.class);
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
    public void testIsRest(BaseContext.Env environment) throws Exception {
        assertThat("Expected false", testling.is(environment), is(Boolean.FALSE));
    }

    @Test
    public void testIsHeadless() throws Exception {
        assertThat("Expected true", testling.is(BaseContext.Env.HEADLESS), is(Boolean.TRUE));
    }

    @Test
    public void testRequireSpecialist() throws Exception {
        final LanguageAgent languageAgent = testling.requireSpecialist(LanguageAgent.TYPE);
        assertThat("Expected a non-null value", languageAgent, is(notNullValue()));
        verify(specialistsBroker, times(1)).requireSpecialist(LanguageAgent.TYPE);
    }

    @Test
    public void testRequestSpecialist() throws Exception {
        final LanguageAgent languageAgent = testling.requestSpecialist(LanguageAgent.TYPE);
        assertThat("Expected a non-null value", languageAgent, is(notNullValue()));
        verify(specialistsBroker, times(1)).requestSpecialist(LanguageAgent.TYPE);
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
        public void addFilter(Filter filter) {

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
        public void doAppend(LoggingEvent loggingEvent) {
            if (loggingEvent.getLogger() == Category.getInstance(CliContextImpl.class)) {
                message.append(loggingEvent.getMessage().toString());
            }
        }

        @Override
        public String getName() {
            return getClass().getSimpleName();
        }

        @Override
        public void setErrorHandler(ErrorHandler errorHandler) {

        }

        @Override
        public ErrorHandler getErrorHandler() {
            return fallbackErrorHandler;
        }

        @Override
        public void setLayout(Layout layout) {

        }

        @Override
        public Layout getLayout() {
            return null;
        }

        @Override
        public void setName(String s) {

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
        public TestContext(Config clientConfig) {
            super(clientConfig);
        }

        @Override
        protected Connection obtainConnection() {
            return connection;
        }

    }
}
