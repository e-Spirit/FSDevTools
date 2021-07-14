/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2021 e-Spirit AG
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

package com.espirit.moddev.cli.util;

import de.espirit.common.util.Listable;
import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.access.store.StoreElement;
import de.espirit.firstspirit.access.store.templatestore.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This utility class provides methods for testing with the firstspirit template store.
 *
 * @author e-Spirit AG
 */
public class StoreTestUtil {

    /**
     * Initializes a store context with mocks. This includes the functionality to
     * request IDProvider children from the store and the substore.
     * @param store the template store root node that should be initialized recursively.
     */
    public static void mockTemplateStores(final TemplateStoreRoot store) {
        {
            Listable<IDProvider> storeChildren = new SimpleListable(new ArrayList());
            when(store.getChildren(IDProvider.class, true)).thenReturn(storeChildren);
            when(store.getChildren(IDProvider.class)).thenReturn(storeChildren);
        }
        {
            PageTemplates templates = getTemplatesMockForType(PageTemplates.class);
            when(store.getPageTemplates()).thenReturn(templates);
        }
        {
            SectionTemplates templates = getTemplatesMockForType(SectionTemplates.class);
            when(store.getSectionTemplates()).thenReturn(templates);
        }
        {
            FormatTemplates templates = getTemplatesMockForType(FormatTemplates.class);
            when(store.getFormatTemplates()).thenReturn(templates);
        }
        {
            LinkTemplates templates = getTemplatesMockForType(LinkTemplates.class);
            when(store.getLinkTemplates()).thenReturn(templates);
        }
        {
            Scripts templates = getTemplatesMockForType(Scripts.class);
            when(store.getScripts()).thenReturn(templates);
        }
        {
            Workflows templates = getTemplatesMockForType(Workflows.class);
            when(store.getWorkflows()).thenReturn(templates);
        }
    }

    /**
     * Adds a list of IDProviders to a given store element, so that a call to getChildren
     * returns some entries.
     * @param storeElement the store element to add the children to.
     * @param idProviders a list of IDProviders that should be added to the existing children
     *                    of the given storeElement.
     */
    public static void addChildren(StoreElement storeElement, List<IDProvider> idProviders) {
        SimpleListable.addToSimpleListable(storeElement.getChildren(IDProvider.class), idProviders);
    }

    /**
     * Adds an IDProvider to a given store element, so that a call to getChildren returns
     * some entries.
     * @param storeElement the store element to add the children to.
     * @param idProvider the IDProvider to add as a child of the given storeElement.
     */
    public static void addChild(StoreElement storeElement, IDProvider idProvider) {
        SimpleListable.addToSimpleListable(storeElement.getChildren(IDProvider.class), idProvider);
    }

    /**
     * Creates a mock for a given templates type and initializes its children to an empty list.
     * @param templatesClass the class of the template store element that should be mocked. For example
     *                       {@link de.espirit.firstspirit.access.store.templatestore.PageTemplates}.
     * @param <TEMPLATES_TYPE> the type of the class to mock.
     * @return the mock of the given type.
     */
    private static <TEMPLATES_TYPE extends StoreElement> TEMPLATES_TYPE getTemplatesMockForType(Class<TEMPLATES_TYPE> templatesClass) {
        return (TEMPLATES_TYPE) getTemplatesMockForType(templatesClass, new ArrayList());
    }

    /**
     * Creates a mock for a given templates type and initializes its children with the given list. This overrides
     * a previous initialization, in the means that the list of children is replaced.
     * @param templatesClass the class of the template store element that should be mocked. For example
     *                       {@link de.espirit.firstspirit.access.store.templatestore.PageTemplates}.
     * @param childrenToAdd a list of children to initialize the store element with.
     * @param <TEMPLATES_TYPE> the type of the class to mock.
     * @return the mock of the given type.
     */
    private static <TEMPLATES_TYPE extends StoreElement> TEMPLATES_TYPE getTemplatesMockForType(Class<TEMPLATES_TYPE> templatesClass, List<IDProvider> childrenToAdd) {
        TEMPLATES_TYPE templates = mock(templatesClass);
        Listable<IDProvider> children = new SimpleListable(childrenToAdd);
        when(templates.getChildren(IDProvider.class, true)).thenReturn(children);
        when(templates.getChildren(IDProvider.class)).thenReturn(children);
        return templates;
    }

    /**
     * A utility class that implements the firstspirit Listable interface for IDProviders.
     * Uses a backing collection to be able to freely add and remove entries - a functionality
     * that the firstspirit implementations consciously don't do. So this is should only be used in
     * testing environments for mocking.
     *
     * @author e-Spirit AG
     */
    public static class SimpleListable implements Listable<IDProvider> {
        List<IDProvider> children = new ArrayList<>();
        public SimpleListable(List children) {
            this.children = children;
        }

        @Override public Iterator<IDProvider> iterator() {
            return children.iterator();
        }
        @Override public IDProvider getFirst() {
            return children.get(0);
        }
        @Override public List<IDProvider> toList() {
            return children;
        }

        public void addChild(IDProvider idProvider) {
            children.add(idProvider);
        }

        /**
         * Utility method for adding an IDProvider to a Listable. Since this is only possible with
         * specific implementations of the interface (for example {@link StoreTestUtil.SimpleListable},
         * an UnsupportedOperationException is thrown when passing a wrong implementation.
         * @param targetListable the Listable to add the IDProvider to. Should be an implementation of
         *                       Listable that supports adding/removing entries.
         * @param idProvider the IDProvider to add to the Listable.
         */
        public static void addToSimpleListable(Listable<IDProvider> targetListable, IDProvider idProvider) {
            if(SimpleListable.class.equals(targetListable.getClass())) {
                ((SimpleListable) targetListable).addChild(idProvider);
            } else {
                throw new UnsupportedOperationException("Can not add an element to a class other than SimpleListable");
            }
        }

        /**
         * Utility method for adding a list of IDProviders to a Listable. Have a look at {@link StoreTestUtil.SimpleListable#addChild(StoreElement, IDProvider)}
         * for further details.
         * @param targetListable the Listable to add the IDProvider to.
         * @param idProviders the IDProviders to add to the Listable.
         */
        public static void addToSimpleListable(Listable<IDProvider> targetListable, List<IDProvider> idProviders) {
            if(targetListable == null) {
                throw new IllegalArgumentException("Target listable to add idProviders to should not be null");
            }
            for(IDProvider idProvider : idProviders) {
                addToSimpleListable(targetListable, idProvider);
            }
        }
    }
}
