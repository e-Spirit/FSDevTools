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

import de.espirit.firstspirit.access.database.BasicEntityInfo;
import de.espirit.firstspirit.access.database.BasicEntityInfoImpl;
import de.espirit.firstspirit.access.store.BasicElementInfo;
import de.espirit.firstspirit.access.store.Store;
import de.espirit.firstspirit.agency.StoreAgent;
import de.espirit.firstspirit.store.access.BasicElementInfoImpl;
import de.espirit.firstspirit.store.access.nexport.operations.ImportOperation;
import de.espirit.firstspirit.transport.PropertiesTransportOptions;

import java.util.*;

public class MockedImportResult implements ImportOperation.Result {

	private Set<BasicElementInfo> _createdElements, _updateElements, _deletedElements, _movedElements, _lostAndFoundElements;
	private Set<BasicEntityInfo> _createdEntities, _updatedEntities;
	private EnumSet<PropertiesTransportOptions.ProjectPropertyType> _projectProperties;
	private List<ImportOperation.Problem> _problems;

	MockedImportResult(boolean fill) {
		if (fill) {
			fill();
		} else {
			_createdElements = Collections.emptySet();
			_updateElements = Collections.emptySet();
			_deletedElements = Collections.emptySet();
			_movedElements = Collections.emptySet();
			_lostAndFoundElements = Collections.emptySet();
			_createdEntities = Collections.emptySet();
			_updatedEntities = Collections.emptySet();
			_problems = Collections.emptyList();
			_projectProperties = EnumSet.noneOf(PropertiesTransportOptions.ProjectPropertyType.class);
		}
	}

	void fill() {
		{
			_createdElements = new HashSet<>();
			fillCollection(_createdElements, "created");
		}
		{
			_updateElements = new HashSet<>();
			fillCollection(_updateElements, "updated");
		}
		{
			_deletedElements = new HashSet<>();
			fillCollection(_deletedElements, "deleted");
		}
		{
			_movedElements = new HashSet<>();
			fillCollection(_movedElements, "moved");
		}
		{
			_lostAndFoundElements = new HashSet<>();
			fillCollection(_lostAndFoundElements, "lostAndFound");
		}
		{
			_lostAndFoundElements = new HashSet<>();
			fillCollection(_lostAndFoundElements, "lostAndFound");
		}
		{
			_projectProperties = EnumSet.allOf(PropertiesTransportOptions.ProjectPropertyType.class);
		}
		{
			_createdEntities = new HashSet<>();
			fillEntities(_createdEntities, "created");
			_updatedEntities = new HashSet<>();
			fillEntities(_updatedEntities, "updated");
		}
		{
			_problems = new ArrayList<>();
			_problems.add(createProblem(Store.Type.PAGESTORE, 1337, "IdProvider not found"));
			_problems.add(createProblem(Store.Type.MEDIASTORE, 1932, "Medium invalid"));
			_problems.add(createProblem(Store.Type.MEDIASTORE, 123, "Resolution invalid"));
			_problems.add(createProblem(Store.Type.TEMPLATESTORE, 1231, "GOM is invalid"));
		}
	}

	private ImportOperation.Problem createProblem(final Store.Type storeType, final long nodeId, final String message) {
		return new ImportOperation.Problem() {
			@Override
			public Store.Type getStoreType() {
				return storeType;
			}

			@Override
			public long getNodeId() {
				return nodeId;
			}

			@Override
			public String getMessage() {
				return message;
			}
		};
	}

	private void fillEntities(final Set<BasicEntityInfo> set, final String description) {
		set.add(createEntityInfo(4, "entityType1", "schema2", description));
		set.add(createEntityInfo(1, "entityType1", "schema1", description));
		set.add(createEntityInfo(6, "entityType1", "schema3", description));
		set.add(createEntityInfo(3, "entityType2", "schema1", description));
		set.add(createEntityInfo(2, "entityType1", "schema1", description));
		set.add(createEntityInfo(5, "entityType2", "schema2", description));
	}

	private void fillCollection(final Set<BasicElementInfo> set, final String description) {
		set.add(createElementInfo(4, Store.Type.TEMPLATESTORE, TagNames.PAGETEMPLATES.getName(), description));
		set.add(createElementInfo(3, Store.Type.MEDIASTORE, TagNames.MEDIUM.getName(), description));
		set.add(createElementInfo(1, Store.Type.PAGESTORE, TagNames.PAGE.getName(), description));
		set.add(createElementInfo(5, Store.Type.TEMPLATESTORE, TagNames.LINKTEMPLATE.getName(), description));
		set.add(createElementInfo(2, Store.Type.MEDIASTORE, TagNames.MEDIUM.getName(), description));
		set.add(createElementInfo(6, Store.Type.TEMPLATESTORE, TagNames.FORMATTEMPLATE.getName(), description));
		set.add(createElementInfo(7, Store.Type.TEMPLATESTORE, TagNames.FORMATTEMPLATE.getName(), description));
	}

	private BasicElementInfoImpl createElementInfo(final int id, final Store.Type storeType, final String nodeTag, final String description) {
		return new BasicElementInfoImpl(storeType, nodeTag, id, description + "_" + nodeTag + "_" + id, -1);
	}

	private BasicEntityInfoImpl createEntityInfo(final int id, final String entityType, final String schemaUid, final String description) {
		return new BasicEntityInfoImpl(UUID.nameUUIDFromBytes((description + "_" + entityType + "_" + schemaUid + "_" + id).getBytes()), description + "_" + entityType, schemaUid);
	}

	@Override
	public Set<BasicElementInfo> getCreatedElements() {
		return _createdElements;
	}

	@Override
	public Set<BasicElementInfo> getUpdatedElements() {
		return _updateElements;
	}

	@Override
	public Set<BasicElementInfo> getDeletedElements() {
		return _deletedElements;
	}

	@Override
	public Set<BasicElementInfo> getLostAndFoundElements() {
		return _lostAndFoundElements;
	}

	@Override
	public Set<BasicElementInfo> getMovedElements() {
		return _movedElements;
	}

	@Override
	public Set<BasicEntityInfo> getCreatedEntities() {
		return _createdEntities;
	}

	@Override
	public Set<BasicEntityInfo> getUpdatedEntities() {
		return _updatedEntities;
	}

	@Override
	public EnumSet<PropertiesTransportOptions.ProjectPropertyType> getModifiedProjectProperties() {
		return _projectProperties;
	}

	@Override
	public List<ImportOperation.Problem> getProblems() {
		return _problems;
	}

	StoreAgent getStoreAgent() {
		final MockedStoreAgent storeAgent = new MockedStoreAgent();
		final List<ImportOperation.Problem> problems = getProblems();
		for (final ImportOperation.Problem problem : problems) {
			final MockedStore store = (MockedStore) storeAgent.getStore(problem.getStoreType());
			store.getOrCreateElement(problem.getNodeId());
		}
		return storeAgent;
	}
}
