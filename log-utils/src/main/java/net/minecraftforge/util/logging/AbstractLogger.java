/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.logging;

import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class AbstractLogger implements Logger {
    /* CREATION */

    protected AbstractLogger() {
        this(Level.INFO, (byte) 0);
    }

    protected AbstractLogger(byte indentLevel) {
        this(Level.INFO, indentLevel);
    }

    protected AbstractLogger(Level enabled) {
        this(enabled, (byte) 0);
    }

    protected AbstractLogger(Level enabled, byte indentLevel) {
        this.enabled = enabled;
        this.indentLevel = indentLevel;
    }

    /* LOG LEVELS */

    private @Nullable Level enabled;

    @Override
    public final @Nullable Level getEnabled() {
        return this.enabled;
    }

    @Override
    public final void setEnabled(@Nullable Level level) {
        this.enabled = level;
    }

    /* INDENTATIONS */

    private static final String INDENT_STRING = "  ";
    private static final String[] INDENT_CACHE = new String[Byte.MAX_VALUE];
    private byte indentLevel;

    static {
        INDENT_CACHE[0] = "";
        INDENT_CACHE[1] = INDENT_STRING;
    }

    @Override
    public final byte push() {
        return indentLevel++;
    }

    @Override
    public final byte pop() {
        if (--indentLevel < 0)
            throw new IllegalStateException("Cannot pop Log below 0");

        return indentLevel;
    }

    @Override
    public final byte pop(byte indent) {
        if (indent < 0)
            throw new IllegalArgumentException("Cannot pop Log below 0");

        return indentLevel = indent;
    }

    @Override
    public final String getIndentation() {
        String ret = INDENT_CACHE[indentLevel];
        //noinspection ConstantValue -- IntelliJ skill issue
        return ret == null ? INDENT_CACHE[indentLevel] = getIndentation(indentLevel) : ret;
    }

    private static String getIndentation(byte indent) {
        StringBuilder builder = new StringBuilder(INDENT_STRING.length() * indent);
        for (int i = 0; i < indent; i++)
            builder.append(INDENT_STRING);
        return builder.toString();
    }

    /* CAPTURING */

    private @Nullable List<Map.Entry<Level, String>> captured;

    @Override
    public final boolean isCapturing() {
        return captured != null;
    }

    @Override
    public final void capture() {
        if (captured != null) return;
        captured = new ArrayList<>(128);
    }

    final void tryCapture(Consumer<? super String> logger, Level level, String message) {
        if (captured != null)
            captured.add(new AbstractMap.SimpleImmutableEntry<>(level, message));
        else
            logger.accept(message);
    }

    @Override
    public final void drop() {
        captured = null;
    }

    @Override
    public final void release() {
        release(this::logDirectly);
    }

    @Override
    public final void release(BiConsumer<Level, String> consumer) {
        if (captured == null) return;

        Iterator<Map.Entry<Level, String>> itor = captured.iterator();
        captured = null;
        while (itor.hasNext()) {
            Map.Entry<Level, String> capture = itor.next();
            consumer.accept(capture.getKey(), capture.getValue());
        }
    }

    /* LOGGING */

    static final DelegatePrintStream.Empty EMPTY = DelegatePrintStream.EMPTY;

    private void logDirectly(Level level, String message) {
        ((DelegatePrintStream) this.getLog(level)).getDelegate().accept(message);
    }
}
