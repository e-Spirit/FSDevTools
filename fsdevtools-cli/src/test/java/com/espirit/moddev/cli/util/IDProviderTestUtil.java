/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2024 Crownpeak Technology GmbH
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
import de.espirit.firstspirit.access.store.templatestore.TemplateContainer;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This utility class provides methods for testing with firstspirit IDProviders.
 */
public class IDProviderTestUtil {

	/**
	 * Creates an IDProvider mock with the given uid. Caution: Since this is a mock,
	 * nearly all of the state of a regular IDProvider is missing.
	 *
	 * @param uid the uid the mock should have.
	 * @return an IDProvider mock.
	 */
	public static IDProvider getMock(String uid) {
		IDProvider mock = mock(IDProvider.class);
		when(mock.hasUid()).thenReturn(true);
		when(mock.getUid()).thenReturn(uid);
		return mock;
	}

	/**
	 * Creates a bunch of IDProvider mocks. For each given uid, a new mock is created.
	 * For further information, have a look at {@link IDProviderTestUtil#getMock(String)}.
	 *
	 * @param uids the uids for the mocks.
	 * @return a list of IDProvider mocks.
	 */
	public static List<IDProvider> getMocks(String... uids) {
		List<IDProvider> idProviders = new ArrayList();

		for (String uid : uids) {
			idProviders.add(getMock(uid));
		}

		return idProviders;
	}

	/**
	 * Creates a template container (folder) of a specific type. Children can be queried on the container.
	 *
	 * @param uid                    the name of the new container.
	 * @param templateContainerClass the class the container should have.
	 * @param <FOLDER_TYPE>          the type of the folder.
	 * @return a template container.
	 */
	public static <FOLDER_TYPE extends TemplateContainer>
	FOLDER_TYPE getFolderMockType(String uid, Class<FOLDER_TYPE> templateContainerClass) {

		FOLDER_TYPE folder = mock(templateContainerClass);
		when(folder.hasUid()).thenReturn(false);
		when(folder.getName()).thenReturn(uid);
		Listable<IDProvider> children = new StoreTestUtil.SimpleListable(new ArrayList());
		when(folder.getChildren(IDProvider.class)).thenReturn(children);
		when(folder.getChildren(IDProvider.class, true)).thenReturn(children);

		return folder;
	}
}
