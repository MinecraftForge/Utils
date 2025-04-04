/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.logging;

import java.io.PrintStream;
import java.util.EnumMap;

import static net.minecraftforge.util.logging.DelegatePrintStream.EMPTY;

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
    public static Level enabled = Level.INFO;

    @SuppressWarnings({"unchecked", "rawtypes"}) // Java 5 restrictions
    private static final EnumMap<Level, PrintStream> STREAMS = new EnumMap(Level.class);

    /** The stream used for {@link Level#DEBUG}. */
    public static final PrintStream DEBUG = streamOf(Level.DEBUG, System.out);
    /** The stream used for {@link Level#QUIET}. */
    public static final PrintStream QUIET = streamOf(Level.QUIET, System.out);
    /** The stream used for {@link Level#INFO}. */
    public static final PrintStream INFO = streamOf(Level.INFO, System.out);
    /** The stream used for {@link Level#WARN}. */
    public static final PrintStream WARN = streamOf(Level.WARN, System.out);
    /** The stream used for {@link Level#ERROR}. */
    public static final PrintStream ERROR = streamOf(Level.ERROR, System.err);
    /** The stream used for {@link Level#FATAL}. */
    public static final PrintStream FATAL = streamOf(Level.FATAL, System.err);

    private static PrintStream streamOf(Level level, PrintStream delegate) {
        PrintStream ret = new DelegatePrintStream(level, delegate);
        STREAMS.put(level, ret);
        return ret;
    }

    private static final String INDENT_STRING = "  ";
    private static byte indent = 0;

    /** Pushes the current indentation level up by one. */
    public static void push() {
        indent++;
    }

    /**
     * Pops the current indentation level down by one.
     *
     * @throws IllegalStateException If the indentation level is already 0
     */
    public static void pop() {
        if (--indent < 0)
            throw new IllegalStateException("Cannot pop logger below 0");
    }

    static String getIndentation() {
        if (indent == 0) return "";

        StringBuilder builder = new StringBuilder(INDENT_STRING.length() * indent);
        for (int i = 0; i < indent; i++)
            builder.append(INDENT_STRING);
        return builder.toString();
    }

    /**
     * Gets the {@linkplain PrintStream print stream} for the given {@linkplain Level level}. If the given level is not
     * {@linkplain #enabled enabled} or is {@code null}, then an empty print stream is returned that ignores all method
     * calls.
     *
     * @param level The level to get the print stream for
     * @return The print stream
     */
    public static PrintStream getLog(Level level) {
        return level != null ? STREAMS.get(level) : EMPTY;
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

    public static void debug(Object message) {
        log(Level.DEBUG, message);
    }

    public static void debug(Throwable throwable) {
        log(Level.DEBUG, throwable);
    }

    public static void debug(Object message, Throwable throwable) {
        log(Level.DEBUG, message, throwable);
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

    public static void info(Object message) {
        log(Level.INFO, message);
    }

    public static void info(Throwable throwable) {
        log(Level.INFO, throwable);
    }

    public static void info(Object message, Throwable throwable) {
        log(Level.INFO, message, throwable);
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

    public static void error(Object message) {
        log(Level.ERROR, message);
    }

    public static void error(Throwable throwable) {
        log(Level.ERROR, throwable);
    }

    public static void error(Object message, Throwable throwable) {
        log(Level.ERROR, message, throwable);
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
