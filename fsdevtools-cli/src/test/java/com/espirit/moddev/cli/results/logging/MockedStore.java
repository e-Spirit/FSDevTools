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

package com.espirit.moddev.cli.results.logging;

import de.espirit.common.util.ElementProvider;
import de.espirit.common.util.Filter;
import de.espirit.common.util.Listable;
import de.espirit.firstspirit.access.*;
import de.espirit.firstspirit.access.project.Group;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.access.store.*;
import de.espirit.firstspirit.access.store.templatestore.Workflow;
import de.espirit.firstspirit.access.store.templatestore.WorkflowLockException;
import de.espirit.firstspirit.access.store.templatestore.WorkflowPermission;
import de.espirit.firstspirit.forms.FormData;
import de.espirit.firstspirit.storage.Contrast;
import de.espirit.firstspirit.storage.Revision;
import de.espirit.firstspirit.store.access.PermissionMap;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.List;
import java.util.zip.ZipFile;

public class MockedStore implements Store {

	private boolean _release;
	private Type _type;
	private Map<Long, IDProvider> _storeElementsById;

	MockedStore(final Type type) {
		_type = type;
		_storeElementsById = new HashMap<>();
		setRelease(false);
	}

	public void addMockedStoreElement(final IDProvider element) {
		_storeElementsById.put(element.getId(), element);
	}

	void setRelease(final boolean release) {
		_release = release;
	}

	@Override
	public Type getType() {
		return _type;
	}

	@Override
	public void addStoreListener(final StoreListener storeListener) {
		// nothing to do
	}

	@Override
	public void removeStoreListener(final StoreListener storeListener) {
		// nothing to do
	}

	@Override
	public boolean isRelease() {
		return _release;
	}

	@Override
	public List<DeletedElementsInfo> getDeletedChilds() {
		return Collections.EMPTY_LIST;
	}

	@Override
	public List<DeletedElementsInfo> getDeletedChildren() {
		return Collections.EMPTY_LIST;
	}

	@Override
	public List<DeletedElementsInfo> getDeletedChilds(final long l, final int i) {
		return Collections.EMPTY_LIST;
	}

	@Override
	public List<DeletedElementsInfo> getDeletedChildren(final long l, final int i) {
		return Collections.EMPTY_LIST;
	}

	@Override
	public StoreElement restore(final ElementInfo elementInfo, final IDProvider idProvider) throws LockException {
		return null;
	}

	@Override
	public boolean isFolder() {
		return false;
	}

	@Override
	public boolean isPermissionSupported() {
		return false;
	}

	@Override
	public boolean hasPermissions() {
		return false;
	}

	@Override
	public Permission getPermission() {
		return null;
	}

	@Override
	public Permission getPermission(final User user) {
		return null;
	}

	@Override
	public Permission getPermission(final Group group) {
		return null;
	}

	@Override
	public void setPermission(final User user, final Permission permission) {
		// nothing to do
	}

	@Override
	public void setPermission(final User[] users, final Permission permission) {
		// nothing to do
	}

	@Override
	public void setPermission(final Group group, final Permission permission) {
		// nothing to do
	}

	@Override
	public void removePermission(final User user) {
		// nothing to do
	}

	@Override
	public void removePermission(final User[] users) {
		// nothing to do
	}

	@Override
	public void removePermission(final Group group) {
		// nothing to do
	}

	@Override
	public PermissionMap getTreePermission() {
		return null;
	}

	@Override
	public List<Principal> getDefinedPrincipalPermissions() {
		return null;
	}

	@Override
	public List<Principal> getInheritedPrincipalPermissions() {
		return null;
	}

	@Override
	public long getLastChanged() {
		return 0;
	}

	@Override
	public User getEditor() {
		return null;
	}

	@Override
	public boolean isWorkflowSupported() {
		return false;
	}

	@Override
	public WorkflowPermission[] getWorkflowPermissions() {
		return new WorkflowPermission[0];
	}

	@Override
	public WorkflowPermission getWorkflowPermission(final Workflow workflow) {
		return null;
	}

	@Override
	public WorkflowPermission getCreateWorkflowPermission(final Workflow workflow) {
		return null;
	}

	@Override
	public void setWorkflowPermission(final WorkflowPermission workflowPermission) {
		// nothing to do
	}

	@Override
	public void setWorkflowPermissions(final WorkflowPermission[] workflowPermissions) {
		// nothing to do
	}

	@Override
	public void removeWorkflowPermission(final Workflow workflow) {
		// nothing to do
	}

	@Override
	public void removeAllWorkflowPermissions() {
		// nothing to do
	}

	@Override
	public boolean isWorkflowAllowed(final Workflow workflow, final User user) {
		return false;
	}

	@Override
	public boolean inheritWorkflowPermission() {
		return false;
	}

	@Override
	public void setInheritWorkflowPermission(final boolean b) {
		// nothing to do
	}

	@Override
	public void setWriteLock(final boolean b) {
		// nothing to do
	}

	@Override
	public boolean getWriteLock() {
		return false;
	}

	@Override
	public boolean isLockSupported() {
		return false;
	}

	@Override
	public void setLock(final boolean b) throws LockException, ElementDeletedException {
		// nothing to do
	}

	@Override
	public void setLock(final boolean b, final boolean b1) throws LockException, ElementDeletedException {
		// nothing to do
	}

	@Override
	public boolean isLocked() {
		return false;
	}

	@Override
	public boolean isLockedOnServer(final boolean b) {
		return false;
	}

	@Override
	public void save() {
		// nothing to do
	}

	@Override
	public void save(final String s) {
		// nothing to do
	}

	@Override
	public void save(final String s, final boolean b) {
		// nothing to do
	}

	@Override
	public boolean hasTask() {
		return false;
	}

	@Override
	public Task getTask() {
		return null;
	}

	@Override
	public void setTask(final Task task) {
		// nothing to do
	}

	@Override
	public void removeTask() {
		// nothing to do
	}

	@Override
	public Color getColor() {
		return null;
	}

	@Override
	public void setColor(final Color color) {
		// nothing to do
	}

	@Override
	public void delete() throws LockException {
		// nothing to do
	}

	@Override
	public void refresh() {
		// nothing to do
	}

	@Override
	public String toXml() {
		return null;
	}

	@Override
	public String toXml(final boolean b) {
		return null;
	}

	@Override
	public String toXml(final boolean b, final boolean b1) {
		return null;
	}

	@Override
	public boolean isImportSupported() {
		return false;
	}

	@Override
	public boolean isExportSupported() {
		return false;
	}

	@Override
	public void exportStoreElement(final OutputStream outputStream, final ExportHandler exportHandler) throws IOException {
		// nothing to do
	}

	@Override
	public StoreElement importStoreElement(final ZipFile zipFile, final ImportHandler importHandler) throws IOException, ElementDeletedException, WorkflowLockException {
		return null;
	}

	@Override
	public Listable<StoreElement> importStoreElements(final ZipFile zipFile, final ImportHandler importHandler) throws IOException, ElementDeletedException, WorkflowLockException {
		return null;
	}

	@Override
	public String getElementType() {
		return null;
	}

	@Override
	public IDProvider getStoreElement(final long id) {
		return _storeElementsById.get(id);
	}

	@Override
	public IDProvider getStoreElement(final Long id) {
		return getStoreElement((long) id);
	}

	@Override
	public List<? extends IDProvider> getElements(final Collection<Long> collection) {
		return null;
	}

	@Override
	public IDProvider getStoreElement(final String s, final UidType uidType) {
		return null;
	}

	@Override
	public IDProvider getStoreElement(final String s, final String s1) {
		return null;
	}

	@Override
	public Project getProject() {
		return null;
	}

	@Override
	public ReferenceEntry[] getIncomingReferences() {
		return new ReferenceEntry[0];
	}

	@Override
	public boolean hasIncomingReferences() {
		return false;
	}

	@Override
	public ReferenceEntry[] getOutgoingReferences() {
		return new ReferenceEntry[0];
	}

	@Override
	public String getReferenceName() {
		return null;
	}

	@Override
	public Set<ReferenceEntry> getReferences() {
		return null;
	}

	@Override
	public boolean isDeleted() {
		return false;
	}

	@Override
	public UserService getUserService() {
		return null;
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public Revision getMaxRevision() {
		return null;
	}

	@Override
	public long getId() {
		return _type.ordinal();
	}

	@Override
	public Long getLongID() {
		return getId();
	}

	@Override
	public Revision getRevision() {
		return null;
	}

	@Override
	public Revision getReleaseRevision() {
		return null;
	}

	@Override
	public IDProvider getInRevision(final Revision revision) {
		return null;
	}

	@Override
	public String getUid() {
		return null;
	}

	@Override
	public void setUid(final String s) {

	}

	@Override
	public UidType getUidType() {
		return null;
	}

	@Override
	public boolean hasUid() {
		return false;
	}

	@Override
	public LanguageInfo getLanguageInfo(final Language language) {
		return null;
	}

	@Override
	public void moveChild(final IDProvider idProvider) throws LockException, ElementMovedException {
		// nothing to do
	}

	@Override
	public void moveChild(final IDProvider idProvider, final int i) throws LockException, ElementMovedException {
		// nothing to do
	}

	@Override
	public String getDisplayName(final Language language) {
		return null;
	}

	@Override
	public void setDisplayName(final Language language, final String s) {
		// nothing to do
	}

	@Override
	public boolean isReleaseSupported() {
		return false;
	}

	@Override
	public int getReleaseStatus() {
		return 0;
	}

	@Override
	public boolean isReleased() {
		return false;
	}

	@Override
	public User getReleasedBy() {
		return null;
	}

	@Override
	public boolean isInReleaseStore() {
		return false;
	}

	@Override
	public void release() {
		// nothing to do
	}

	@Override
	public void release(final boolean b) {
		// nothing to do
	}

	@Override
	public String getName() {
		return _type.getName();
	}

	@Override
	public Listable<StoreElement> getChildren() {
		return null;
	}

	@Override
	public <T extends StoreElement> Listable<T> getChildren(final Class<T> aClass) {
		return null;
	}

	@Override
	public <T extends StoreElement> Listable<T> getChildren(final Class<T> aClass, final boolean b) {
		return null;
	}

	@Override
	public <T extends StoreElement> Listable<T> getChildren(final Filter.TypedFilter<T> typedFilter, final boolean b) {
		return null;
	}

	@Override
	public void appendChild(final StoreElement storeElement) {
		// nothing to do
	}

	@Override
	public void appendChildBefore(final StoreElement storeElement, final StoreElement storeElement1) {
		// nothing to do
	}

	@Override
	public void removeChild(final StoreElement storeElement) {
		// nothing to do
	}

	@Override
	public void replaceChild(final StoreElement storeElement, final StoreElement storeElement1) {
		// nothing to do
	}

	@Override
	public int getChildCount() {
		return 0;
	}

	@Override
	public int getChildIndex(final StoreElement storeElement) {
		return 0;
	}

	@Override
	public IDProvider getParent() {
		return null;
	}

	@Override
	public StoreElement getNextSibling() {
		return null;
	}

	@Override
	public StoreElement getPreviousSibling() {
		return null;
	}

	@Override
	public StoreElement getFirstChild() {
		return null;
	}

	@Override
	public Store getStore() {
		return null;
	}

	@Override
	public Set<Contrast> contrastWith(final IDProvider idProvider) {
		return null;
	}

	@Override
	public void revert(final Revision revision, final boolean b, final EnumSet<RevertType> enumSet) throws LockException {
		// nothing to do
	}

	@Override
	public Data getMeta() {
		return null;
	}

	@Override
	public void setMeta(final Data data) {
		// nothing to do
	}

	@Override
	public boolean hasMeta() {
		return false;
	}

	@Override
	public FormData getMetaFormData() {
		return null;
	}

	@Override
	public void setMetaFormData(final FormData formData) {
		// nothing to do
	}

	@Override
	public List<Revision> getHistory() {
		return null;
	}

	@Override
	public List<Revision> getHistory(final Date date, final Date date1, final int i, final Filter<Revision> filter) {
		return null;
	}

	@Override
	public ElementProvider<Revision> asRevisionProvider() {
		return null;
	}

	@Override
	public int compareTo(final StoreElement o) {
		return 0;
	}

	IDProvider getOrCreateElement(final long nodeId) {
		IDProvider storeElement = getStoreElement(nodeId);
		if (storeElement == null) {
			storeElement = new MockedStoreElement(nodeId, getName() + "_name_" + nodeId, getType() == Store.Type.TEMPLATESTORE ? null : getName() + "_uid_" + nodeId);
			addMockedStoreElement(storeElement);
		}
		return storeElement;
	}
}
