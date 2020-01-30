package com.espirit.moddev.util;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public enum OsUtil {

	;

	public static boolean isWindows() {
		return System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("windows");
	}

	@NotNull
	public static List<String> asWindowsCommands(@NotNull final List<String> commands) {
		if (commands.isEmpty()) {
			return commands;
		}
		final ArrayList<String> result = new ArrayList<>(commands);
		result.set(0, commands.get(0).replace("\\./", ""));
		result.add(0, "cmd");
		result.add(1, "/c");
		return result;
	}

	@NotNull
	public static List<String> asUnixCommands(@NotNull final List<String> commands) {
		if (commands.isEmpty()) {
			return commands;
		}
		final ArrayList<String> result = new ArrayList<>(commands);
		result.set(0, commands.get(0).replace("\\./", ""));
		result.add(0, "sh");
		return result;
	}

	@NotNull
	public static List<String> convertForCurrentOs(@NotNull final List<String> commands) {
		if (isWindows()) {
			return asWindowsCommands(commands);
		} else {
			return asUnixCommands(commands);
		}
	}

}