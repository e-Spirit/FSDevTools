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

package com.espirit.moddev.cli.exception;

import de.espirit.common.base.Logger;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

/**
 * The type FsLoggingBridge is the connection between FS Logging and the SLF4J API.
 */
public class FsLoggingBridge implements Logger {

	@Override
	public boolean isTraceEnabled(@NotNull final Class<?> className) {
		return LoggerFactory.getLogger(className).isTraceEnabled();
	}

	@Override
	public boolean isTraceEnabled(@NotNull final String className) {
		return LoggerFactory.getLogger(className).isTraceEnabled();
	}

	@Override
	public void logTrace(@NotNull final String message, @NotNull final Class<?> className) {
		LoggerFactory.getLogger(className).trace(message);
	}

	@Override
	public void logTrace(@NotNull final String message, @NotNull final String className) {
		LoggerFactory.getLogger(className).trace(message);
	}

	@Override
	public void logTrace(@NotNull final String message, @NotNull final Throwable throwable, final Class<?> className) {
		LoggerFactory.getLogger(className).trace(message, throwable);
	}

	@Override
	public void logTrace(@NotNull final String message, @NotNull final Throwable throwable, @NotNull final String className) {
		LoggerFactory.getLogger(className).trace(message, throwable);
	}

	@Override
	public boolean isDebugEnabled(@NotNull final Class<?> className) {
		return LoggerFactory.getLogger(className).isDebugEnabled();
	}

	@Override
	public boolean isDebugEnabled(@NotNull final String className) {
		return LoggerFactory.getLogger(className).isDebugEnabled();
	}

	@Override
	public void logDebug(@NotNull final String message, @NotNull final Throwable throwable, @NotNull final Class<?> className) {
		LoggerFactory.getLogger(className).debug(message, throwable);
	}

	@Override
	public void logDebug(@NotNull final String message, @NotNull final Throwable throwable, @NotNull final String className) {
		LoggerFactory.getLogger(className).debug(message, throwable);
	}

	@Override
	public void logDebug(@NotNull final String message, @NotNull final Class<?> className) {
		LoggerFactory.getLogger(className).debug(message);
	}

	@Override
	public void logDebug(@NotNull final String message, @NotNull final String className) {
		LoggerFactory.getLogger(className).debug(message);
	}

	@Override
	public boolean isInfoEnabled(@NotNull final Class<?> className) {
		return LoggerFactory.getLogger(className).isInfoEnabled();
	}

	@Override
	public boolean isInfoEnabled(@NotNull final String className) {
		return LoggerFactory.getLogger(className).isInfoEnabled();
	}

	@Override
	public void logInfo(@NotNull final String message, @NotNull final Throwable throwable, @NotNull final Class<?> className) {
		LoggerFactory.getLogger(className).info(message, throwable);
	}

	@Override
	public void logInfo(@NotNull final String message, @NotNull final Throwable throwable, @NotNull final String className) {
		LoggerFactory.getLogger(className).info(message, throwable);
	}

	@Override
	public void logInfo(@NotNull final String message, @NotNull final Class<?> className) {
		LoggerFactory.getLogger(className).info(message);
	}

	@Override
	public void logInfo(@NotNull final String message, @NotNull final String className) {
		LoggerFactory.getLogger(className).info(message);
	}

	@Override
	public boolean isWarnEnabled(@NotNull final Class<?> className) {
		return LoggerFactory.getLogger(className).isWarnEnabled();
	}

	@Override
	public boolean isWarnEnabled(@NotNull final String className) {
		return LoggerFactory.getLogger(className).isWarnEnabled();
	}

	@Override
	public void logWarning(@NotNull final String message, @NotNull final Throwable throwable, @NotNull final Class<?> className) {
		LoggerFactory.getLogger(className).warn(message, throwable);
	}

	@Override
	public void logWarning(@NotNull final String message, @NotNull final Throwable throwable, @NotNull final String className) {
		LoggerFactory.getLogger(className).warn(message, throwable);
	}

	@Override
	public void logWarning(@NotNull final String message, @NotNull final Class<?> className) {
		LoggerFactory.getLogger(className).warn(message);
	}

	@Override
	public void logWarning(@NotNull final String message, @NotNull final String className) {
		LoggerFactory.getLogger(className).warn(message);
	}

	@Override
	public void logError(@NotNull final String message, @NotNull final Throwable throwable, @NotNull final Class<?> className) {
		LoggerFactory.getLogger(className).error(message, throwable);
	}

	@Override
	public void logError(@NotNull final String message, @NotNull final Throwable throwable, @NotNull final String className) {
		LoggerFactory.getLogger(className).error(message, throwable);
	}

	@Override
	public void logError(@NotNull final String message, @NotNull final Class<?> className) {
		LoggerFactory.getLogger(className).error(message);
	}

	@Override
	public void logError(@NotNull final String message, @NotNull final String className) {
		LoggerFactory.getLogger(className).error(message);
	}

	@Override
	public void logFatal(@NotNull final String message, @NotNull final Class<?> className) {
		logError(message, className);
	}

	@Override
	public void logFatal(@NotNull final String message, @NotNull final String className) {
		logError(message, className);
	}

	@Override
	public void logFatal(@NotNull final String message, @NotNull final Throwable throwable, @NotNull final Class<?> className) {
		logError(message, throwable, className);
	}

	@Override
	public void logFatal(@NotNull final String message, @NotNull final Throwable throwable, @NotNull final String className) {
		logError(message, throwable, className);
	}

}
