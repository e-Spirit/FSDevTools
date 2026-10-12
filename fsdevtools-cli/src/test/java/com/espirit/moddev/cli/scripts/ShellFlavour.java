/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2026 Crownpeak Technology GmbH
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

package com.espirit.moddev.cli.scripts;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/** {@code fs-cli.sh}, run with {@code /bin/sh} like its shebang does. */
final class ShellFlavour extends ScriptFlavour {

    private static final List<String> TOOLS = List.of("find", "tr", "sed", "head", "wc", "mv", "rm", "dirname", "readlink", "realpath");

    @Override
    String scriptName() {
        return "fs-cli.sh";
    }

    @Override
    String lineEnding() {
        return "\n";
    }

    @Override
    boolean isAvailable() {
        return Files.isExecutable(shell()) && hasCurl();
    }

    private boolean hasCurl() {
        try {
            final ProcessBuilder builder = new ProcessBuilder(shell().toString(), "-c", "command -v curl")
                    .redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD);
            final Process process = builder.start();
            return process.waitFor() == 0;
        } catch (final IOException | InterruptedException e) {
            return false;
        }
    }

    private static Path shell() {
        return Path.of("/bin/sh");
    }

    @Override
    List<String> command(@NotNull final Path script, @NotNull final Path workDir, @NotNull final List<String> args) {
        final List<String> command = withCommand(shell().toString(), script.toString());
        command.addAll(args);
        return command;
    }

    @Override
    Map<String, String> environmentWithoutCurl(@NotNull final Path workDir) throws IOException {
        // curl usually sits next to the other tools: link just the needed tools into an own directory
        final Path bin = Files.createDirectories(workDir.resolve("no-curl-bin"));
        final String[] pathDirs = System.getenv("PATH").split(java.io.File.pathSeparator);
        for (final String tool : TOOLS) {
            final Path found = Stream.of(pathDirs).map(dir -> Path.of(dir).resolve(tool)).filter(Files::isExecutable).findFirst().orElse(null);
            if (found != null) {
                Files.createSymbolicLink(bin.resolve(tool), found);
            }
        }
        return Map.of("PATH", bin.toString());
    }

    @Override
    String trickyPassword() {
        return "p$w 'q' &;|<>*?#~`x";
    }

}
