/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.logging;

import java.io.PrintStream;
import java.util.Locale;

final class EmptyPrintStream extends PrintStream {
    static final EmptyPrintStream INSTANCE = new EmptyPrintStream();

    private EmptyPrintStream() {
        super(System.out);
    }

    //@formatter:off
    @Override public void flush() { }
    @Override public void close() { }
    @Override public boolean checkError() { return false; }
    @Override protected void setError() { }
    @Override protected void clearError() { }
    @Override public void write(int b) { }
    @Override public void write(byte[] b) { }
    @Override public void write(byte[] buf, int off, int len) { }
    @Override public void print(boolean b) { }
    @Override public void print(char c) { }
    @Override public void print(int i) { }
    @Override public void print(long l) { }
    @Override public void print(float f) { }
    @Override public void print(double d) { }
    @Override public void print(char[] s) { }
    @Override public void print(String s) { }
    @Override public void print(Object obj) { }
    @Override public void println() { }
    @Override public void println(boolean x) { }
    @Override public void println(char x) { }
    @Override public void println(int x) { }
    @Override public void println(long x) { }
    @Override public void println(float x) { }
    @Override public void println(double x) { }
    @Override public void println(char[] x) { }
    @Override public void println(String x) { }
    @Override public void println(Object x) { }
    @Override public PrintStream printf(String format, Object... args) { return this; }
    @Override public PrintStream printf(Locale l, String format, Object... args) { return this; }
    @Override public PrintStream format(String format, Object... args) { return this; }
    @Override public PrintStream format(Locale l, String format, Object... args) { return this;}
    @Override public PrintStream append(CharSequence csq) { return this; }
    @Override public PrintStream append(CharSequence csq, int start, int end) { return this; }
    @Override public PrintStream append(char c) { return this; }
    //@formatter:on
}
