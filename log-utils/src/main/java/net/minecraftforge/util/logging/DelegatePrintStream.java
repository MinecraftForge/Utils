/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.logging;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Locale;

final class DelegatePrintStream extends PrintStream {
    private final Log.Level level;
    private final PrintStream delegate;
    private boolean trouble;

    DelegatePrintStream(Log.Level level, PrintStream delegate) {
        super(System.err);
        this.level = level;
        this.delegate = delegate;
    }

    PrintStream getDelegate() {
        return Log.enabled != null && this.level.compareTo(Log.enabled) >= 0 ? this.delegate : EmptyPrintStream.INSTANCE;
    }

    @Override
    public void flush() {
        this.getDelegate().flush();
    }

    @Override
    public void close() {
        this.getDelegate().close();
    }

    @Override
    public boolean checkError() {
        return this.getDelegate().checkError() || this.trouble;
    }

    @Override
    protected void setError() {
        this.trouble = true;
    }

    @Override
    protected void clearError() {
        this.trouble = false;
    }

    @Override
    public void write(int b) {
        this.getDelegate().write(b);
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        this.getDelegate().write(buf, off, len);
    }

    @Override
    public void print(boolean b) {
        this.getDelegate().print(b);
    }

    @Override
    public void print(char c) {
        this.getDelegate().print(c);
    }

    @Override
    public void print(int i) {
        this.getDelegate().print(i);
    }

    @Override
    public void print(long l) {
        this.getDelegate().print(l);
    }

    @Override
    public void print(float f) {
        this.getDelegate().print(f);
    }

    @Override
    public void print(double d) {
        this.getDelegate().print(d);
    }

    @Override
    public void print(char[] s) {
        this.getDelegate().print(s);
    }

    @Override
    public void print(String s) {
        this.getDelegate().print(s);
    }

    @Override
    public void print(Object obj) {
        this.getDelegate().print(obj);
    }

    @Override
    public void println() {
        this.getDelegate().println();
    }

    @Override
    public void println(boolean x) {
        this.getDelegate().println(x);
    }

    @Override
    public void println(char x) {
        this.getDelegate().println(x);
    }

    @Override
    public void println(int x) {
        this.getDelegate().println(x);
    }

    @Override
    public void println(long x) {
        this.getDelegate().println(x);
    }

    @Override
    public void println(float x) {
        this.getDelegate().println(x);
    }

    @Override
    public void println(double x) {
        this.getDelegate().println(x);
    }

    @Override
    public void println(char[] x) {
        this.getDelegate().println(x);
    }

    @Override
    public void println(String x) {
        this.getDelegate().println(x);
    }

    @Override
    public void println(Object x) {
        this.getDelegate().println(x);
    }

    @Override
    public PrintStream printf(String format, Object... args) {
        return this.getDelegate().printf(format, args);
    }

    @Override
    public PrintStream printf(Locale l, String format, Object... args) {
        return this.getDelegate().printf(l, format, args);
    }

    @Override
    public PrintStream format(String format, Object... args) {
        return this.getDelegate().format(format, args);
    }

    @Override
    public PrintStream format(Locale l, String format, Object... args) {
        return this.getDelegate().format(l, format, args);
    }

    @Override
    public PrintStream append(CharSequence csq) {
        return this.getDelegate().append(csq);
    }

    @Override
    public PrintStream append(CharSequence csq, int start, int end) {
        return this.getDelegate().append(csq, start, end);
    }

    @Override
    public PrintStream append(char c) {
        return this.getDelegate().append(c);
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.getDelegate().write(b);
    }

    @Override
    public int hashCode() {
        return this.getDelegate().hashCode();
    }

    @SuppressWarnings("EqualsDoesntCheckParameterClass")
    @Override
    public boolean equals(Object obj) {
        return this.getDelegate().equals(obj);
    }

    @Override
    public String toString() {
        return this.getDelegate().toString();
    }
}
