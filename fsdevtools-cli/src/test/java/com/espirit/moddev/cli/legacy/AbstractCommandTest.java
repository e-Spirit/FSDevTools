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

package com.espirit.moddev.cli.legacy;

import com.espirit.moddev.cli.CliConstants;
import com.espirit.moddev.cli.commands.SimpleCommand;
import com.espirit.moddev.test.rules.logging.InitLog4jLoggingRule;
import com.espirit.moddev.test.rules.logging.LogTestMethodNameRule;
import com.espirit.moddev.test.rules.mock.MockitoInjectionRule;
import de.espirit.firstspirit.access.BaseContext;
import de.espirit.firstspirit.access.project.ProjectScriptContext;
import de.espirit.firstspirit.access.store.Store;
import de.espirit.firstspirit.access.store.StoreElement;
import de.espirit.firstspirit.access.store.templatestore.TemplateStoreRoot;
import de.espirit.firstspirit.agency.StoreAgent;
import de.espirit.firstspirit.io.FileHandle;
import de.espirit.firstspirit.io.FileSystem;
import de.espirit.firstspirit.io.FileSystemsAgent;
import org.apache.log4j.Level;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;

import java.util.Collections;
import java.util.Locale;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @param <C>
 * @author e-Spirit AG
 */
public abstract class AbstractCommandTest<C extends SimpleCommand> {

    @ClassRule
    public static InitLog4jLoggingRule initLog4jLoggingRule = new InitLog4jLoggingRule(Level.DEBUG);

    @Rule
    public LogTestMethodNameRule logTestMethodNameRule = new LogTestMethodNameRule();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public MockitoInjectionRule rule = MockitoInjectionRule.injectMocksFor(this);

    @Mock
    private de.espirit.firstspirit.access.store.templatestore.PageTemplates pageTemplates;

    @Mock
    private de.espirit.firstspirit.access.store.templatestore.SectionTemplates sectionTemplates;

    @Mock
    private de.espirit.firstspirit.access.store.templatestore.FormatTemplates formatTemplates;

    @Mock
    private de.espirit.firstspirit.access.store.templatestore.LinkTemplates linkTemplates;

    @Mock
    private de.espirit.firstspirit.access.store.templatestore.Scripts scripts;

    @Mock
    private de.espirit.firstspirit.access.store.templatestore.Workflows workflows;

    @Mock
    private de.espirit.common.util.Listable<de.espirit.firstspirit.access.store.StoreElement> list;

    @Mock
    private de.espirit.firstspirit.access.store.templatestore.Schemes schemes;

    @Mock
    private de.espirit.firstspirit.access.store.Store contentStore;

    protected ProjectScriptContext mockContext;

    protected C testling;

    private FileSystem fileHandle;


    @Before
    public void setUp() throws Exception {
        mockContext = new MockingProjectScriptContext("myProject", Locale.ENGLISH, temporaryFolder.newFolder(), BaseContext.Env.HEADLESS);
        testling = createTestling(mockContext);
        final FileSystemsAgent fileSystemsAgent = mockContext.requireSpecialist(FileSystemsAgent.TYPE);
        fileHandle = mock(FileSystem.class);
        when(fileSystemsAgent.getOSFileSystem((String) mockContext.getProperty(CliConstants.SYNC_DIR.value()))).thenReturn(fileHandle);
    }

    protected abstract C createTestling(ProjectScriptContext context);

    @Test
    public void testGetSyncDirectory() throws Exception {
        final FileSystem<FileHandle> syncDirectory = testling.getSynchronizationDirectory();

        assertThat("Expect a non-null value", syncDirectory, is(notNullValue()));
        assertThat("Expect same instance", syncDirectory, sameInstance(syncDirectory));
    }

    protected void mockStores() {
        final StoreAgent storeAgent = mockContext.requestSpecialist(StoreAgent.TYPE);
        final TemplateStoreRoot store = mock(TemplateStoreRoot.class);
        when(storeAgent.getStore(any(), eq(false))).thenReturn(store);
        when(storeAgent.getStore(Store.Type.CONTENTSTORE, false)).thenReturn(contentStore);
        when(contentStore.getChildren()).thenReturn(list);

        when(store.getPageTemplates()).thenReturn(pageTemplates);
        when(store.getSectionTemplates()).thenReturn(sectionTemplates);
        when(store.getFormatTemplates()).thenReturn(formatTemplates);
        when(store.getLinkTemplates()).thenReturn(linkTemplates);
        when(store.getScripts()).thenReturn(scripts);
        when(store.getWorkflows()).thenReturn(workflows);
        when(store.getSchemes()).thenReturn(schemes);

        when(pageTemplates.getChildren()).thenReturn(list);
        when(sectionTemplates.getChildren()).thenReturn(list);
        when(formatTemplates.getChildren()).thenReturn(list);
        when(linkTemplates.getChildren()).thenReturn(list);
        when(scripts.getChildren()).thenReturn(list);
        when(workflows.getChildren()).thenReturn(list);
        when(schemes.getChildren()).thenReturn(list);

        when(list.iterator()).thenReturn(Collections.<StoreElement>emptyIterator());
    }


}
