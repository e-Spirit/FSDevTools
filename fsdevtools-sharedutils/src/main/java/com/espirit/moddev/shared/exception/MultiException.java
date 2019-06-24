package com.espirit.moddev.shared.exception;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * {@link Exception} class that works as a container for a list of {@link Exception exceptions}.
 *
 * @see Exception
 * @see RuntimeException
 */
@SuppressWarnings("Duplicates")
public class MultiException extends RuntimeException {

    private final List<Exception> _exceptions;

    public MultiException(final String message, final Collection<Exception> exceptions) {
        super(message);
        _exceptions = new ArrayList<>();
        addExceptions(exceptions);
    }

    private void addExceptions(final Collection<Exception> exceptions) {
        for (final Exception exception : exceptions) {
            if (exception instanceof MultiException) {
                addExceptions(((MultiException) exception)._exceptions);
            } else {
                _exceptions.add(exception);
            }
        }
    }

    public List<Exception> getExceptions() {
        return _exceptions;
    }

    @Override
    public void printStackTrace(final PrintStream writer) {
        writer.println(getMessage());
        for (int index = 0; index < _exceptions.size(); index++) {
            final Exception exception = _exceptions.get(index);
            writer.print("#" + getNumberWithLeadingChar(index + 1, _exceptions.size(), '0') + ": " + exception.getMessage() + (index < _exceptions.size() - 1 ? '\n' : ""));
        }
    }

    @Override
    public void printStackTrace(final PrintWriter writer) {
        writer.println(getMessage());
        for (int index = 0; index < _exceptions.size(); index++) {
            final Exception exception = _exceptions.get(index);
            writer.print("#" + getNumberWithLeadingChar(index + 1, _exceptions.size(), '0') + ": " + exception.getMessage() + (index < _exceptions.size() - 1 ? '\n' : ""));
        }
    }

    private String getNumberWithLeadingChar(final int number, final int max, final char leadingChar) {
        final int digits = String.valueOf(number).length();
        final int maxLength = String.valueOf(max).length();
        final int leadingZeros = maxLength - digits;
        final StringBuilder sb = new StringBuilder();
        if (leadingZeros > 0) {
            for (int i = 0; i < leadingZeros; i++) {
                sb.append(leadingChar);
            }
        }
        sb.append(number);
        return sb.toString();
    }

}