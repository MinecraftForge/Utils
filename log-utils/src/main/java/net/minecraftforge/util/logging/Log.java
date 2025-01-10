/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.logging;

/**
 * Simple logging utility. Covers logging for the entire program.
 */
public final class Log {
    public static boolean debug = false;

    private static int indent = 0;

    /** Increase the indent level. */
    public static void push() {
        indent++;
    }

    /** Decreases the indent level. */
    public static void pop() {
        indent--;
    }

    private static void indent() {
        for (int x = 0; x < indent; x++)
            System.out.print("  ");
    }

    /** Logs a message, presumably at the {@code INFO} level. */
    public static void log(String message) {
        indent();
        System.out.println(message);
    }

    /** Logs a message at the {@code ERROR} level. */
    public static void error(String message) {
        indent();
        System.err.println(message);
    }

    /** Logs a message at the {@code DEBUG} level. */
    public static void debug(String message) {
        if (!debug) return;

        indent();
        System.out.println(message);
    }
}
