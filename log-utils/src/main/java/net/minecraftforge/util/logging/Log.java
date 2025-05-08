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
        return ret == null ? INDENT_CACHE[indentLevel] = getIndentation(indentLevel) : ret;
    }

    private static String getIndentation(byte indent) {
        StringBuilder builder = new StringBuilder(INDENT_STRING.length() * indent);
        for (int i = 0; i < indent; i++)
            builder.append(INDENT_STRING);
        return builder.toString();
    }


    /* CAPTURING */

    static @UnknownNullability List<CapturedMessage> CAPTURED;

    private static final class CapturedMessage {
        private final Level level;
        private final String message;

        private CapturedMessage(Level level, String message) {
            this.level = level;
            this.message = message;
        }
    }

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
            CAPTURED.add(new CapturedMessage(level, message));
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
     * @param consumer The consumer to release the captured log messages to
     * @see #capture()
     */
    public static void release(BiConsumer<Level, String> consumer) {
        if (CAPTURED == null) return;

        Iterator<CapturedMessage> itor = CAPTURED.iterator();
        CAPTURED = null;
        while (itor.hasNext()) {
            CapturedMessage capture = itor.next();
            consumer.accept(capture.level, capture.message);
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
     * @param level The level to log the message at
     */
    public static void log(Level level) {
        getLog(level).println();
    }

    /**
     * Logs a message for the given level.
     *
     * @param level   The level to log the message at
     * @param message The message to log
     */
    public static void log(Level level, Object message) {
        getLog(level).println(message);
    }

    /**
     * Logs the stacktrace of a throwable for the given level.
     *
     * @param level     The level to log the message at
     * @param throwable The throwable to log
     */
    public static void log(Level level, Throwable throwable) {
        throwable.printStackTrace(getLog(level));
    }

    /**
     * Logs a message and THEN the stacktrace of a throwable for the given level.
     *
     * @param level     The level to log the message at
     * @param throwable The throwable to log
     */
    public static void log(Level level, Object message, Throwable throwable) {
        log(level, message);
        log(level, throwable);
    }

    /** Represents an individual logging level. */
    public enum Level {
        DEBUG, QUIET, INFO, WARN, ERROR, FATAL;
    }

    public static void debug() {
        log(Level.DEBUG);
    }

    public static void debug(Object message) {
        log(Level.DEBUG, message);
    }

    public static void debug(Throwable throwable) {
        log(Level.DEBUG, throwable);
    }

    public static void debug(Object message, Throwable throwable) {
        log(Level.DEBUG, message, throwable);
    }

    public static void quiet() {
        log(Level.QUIET);
    }

    public static void quiet(Object message) {
        log(Level.QUIET, message);
    }

    public static void quiet(Throwable throwable) {
        log(Level.QUIET, throwable);
    }

    public static void quiet(Object message, Throwable throwable) {
        log(Level.QUIET, message, throwable);
    }

    public static void info() {
        log(Level.INFO);
    }

    public static void info(Object message) {
        log(Level.INFO, message);
    }

    public static void info(Throwable throwable) {
        log(Level.INFO, throwable);
    }

    public static void info(Object message, Throwable throwable) {
        log(Level.INFO, message, throwable);
    }

    public static void warn() {
        log(Level.WARN);
    }

    public static void warn(Object message) {
        log(Level.WARN, message);
    }

    public static void warn(Throwable throwable) {
        log(Level.WARN, throwable);
    }

    public static void warn(Object message, Throwable throwable) {
        log(Level.WARN, message, throwable);
    }

    public static void error() {
        log(Level.ERROR);
    }

    public static void error(Object message) {
        log(Level.ERROR, message);
    }

    public static void error(Throwable throwable) {
        log(Level.ERROR, throwable);
    }

    public static void error(Object message, Throwable throwable) {
        log(Level.ERROR, message, throwable);
    }

    public static void fatal() {
        log(Level.FATAL);
    }

    public static void fatal(Object message) {
        log(Level.FATAL, message);
    }

    public static void fatal(Throwable throwable) {
        log(Level.FATAL, throwable);
    }

    public static void fatal(Object message, Throwable throwable) {
        log(Level.FATAL, message, throwable);
    }

    private Log() { }
}
