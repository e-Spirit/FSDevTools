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

package com.espirit.moddev.cli.results.logging;

import de.espirit.firstspirit.access.store.Store;
import de.espirit.firstspirit.agency.StoreAgent;

import java.util.HashMap;
import java.util.Map;

public class MockedStoreAgent implements StoreAgent {

	private final Map<Store.Type, Store> _stores = new HashMap<>();

	void addStore(final Store.Type type, final Store store) {
		_stores.put(type, store);
	}

	@Override
	public Store getStore(final Store.Type type) {
		Store store = _stores.get(type);
		if (store == null) {
			store = new MockedStore(type);
			addStore(type, store);
		}
		return store;
	}

	@Override
	public Store getStore(final Store.Type type, final boolean b) {
		return getStore(type);
	}
}
