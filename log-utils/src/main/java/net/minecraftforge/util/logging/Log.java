/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.logging;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Logging Utils uses this single static logger to log messages.
 *
 * <h2>Features</h2>
 * <ul>
 *     <li>A single, static logger to be used primarily by top-level CLIs/programs.
 *     <ul>
 *         <li>It is essentially a wrapper around {@link System#out} and {@link System#err}</li>
 *     </ul></li>
 *     <li>The ability to add indentations using {@link #push()} and {@link #pop()}</li>
 *     <li>A simple level system using {@link Level}.</li>
 *     <li>Delegate to a {@link PrintStream} object (for things like {@link Throwable#printStackTrace(PrintStream)}) using {@link #getLog(Level)} or similar.
 *     <ul>
 *         <li>If a level is not being logged (i.e. lower than {@link Log#enabled}), an empty print stream is returned that will ignore all method calls.</li>
 *         <li>Each level's {@link PrintStream} also exists as a public static final field, such as {@link #WARN}.</li>
 *     </ul></li>
 *     <li>No config files, no managers, no bullshit.</li>
 * </ul>
 */
public final class Log {
    /** The lowest level that should be logged. If {@code null}, all logging is completely disabled. */
    public static @Nullable Level enabled = Level.INFO;

    public static final PrintStream EMPTY = DelegatePrintStream.EMPTY;
    /** The stream used for {@link Level#DEBUG}. */
    public static final PrintStream DEBUG = new DelegatePrintStream.Capturing(Level.DEBUG, System.out);
    /** The stream used for {@link Level#QUIET}. */
    public static final PrintStream QUIET = new DelegatePrintStream.Capturing(Level.QUIET, System.out);
    /** The stream used for {@link Level#INFO}. */
    public static final PrintStream INFO = new DelegatePrintStream.Capturing(Level.INFO, System.out);
    /** The stream used for {@link Level#WARN}. */
    public static final PrintStream WARN = new DelegatePrintStream.Capturing(Level.WARN, System.out);
    /** The stream used for {@link Level#ERROR}. */
    public static final PrintStream ERROR = new DelegatePrintStream.Capturing(Level.ERROR, System.err);
    /** The stream used for {@link Level#FATAL}. */
    public static final PrintStream FATAL = new DelegatePrintStream.Capturing(Level.FATAL, System.err);


    /* INDENTATIONS */

    private static final String INDENT_STRING = "  ";
    private static final String[] INDENT_CACHE = new String[Byte.MAX_VALUE];
    private static byte indentLevel = 0;

    static {
        INDENT_CACHE[0] = "";
        INDENT_CACHE[1] = INDENT_STRING;
    }

    /** Pushes the current indentation level up by one. */
    public static byte push() {
        return indentLevel++;
    }

    /**
     * Pops the current indentation level down by one.
     *
     * @throws IllegalStateException If the indentation level is already 0
     */
    public static void pop() {
        if (--indentLevel < 0)
            throw new IllegalStateException("Cannot pop Log below 0");
    }

    public static void pop(byte indent) {
        if (indent < 0)
            throw new IllegalArgumentException("Cannot pop Log below 0");

        indentLevel = indent;
    }

    static String getIndentation() {
        String ret = INDENT_CACHE[indentLevel];
        //noinspection ConstantValue -- IntelliJ skill issue
        return ret == null ? INDENT_CACHE[indentLevel] = MultiReleaseMethods.getIndentation(indentLevel) : ret;
    }


    /* CAPTURING */

    private static @UnknownNullability List<Map.Entry<Level, String>> CAPTURED;

    /**
     * If the log is capturing log messages.
     *
     * @return The capturing state of the Log
     * @see #capture()
     */
    public static boolean isCapturing() {
        return CAPTURED != null;
    }

    /**
     * Begins capturing log messages.
     * <p>When capturing, all log messages are stored in memory and not printed to the screen. They can either be
     * {@linkplain #release() released} to be printed on the screen or {@linkplain #drop() dropped} to be removed and
     * never printed.</p>
     */
    public static void capture() {
        if (CAPTURED != null) return;
        CAPTURED = new ArrayList<>(128);
    }

    static void tryCapture(Consumer<String> logger, Level level, String message) {
        if (CAPTURED != null)
            CAPTURED.add(MultiReleaseMethods.mapEntry(level, message));
        else
            logger.accept(message);
    }

    /**
     * Drops all captured log messages and stops capturing.
     *
     * @see #capture()
     */
    public static void drop() {
        CAPTURED = null;
    }

    /**
     * Releases all captured log messages (using {@link Log#log(Level, Object)}), then stops capturing.
     *
     * @see #capture()
     * @see #release(BiConsumer)
     */
    public static void release() {
        release(Log::logCaptured);
    }

    /**
     * Releases all captured log messages into the given consumer, then stops capturing.
     *
     * @param consumer The consumer to release the captured log messages to. The first input is the {@link Level} of the
     *                 message, the second input is the message itself.
     * @see #capture()
     */
    public static void release(BiConsumer<Level, String> consumer) {
        if (CAPTURED == null) return;

        Iterator<Map.Entry<Level, String>> itor = CAPTURED.iterator();
        CAPTURED = null;
        while (itor.hasNext()) {
            Map.Entry<Level, String> capture = itor.next();
            consumer.accept(capture.getKey(), capture.getValue());
        }
    }

    // so we can use a method reference instead of allocate a lambda
    private static void logCaptured(Level level, String message) {
        ((DelegatePrintStream) Log.getLog(level)).getDelegate().println(message);
    }


    /**
     * Gets the {@linkplain PrintStream print stream} for the given {@linkplain Level level}. If the given level is not
     * {@linkplain #enabled enabled} or is {@code null}, then an empty print stream is returned that ignores all method
     * calls.
     *
     * @param level The level to get the print stream for
     * @return The print stream
     */
    public static PrintStream getLog(@Nullable Level level) {
        if (level == null) return EMPTY;
        switch (level) {
            case DEBUG: return DEBUG;
            case QUIET: return QUIET;
            case INFO: return INFO;
            case WARN: return WARN;
            case ERROR: return ERROR;
            case FATAL: return FATAL;
        }

        throw new IllegalStateException("Unexpected value: " + level);
    }

    /**
     * Prints an empty message to the given level, similar to {@link PrintStream#println()}.
     *
     * @deprecated Directly use the no-args method for the given level instead - e.g. {@link Log#info()} for the
     *             {@link Level#INFO INFO} level.
     *
     * @param level The level to log the message at
     */
    @Deprecated
    public static void log(Level level) {
        getLog(level).println();
    }

    /**
     * Logs a message for the given level.
     *
     * @deprecated Directly use the object arg method for the given level instead - e.g. {@link Log#info(Object)} for
     *             the {@link Level#INFO INFO} level.
     *
     * @param level   The level to log the message at
     * @param message The message to log
     */
    @Deprecated
    public static void log(Level level, Object message) {
        getLog(level).println(message);
    }

    /**
     * Logs the stacktrace of a throwable for the given level.
     *
     * @deprecated Directly use the throwable arg method for the given level instead - e.g. {@link Log#error(Throwable)}
     *             for the {@link Level#ERROR ERROR} level.
     *
     * @param level     The level to log the message at
     * @param throwable The throwable to log
     */
    @Deprecated
    public static void log(Level level, Throwable throwable) {
        throwable.printStackTrace(getLog(level));
    }

    /**
     * Logs a message and THEN the stacktrace of a throwable for the given level.
     *
     * @deprecated Directly use the object and throwable args method for the given level instead - e.g.
     *             {@link Log#error(Object, Throwable)} for the {@link Level#ERROR ERROR} level.
     *
     * @param level     The level to log the message at
     * @param throwable The throwable to log
     */
    @Deprecated
    public static void log(Level level, Object message, Throwable throwable) {
        PrintStream log = getLog(level);
        log.println(message);
        throwable.printStackTrace(log);
    }

    /** Represents an individual logging level. */
    public enum Level {
        DEBUG, QUIET, INFO, WARN, ERROR, FATAL;
    }

    /**
     * Prints an empty message to the {@link Level#DEBUG DEBUG} level, similar to {@link PrintStream#println()}.
     */
    public static void debug() {
        DEBUG.println();
    }

    /**
     * Logs a message for the {@link Level#DEBUG DEBUG} level.
     *
     * @param message The message to log
     */
    public static void debug(String message) {
        DEBUG.println(message);
    }

    /**
     * Logs a message for the {@link Level#DEBUG DEBUG} level.
     *
     * @param message The message to log
     */
    public static void debug(Object message) {
        DEBUG.println(message);
    }

    /**
     * Logs the stacktrace of a throwable for the {@link Level#DEBUG DEBUG} level.
     *
     * @param throwable The throwable to log
     */
    public static void debug(Throwable throwable) {
        throwable.printStackTrace(DEBUG);
    }

    /**
     * Logs a message and THEN the stacktrace of a throwable for the {@link Level#DEBUG DEBUG} level.
     *
     * @param message   The message to log
     * @param throwable The throwable to log
     */
    public static void debug(String message, Throwable throwable) {
        debug(message);
        debug(throwable);
    }

    /**
     * Logs a message and THEN the stacktrace of a throwable for the {@link Level#DEBUG DEBUG} level.
     *
     * @param message   The message to log
     * @param throwable The throwable to log
     */
    public static void debug(Object message, Throwable throwable) {
        debug(message);
        debug(throwable);
    }

    /**
     * Prints an empty message to the {@link Level#QUIET QUIET} level, similar to {@link PrintStream#println()}.
     */
    public static void quiet() {
        QUIET.println();
    }

    /**
     * Logs a message for the {@link Level#QUIET QUIET} level.
     *
     * @param message The message to log
     */
    public static void quiet(String message) {
        QUIET.println(message);
    }

    /**
     * Logs a message for the {@link Level#QUIET QUIET} level.
     *
     * @param message The message to log
     */
    public static void quiet(Object message) {
        QUIET.println(message);
    }

    /**
     * Logs the stacktrace of a throwable for the {@link Level#QUIET QUIET} level.
     *
     * @param throwable The throwable to log
     */
    public static void quiet(Throwable throwable) {
        throwable.printStackTrace(QUIET);
    }

    /**
     * Logs a message and THEN the stacktrace of a throwable for the {@link Level#QUIET QUIET} level.
     *
     * @param message   The message to log
     * @param throwable The throwable to log
     */
    public static void quiet(String message, Throwable throwable) {
        quiet(message);
        quiet(throwable);
    }

    /**
     * Logs a message and THEN the stacktrace of a throwable for the {@link Level#QUIET QUIET} level.
     *
     * @param message   The message to log
     * @param throwable The throwable to log
     */
    public static void quiet(Object message, Throwable throwable) {
        quiet(message);
        quiet(throwable);
    }

    /**
     * Prints an empty message to the {@link Level#INFO INFO} level, similar to {@link PrintStream#println()}.
     */
    public static void info() {
        INFO.println();
    }

    /**
     * Logs a message for the {@link Level#INFO INFO} level.
     *
     * @param message The message to log
     */
    public static void info(String message) {
        INFO.println(message);
    }

    /**
     * Logs a message for the {@link Level#INFO INFO} level.
     *
     * @param message The message to log
     */
    public static void info(Object message) {
        INFO.println(message);
    }

    /**
     * Logs the stacktrace of a throwable for the {@link Level#INFO INFO} level.
     *
     * @param throwable The throwable to log
     */
    public static void info(Throwable throwable) {
        throwable.printStackTrace(INFO);
    }

    /**
     * Logs a message and THEN the stacktrace of a throwable for the {@link Level#INFO INFO} level.
     *
     * @param message   The message to log
     * @param throwable The throwable to log
     */
    public static void info(String message, Throwable throwable) {
        info(message);
        info(throwable);
    }

    /**
     * Logs a message and THEN the stacktrace of a throwable for the {@link Level#INFO INFO} level.
     *
     * @param message   The message to log
     * @param throwable The throwable to log
     */
    public static void info(Object message, Throwable throwable) {
        info(message);
        info(throwable);
    }

    /**
     * Prints an empty message to the {@link Level#WARN WARN} level, similar to {@link PrintStream#println()}.
     */
    public static void warn() {
        WARN.println();
    }

    /**
     * Logs a message for the {@link Level#WARN WARN} level.
     *
     * @param message The message to log
     */
    public static void warn(String message) {
        WARN.println(message);
    }

    /**
     * Logs a message for the {@link Level#WARN WARN} level.
     *
     * @param message The message to log
     */
    public static void warn(Object message) {
        WARN.println(message);
    }

    /**
     * Logs the stacktrace of a throwable for the {@link Level#WARN WARN} level.
     *
     * @param throwable The throwable to log
     */
    public static void warn(Throwable throwable) {
        throwable.printStackTrace(WARN);
    }

    /**
     * Logs a message and THEN the stacktrace of a throwable for the {@link Level#WARN WARN} level.
     *
     * @param message   The message to log
     * @param throwable The throwable to log
     */
    public static void warn(String message, Throwable throwable) {
        warn(message);
        warn(throwable);
    }

    /**
     * Logs a message and THEN the stacktrace of a throwable for the {@link Level#WARN WARN} level.
     *
     * @param message   The message to log
     * @param throwable The throwable to log
     */
    public static void warn(Object message, Throwable throwable) {
        warn(message);
        warn(throwable);
    }

    /**
     * Prints an empty message to the {@link Level#ERROR ERROR} level, similar to {@link PrintStream#println()}.
     */
    public static void error() {
        ERROR.println();
    }

    /**
     * Logs a message for the {@link Level#ERROR ERROR} level.
     *
     * @param message The message to log
     */
    public static void error(String message) {
        ERROR.println(message);
    }

    /**
     * Logs a message for the {@link Level#ERROR ERROR} level.
     *
     * @param message The message to log
     */
    public static void error(Object message) {
        ERROR.println(message);
    }

    /**
     * Logs the stacktrace of a throwable for the {@link Level#ERROR ERROR} level.
     *
     * @param throwable The throwable to log
     */
    public static void error(Throwable throwable) {
        throwable.printStackTrace(ERROR);
    }

    /**
     * Logs a message and THEN the stacktrace of a throwable for the {@link Level#ERROR ERROR} level.
     *
     * @param message   The message to log
     * @param throwable The throwable to log
     */
    public static void error(String message, Throwable throwable) {
        error(message);
        error(throwable);
    }

    /**
     * Logs a message and THEN the stacktrace of a throwable for the {@link Level#ERROR ERROR} level.
     *
     * @param message   The message to log
     * @param throwable The throwable to log
     */
    public static void error(Object message, Throwable throwable) {
        error(message);
        error(throwable);
    }

    /**
     * Prints an empty message to the {@link Level#FATAL FATAL} level, similar to {@link PrintStream#println()}.
     */
    public static void fatal() {
        FATAL.println();
    }

    /**
     * Logs a message for the {@link Level#FATAL FATAL} level.
     *
     * @param message The message to log
     */
    public static void fatal(String message) {
        FATAL.println(message);
    }

    /**
     * Logs a message for the {@link Level#FATAL FATAL} level.
     *
     * @param message The message to log
     */
    public static void fatal(Object message) {
        FATAL.println(message);
    }

    /**
     * Logs the stacktrace of a throwable for the {@link Level#FATAL FATAL} level.
     *
     * @param throwable The throwable to log
     */
    public static void fatal(Throwable throwable) {
        throwable.printStackTrace(FATAL);
    }

    /**
     * Logs a message and THEN the stacktrace of a throwable for the {@link Level#FATAL FATAL} level.
     *
     * @param message   The message to log
     * @param throwable The throwable to log
     */
    public static void fatal(String message, Throwable throwable) {
        fatal(message);
        fatal(throwable);
    }

    /**
     * Logs a message and THEN the stacktrace of a throwable for the {@link Level#FATAL FATAL} level.
     *
     * @param message   The message to log
     * @param throwable The throwable to log
     */
    public static void fatal(Object message, Throwable throwable) {
        fatal(message);
        fatal(throwable);
    }

    private Log() { }
}
