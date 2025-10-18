/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.os;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Scanner;

/**
 * Represents an operating system.
 *
 * <p>When in doubt, use {@link #current()} and fall back to {@link #LINUX}. Try to avoid using {@link #UNKNOWN} unless
 * your system supports it.</p>
 *
 * @see Arch
 */
public enum OS {
    /**
     * Linux is a family of (typically open-source) UNIX-based operating systems based on the Linux kernel, developed by
     * Linus Torvalds since 1991.
     *
     * @implNote This entry specifically denotes Linux using glibc.
     * @see #MUSL
     */
    LINUX("linux", "unix"),
    /**
     * musl is a C standard library, much like glibc, intended for {@linkplain #LINUX Linux}-based operating systems.
     * <p>As the JDK uses the C standard library in its implementation, it distributes specially built binaries that
     * are linked to use musl.</p>
     *
     * @apiNote Falling back to {@link #LINUX} is recommended if your system does not directly support it.
     * @see <a href="https://en.wikipedia.org/wiki/Musl">musl on Wikipedia</a>
     * @see #LINUX
     */
    MUSL(LINUX, "linux_musl", "musl"),
    /**
     * AIX is a proprietary UNIX-based operating system developed by IBM since 1986.
     *
     * @apiNote Since this operating system is based on UNIX, falling back to {@link #LINUX} is recommended if your
     * systemdoes not directly support it.
     * @see <a href="https://en.wikipedia.org/wiki/IBM_AIX">IBM AIX on Wikipedia</a>
     * @see #LINUX
     */
    AIX(LINUX, "aix"),
    /**
     * Alpine Linux is an open-source Linux distribution developed since 2005.
     * <p>Its main difference from standard Linux is that it uses musl, BusyBox, and OpenRC as opposed to glibc, GNU
     * Core Utilities, and systemd. It is for this reason that OpenJDK distributes specially built binaries for Alpine
     * Linux.</p>
     *
     * @apiNote Falling back to {@link #MUSL} is recommended if your system does not directly support it.
     * @see <a href="https://en.wikipedia.org/wiki/Alpine_Linux">Alpine Linux on Wikipedia</a>
     * @see #LINUX
     */
    ALPINE(MUSL, "apline_linux", "alpine"),
    /**
     * macOS (formally OS X) is a proprietary Darwin UNIX-based operating system developed by Apple since 2001.
     *
     * @apiNote Since this operating system is based on UNIX, falling back to {@link #LINUX} is recommended if your
     * system does not directly support it.
     * @see <a href="https://en.wikipedia.org/wiki/MacOS">macOS on Wikipedia</a>
     * @see #LINUX
     */
    MACOS(LINUX, "macos", "mac", "osx", "os x", "darwin"),
    /**
     * QNX is a proprietary UNIX-like operating system for embedded systems created by QNX Software Systems in 1982 and
     * acquired by BlackBerry Limited in 2011.
     *
     * @apiNote Since this operating system is based on UNIX, falling back to {@link #LINUX} is recommended if your
     * system does not directly support it.
     * @see <a href="https://en.wikipedia.org/wiki/QNX">QNX on Wikipedia</a>
     * @see #LINUX
     */
    QNX(LINUX, "qnx"),
    /**
     * Solaris is a proprietary UNIX-based operating system for servers and workstations created by Sun Microsystems in
     * 1993 and acquired by Oracle in 2010.
     *
     * @apiNote Since this operating system is based on UNIX, falling back to {@link #LINUX} is recommended if your
     * system does not directly support it.
     * @see <a href="https://en.wikipedia.org/wiki/Oracle_Solaris">Oracle Solaris on Wikipedia</a>
     * @see #LINUX
     */
    SOLARIS(LINUX, "solaris", "sunos"),
    /**
     * Windows is a proprietary operating system developed by Microsoft since 1985. Originally based on MS-DOS, it is
     * now based on Windows NT since 1993.
     *
     * @see <a href="https://en.wikipedia.org/wiki/Microsoft_Windows">Microsoft Windows on Wikipedia</a>
     */
    WINDOWS("windows", "win"),
    /**
     * The default enum representing an unknown operating system for when it cannot be determined by this library.
     *
     * @apiNote Proper handling of this enum is your responsibility. No exceptions will be thrown if this enum is
     * returned by any of the methods in this class. The recommendation for consumers is to fall back to {@link #LINUX}
     * due to its widespread use in servers or {@link #WINDOWS} if your system actively targets desktop computers.
     * @see #LINUX
     * @see #WINDOWS
     */
    UNKNOWN("unknown");

    private static final OS[] $values = values();

    private final @Nullable OS fallback;
    private final String key;
    private final String[] names;
    private final String exe;

    OS(String... names) {
        this(null, names);
    }

    OS(OS fallback, String... names) {
        this.fallback = fallback;
        this.key = names[0];
        this.names = names;
        this.exe = "windows".equals(this.key) ? ".exe" : "";
    }

    public @NotNull OS fallback() {
        return this.fallback == null ? UNKNOWN : this.fallback;
    }

    /**
     * The primary name, and enum key, of the operating system.
     *
     * @return The primary name
     */
    public @NotNull String key() {
        return this.key;
    }

    /**
     * Returns the mandatory file extension for executables on this operating system.
     *
     * @return The file extension
     */
    public @NotNull String exe() {
        return this.exe;
    }

    private static final class CurrentOS {
        private static final OS CURRENT;

        static {
            OS current = UNKNOWN;
            String prop = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
            for (OS os : $values) {
                for (String key : os.names) {
                    if (!prop.contains(key)) continue;

                    current = os == LINUX ? getCurrentLinux() : os;
                    break;
                }
            }

            CURRENT = current;
        }
    }

    /**
     * Gets the current operating system for the JVM running this library.
     *
     * @return The current operating system
     * @apiNote If you are using this from Gradle, it is strongly recommended to use this method as the return value of
     * a <a
     * href="https://docs.gradle.org/current/javadoc/org/gradle/api/provider/ValueSource.html">{@code ValueSource}</a>.
     * This will inform Gradle of the required system property call and be able to use it with the configuration cache.
     */
    public static @NotNull OS current() {
        return CurrentOS.CURRENT;
    }

    /**
     * Gets the current operating system for the JVM running this library, returning {@link #UNKNOWN} if it is not
     * included in the given allowed operating systems.
     * <p>If the current OS has a {@link #fallback()} OS that is in the given allowed operating systems, that fallback
     * will be returned instead (ex. on a {@link #MUSL} system, if only {@link #LINUX} is passed into this method, then
     * {@link #LINUX} will be returned).</p>
     * <p>Please see {@link #current()} for important implementation and API notes.</p>
     *
     * @param allowed The allowed operating systems
     * @return The current operating system
     * @see #current()
     */
    public static @NotNull OS current(OS... allowed) {
        return current(current(), allowed);
    }

    private static @NotNull OS current(OS current, OS... allowed) {
        if (current == UNKNOWN)
            return UNKNOWN;

        for (OS os : allowed) {
            if (os == current)
                return current;
        }

        return current(current.fallback(), allowed);
    }

    /**
     * Returns the operating system for the given key.
     * <p>This search strictly checks the key and does not account for aliases.</p>
     *
     * @param key The key to search for
     * @return The operating system, or {@link #UNKNOWN} if not found
     */
    public static @NotNull OS byKey(@Nullable String key) {
        for (OS value : $values) {
            if (value.key.equals(key))
                return value;
        }

        return UNKNOWN;
    }

    /**
     * Returns the operating system for the given name, accounting for aliases.
     *
     * @param name The name to search for
     * @return The operating system, or {@link #UNKNOWN} if not found
     */
    public static @NotNull OS byName(@Nullable String name) {
        for (OS value : $values) {
            for (String n : value.names) {
                if (n.equals(name))
                    return value;
            }
        }

        return UNKNOWN;
    }

    private static OS getCurrentLinux() {
        Scanner scanner = null;

        try {
            scanner = new Scanner(new File("/etc/os-release"), "UTF-8");

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().toLowerCase(Locale.ENGLISH);
                if (line.startsWith("name=") && line.contains("alpine"))
                    return ALPINE;
            }
        } catch (IOException ignored) {
            // Assume any linux if we can't determine the OS from the file
        } finally {
            if (scanner != null)
                scanner.close();
        }

        return LINUX;
    }
}
