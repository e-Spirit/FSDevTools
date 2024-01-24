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

import com.espirit.moddev.cli.api.parsing.exceptions.IDProviderNotFoundException;
import com.espirit.moddev.shared.StringUtils;
import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.access.store.Store;
import de.espirit.firstspirit.agency.StoreAgent;
import de.espirit.firstspirit.store.access.nexport.operations.ExportOperation;
import org.slf4j.LoggerFactory;

/**
 * FirstSpirit's uids are unique across all stores only in conjunction with their {@link de.espirit.firstspirit.access.store.IDProvider.UidType}.
 * This class encapsulates a uid and its {@link de.espirit.firstspirit.access.store.IDProvider.UidType} and therewith provides a full qualified representation of the uid.
 * It also allows to parse an arbitrary number of {@link java.lang.String} representations of combinations of uids and {@link de.espirit.firstspirit.access.store.IDProvider.UidType}s to instances of this class.
 */
public class UidIdentifier implements Identifier {
	protected static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(UidIdentifier.class);

	private final UidMapping uidMapping;
	private final String uid;
	private final String stringRepresentation;

	/**
	 * Instantiates a new full qualified uid.
	 *
	 * @param uidMapping {@link UidMapping} of the uid
	 * @param uid        the uid
	 * @throws IllegalArgumentException if uidMapping or uid is null or blank
	 */
	public UidIdentifier(final UidMapping uidMapping, final String uid) {
		if (uidMapping == null) {
			throw new IllegalArgumentException("uidMapping is null.");
		}
		if (StringUtils.isNullOrEmpty(uid)) {
			throw new IllegalArgumentException("Uid is null or empty.");
		}
		this.uidMapping = uidMapping;
		this.uid = uid;
		stringRepresentation = uidMapping.getPrefix() + ":" + uid;
	}

	/**
	 * Get the {@link de.espirit.firstspirit.access.store.IDProvider.UidType} of this uid.
	 *
	 * @return the {@link de.espirit.firstspirit.access.store.IDProvider.UidType} of this uid.
	 */
	public UidMapping getUidMapping() {
		return uidMapping;
	}

	/**
	 * Get the uid.
	 *
	 * @return the uid
	 */
	public String getUid() {
		return uid;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || o.getClass() != this.getClass()) {
			return false;
		} else if (this == o) {
			return true;
		} else {
			final UidIdentifier that = (UidIdentifier) o;
			return uidMapping.equals(that.uidMapping) && uid.equals(that.uid);
		}
	}

	@Override
	public int hashCode() {
		int result = uidMapping.hashCode();
		result = 31 * result + uid.hashCode(); //NOSONAR
		return result;
	}

	@Override
	public String toString() {
		return stringRepresentation;
	}

	/**
	 * Selects a StoreElement from the store corresponding to this element's uidMapping. If any
	 * object matching the uid could be retrieved, a check is performed, if its class
	 * matches the class specified by this identifiers class (@code {@link UidMapping#getCorrespondingType()}).
	 * <p>
	 * That is, because multiple implementing classes (for example FILE and MEDIA) can share the same UidType.
	 * If you query the store with uid and UidType only, you could retrieve a MEDIA item, even if you only wanted
	 * a FILE item. Since uids are unique across stores, there shouldn't be further problems.
	 *
	 * @param storeAgent      the StoreAgent to retrieve store instances from
	 * @param useReleaseState indicates whether to request elements from {@link Store#isRelease() release} or current store via given {@link StoreAgent}
	 * @param exportOperation the ExportOperation matching elements should be added to
	 */
	@Override
	public void addToExportOperation(StoreAgent storeAgent, boolean useReleaseState, ExportOperation exportOperation) {
		final IDProvider.UidType uidType = getUidMapping().getUidType();
		final Store.Type storeType = getUidMapping().getStoreType();
		final IDProvider storeElement = storeAgent.getStore(storeType, useReleaseState).getStoreElement(getUid(), uidType);
		if (storeElement != null) {
			if (isAssignableFrom(storeElement)) {
				LOGGER.debug("Adding store element: {}", storeElement);
				exportOperation.addElement(storeElement);
			} else {
				final String errorMessage = "IDProvider of class " + storeElement.getClass().getSimpleName() +
						" found, but expected to find one of class " + getUidMapping().getCorrespondingType().getSimpleName() +
						" for uid=" + getUid() + ", uidType=" + uidType + ", store=" + storeType + ", release=" + useReleaseState;
				throw new IDProviderNotFoundException(errorMessage);
			}
		} else {
			throw new IDProviderNotFoundException("IDProvider cannot be retrieved for uid=" + getUid() + ", uidType=" + uidType + ", store=" + storeType + ", release=" + useReleaseState);
		}
	}

	private boolean isAssignableFrom(IDProvider storeElement) {
		return getUidMapping().getCorrespondingType().isAssignableFrom(storeElement.getClass());
	}
}
