/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.logging;

import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.function.Consumer;

/**
 * A simple logger. Used to wrap loggers or output streams to make logging less of a pain in the ass. Each program that
 * uses Logging Utils should have their own instance of a simple logger.
 *
 * @see SysOut
 */
public interface SimpleLogger {
    /* GLOBAL */

    static SimpleLogger getGlobal() {
        return Util.global;
    }

    static void setGlobal(SimpleLogger logger) {
        Util.global = logger;
    }


    /* INDENTATION */

    void push();

    void pop();

    int getIndent();


    /* LOGGING */

    void log(Level level, String message);

    default void log(Level level, Consumer<? super PrintStream> message) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        message.accept(new PrintStream(os));
        this.log(level, os.toString());
    }

    enum Level {
        TRACE, DEBUG, QUIET, INFO, WARN, ERROR, FATAL
    }

    default void trace(String message) {
        log(Level.TRACE, message);
    }

    default void trace(Consumer<? super PrintStream> message) {
        log(Level.TRACE, message);
    }

    default void debug(String message) {
        log(Level.DEBUG, message);
    }

    default void debug(Consumer<? super PrintStream> message) {
        log(Level.DEBUG, message);
    }

    default void quiet(String message) {
        log(Level.QUIET, message);
    }

    default void quiet(Consumer<? super PrintStream> message) {
        log(Level.QUIET, message);
    }

    default void info(String message) {
        log(Level.INFO, message);
    }

    default void info(Consumer<? super PrintStream> message) {
        log(Level.INFO, message);
    }

    default void warn(String message) {
        log(Level.WARN, message);
    }

    default void warn(Consumer<? super PrintStream> message) {
        log(Level.WARN, message);
    }

    default void error(String message) {
        log(Level.ERROR, message);
    }

    default void error(Consumer<? super PrintStream> message) {
        log(Level.ERROR, message);
    }

    default void fatal(String message) {
        log(Level.FATAL, message);
    }

    default void fatal(Consumer<? super PrintStream> message) {
        log(Level.FATAL, message);
    }

    abstract class Consuming implements SimpleLogger {
        private byte indent;

        @Override
        public void push() {
            this.indent++;
        }

        @Override
        public void pop() {
            if (--this.indent < 0)
                throw new IllegalArgumentException("Cannot pop logger below 0");
        }

        @Override
        public int getIndent() {
            return this.indent;
        }

        protected abstract @Nullable Consumer<String> getOut(Level level);

        @Override
        public void log(Level level, String message) {
            Consumer<String> out = this.getOut(level);
            if (out == null) return;

            if (this.indent != 0) {
                StringBuilder builder = new StringBuilder(message);
                for (int i = 0; i < this.indent; i++)
                    builder.insert(0, "  ");
                message = builder.toString();
            }

            out.accept(message);
        }
    }

    final class SysOut extends Consuming {
        private final boolean debug;
        private final boolean verbose;

        public SysOut() {
            this(false, false);
        }

        public SysOut(boolean debug, boolean verbose) {
            this.debug = debug;
            this.verbose = verbose;
        }

        @Override
        protected @Nullable Consumer<String> getOut(Level level) {
            if (!this.debug && (level == Level.TRACE || level == Level.DEBUG))
                return null;
            else if (!this.verbose && level == Level.QUIET)
                return null;
            else if (level == Level.INFO)
                return System.out::println;
            else
                return System.err::println;
        }
    }
}
