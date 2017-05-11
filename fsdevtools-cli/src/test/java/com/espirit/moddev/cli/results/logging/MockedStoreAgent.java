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