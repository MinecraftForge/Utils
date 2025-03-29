/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.logging;

import java.io.PrintStream;

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
    /** Whether {@link Level#DEBUG} should be logged to {@link System#out}. */
    public static Level enabled = Level.INFO;

    public static final PrintStream DEBUG = new DelegatePrintStream(Level.DEBUG, System.out);
    public static final PrintStream QUIET = new DelegatePrintStream(Level.QUIET, System.out);
    public static final PrintStream INFO = new DelegatePrintStream(Level.INFO, System.out);
    public static final PrintStream WARN = new DelegatePrintStream(Level.WARN, System.out);
    public static final PrintStream ERROR = new DelegatePrintStream(Level.ERROR, System.err);
    public static final PrintStream FATAL = System.err;

    private static byte indent = 0;

    public static void push() {
        indent++;
    }

    public static void pop() {
        if (--indent < 0)
            throw new IllegalArgumentException("Cannot pop logger below 0");
    }

    public static PrintStream getLog(Level level) {
        return level != null ? level.logger : EmptyPrintStream.INSTANCE;
    }

    public static void log(Level level, String message) {
        PrintStream out = getLog(level);

        if (indent != 0) {
            StringBuilder builder = new StringBuilder(message);
            for (int i = 0; i < indent; i++)
                builder.insert(0, "  ");
            message = builder.toString();
        }

        out.println(message);
    }

    public enum Level {
        DEBUG(Log.DEBUG),
        QUIET(Log.QUIET),
        INFO(Log.INFO),
        WARN(Log.WARN),
        ERROR(Log.ERROR),
        FATAL(Log.FATAL);

        public final PrintStream logger;

        Level(PrintStream logger) {
            this.logger = logger;
        }
    }

    public static void debug(String message) {
        log(Level.DEBUG, message);
    }

    public static void quiet(String message) {
        log(Level.QUIET, message);
    }

    public static void info(String message) {
        log(Level.INFO, message);
    }

    public static void warn(String message) {
        log(Level.WARN, message);
    }

    public static void error(String message) {
        log(Level.ERROR, message);
    }

    public static void fatal(String message) {
        log(Level.FATAL, message);
    }

    private Log() { }
}
