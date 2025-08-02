/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.logging;

import java.util.AbstractMap;
import java.util.Map;

/**
 * Utility class for methods that are exclusively used in multi-release form. (Java 8)
 */
final class MultiReleaseMethods {
    static final String INDENT_STRING = "  ";

    static <K, V> Map.Entry<K, V> mapEntry(K key, V value) {
        return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }

    static String getIndentation(byte indent) {
        StringBuilder builder = new StringBuilder(INDENT_STRING.length() * indent);
        for (int i = 0; i < indent; i++) {
            builder.append(INDENT_STRING);
        }
        return builder.toString();
    }

    private MultiReleaseMethods() { }
}
