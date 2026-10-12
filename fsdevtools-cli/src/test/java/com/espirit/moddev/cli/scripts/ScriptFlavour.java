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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * Runs one of the start scripts of the distribution ({@code fs-cli.sh} or {@code fs-cli.cmd}) as an external process.
 * <p>
 * The script is copied into a fake distribution ({@code <dist>/bin}, {@code <dist>/lib}) with the line endings the
 * distribution build gives it (LF for the shell script, CRLF for the batch file), so that it resolves its {@code lib}
 * directory exactly as in a real distribution.
 */
abstract class ScriptFlavour {

    /** the {@code fs-cli.*} file name in {@code archive/bin} */
    abstract String scriptName();

    /** whether the interpreter and curl needed to run this flavour exist on this machine */
    abstract boolean isAvailable();

    /** the command line that runs the installed script with the given arguments */
    abstract List<String> command(@NotNull Path script, @NotNull Path workDir, @NotNull List<String> args) throws IOException;

    /** line ending of the installed script */
    abstract String lineEnding();

    /** environment entries every run of this flavour needs (e.g. a PATH that finds the right tools) */
    Map<String, String> baseEnvironment() {
        return Map.of();
    }

    /**
     * Environment that makes {@code curl} unfindable while keeping all other tools the script uses.
     *
     * @return environment overrides, or {@code null} if that cannot be arranged on this machine
     */
    abstract Map<String, String> environmentWithoutCurl(@NotNull Path workDir) throws IOException;

    /** a password full of characters that need quoting in this flavour, as the user would pass it */
    abstract String trickyPassword();

    /** Copies {@code archive/bin/<script>} into {@code <dist>/bin} with the line endings of the distribution. */
    Path install(@NotNull Path dist) throws IOException {
        final Path source = findScriptSource();
        final String text = Files.readString(source, StandardCharsets.UTF_8).replace("\r\n", "\n").replace("\n", lineEnding());
        final Path bin = Files.createDirectories(dist.resolve("bin"));
        final Path target = bin.resolve(scriptName());
        Files.writeString(target, text, StandardCharsets.UTF_8);
        target.toFile().setExecutable(true);
        return target;
    }

    /**
     * Runs the script.
     *
     * @param env overrides on top of the inherited environment; a {@code null} value removes the variable
     */
    ScriptResult run(@NotNull Path script, @NotNull Path workDir, @NotNull List<String> args, @NotNull Map<String, String> env) throws IOException, InterruptedException {
        final ProcessBuilder builder = new ProcessBuilder(command(script, workDir, args));
        builder.directory(workDir.toFile());
        final Map<String, String> environment = builder.environment();
        final Map<String, String> overrides = new TreeMap<>(baseEnvironment());
        overrides.putAll(env);
        overrides.forEach((key, value) -> {
            if (value == null) {
                environment.remove(key);
            } else {
                environment.put(key, value);
            }
        });
        final Path stdout = workDir.resolve("stdout.txt");
        final Path stderr = workDir.resolve("stderr.txt");
        builder.redirectOutput(stdout.toFile());
        builder.redirectError(stderr.toFile());
        // an empty stdin, so that a script that wrongly prompts fails instead of hanging
        final Path stdin = workDir.resolve("stdin.txt");
        Files.writeString(stdin, "");
        builder.redirectInput(stdin.toFile());
        final Process process = builder.start();
        if (!process.waitFor(90, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            throw new IllegalStateException("script did not finish within 90 seconds");
        }
        return new ScriptResult(process.exitValue(), Files.readString(stdout), Files.readString(stderr));
    }

    private static Path findScriptSourceDir() {
        Path dir = Path.of("").toAbsolutePath();
        while (dir != null) {
            final Path candidate = dir.resolve("archive").resolve("bin");
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
            dir = dir.getParent();
        }
        throw new IllegalStateException("archive/bin not found above " + Path.of("").toAbsolutePath());
    }

    private Path findScriptSource() {
        return findScriptSourceDir().resolve(scriptName());
    }

    static List<String> withCommand(final String... command) {
        return new ArrayList<>(List.of(command));
    }

    static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }
}
