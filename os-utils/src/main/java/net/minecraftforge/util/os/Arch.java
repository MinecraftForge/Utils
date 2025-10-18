/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.os;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Represents a machine architecture.
 * <p>When in doubt, use {@link #current()} and fall back to {@link #X86_64}. Try to avoid using {@link #UNKNOWN} unless
 * your system
 * supports it.</p>
 *
 * @see OS
 */
public enum Arch {
    /**
     * The 32-bit x86 architecture created by Intel in 1985 and later adopted by AMD that same year.
     * <p>32-bit x86 is widely obsolete. The last version of Windows to use it was Windows 10, and for Java it was Java
     * 23. It has since been superseded by {@linkplain #X86_64 64-bit x86}.</p>
     *
     * @see <a href="https://en.wikipedia.org/wiki/X86">x86 on Wikipedia</a>
     * @see #X86_64
     */
    X86("x86", "i386", "i686"),
    /**
     * The 64-bit x86 architecture created by AMD in 2003 and later adopted by Intel that same year under the name
     * IA-64.
     * <p>64-bit x86 is the most common processor type used in consumer computers. Since its effective deprecation in
     * Windows since the release of Windows 11, where Microsoft dropped support for 32-bit x86, it is now the primary
     * target for most systems and applications.</p>
     *
     * @see <a href="https://en.wikipedia.org/wiki/X86-64">x86-64 (AMD64) on Wikipedia</a>
     */
    X86_64("x86_64", "x64", "amd64"),
    /**
     * The 32-bit Advanced RISC Machines (ARM) architecture created by ARM Architecture in 1985.
     * <p>The most recent implementation of 32-bit ARM is the AArch32 architecture, created with ARMv7. It is mainly
     * used in older or lower end Android phones. It is currently unknown if OpenJDK actively supports this
     * architecture, but there was an effort to maintain an ARM32 port of Java 8.</p>
     * <p>For Java in desktop computers, it has been effectively superseded by {@linkplain #ARM64 64-bit ARM}.</p>
     *
     * @see <a href="https://en.wikipedia.org/wiki/ARM_architecture_family#32-bit_architecture">ARM 32-bit architecture
     * on Wikipedia</a>
     * @see <a href="https://openjdk.org/projects/aarch32-port/">OpenJDK AArch32 Port Project</a>
     * @see #ARM64
     */
    ARM("arm", "aarch32", "aarch_32"),
    /**
     * The 64-bit Advanced RISC Machines (ARM) architecture created by ARM Architecture in 2011 with the introduction of
     * AArch64.
     * <p>64-bit ARM has become a popular computing architecture used in several new consumer computers today. These
     * include the Mac lineup since 2020 beginning with the Apple M1 chip, and now including new Windows on ARM machines
     * such as the Microsoft Surface X.</p>
     *
     * @see <a href="https://en.wikipedia.org/wiki/AArch64">AArch64 on Wikipedia</a>
     * @see <a href="https://en.wikipedia.org/wiki/ARM_architecture_family">ARM architecture family on Wikipedia</a>
     */
    ARM64("arm64", "aarch64", "aarch_64"),
    /**
     * The default enum representing an unknown architecture for when it cannot be determined by this library.
     *
     * @apiNote Proper handling of this enum is your responsibility. No exceptions will be thrown if this enum is
     * returned by any of the methods in this class. The recommendation for consumers is to fall back to {@link #X86_64}
     * due to its widespread use.
     * @see #X86_64
     */
    UNKNOWN("unknown");

    private static final Arch[] $values = values();

    private final String key;
    private final String[] names;

    Arch(String... names) {
        this.key = names[0];
        this.names = names;
    }

    /**
     * The primary name, and enum key, of the machine architecture.
     *
     * @return The primary name
     */
    public @NotNull String key() {
        return this.key;
    }

    private static final class CurrentArch {
        private static final Arch CURRENT;

        static {
            Arch current = UNKNOWN;
            String prop = System.getProperty("os.arch").toLowerCase(Locale.ENGLISH);
            for (Arch os : $values) {
                for (String key : os.names) {
                    if (prop.contains(key)) {
                        current = os;
                        break;
                    }
                }
            }

            CURRENT = current;
        }
    }

    /**
     * Gets the current machine architecture for the JVM running this library.
     *
     * @return The current machine architecture
     * @apiNote If you are using this from Gradle, it is strongly recommended to use this method as the return value of
     * a <a
     * href="https://docs.gradle.org/current/javadoc/org/gradle/api/provider/ValueSource.html">{@code ValueSource}</a>.
     * This will inform Gradle of the required system property call and be able to use it with the configuration cache.
     */
    public static @NotNull Arch current() {
        return CurrentArch.CURRENT;
    }

    /**
     * Returns the machine architecture for the given key.
     * <p>This search strictly checks the key and does not account for aliases.</p>
     *
     * @param key The key to search for
     * @return The machine architecture, or {@link #UNKNOWN} if not found
     */
    public static @NotNull Arch byKey(@Nullable String key) {
        for (Arch value : $values) {
            if (value.key.equalsIgnoreCase(key))
                return value;
        }

        return UNKNOWN;
    }

    /**
     * Returns the machine architecture for the given name, accounting for aliases.
     *
     * @param name The name to search for
     * @return The machine architecture, or {@link #UNKNOWN} if not found
     */
    public static @NotNull Arch byName(@Nullable String name) {
        for (Arch value : $values) {
            for (String n : value.names) {
                if (n.equalsIgnoreCase(name))
                    return value;
            }
        }

        return UNKNOWN;
    }
}
