package com.espirit.moddev.cli.exception;

import de.espirit.common.base.Logger;

import org.slf4j.LoggerFactory;


/**
 * The type FsLoggingBridge is the connection between FS Logging and the SLF4J API.
 */
public class FsLoggingBridge implements Logger {

    @Override
    public boolean isTraceEnabled(final Class<?> aClass) {
        return LoggerFactory.getLogger(aClass).isTraceEnabled();
    }

    @Override
    public void logTrace(final String s, final Class<?> aClass) {
        LoggerFactory.getLogger(aClass).trace(s);
    }

    @Override
    public void logTrace(final String s, final Throwable throwable, final Class<?> aClass) {
        LoggerFactory.getLogger(aClass).trace(s, throwable);
    }

    @Override
    public boolean isDebugEnabled(final Class<?> aClass) {
        return LoggerFactory.getLogger(aClass).isDebugEnabled();
    }

    @Override
    public void logDebug(final String s, final Throwable throwable, final Class<?> aClass) {
        LoggerFactory.getLogger(aClass).debug(s, throwable);
    }

    @Override
    public void logDebug(final String s, final Class<?> aClass) {
        LoggerFactory.getLogger(aClass).debug(s);
    }

    @Override
    public boolean isInfoEnabled(final Class<?> aClass) {
        return LoggerFactory.getLogger(aClass).isInfoEnabled();
    }

    @Override
    public void logInfo(final String s, final Throwable throwable, final Class<?> aClass) {
        LoggerFactory.getLogger(aClass).info(s, throwable);
    }

    @Override
    public void logInfo(final String s, final Class<?> aClass) {
        LoggerFactory.getLogger(aClass).info(s);
    }

    @Override
    public boolean isWarnEnabled(final Class<?> aClass) {
        return LoggerFactory.getLogger(aClass).isWarnEnabled();
    }

    @Override
    public void logWarning(final String s, final Throwable throwable, final Class<?> aClass) {
        LoggerFactory.getLogger(aClass).warn(s, throwable);
    }

    @Override
    public void logWarning(final String s, final Class<?> aClass) {
        LoggerFactory.getLogger(aClass).warn(s);
    }

    @Override
    public void logError(final String s, final Throwable throwable, final Class<?> aClass) {
        LoggerFactory.getLogger(aClass).error(s, throwable);
    }

    @Override
    public void logError(final String s, final Class<?> aClass) {
        LoggerFactory.getLogger(aClass).error(s);
    }

    @Override
    public void logFatal(final String s, final Class<?> aClass) {
        logError(s, aClass);
    }

    @Override
    public void logFatal(final String s, final Throwable throwable, final Class<?> aClass) {
        logError(s, throwable, aClass);
    }
}
