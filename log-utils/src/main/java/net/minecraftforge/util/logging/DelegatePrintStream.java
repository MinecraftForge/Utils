/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.logging;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.function.Consumer;

interface DelegatePrintStream {
    DelegatePrintStream.Empty EMPTY = new Empty();

    Consumer<? super String> getDelegate();

    final class Capturing extends PrintStream implements DelegatePrintStream {
        private final Consumer<? super String> delegate;

        public Capturing(AbstractLogger logger, Logger.Level level, Consumer<? super String> output) {
            super(new OutputStream() {
                private final Consumer<? super String> consumer = new LogConsumer(level, output);
                private StringBuffer buffer = new StringBuffer(512);
                private char last = 0;

                @Override
                public void write(int b) {
                    this.write((char) b);
                }

                private void write(char c) {
                    if (c == '\n' || c == '\r') {
                        // Prevent \r\n (windows) from making two lines
                        if (last != '\r') {
                            consumer.accept(this.buffer.insert(0, logger.getIndentationImpl() + logger.tag).toString());
                            this.buffer = new StringBuffer(512);
                        }
                    } else {
                        this.buffer.append(c);
                    }
                    this.last = c;
                }
            });

            this.delegate = output;
        }

        @Override
        public Consumer<? super String> getDelegate() {
            return this.delegate;
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
        public Consumer<? super String> getDelegate() {
            return s -> {};
        }
    }
}
