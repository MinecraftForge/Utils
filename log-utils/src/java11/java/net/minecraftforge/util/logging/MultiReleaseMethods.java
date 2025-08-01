package net.minecraftforge.util.logging;

import java.util.Map;

/**
 * Utility class for methods that are exclusively used in multi-release form. (Java 11+)
 */
final class MultiReleaseMethods {
    static final String INDENT_STRING = "  ";

    static <K, V> Map.Entry<K, V> mapEntry(K key, V value) {
        return Map.entry(key, value);
    }

    static String getIndentation(byte indent) {
        return INDENT_STRING.repeat(indent);
    }

    private MultiReleaseMethods() { }
}
