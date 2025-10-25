/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.logging;

import org.jetbrains.annotations.Nullable;

import java.io.PrintStream;
import java.util.function.BiConsumer;

public interface Logger {
    /* CREATION */

    static Logger create() {
        return new DefaultLogger();
    }

    static Logger create(byte indentLevel) {
        return new DefaultLogger(indentLevel);
    }

    static Logger create(Level enabled) {
        return new DefaultLogger(enabled);
    }

    static Logger create(Level enabled, byte indentLevel) {
        return new DefaultLogger(enabled, indentLevel);
    }

    /* LOG LEVELS */

    /** Represents an individual logging level. */
    enum Level {
        DEBUG, QUIET, INFO, WARN, ERROR, FATAL
    }

    @Nullable Level getEnabled();

    void setEnabled(@Nullable Level level);

    default boolean isEnabled(Level level) {
        Level enabled = this.getEnabled();
        return enabled != null && level.compareTo(enabled) >= 0;
    }

    /* INDENTATIONS */

    byte push();

    byte pop();

    byte pop(byte indent);

    String getIndentation();

    /* CAPTURING */

    /**
     * If the log is capturing log messages.
     *
     * @return The capturing state of the Log
     * @see #capture()
     */
    boolean isCapturing();

    /**
     * Begins capturing log messages.
     * <p>When capturing, all log messages are stored in memory and not printed to the screen. They can either be
     * {@linkplain #release() released} to be printed on the screen or {@linkplain #drop() dropped} to be removed and
     * never printed.</p>
     */
    void capture();

    /**
     * Drops all captured log messages and stops capturing.
     *
     * @see #capture()
     */
    void drop();

    /**
     * Releases all captured log messages (using {@link #log(Level, Object)}), then stops capturing.
     *
     * @see #capture()
     * @see #release(BiConsumer)
     */
    void release();

    /**
     * Releases all captured log messages into the given consumer, then stops capturing.
     *
     * @param consumer The consumer to release the captured log messages to
     * @see #capture()
     */
    void release(BiConsumer<Level, String> consumer);

    /* LOGGING */

    /**
     * Gets the {@linkplain PrintStream print stream} for the given {@linkplain Level level}. If the given level is not
     * {@linkplain #isEnabled(Level) enabled} or is {@code null}, then an empty print stream is returned that ignores
     * all method calls.
     *
     * @param level The level to get the print stream for
     * @return The print stream
     */
    default PrintStream getLog(@Nullable Level level) {
        if (level == null) return AbstractLogger.EMPTY;
        switch (level) {
            case DEBUG: return getDebug();
            case QUIET: return getQuiet();
            case INFO: return getInfo();
            case WARN: return getWarn();
            case ERROR: return getError();
            case FATAL: return getFatal();
        }

        throw new IllegalStateException("Unexpected value: " + level);
    }

    /**
     * Prints an empty message to the given level, similar to {@link PrintStream#println()}.
     *
     * @param level The level to log the message at
     */
    default void log(Level level) {
        getLog(level).println();
    }

    /**
     * Logs a message for the given level.
     *
     * @param level   The level to log the message at
     * @param message The message to log
     */
    default void log(Level level, String message) {
        getLog(level).println(message);
    }

    /**
     * Logs a message for the given level.
     *
     * @param level   The level to log the message at
     * @param message The message to log
     */
    default void log(Level level, Object message) {
        getLog(level).println(message);
    }

    /**
     * Logs the stacktrace of a throwable for the given level.
     *
     * @param level     The level to log the message at
     * @param throwable The throwable to log
     */
    default void log(Level level, Throwable throwable) {
        throwable.printStackTrace(getLog(level));
    }

    /**
     * Logs a message and THEN the stacktrace of a throwable for the given level.
     *
     * @param level     The level to log the message at
     * @param throwable The throwable to log
     */
    default void log(Level level, Object message, Throwable throwable) {
        PrintStream log = getLog(level);
        log.println(message);
        throwable.printStackTrace(log);
    }

    /**
     * Logs a message and THEN the stacktrace of a throwable for the given level.
     *
     * @param level     The level to log the message at
     * @param throwable The throwable to log
     */
    default void log(Level level, String message, Throwable throwable) {
        PrintStream log = getLog(level);
        log.println(message);
        throwable.printStackTrace(log);
    }

    /**
     * Gets the {@linkplain PrintStream print stream} for {@link Level#DEBUG}.
     *
     * @return The print stream
     */
    PrintStream getDebug();

    /**
     * Prints an empty message to the {@link Level#DEBUG DEBUG} level, similar to {@link PrintStream#println()}.
     */
    default void debug() {
        this.getDebug().println();
    }

    /**
     * Logs a message for the {@link Level#DEBUG DEBUG} level.
     *
     * @param message The message to log
     */
    default void debug(String message) {
        this.getDebug().println(message);
    }

    /**
     * Logs a message for the {@link Level#DEBUG DEBUG} level.
     *
     * @param message The message to log
     */
    default void debug(Object message) {
        this.getDebug().println(message);
    }

    /**
     * Logs the stacktrace of a throwable for the {@link Level#DEBUG DEBUG} level.
     *
     * @param throwable The throwable to log
     */
    default void debug(Throwable throwable) {
        throwable.printStackTrace(this.getDebug());
    }

    /**
     * Logs a message and THEN the stacktrace of a throwable for the {@link Level#DEBUG DEBUG} level.
     *
     * @param message   The message to log
     * @param throwable The throwable to log
     */
    default void debug(String message, Throwable throwable) {
        debug(message);
        debug(throwable);
    }

    /**
     * Logs a message and THEN the stacktrace of a throwable for the {@link Level#DEBUG DEBUG} level.
     *
     * @param message   The message to log
     * @param throwable The throwable to log
     */
    default void debug(Object message, Throwable throwable) {
        debug(message);
        debug(throwable);
    }

    /**
     * Gets the {@linkplain PrintStream print stream} for {@link Level#QUIET}.
     *
     * @return The print stream
     */
    PrintStream getQuiet();

    /**
     * Prints an empty message to the {@link Level#QUIET QUIET} level, similar to {@link PrintStream#println()}.
     */
    default void quiet() {
        this.getQuiet().println();
    }

    /**
     * Logs a message for the {@link Level#QUIET QUIET} level.
     *
     * @param message The message to log
     */
    default void quiet(String message) {
        this.getQuiet().println(message);
    }

    /**
     * Logs a message for the {@link Level#QUIET QUIET} level.
     *
     * @param message The message to log
     */
    default void quiet(Object message) {
        this.getQuiet().println(message);
    }

    /**
     * Logs the stacktrace of a throwable for the {@link Level#QUIET QUIET} level.
     *
     * @param throwable The throwable to log
     */
    default void quiet(Throwable throwable) {
        throwable.printStackTrace(this.getQuiet());
    }

    /**
     * Logs a message and THEN the stacktrace of a throwable for the {@link Level#QUIET QUIET} level.
     *
     * @param message   The message to log
     * @param throwable The throwable to log
     */
    default void quiet(String message, Throwable throwable) {
        quiet(message);
        quiet(throwable);
    }

    /**
     * Logs a message and THEN the stacktrace of a throwable for the {@link Level#QUIET QUIET} level.
     *
     * @param message   The message to log
     * @param throwable The throwable to log
     */
    default void quiet(Object message, Throwable throwable) {
        quiet(message);
        quiet(throwable);
    }

    /**
     * Gets the {@linkplain PrintStream print stream} for {@link Level#INFO}.
     *
     * @return The print stream
     */
    PrintStream getInfo();

    /**
     * Prints an empty message to the {@link Level#INFO INFO} level, similar to {@link PrintStream#println()}.
     */
    default void info() {
        this.getInfo().println();
    }

    /**
     * Logs a message for the {@link Level#INFO INFO} level.
     *
     * @param message The message to log
     */
    default void info(String message) {
        this.getInfo().println(message);
    }

    /**
     * Logs a message for the {@link Level#INFO INFO} level.
     *
     * @param message The message to log
     */
    default void info(Object message) {
        this.getInfo().println(message);
    }

    /**
     * Logs the stacktrace of a throwable for the {@link Level#INFO INFO} level.
     *
     * @param throwable The throwable to log
     */
    default void info(Throwable throwable) {
        throwable.printStackTrace(this.getInfo());
    }

    /**
     * Logs a message and THEN the stacktrace of a throwable for the {@link Level#INFO INFO} level.
     *
     * @param message   The message to log
     * @param throwable The throwable to log
     */
    default void info(String message, Throwable throwable) {
        info(message);
        info(throwable);
    }

    /**
     * Logs a message and THEN the stacktrace of a throwable for the {@link Level#INFO INFO} level.
     *
     * @param message   The message to log
     * @param throwable The throwable to log
     */
    default void info(Object message, Throwable throwable) {
        info(message);
        info(throwable);
    }

    /**
     * Gets the {@linkplain PrintStream print stream} for {@link Level#WARN}.
     *
     * @return The print stream
     */
    PrintStream getWarn();

    /**
     * Prints an empty message to the {@link Level#WARN WARN} level, similar to {@link PrintStream#println()}.
     */
    default void warn() {
        this.getWarn().println();
    }

    /**
     * Logs a message for the {@link Level#WARN WARN} level.
     *
     * @param message The message to log
     */
    default void warn(String message) {
        this.getWarn().println(message);
    }

    /**
     * Logs a message for the {@link Level#WARN WARN} level.
     *
     * @param message The message to log
     */
    default void warn(Object message) {
        this.getWarn().println(message);
    }

    /**
     * Logs the stacktrace of a throwable for the {@link Level#WARN WARN} level.
     *
     * @param throwable The throwable to log
     */
    default void warn(Throwable throwable) {
        throwable.printStackTrace(this.getWarn());
    }

    /**
     * Logs a message and THEN the stacktrace of a throwable for the {@link Level#WARN WARN} level.
     *
     * @param message   The message to log
     * @param throwable The throwable to log
     */
    default void warn(String message, Throwable throwable) {
        warn(message);
        warn(throwable);
    }

    /**
     * Logs a message and THEN the stacktrace of a throwable for the {@link Level#WARN WARN} level.
     *
     * @param message   The message to log
     * @param throwable The throwable to log
     */
    default void warn(Object message, Throwable throwable) {
        warn(message);
        warn(throwable);
    }

    /**
     * Gets the {@linkplain PrintStream print stream} for {@link Level#ERROR}.
     *
     * @return The print stream
     */
    PrintStream getError();

    /**
     * Prints an empty message to the {@link Level#ERROR ERROR} level, similar to {@link PrintStream#println()}.
     */
    default void error() {
        this.getError().println();
    }

    /**
     * Logs a message for the {@link Level#ERROR ERROR} level.
     *
     * @param message The message to log
     */
    default void error(String message) {
        this.getError().println(message);
    }

    /**
     * Logs a message for the {@link Level#ERROR ERROR} level.
     *
     * @param message The message to log
     */
    default void error(Object message) {
        this.getError().println(message);
    }

    /**
     * Logs the stacktrace of a throwable for the {@link Level#ERROR ERROR} level.
     *
     * @param throwable The throwable to log
     */
    default void error(Throwable throwable) {
        throwable.printStackTrace(this.getError());
    }

    /**
     * Logs a message and THEN the stacktrace of a throwable for the {@link Level#ERROR ERROR} level.
     *
     * @param message   The message to log
     * @param throwable The throwable to log
     */
    default void error(String message, Throwable throwable) {
        error(message);
        error(throwable);
    }

    /**
     * Logs a message and THEN the stacktrace of a throwable for the {@link Level#ERROR ERROR} level.
     *
     * @param message   The message to log
     * @param throwable The throwable to log
     */
    default void error(Object message, Throwable throwable) {
        error(message);
        error(throwable);
    }

    /**
     * Gets the {@linkplain PrintStream print stream} for {@link Level#FATAL}.
     *
     * @return The print stream
     */
    PrintStream getFatal();

    /**
     * Prints an empty message to the {@link Level#FATAL FATAL} level, similar to {@link PrintStream#println()}.
     */
    default void fatal() {
        this.getFatal().println();
    }

    /**
     * Logs a message for the {@link Level#FATAL FATAL} level.
     *
     * @param message The message to log
     */
    default void fatal(String message) {
        this.getFatal().println(message);
    }

    /**
     * Logs a message for the {@link Level#FATAL FATAL} level.
     *
     * @param message The message to log
     */
    default void fatal(Object message) {
        this.getFatal().println(message);
    }

    /**
     * Logs the stacktrace of a throwable for the {@link Level#FATAL FATAL} level.
     *
     * @param throwable The throwable to log
     */
    default void fatal(Throwable throwable) {
        throwable.printStackTrace(this.getFatal());
    }

    /**
     * Logs a message and THEN the stacktrace of a throwable for the {@link Level#FATAL FATAL} level.
     *
     * @param message   The message to log
     * @param throwable The throwable to log
     */
    default void fatal(String message, Throwable throwable) {
        fatal(message);
        fatal(throwable);
    }

    /**
     * Logs a message and THEN the stacktrace of a throwable for the {@link Level#FATAL FATAL} level.
     *
     * @param message   The message to log
     * @param throwable The throwable to log
     */
    default void fatal(Object message, Throwable throwable) {
        fatal(message);
        fatal(throwable);
    }
}
