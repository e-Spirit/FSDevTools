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

package com.espirit.moddev.cli.results.logging;

import de.espirit.common.util.ElementProvider;
import de.espirit.common.util.Filter;
import de.espirit.common.util.Listable;
import de.espirit.firstspirit.access.*;
import de.espirit.firstspirit.access.packagepool.Package;
import de.espirit.firstspirit.access.project.Group;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.access.project.TemplateSet;
import de.espirit.firstspirit.access.store.*;
import de.espirit.firstspirit.access.store.sitestore.PageRef;
import de.espirit.firstspirit.access.store.templatestore.*;
import de.espirit.firstspirit.access.store.templatestore.gom.GomEditorProvider;
import de.espirit.firstspirit.access.store.templatestore.gom.GomValidationError;
import de.espirit.firstspirit.access.template.ParsingError;
import de.espirit.firstspirit.access.template.TemplateDocument;
import de.espirit.firstspirit.forms.FormData;
import de.espirit.firstspirit.storage.Contrast;
import de.espirit.firstspirit.storage.Revision;
import de.espirit.firstspirit.store.access.PermissionMap;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipFile;

public class SectionTemplateMock implements SectionTemplate {

    private final long _id;
    private final String _name;

    SectionTemplateMock(final long nodeId, final String name) {
        _id = nodeId;
        _name = name;
    }

    @Override
    public long getTemplateId() {
        return -1;
    }

    @Override
    public boolean hasPreviewPageRef() {
        return false;
    }

    @Override
    public PageRef getPreviewPageRef() {
        return null;
    }

    @Override
    public void setPreviewPageRef(final PageRef pageRef) {

    }

    @Override
    public TemplateExtension getTemplateExtension(final TemplateSet templateSet) {
        return null;
    }

    @Override
    public TemplateExtension[] getExtensions() {
        return new TemplateExtension[0];
    }

    @Override
    public boolean hasMultiPageParamsProviderEditor() {
        return false;
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public String getComment() {
        return null;
    }

    @Override
    public void setComment(final String s) {

    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public void setHidden(final boolean b) {

    }

    @Override
    public boolean isPackageItem() {
        return false;
    }

    @Override
    public boolean isSubscribedItem() {
        return false;
    }

    @Override
    public void addToPackage(final Package aPackage) throws LockException, ElementDeletedException {

    }

    @Override
    public void removeFromPackage(final Package aPackage) throws LockException, ElementDeletedException {

    }

    @Override
    public boolean isAddable(final Package aPackage) {
        return false;
    }

    @Override
    public boolean isChangeable() {
        return false;
    }

    @Override
    public String getPackageName() {
        return null;
    }

    @Override
    public Package getPackage() {
        return null;
    }

    @Override
    public int getChangeState() {
        return 0;
    }

    @Override
    public void setChangeState(final int i) throws IllegalAccessException {

    }

    @Override
    public String getReferenceName() {
        return null;
    }

    @Override
    public String getChannelSource(final TemplateSet templateSet) {
        return null;
    }

    @Override
    public void setChannelSource(final TemplateSet templateSet, final String s) {

    }

    @Override
    public String getGomSource() {
        return null;
    }

    @Override
    public void setGomSource(final String s) throws ParsingError {

    }

    @Override
    public FormData getFormDefaults() {
        return null;
    }

    @Override
    public void setFormDefaults(final FormData formData) {

    }

    @Override
    public Data getDefaults() throws ParsingError {
        return null;
    }

    @Override
    public void setDefaults(final Data data) {

    }

    @Override
    public GomEditorProvider getGomProvider() throws ParsingError {
        return null;
    }

    @Override
    public void setGomProvider(final GomEditorProvider gomEditorProvider) throws ParsingError, GomValidationError {

    }

    @Override
    public byte[] getPreviewImage() throws IOException {
        return new byte[0];
    }

    @Override
    public void setPreviewImage(final byte[] bytes) {

    }

    @Override
    public TemplateDocument getTemplateDocument(final TemplateSet templateSet) {
        return null;
    }

    @Override
    public IDProvider getSubRootInStore(final UserService userService, final boolean b) throws IOException {
        return null;
    }

    @Override
    public long getId() {
        return _id;
    }

    @Override
    public Long getLongID() {
        return _id;
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

    }

    @Override
    public void moveChild(final IDProvider idProvider, final int i) throws LockException, ElementMovedException {

    }

    @Override
    public String getDisplayName(final Language language) {
        return null;
    }

    @Override
    public void setDisplayName(final Language language, final String s) {

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

    }

    @Override
    public void release(final boolean b) {

    }

    @Override
    public IDProvider getParent() {
        return null;
    }

    @Override
    public Set<Contrast> contrastWith(final IDProvider idProvider) {
        return null;
    }

    @Override
    public void revert(final Revision revision, final boolean b, final EnumSet<RevertType> enumSet) throws LockException {

    }

    @Override
    public Data getMeta() {
        return null;
    }

    @Override
    public void setMeta(final Data data) {

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

    }

    @Override
    public String getName() {
        return _name;
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

    }

    @Override
    public void appendChildBefore(final StoreElement storeElement, final StoreElement storeElement1) {

    }

    @Override
    public void removeChild(final StoreElement storeElement) {

    }

    @Override
    public void replaceChild(final StoreElement storeElement, final StoreElement storeElement1) {

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

    }

    @Override
    public void setPermission(final User[] users, final Permission permission) {

    }

    @Override
    public void setPermission(final Group group, final Permission permission) {

    }

    @Override
    public void removePermission(final User user) {

    }

    @Override
    public void removePermission(final User[] users) {

    }

    @Override
    public void removePermission(final Group group) {

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

    }

    @Override
    public void setWorkflowPermissions(final WorkflowPermission[] workflowPermissions) {

    }

    @Override
    public void removeWorkflowPermission(final Workflow workflow) {

    }

    @Override
    public void removeAllWorkflowPermissions() {

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

    }

    @Override
    public void setWriteLock(final boolean b) {

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

    }

    @Override
    public void setLock(final boolean b, final boolean b1) throws LockException, ElementDeletedException {

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

    }

    @Override
    public void save(final String s) {

    }

    @Override
    public void save(final String s, final boolean b) {

    }

    @Override
    public void delete() throws LockException {

    }

    @Override
    public void refresh() {

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
    public Set<ReferenceEntry> getReferences() {
        return null;
    }

    @Override
    public boolean isDeleted() {
        return false;
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

    }

    @Override
    public void removeTask() {

    }

    @Override
    public Color getColor() {
        return null;
    }

    @Override
    public void setColor(final Color color) {

    }

    @Override
    public String getRulesetDefinition() {
        return null;
    }

    @Override
    public void setRulesetDefinition(final String s) {

    }

    @Override
    public Snippet getSnippet() {
        return null;
    }

    @Override
    public void setSnippet(final Snippet snippet) {

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
}
