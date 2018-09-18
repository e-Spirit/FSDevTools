package com.espirit.moddev.serverrunner;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author e-Spirit AG
 */
public enum ServerType {
    LEGACY(() -> Arrays.asList(Paths.get("server", "lib", "fs-server.jar"), Paths.get("server", "lib", "wrapper.jar")), "de.espirit.firstspirit.server.CMSServer"),
    ISOLATED(() -> Arrays.asList(Paths.get("server", "lib-isolated", "fs-isolated-server.jar"), Paths.get("server", "lib-isolated", "wrapper.jar")), "de.espirit.common.bootstrap.Bootstrap");

    private final List<Path> startUpJars;
    private final String startUpClass;

    ServerType(final Supplier<List<Path>> startUpJars, final String startUpClass) {
        this.startUpJars = startUpJars.get();
        this.startUpClass = startUpClass;
    }

    public List<File> resolveJars(final Path serverInstallationDir) {
        return startUpJars.stream()
                .map(file -> serverInstallationDir.resolve(file).toFile())
                .collect(Collectors.toList());
    }

    public String getStartUpClass() {
        return startUpClass;
    }
}
