package com.espirit.moddev.cli.results.imports;

import de.espirit.common.util.HashCodeBuilder;
import de.espirit.firstspirit.access.store.BasicElementInfo;
import de.espirit.firstspirit.store.access.nexport.ElementExportInfo;
import de.espirit.firstspirit.store.access.nexport.ExportStatus;

/**
 * ImportInfo for store elements. This class <b>may</b> be included in newer versions of FirstSpirit.
 * This class wraps the imported/updated store elements for the result logging.
 */
public class ElementImportInfoImpl extends ImportInfoImpl implements ElementExportInfo {

    private final BasicElementInfo _elementInfo;

    /**
     * Constructor
     * @param status the status
     * @param elementInfo the element info
     */
    public ElementImportInfoImpl(final ExportStatus status, final BasicElementInfo elementInfo) {
        super(Type.ELEMENT, status);
        _elementInfo = elementInfo;
    }

    @Override
    public BasicElementInfo getElementInfo() {
        return _elementInfo;
    }

    @Override
    public String getName() {
        return _elementInfo.getUid();
    }

    @Override
    public String toString() {
        return "ElementImportInfo { store=" + getElementInfo().getStoreType().getName() + " ; uid=" + getElementInfo().getUid() + " ; id=" + getElementInfo().getNodeId() + " }";
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getType()).append(_elementInfo.getUid()).append(_elementInfo).toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return obj != null && this.getClass().equals(obj.getClass()) && obj.hashCode() == hashCode();
    }
}
