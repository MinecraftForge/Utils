/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.logging;

import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public abstract class AbstractLogger implements Logger {
    /* CREATION */

    protected AbstractLogger() {
        this.tag = "";
    }

    protected AbstractLogger(String tag) {
        this.tag = '[' + tag + "] ";
    }

    /* LOG LEVELS */

    private static @Nullable Level enabled = Level.INFO;

    static @Nullable Level getEnabledImpl() {
        return enabled;
    }

    static void setEnabledImpl(@Nullable Level level) {
        enabled = level;
    }

    static boolean isEnabledImpl(Level level) {
        return enabled != null && level.compareTo(enabled) >= 0;
    }

    /* TAGGING */

    final String tag;

    /* INDENTATIONS */

    private static final String INDENT_STRING = "  ";
    private static final @Nullable String[] INDENT_CACHE = new String[Byte.MAX_VALUE];
    private static byte indentLevel;

    static {
        INDENT_CACHE[0] = "";
        INDENT_CACHE[1] = INDENT_STRING;
    }

    static byte pushImpl() {
        return indentLevel++;
    }

    static byte popImpl() {
        if (--indentLevel < 0)
            throw new IllegalStateException("Cannot pop Log below 0");

        return indentLevel;
    }

    static byte popImpl(byte indent) {
        if (indent < 0)
            throw new IllegalArgumentException("Cannot pop Log below 0");

        return indentLevel = indent;
    }

    final String getIndentationImpl() {
        String ret = INDENT_CACHE[indentLevel];
        return ret == null ? INDENT_CACHE[indentLevel] = getIndentation(indentLevel) : ret;
    }

    private static String getIndentation(byte indent) {
        StringBuilder builder = new StringBuilder(INDENT_STRING.length() * indent);
        for (int i = 0; i < indent; i++)
            builder.append(INDENT_STRING);
        return builder.toString();
    }

    /* CAPTURING */

    private static @Nullable Queue<Map.Entry<Consumer<? super String>, String>> captured;

    static boolean isCapturingImpl() {
        return captured != null;
    }

    static void captureImpl() {
        if (captured != null) return;
        captured = new ConcurrentLinkedQueue<>();
    }

    static void tryCapture(Consumer<? super String> logger, String message) {
        if (captured != null)
            captured.add(new AbstractMap.SimpleImmutableEntry<>(logger, message));
        else
            logger.accept(message);
    }

    static void dropImpl() {
        captured = null;
    }

    static void releaseImpl() {
        if (captured == null) return;

        for (Map.Entry<Consumer<? super String>, String> capture : captured) {
            capture.getKey().accept(capture.getValue());
        }
        captured = null;
    }

    /* LOGGING */

    static final DelegatePrintStream.Empty EMPTY = DelegatePrintStream.EMPTY;
}
