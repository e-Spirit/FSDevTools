/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2022 Crownpeak Technology GmbH
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

package com.espirit.moddev.cli.api.parsing.identifier;

import com.espirit.moddev.cli.api.parsing.exceptions.IDProviderNotFoundException;
import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.access.store.Store;
import de.espirit.firstspirit.access.store.mediastore.Media;
import de.espirit.firstspirit.access.store.mediastore.MediaStoreRoot;
import de.espirit.firstspirit.agency.StoreAgent;
import de.espirit.firstspirit.store.access.nexport.operations.ExportOperation;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author e-Spirit GmbH
 */
public class UidIdentifierTest {

	@NotNull
	private static Stream<String> parameterSet() {
		return Stream.of("", " ", null);
	}

	@ParameterizedTest
	@MethodSource("parameterSet")
	public void testEmptyOrNullUid(String uid) {
		Assertions.assertThrows(IllegalArgumentException.class, () -> new UidIdentifier(UidMapping.PAGEREF, uid));
	}

	@ParameterizedTest
	@MethodSource("parameterSet")
	public void testEmptyOrNullUidWithSubStore(String uid) {
		Assertions.assertThrows(IllegalArgumentException.class, () -> new UidIdentifier(null, uid));
	}

	@Test
	public void testEquality() {
		UidIdentifier uid = new UidIdentifier(UidMapping.PAGEREF, "reference_name");
		UidIdentifier anEqualUid = new UidIdentifier(UidMapping.PAGEREF, "reference_name");

		UidIdentifier anUnequalUid = new UidIdentifier(UidMapping.PAGEREF, "another_reference_name");
		UidIdentifier anotherUnequalUid = new UidIdentifier(UidMapping.FORMATTEMPLATE, "reference_name");

		assertThat("Expected two equal full qualified uids for equal template store and uid", uid, equalTo(anEqualUid));
		assertThat("Expected two different full qualified uids for non equal uid", uid, not(equalTo(anUnequalUid)));
		assertThat("Expected two different full qualified uids for non equal template store and same uid", uid, not(equalTo(anotherUnequalUid)));
	}

	/**
	 * The following two tests should ensure, that only elements are added to an ExportOperation
	 * whose mapped classes correspond with the element selected from the corresponding store.
	 */
	@Test
	public void addToExportOperationWithMatchingClass() {
		ExportOperation exportOperation = createMediaElementAndExportItWithGivenUidMappingUidType(UidMapping.MEDIA);
		verify(exportOperation).addElement(any());
	}

	@Test
	public void addToExportOperationWithNonMatchingClass() {
		assertThrows(IDProviderNotFoundException.class, () -> createMediaElementAndExportItWithGivenUidMappingUidType(UidMapping.PAGE));
	}

	/**
	 * Mocks a MediaStore and adds a dummy object X of type Media to it. Afterwards creates an ExportOperation
	 * and adds an identifier matching X's uid to it, but uses the passed uidMappings uidType.
	 *
	 * @param uidMapping the mapping from which the uidType is retrieved that is used for adding an element to
	 *                   the export operation
	 * @return the created ExportOperation mock
	 */
	private ExportOperation createMediaElementAndExportItWithGivenUidMappingUidType(UidMapping uidMapping) {
		String uidString = "reference_name";
		UidIdentifier uid = new UidIdentifier(uidMapping, uidString);

		StoreAgent storeAgent = mock(StoreAgent.class);
		ExportOperation exportOperation = mock(ExportOperation.class);
		Store mediaStoreRoot = mock(MediaStoreRoot.class);
		Store.Type storeType = uidMapping.getStoreType();
		when(storeAgent.getStore(storeType, false)).thenReturn(mediaStoreRoot);
		IDProvider storeElement = mock(Media.class);
		when(mediaStoreRoot.getStoreElement(uidString, uidMapping.getUidType())).thenReturn(storeElement);
		uid.addToExportOperation(storeAgent, false, exportOperation);
		return exportOperation;
	}

}
