/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.logging;

import java.io.PrintStream;

final class DefaultLogger extends AbstractLogger {
    DefaultLogger() {
        super();
    }

    DefaultLogger(byte indentLevel) {
        super(indentLevel);
    }

    DefaultLogger(Level enabled) {
        super(enabled);
    }

    DefaultLogger(Level enabled, byte indentLevel) {
        super(enabled, indentLevel);
    }

    private final DelegatePrintStream.Capturing debug = new DelegatePrintStream.Capturing(this, Level.DEBUG, System.out::println);
    private final DelegatePrintStream.Capturing quiet = new DelegatePrintStream.Capturing(this, Level.QUIET, System.out::println);
    private final DelegatePrintStream.Capturing info = new DelegatePrintStream.Capturing(this, Level.INFO, System.out::println);
    private final DelegatePrintStream.Capturing warn = new DelegatePrintStream.Capturing(this, Level.WARN, System.out::println);
    private final DelegatePrintStream.Capturing error = new DelegatePrintStream.Capturing(this, Level.ERROR, System.err::println);
    private final DelegatePrintStream.Capturing fatal = new DelegatePrintStream.Capturing(this, Level.FATAL, System.err::println);

    @Override
    public PrintStream getDebug() {
        return this.debug;
    }

    @Override
    public PrintStream getQuiet() {
        return this.quiet;
    }

    @Override
    public PrintStream getInfo() {
        return this.info;
    }

    @Override
    public PrintStream getWarn() {
        return this.warn;
    }

    @Override
    public PrintStream getError() {
        return this.error;
    }

    @Override
    public PrintStream getFatal() {
        return this.fatal;
    }
}
