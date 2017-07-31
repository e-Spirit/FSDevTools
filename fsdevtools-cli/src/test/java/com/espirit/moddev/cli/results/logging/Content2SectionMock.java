package com.espirit.moddev.cli.results.logging;

import de.espirit.common.util.ElementProvider;
import de.espirit.common.util.Filter;
import de.espirit.common.util.Listable;
import de.espirit.firstspirit.access.*;
import de.espirit.firstspirit.access.packagepool.Package;
import de.espirit.firstspirit.access.project.Group;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.access.store.*;
import de.espirit.firstspirit.access.store.pagestore.Content2Section;
import de.espirit.firstspirit.access.store.sitestore.ContentMultiPageParams;
import de.espirit.firstspirit.access.store.templatestore.TableTemplate;
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
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipFile;

public class Content2SectionMock implements Content2Section {

    private final long _id;
    private final String _name;

    Content2SectionMock(final long nodeId, final String name) {
        _id = nodeId;
        _name = name;
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
    public void setName(final String s) {

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

    @Override
    public TableTemplate getTableTemplate() throws ReferenceNotFoundException {
        return null;
    }

    @Override
    public String getTableName() {
        return null;
    }

    @Override
    public TableTemplate getTemplate() throws ReferenceNotFoundException {
        return null;
    }

    @Override
    public void setTemplate(final TableTemplate tableTemplate) {

    }

    @Override
    public long[] getLifespan() {
        return new long[0];
    }

    @Override
    public void setLifespan(final long l, final long l1) throws UnsupportedOperationException {

    }

    @Override
    public void removeLifespan() {

    }

    @Override
    public boolean isInLifespan() {
        return false;
    }

    @Override
    public boolean isInLifespan(final long l) {
        return false;
    }

    @Override
    public void includeInOutput(final Language language) {

    }

    @Override
    public void excludeFromOutput(final Language language) throws UnsupportedOperationException {

    }

    @Override
    public boolean isInOutputIncluded(final Language language) {
        return false;
    }

    @Override
    public boolean isReference() {
        return false;
    }

    @Override
    public ContentMultiPageParams getMultiPageParams(final Language language) {
        return null;
    }

    @Override
    public Data getData() {
        return null;
    }

    @Override
    public void setData(final Data data) {

    }

    @Override
    public void clearCachedData() {

    }

    @Override
    public FormData getFormData() {
        return null;
    }

    @Override
    public void setFormData(final FormData formData) {

    }
}
