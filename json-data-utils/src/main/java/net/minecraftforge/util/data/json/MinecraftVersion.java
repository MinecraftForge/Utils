/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.data.json;

import net.minecraftforge.util.data.OS;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// TODO: [MCMaven][Documentation][MinecraftVersion] What in here is nullable?
/** Represents a Minecraft version and its artifacts. */
public class MinecraftVersion {
    /** The version id. */
    public String id;
    /** The Java version ({@code null} before 1.17). */
    public JavaVersion javaVersion;
    /** The artifact downloads. */
    public Map<String, Download> downloads;
    /** The required libraries. */
    public Library[] libraries;
    /** The asset index reference. */
    public AssetsIndexInfo assetIndex;

    public static final class JavaVersion {
        public String component;
        public int majorVersion;
    }

    public List<Lib> getLibs() {
        List<Lib> libs = new ArrayList<>();

        for (Library lib : this.libraries) {
            EnumSet<OS> os = EnumSet.noneOf(OS.class);
            if (lib.rules != null) {
                for (Rule rule : lib.rules) {
                    switch (rule.action) {
                        case "allow":
                            if (rule.os == null)
                                os.addAll(Arrays.asList(OS.WINDOWS, OS.MACOS, OS.LINUX));
                            else
                                os.add(rule.os.toOS());
                            break;
                        case "disallow":
                            if (os.isEmpty())
                                os.addAll(Arrays.asList(OS.WINDOWS, OS.MACOS, OS.LINUX));

                            //noinspection DataFlowIssue
                            os.remove(rule.os.toOS());
                            break;
                    }
                }
            }
            libs.add(new Lib(lib.name, lib.downloads.artifact, os));

            if (lib.natives != null && lib.downloads.classifiers != null) {
                lib.natives.forEach((nativeOS, classifier) -> {
                    LibraryDownload nativeLib = lib.downloads.classifiers.get(nativeOS);
                    if (nativeLib != null)
                        libs.add(new Lib(lib.name + ':' + classifier, nativeLib, EnumSet.of(Rule.OS.toOS(nativeOS))));
                });
            }
        }

        return libs;
    }

    public static final class Lib {
        public final String coord;
        public final LibraryDownload dl;
        public final EnumSet<OS> os;

        public Lib(String coord, LibraryDownload dl, EnumSet<OS> os) {
            this.coord = coord;
            this.dl = dl;
            this.os = os;
        }

        public boolean allows(OS os) {
            return this.os.isEmpty() || this.os.contains(os);
        }
    }

    // TODO: [MCMaven][MinecraftVersion] Add function to filter libraries based on current operating systems

    /** Represents a library that is required by a minecraft version. */
    public static class Library {
        /** The artifact coordinates of the library. */
        public String name;
        /** The downloads provided by the library. */
        public Downloads downloads;
        /** The natives. */
        public Map<String, String> natives;
        /** The rules for the library. */
        public List<Rule> rules;
    }

    /** Represents the downloads for a library. */
    public static class Downloads {
        /** The downloadable classifiers for the library. */
        public Map<String, LibraryDownload> classifiers;
        /** The maven artifact of the library. */
        public LibraryDownload artifact;
    }

    /** Represents a downloadable artifact for a Minecraft version. */
    public static class Download {
        /** The SHA1 of the artifact. */
        public String sha1;
        /** The URL of the artifact. */
        public URL url;
        // TODO [MCMaven][Documentation][MinecraftVersion] What is the size? KB? MB? Bytes?
        /** The size of the artifact. */
        public int size;
    }

    /** Represents a downloadable library. */
    public static class LibraryDownload extends Download {
        /** The path of the library. */
        public String path;
    }

    public static class Rule {
        public String action;
        public @Nullable OS os;

        public static class OS {
            public String name;

            public static net.minecraftforge.util.data.OS toOS(String name) {
                switch (name) {
                    case "windows":
                        return net.minecraftforge.util.data.OS.WINDOWS;
                    case "linux":
                        return net.minecraftforge.util.data.OS.LINUX;
                    case "osx":
                        return net.minecraftforge.util.data.OS.MACOS;
                    default:
                        return net.minecraftforge.util.data.OS.UNKNOWN;
                }
            }

            public net.minecraftforge.util.data.OS toOS() {
                return toOS(this.name);
            }
        }
    }

    /**
     * Gets the download for the given key.
     *
     * @param key The key of the download
     * @return The download, or {@code null} if it does not exist
     */
    public @Nullable Download getDownload(String key) {
        return this.downloads == null ? null : this.downloads.get(key);
    }

    public static class AssetsIndexInfo {
        public String id;
        public String sha1;
        public int size;
        public int totalSize;
        public String url;
    }
}
