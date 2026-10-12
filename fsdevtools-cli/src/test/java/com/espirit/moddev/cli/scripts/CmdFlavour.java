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
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * {@code fs-cli.cmd}, run by {@code cmd.exe} through a small wrapper batch file that holds the command line exactly as a
 * user would type it. That keeps the quoting of special characters ({@code & ^ % !}) under the control of the test
 * instead of the Java process launcher.
 */
final class CmdFlavour extends ScriptFlavour {

    private static final Pattern NEEDS_QUOTES = Pattern.compile("[\\s&^%!()<>|,;=]");

    @Override
    String scriptName() {
        return "fs-cli.cmd";
    }

    @Override
    String lineEnding() {
        return "\r\n";
    }

    @Override
    boolean isAvailable() {
        return isWindows();
    }

    @Override
    List<String> command(@NotNull final Path script, @NotNull final Path workDir, @NotNull final List<String> args) throws IOException {
        final String line = quote(script.toString()) + args.stream().map(CmdFlavour::quote).map(a -> " " + a).collect(Collectors.joining());
        final Path wrapper = workDir.resolve("run.cmd");
        // no "call": that is how the README tells users to start the script
        Files.writeString(wrapper, "@echo off\r\n" + line + "\r\n", StandardCharsets.US_ASCII);
        return withCommand(Path.of(System.getenv("SystemRoot"), "System32", "cmd.exe").toString(), "/d", "/c", wrapper.toString());
    }

    @Override
    Map<String, String> environmentWithoutCurl(@NotNull final Path workDir) throws IOException {
        // curl.exe sits in System32 next to where.exe and findstr.exe, so give the script copies of just these two
        final Path bin = Files.createDirectories(workDir.resolve("no-curl-bin"));
        final Path system32 = Path.of(System.getenv("SystemRoot"), "System32");
        for (final String tool : List.of("where.exe", "findstr.exe")) {
            Files.copy(system32.resolve(tool), bin.resolve(tool));
        }
        return Map.of("PATH", bin.toString());
    }

    @Override
    String trickyPassword() {
        // the README: quote passwords with special characters
        return "p&w^%!x y(z)";
    }

    /** quotes an argument like a user has to at the prompt, and doubles % because the line sits in a batch file */
    private static String quote(final String arg) {
        final String escaped = arg.replace("%", "%%");
        return arg.isEmpty() || NEEDS_QUOTES.matcher(arg).find() ? '"' + escaped + '"' : escaped;
    }
}
