/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.data;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;

// TODO Always warn on unknown OS
/**
 * Enum representing the operating system.
 */
public enum OS {
    AIX    ("aix"),
    /** @apiNote When working with Minecraft natives, this will be treated as {@link #LINUX}. */
    ALPINE ("apline_linux", "alpine"),
    LINUX  ("linux",        "unix"),
    /** @apiNote When working with Minecraft natives, this will be treated as {@link #LINUX}. */
    MUSL   ("linux_musl",   "musl"),
    /** @apiNote When working with Minecraft natives, this is primarily referred to as {@code osx}. */
    MACOS  ("macos",        "mac", "osx", "darwin"),
    QNX    ("qnx"),
    SOLARIS("solaris",      "sunos"),
    WINDOWS("windows",      "win"),
    UNKNOWN("unknown");

    private static final OS[] $values = values();
    /** The operating system that this tool is being run on. */
    public static final OS CURRENT = getCurrent();

    /** The primary name, and enum key, of the operating system. */
    public final String key;
    private final String[] names;

    OS(String... names) {
        this.key = names[0];
        this.names = names;
    }

    /**
     * Returns the OS enum for the given key.
     *
     * @param key The key to search for
     * @return The OS enum, or null if not found
     */
    public static OS byKey(@Nullable String key) {
        for (OS value : $values) {
            if (value.key.equals(key))
                return value;
        }

        return UNKNOWN;
    }

    public static OS byName(@Nullable String name) {
        for (OS value : $values) {
            for (String n : value.names) {
                if (n.equals(name))
                    return value;
            }
        }

        return UNKNOWN;
    }

    /**
     * Returns the mandatory file extension for executables on this OS.
     *
     * @return The file extension
     */
    public String exe() {
        return this == OS.WINDOWS ? ".exe" : "";
    }

    private static OS getCurrent() {
        String prop = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        for (OS os : $values) {
            for (String key : os.names) {
                if (!prop.contains(key)) continue;

                return os == LINUX ? getCurrentLinux() : os;
            }
        }

        return UNKNOWN;
    }

    private static OS getCurrentLinux() {
        try {
            for (String line : Files.readAllLines(Paths.get("/etc/os-release"), StandardCharsets.UTF_8)) {
                line = line.toLowerCase(Locale.ENGLISH);
                if (line.startsWith("name=") && line.contains("alpine")) {
                    return ALPINE;
                }
            }
        } catch (IOException ignored) { }

        return LINUX;
    }
}
