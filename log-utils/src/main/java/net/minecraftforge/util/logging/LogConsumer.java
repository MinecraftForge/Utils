/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.logging;

import java.util.function.Consumer;

final class LogConsumer implements Consumer<String> {
    private final AbstractLogger logger;
    private final Logger.Level level;
    private final Consumer<? super String> output;

    LogConsumer(AbstractLogger logger, Logger.Level level, Consumer<? super String> output) {
        this.logger = logger;
        this.level = level;
        this.output = output;
    }

    private boolean isEnabled() {
        return this.logger.isEnabled(this.level);
    }

    @Override
    public void accept(String message) {
        if (!this.isEnabled()) return;

        this.logger.tryCapture(this.output, this.level, message);
    }
}
