/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.logging;

import java.util.function.Consumer;

final class LogConsumer implements Consumer<String> {
    private final Logger.Level level;
    private final Consumer<? super String> output;

    LogConsumer(Logger.Level level, Consumer<? super String> output) {
        this.level = level;
        this.output = output;
    }

    private boolean isEnabled() {
        return AbstractLogger.isEnabledImpl(this.level);
    }

    @Override
    public void accept(String message) {
        if (!this.isEnabled()) return;

        AbstractLogger.tryCapture(this.output, message);
    }
}
