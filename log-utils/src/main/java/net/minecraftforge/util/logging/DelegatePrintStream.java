/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.logging;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.function.Consumer;

interface DelegatePrintStream {
    PrintStream EMPTY = new Empty();

    PrintStream getDelegate();

    final class Capturing extends PrintStream implements DelegatePrintStream {
        private final PrintStream raw;

        public Capturing(Log.Level level, PrintStream stream) {
            super(new OutputStream() {
                private final Consumer<String> logger = new LogConsumer(level, stream::println);
                private StringBuffer buffer = new StringBuffer(512);

                @Override
                public void write(int b) {
                    this.write((char) b);
                }

                private void write(char c) {
                    if (c == '\n' || c == '\r') {
                        if (this.buffer.length() != 0) {
                            logger.accept(this.buffer.insert(0, Log.getIndentation()).toString());
                            this.buffer = new StringBuffer(512);
                        }
                    } else {
                        this.buffer.append(c);
                    }
                }
            });

            this.raw = stream;
        }

        @Override
        public PrintStream getDelegate() {
            return this.raw;
        }
    }

    final class Empty extends PrintStream implements DelegatePrintStream {
        private Empty() {
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

        @Override
        public PrintStream getDelegate() {
            return this;
        }
    }
}
