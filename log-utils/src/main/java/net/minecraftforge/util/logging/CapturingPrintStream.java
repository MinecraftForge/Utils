/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.logging;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.function.Consumer;

final class CapturingPrintStream extends OutputStream {
    static PrintStream of(Log.Level level, Consumer<String> logger) {
        return new PrintStream(new CapturingPrintStream(new LogConsumer(level, logger)));
    }

    private final Consumer<String> logger;
    private StringBuffer buffer;

    private CapturingPrintStream(Consumer<String> logger) {
        this.logger = logger;
        this.reset();
    }

    private void reset() {
        this.buffer = new StringBuffer(512);
    }

    @Override
    public void write(int b) {
        this.write((char) b);
    }

    private void write(char c) {
        if (c == '\n' || c == '\r') {
            if (this.buffer.length() != 0) {
                logger.accept(this.buffer.insert(0, Log.getIndentation()).toString());
                this.reset();
            }
        } else {
            this.buffer.append(c);
        }
    }
}
