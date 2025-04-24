/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.logging;

import java.util.function.Consumer;

final class LogConsumer implements Consumer<String> {
    private final Log.Level level;
    private final Consumer<String> logger;

    LogConsumer(Log.Level level, Consumer<String> logger) {
        this.level = level;
        this.logger = logger;
    }

    private boolean isEnabled() {
        return Log.enabled != null && this.level.compareTo(Log.enabled) >= 0;
    }

    @Override
    public void accept(String s) {
        if (!this.isEnabled()) return;

        Log.tryCapture(this.logger, this.level, s);
    }
}
