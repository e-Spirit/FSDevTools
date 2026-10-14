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

package com.espirit.moddev.cli.classloading;

import com.espirit.moddev.cli.api.script.ScriptEngine;
import com.espirit.moddev.cli.commands.script.common.ScriptEngineRegistry;

import java.util.Collections;
import java.util.Optional;

/**
 * Runs next to the CLI jar in the classpath of a test distribution and uses the real script engine registry:
 * <ul>
 *     <li>{@code engines}: prints "&lt;name&gt; registered|missing" for each of the given names</li>
 *     <li>{@code classes}: prints "&lt;name&gt; loadable|missing" for each of the given class names</li>
 *     <li>{@code run <engine> <script>}: runs the script with the named engine and prints the result</li>
 * </ul>
 */
public final class ProbeMain {

	private ProbeMain() {
	}

	public static void main(final String[] args) throws Exception {
		final ScriptEngineRegistry registry = new ScriptEngineRegistry();
		registry.initialize();
		if ("engines".equals(args[0])) {
			for (int i = 1; i < args.length; i++) {
				System.out.println(args[i] + " " + (registry.requestEngine(args[i]).isPresent() ? "registered" : "missing"));
			}
		} else if ("classes".equals(args[0])) {
			for (int i = 1; i < args.length; i++) {
				System.out.println(args[i] + " " + (isLoadable(args[i]) ? "loadable" : "missing"));
			}
		} else if ("run".equals(args[0])) {
			final Optional<ScriptEngine> engine = registry.requestEngine(args[1]);
			if (engine.isEmpty()) {
				System.out.println(args[1] + " missing");
				System.exit(2);
			}
			final Object result = engine.get().getExecutable(args[2]).execute(Collections.emptyMap());
			System.out.println("RESULT_BEGIN");
			System.out.println(result);
			System.out.println("RESULT_END");
		} else {
			System.err.println("unknown mode " + args[0]);
			System.exit(1);
		}
	}


	private static boolean isLoadable(final String className) {
		try {
			Class.forName(className, false, ProbeMain.class.getClassLoader());
			return true;
		} catch (final ClassNotFoundException e) {
			return false;
		}
	}

}
