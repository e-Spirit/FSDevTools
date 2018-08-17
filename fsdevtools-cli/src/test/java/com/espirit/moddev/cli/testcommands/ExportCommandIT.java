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

package com.espirit.moddev.cli.testcommands;

import com.espirit.moddev.IntegrationTest;
import com.espirit.moddev.cli.api.parsing.identifier.RootNodeIdentifier;
import com.espirit.moddev.cli.api.parsing.identifier.UidIdentifier;
import com.espirit.moddev.cli.api.parsing.identifier.UidMapping;
import com.espirit.moddev.cli.commands.export.ExportCommand;
import com.espirit.moddev.cli.results.ExportResult;
import de.espirit.common.io.IoUtil;
import de.espirit.firstspirit.access.Language;
import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.access.store.IDProvider.UidType;
import de.espirit.firstspirit.access.store.Store;
import de.espirit.firstspirit.access.store.pagestore.Page;
import de.espirit.firstspirit.agency.StoreAgent;
import de.espirit.firstspirit.io.FileHandle;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Set;

import static com.espirit.moddev.IntegrationTest.PROJECT_NAME;
import static com.espirit.moddev.IntegrationTest.PROJECT_NAME_WITH_DB;

/**
 * @author e-Spirit AG
 */
@Category(IntegrationTest.class)
public class ExportCommandIT extends AbstractIntegrationTest {

    @Test
    public void parameterLessCommandDoesntExport() throws Exception {
        ExportCommand command = new ExportCommand();
        command.setProject(PROJECT_NAME);
        initContextWithDefaultConfiguration(command);

        ExportResult result = command.call();

        Assert.assertTrue("Exporting with an empty identifier list is permitted", result.isError());
        Assert.assertTrue(result.getError() instanceof IllegalArgumentException);
    }

    @Test
    public void multipleParameterCommandCreatesFiles() throws Exception {
        ExportCommand command = new ExportCommand();

        command.addIdentifier("pagetemplate:default");
        command.addIdentifier("page:homepage");
        command.setProject(PROJECT_NAME);
        initContextWithDefaultConfiguration(command);
        Assert.assertEquals(2, command.getIdentifiers().size());
        Assert.assertTrue(command.getIdentifiers().contains(new UidIdentifier(UidMapping.PAGETEMPLATE, "default")));
        Assert.assertTrue(command.getIdentifiers().contains(new UidIdentifier(UidMapping.PAGE, "homepage")));

        ExportResult result = command.call();
        Assert.assertTrue(result.get().getCreatedFiles().size() > 0);
        Assert.assertTrue("Export folder for templates properties not found.", containsSubDirectory(getFirstSpiritFileSyncFolder(testFolder.getRoot()), "TemplateStore"));
        Assert.assertTrue("Export folder for pages properties not found.", containsSubDirectory(getFirstSpiritFileSyncFolder(testFolder.getRoot()), "PageStore"));
    }

    @Test
    public void templatestoreRootParameterCommandCreatesFiles() throws Exception {
        ExportCommand command = new ExportCommand();
        command.setProject(PROJECT_NAME);
        initContextWithDefaultConfiguration(command);
        command.addIdentifier("root:templatestore");
        Assert.assertEquals(1, command.getIdentifiers().size());
        Assert.assertTrue(command.getIdentifiers().contains(new RootNodeIdentifier(IDProvider.UidType.TEMPLATESTORE)));

        ExportResult result = command.call();
        Assert.assertTrue(result.get().getCreatedFiles().size() > 0);
        Assert.assertTrue("Export folder for templates properties not found.", containsSubDirectory(getFirstSpiritFileSyncFolder(testFolder.getRoot()), "TemplateStore"));
    }

    @Test
    public void singleParameterCommandWithProjectPropertiesCreatesFiles() throws Exception {
        ExportCommand command = new ExportCommand();

        command.addIdentifier("pagetemplate:default");
        command.setProject(PROJECT_NAME_WITH_DB);
        initContextWithDefaultConfiguration(command);
        command.setIncludeProjectProperties(true);

        ExportResult result = command.call();
        Assert.assertTrue("Export folder for project properties not found.", containsSubDirectory(getFirstSpiritFileSyncFolder(testFolder.getRoot()), "Global"));
        Assert.assertTrue(result.get().getCreatedFiles().size() > 0);
    }

    @Test
    public void singleContent2ExportCommand() throws Exception {
        ExportCommand command = new ExportCommand();

        String content2 = "news";
        command.addIdentifier("entities:" + content2);
        command.setProject(PROJECT_NAME_WITH_DB);
        initContextWithDefaultConfiguration(command);

        ExportResult result = command.call();
        Assert.assertTrue("Export folder not found.", containsSubDirectory(testFolder.getRoot(), "Entities"));
        File entitiesFolder = new File(testFolder.getRoot() + "/Entities");
        Assert.assertTrue("Export folder for entities not found.", entitiesFolder.exists());
        Assert.assertTrue("Export folder not for News entities not found.", containsSubDirectory(entitiesFolder, "News"));
        Assert.assertTrue(result.get().getCreatedFiles().size() > 0);
    }


    @Test
    public void exportCurrentState() throws Exception {
        ExportCommand command = new ExportCommand();
        command.setProject(PROJECT_NAME_WITH_DB);
        initContextWithDefaultConfiguration(command);


        // make some changes to page 'imprint' which is based on pagetemplate 'default' of integration test project
        final Page page = (Page) command.getContext().requireSpecialist(StoreAgent.TYPE).getStore(Store.Type.PAGESTORE).getStoreElement("imprint", Page.UID_TYPE);
        changeDisplayName(page, command.getContext().getProject().getMasterLanguage(), "new displayname");

        command.addIdentifier("pagetemplate:default");
        command.addIdentifier("page:imprint");


        ExportResult result = command.call();
        final File syncFolder = getFirstSpiritFileSyncFolder(testFolder.getRoot());
        Assert.assertTrue("Export folder TemplateStore not found.", containsSubDirectory(syncFolder, "TemplateStore"));
        Assert.assertTrue("Export folder PageStore not found.", containsSubDirectory(syncFolder, "PageStore"));

        // check content of storeelement.xml
        final Set<FileHandle> createdFiles = result.get().getCreatedFiles();
        // search imprint storeelement.xml
        FileHandle imprintStoreElementXmlFileHandle = null;
        for (final FileHandle createdFile : createdFiles) {
            if ("/PageStore/imprint_1/imprint/StoreElement.xml".equals(createdFile.getPath())) {
                imprintStoreElementXmlFileHandle = createdFile;
                break;
            }
        }
        Assert.assertNotNull("imprint storeelement.xml should be created", imprintStoreElementXmlFileHandle);

        // check content of StoreElement.xml --> should contain displayname of current state
        final ByteArrayOutputStream bout = new ByteArrayOutputStream((int) imprintStoreElementXmlFileHandle.getSize());
        IoUtil.copyStream(imprintStoreElementXmlFileHandle.load(), bout, true);
        final String xmlContent = new String(bout.toByteArray(), "UTF-8");
        Assert.assertTrue("displayname of currentstate missing", xmlContent.contains("new displayname"));
    }


     @Test
    public void exportReleaseState() throws Exception {
        ExportCommand command = new ExportCommand();
         command.setProject(PROJECT_NAME_WITH_DB);
        initContextWithDefaultConfiguration(command);


        // make some changes to page 'imprint' which is based on pagetemplate 'default' of integration test project
        final Page page = (Page) command.getContext().requireSpecialist(StoreAgent.TYPE).getStore(Store.Type.PAGESTORE).getStoreElement("imprint", Page.UID_TYPE);
        changeDisplayName(page, command.getContext().getProject().getMasterLanguage(), "new displayname");

        command.addIdentifier("pagetemplate:default");
        command.addIdentifier("page:imprint");
        command.setExportReleaseState(true);


        ExportResult result = command.call();
        final File syncFolder = getFirstSpiritFileSyncFolder(testFolder.getRoot());
        Assert.assertTrue("Export folder TemplateStore not found.", containsSubDirectory(syncFolder, "TemplateStore"));
        Assert.assertTrue("Export folder PageStore not found.", containsSubDirectory(syncFolder, "PageStore"));

        // check content of storeelement.xml
        final Set<FileHandle> createdFiles = result.get().getCreatedFiles();
        // search imprint storeelement.xml
        FileHandle imprintStoreElementXmlFileHandle = null;
        for (final FileHandle createdFile : createdFiles) {
            if ("/PageStore/imprint_1/imprint/StoreElement.xml".equals(createdFile.getPath())) {
                imprintStoreElementXmlFileHandle = createdFile;
                break;
            }
        }
        Assert.assertNotNull("imprint storeelement.xml should be created", imprintStoreElementXmlFileHandle);

        // check content of StoreElement.xml --> should contain displayname of current state
        final ByteArrayOutputStream bout = new ByteArrayOutputStream((int) imprintStoreElementXmlFileHandle.getSize());
        IoUtil.copyStream(imprintStoreElementXmlFileHandle.load(), bout, true);
        final String xmlContent = new String(bout.toByteArray(), "UTF-8");
        Assert.assertFalse("displayname of currentstate should not be available in export", xmlContent.contains("new displayname"));
    }


    /**
     * Changes the displayname of given element to given displayname in given language.
     */
    private static void changeDisplayName(final IDProvider element, final Language language, final String displayName) throws Exception {
        element.setLock(true, false);
        try {
            element.getLanguageInfo(language).setDisplayName(displayName);
            element.save("change displayname '" + displayName + "'", false);
        } finally {
            element.setLock(false, false);
        }
    }
}
