/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2024 Crownpeak Technology GmbH
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

import org.slf4j.Logger;
import org.slf4j.Marker;

public class MockLogger implements Logger {

	public static final String NEW_LINE = "\n";

	private final StringBuilder _stringBuilder = new StringBuilder();
	private final boolean _debugEnabled;
	private boolean _infoEnabled;

	public MockLogger() {
		this(false);
	}

	public MockLogger(final boolean debugEnabled) {
		_debugEnabled = debugEnabled;
		_infoEnabled = true;
	}

	public void setInfoEnabled(final boolean infoEnabled) {
		_infoEnabled = infoEnabled;
	}

	@Override
	public String getName() {
		return "MockLogger";
	}

	private void append(final String prefix, final String s) {
		_stringBuilder.append('[').append(prefix).append("] ").append(s).append(NEW_LINE);
	}

	@Override
	public boolean isTraceEnabled() {
		return true;
	}

	@Override
	public void trace(final String s) {
		append("TRACE", s);
	}

	@Override
	public void trace(final String s, final Object o) {
		trace(s);
	}

	@Override
	public void trace(final String s, final Object o, final Object o1) {
		trace(s);
	}

	@Override
	public void trace(final String s, final Object[] objects) {
		trace(s);
	}

	@Override
	public void trace(final String s, final Throwable throwable) {
		trace(s);
	}

	@Override
	public boolean isTraceEnabled(final Marker marker) {
		return true;
	}

	@Override
	public void trace(final Marker marker, final String s) {
		trace(s);
	}

	@Override
	public void trace(final Marker marker, final String s, final Object o) {
		trace(s);
	}

	@Override
	public void trace(final Marker marker, final String s, final Object o, final Object o1) {
		trace(s);
	}

	@Override
	public void trace(final Marker marker, final String s, final Object[] objects) {
		trace(s);
	}

	@Override
	public void trace(final Marker marker, final String s, final Throwable throwable) {
		trace(s);
	}

	@Override
	public boolean isDebugEnabled() {
		return _debugEnabled;
	}

	@Override
	public void debug(final String s) {
		append("DEBUG", s);
	}

	@Override
	public void debug(final String s, final Object o) {
		debug(s);
	}

	@Override
	public void debug(final String s, final Object o, final Object o1) {
		debug(s);
	}

	@Override
	public void debug(final String s, final Object[] objects) {
		debug(s);
	}

	@Override
	public void debug(final String s, final Throwable throwable) {
		debug(s);
	}

	@Override
	public boolean isDebugEnabled(final Marker marker) {
		return _debugEnabled;
	}

	@Override
	public void debug(final Marker marker, final String s) {
		debug(s);
	}

	@Override
	public void debug(final Marker marker, final String s, final Object o) {
		debug(s);
	}

	@Override
	public void debug(final Marker marker, final String s, final Object o, final Object o1) {
		debug(s);
	}

	@Override
	public void debug(final Marker marker, final String s, final Object[] objects) {
		debug(s);
	}

	@Override
	public void debug(final Marker marker, final String s, final Throwable throwable) {
		debug(s);
	}

	@Override
	public boolean isInfoEnabled() {
		return _infoEnabled;
	}

	@Override
	public void info(final String s) {
		append("INFO", s);
	}

	@Override
	public void info(final String s, final Object o) {
		info(s);
	}

	@Override
	public void info(final String s, final Object o, final Object o1) {
		info(s);
	}

	@Override
	public void info(final String s, final Object[] objects) {
		info(s);
	}

	@Override
	public void info(final String s, final Throwable throwable) {
		info(s);
	}

	@Override
	public boolean isInfoEnabled(final Marker marker) {
		return true;
	}

	@Override
	public void info(final Marker marker, final String s) {
		info(s);
	}

	@Override
	public void info(final Marker marker, final String s, final Object o) {
		info(s);
	}

	@Override
	public void info(final Marker marker, final String s, final Object o, final Object o1) {
		info(s);
	}

	@Override
	public void info(final Marker marker, final String s, final Object[] objects) {
		info(s);
	}

	@Override
	public void info(final Marker marker, final String s, final Throwable throwable) {
		info(s);
	}

	@Override
	public boolean isWarnEnabled() {
		return true;
	}

	@Override
	public void warn(final String s) {
		append("WARN", s);
	}

	@Override
	public void warn(final String s, final Object o) {
		warn(s);
	}

	@Override
	public void warn(final String s, final Object[] objects) {
		warn(s);
	}

	@Override
	public void warn(final String s, final Object o, final Object o1) {
		warn(s);
	}

	@Override
	public void warn(final String s, final Throwable throwable) {
		warn(s);
	}

	@Override
	public boolean isWarnEnabled(final Marker marker) {
		return true;
	}

	@Override
	public void warn(final Marker marker, final String s) {
		warn(s);
	}

	@Override
	public void warn(final Marker marker, final String s, final Object o) {
		warn(s);
	}

	@Override
	public void warn(final Marker marker, final String s, final Object o, final Object o1) {
		warn(s);
	}

	@Override
	public void warn(final Marker marker, final String s, final Object[] objects) {
		warn(s);
	}

	@Override
	public void warn(final Marker marker, final String s, final Throwable throwable) {
		warn(s);
	}

	@Override
	public boolean isErrorEnabled() {
		return true;
	}

	@Override
	public void error(final String s) {
		append("ERROR", s);
	}

	@Override
	public void error(final String s, final Object o) {
		error(s);
	}

	@Override
	public void error(final String s, final Object o, final Object o1) {
		error(s);
	}

	@Override
	public void error(final String s, final Object[] objects) {
		error(s);
	}

	@Override
	public void error(final String s, final Throwable throwable) {
		error(s);
	}

	@Override
	public boolean isErrorEnabled(final Marker marker) {
		return true;
	}

	@Override
	public void error(final Marker marker, final String s) {
		error(s);
	}

	@Override
	public void error(final Marker marker, final String s, final Object o) {
		error(s);
	}

	@Override
	public void error(final Marker marker, final String s, final Object o, final Object o1) {
		error(s);
	}

	@Override
	public void error(final Marker marker, final String s, final Object[] objects) {
		error(s);
	}

	@Override
	public void error(final Marker marker, final String s, final Throwable throwable) {
		error(s);
	}

	@Override
	public String toString() {
		return _stringBuilder.toString();
	}
}
