/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.logging;

import java.io.OutputStream;
import java.io.PrintStream;

final class EmptyPrintStream extends PrintStream {
    static final PrintStream INSTANCE = new EmptyPrintStream();

    private EmptyPrintStream() {
        super(new OutputStream() {
            @Override
            public void write(int b) { }
        });
    }

    @Override
    public boolean checkError() {
        return false;
    }

    @Override
    protected void clearError() { }
}
