package com.espirit.moddev.cli.results.imports;

import de.espirit.common.util.Pair;
import de.espirit.firstspirit.store.access.nexport.ExportInfo;
import de.espirit.firstspirit.store.access.nexport.ExportStatus;
import de.espirit.firstspirit.store.access.nexport.io.ExportInfoFileHandle;
import org.jetbrains.annotations.NotNull;

import java.util.*;


/**
 * ImportInfo for entity types. This class <b>may</b> be included in newer versions of FirstSpirit.
 * Currently, this class is only a wrapper of the already existing ExportInfo.
 */
public abstract class ImportInfoImpl implements ExportInfo {

    private final Type _type;
    private final ExportStatus _status;

    ImportInfoImpl(@NotNull final Type type, @NotNull final ExportStatus status) {
        _type = type;
        _status = status;
    }

    @NotNull
    @Override
    public final Type getType() {
        return _type;
    }

    @NotNull
    @Override
    public final ExportStatus getStatus() {
        return _status;
    }

    @NotNull
    @Override
    public final Set<ExportInfoFileHandle> getCreatedFileHandles() {
        return Collections.emptySet();
    }

    @NotNull
    @Override
    public final Set<ExportInfoFileHandle> getUpdatedFileHandles() {
        return Collections.emptySet();
    }

    @NotNull
    @Override
    public final Set<ExportInfoFileHandle> getDeletedFileHandles() {
        return Collections.emptySet();
    }

    @NotNull
    @Override
    public final Collection<Pair<ExportInfoFileHandle, ExportInfoFileHandle>> getMovedFileHandles() {
        return Collections.emptySet();
    }
}
