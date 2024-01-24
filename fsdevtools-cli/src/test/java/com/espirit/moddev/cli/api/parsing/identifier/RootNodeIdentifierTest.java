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

package com.espirit.moddev.cli.api.parsing.identifier;

import de.espirit.firstspirit.access.store.IDProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class RootNodeIdentifierTest {

	@Test
	public void testNullUidType() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> new RootNodeIdentifier(null));
	}

	@Test
	public void testEquality() {
		RootNodeIdentifier identifier = new RootNodeIdentifier(IDProvider.UidType.TEMPLATESTORE);
		RootNodeIdentifier equalIdentifier = new RootNodeIdentifier(IDProvider.UidType.TEMPLATESTORE);

		RootNodeIdentifier anUnequalIdentifier = new RootNodeIdentifier(IDProvider.UidType.CONTENTSTORE);

		assertThat("Expected two equal root node identifiers for equal uidType", identifier, equalTo(equalIdentifier));
		assertThat("Expected two different root node identifiers to not be equal", identifier, not(equalTo(anUnequalIdentifier)));
	}

	@Test
	public void testExceptionOnWrongUidTypeForStoreRoot() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> new RootNodeIdentifier(IDProvider.UidType.TEMPLATESTORE_FORMATTEMPLATE));
	}
}
