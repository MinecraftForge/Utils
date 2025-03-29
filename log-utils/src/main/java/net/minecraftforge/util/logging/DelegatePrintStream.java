/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.logging;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Locale;

class DelegatePrintStream extends PrintStream {
    static final PrintStream EMPTY = new DelegatePrintStream(null, System.err) {
        @Override
        boolean isEnabled() {
            return false;
        }

        @Override
        public boolean checkError() {
            return false;
        }

        @SuppressWarnings({"unused", "ProtectedMemberInFinalClass", "override", "RedundantSuppression"})
        protected void clearError() { }
    };

    private final Log.Level level;
    private boolean shouldIndent;

    DelegatePrintStream(Log.Level level, PrintStream delegate) {
        super(delegate);
        this.level = level;
    }

    boolean isEnabled() {
        return this.level != null && Log.enabled != null && this.level.compareTo(Log.enabled) >= 0;
    }

    private void printPre() {
        if (!this.shouldIndent) return;

        this.shouldIndent = false;
        super.print(Log.getIndentation());
    }

    private void printPost(boolean newline) {
        if (!newline) return;

        this.shouldIndent = true;
    }


    /* DELEGATE */

    @Override
    public void flush() {
        if (!this.isEnabled()) return;
        super.flush();
    }

    @Override
    public void close() { }

    @Override
    protected void setError() {
        if (!this.isEnabled()) return;
        super.setError();
    }

    @Override
    public void write(int b) {
        if (!this.isEnabled()) return;
        super.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        if (!this.isEnabled()) return;
        super.write(b);
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        if (!this.isEnabled()) return;
        super.write(buf, off, len);
    }

    @Override
    public void print(boolean b) {
        if (!this.isEnabled()) return;
        this.printPre();
        super.print(b);
    }

    @Override
    public void print(char c) {
        this.print(String.valueOf(c));
    }

    @Override
    public void print(int i) {
        this.print(String.valueOf(i));
    }

    @Override
    public void print(long l) {
        this.print(String.valueOf(l));
    }

    @Override
    public void print(float f) {
        this.print(String.valueOf(f));
    }

    @Override
    public void print(double d) {
        this.print(String.valueOf(d));
    }

    @Override
    public void print(char[] s) {
        if (!this.isEnabled()) return;
        super.print(s);

        if (s[s.length - 1] == '\n')
            super.print(Log.getIndentation());
    }

    @Override
    public void print(String s) {
        if (!this.isEnabled()) return;
        this.printPre();
        super.print(s);
        this.printPost(s.indexOf('\n') == s.length() - 1);
    }

    @Override
    public void print(Object obj) {
        this.print(String.valueOf(obj));
    }

    @Override
    public void println() {
        if (!this.isEnabled()) return;
        this.printPre();
        super.println();
        this.printPost(true);
    }

    @Override
    public void println(boolean x) {
        if (!this.isEnabled()) return;
        this.printPre();
        super.println(x);
        this.printPost(true);
    }

    @Override
    public void println(char x) {
        this.println(String.valueOf(x));
    }

    @Override
    public void println(int x) {
        this.println(String.valueOf(x));
    }

    @Override
    public void println(long x) {
        this.println(String.valueOf(x));
    }

    @Override
    public void println(float x) {
        this.println(String.valueOf(x));
    }

    @Override
    public void println(double x) {
        this.println(String.valueOf(x));
    }

    @Override
    public void println(char[] x) {
        if (!this.isEnabled()) return;
        this.printPre();
        super.println(x);
        this.printPost(true);
    }

    @Override
    public void println(String x) {
        if (!this.isEnabled()) return;
        this.printPre();
        super.println(x);
        this.printPost(true);
    }

    @Override
    public void println(Object x) {
        this.println(String.valueOf(x));
    }

    @Override
    public PrintStream printf(String format, Object... args) {
        return this.isEnabled() ? super.printf(format, args) : this;
    }

    @Override
    public PrintStream printf(Locale l, String format, Object... args) {
        return this.isEnabled() ? super.printf(l, format, args) : this;
    }

    @Override
    public PrintStream format(String format, Object... args) {
        return this.isEnabled() ? super.format(format, args) : this;
    }

    @Override
    public PrintStream format(Locale l, String format, Object... args) {
        return this.isEnabled() ? super.format(l, format, args) : this;
    }

    @Override
    public PrintStream append(CharSequence csq) {
        return this.isEnabled() ? super.append(csq) : this;
    }

    @Override
    public PrintStream append(CharSequence csq, int start, int end) {
        if (this.isEnabled()) {
            super.append(csq, start, end);
            if (csq.charAt(end) == '\n')
                super.print(Log.getIndentation());
        }

        return this;
    }

    @Override
    public PrintStream append(char c) {
        return this.isEnabled() ? super.append(c) : this;
    }
}
