/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.data.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import net.minecraftforge.util.os.Arch;
import net.minecraftforge.util.os.OS;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.PatternSyntaxException;

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
    /** The release type, typically `release` or `snapshot` */
    public String type;
    /** The version to inherit values from, Nullable */
    public String inheritsFrom;
    /** Arguments to pass into the new Java process */
    public Arguments arguments;

    public static final class JavaVersion {
        public String component;
        public int majorVersion;
    }

    public List<Lib> getLibs() {
        List<Lib> libs = new ArrayList<>();
        List<OS> DEFAULT = Arrays.asList(OS.WINDOWS, OS.MACOS, OS.LINUX);

        for (Library lib : this.libraries) {
            EnumSet<OS> os = EnumSet.noneOf(OS.class);
            if (lib.rules == null) {
                os.addAll(DEFAULT);
            } else {
                for (Rule rule : lib.rules) {
                    switch (rule.action) {
                        case "allow":
                            if (rule.os == null)
                                os.addAll(DEFAULT);
                            else
                                os.add(rule.os.toOS());
                            break;
                        case "disallow":
                            if (os.isEmpty())
                                os.addAll(DEFAULT);

                            //noinspection DataFlowIssue
                            os.remove(rule.os.toOS());
                            break;
                    }
                }
            }

            libs.add(new Lib(lib.name, lib.downloads == null ? null : lib.downloads.artifact, os.containsAll(DEFAULT) ? EnumSet.noneOf(OS.class) : os, lib));

            if (lib.natives != null && lib.downloads != null && lib.downloads.classifiers != null) {
                for (Entry<String, String> entry : lib.natives.entrySet()) {
                    OS nativeOS = Rule.OS.toOS(entry.getKey());
                    String classifier = entry.getValue();
                    if (classifier.contains("${arch}")) {
                        String bits = Arch.current().name().contains("64") ? "64" : "32";
                        classifier = classifier.replace("${arch}", bits);
                    }
                    LibraryDownload nativeLib = lib.downloads.classifiers.get(classifier);
                    if (nativeLib != null && os.contains(nativeOS))
                        libs.add(new Lib(lib.name + ':' + classifier, nativeLib, EnumSet.of(nativeOS), lib));
                }
            }
        }

        return libs;
    }

    public static final class Lib {
        public final String coord;
        public final LibraryDownload dl;
        public final EnumSet<OS> os;
        public final Library info;

        public Lib(String coord, LibraryDownload dl, EnumSet<OS> os, Library info) {
            this.coord = coord;
            this.dl = dl;
            this.os = os;
            this.info = info;
        }

        public boolean allows(OS os) {
            return this.os.contains(os);
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
        public @Nullable List<Rule> rules;
        /** The rules for extracting from this library, only seen for natives */
        public @Nullable ExtractRules extract;

        public boolean testCurrent() {
            if (rules == null || rules.isEmpty())
                return true;

            boolean allowed = false;
            for (Rule rule : rules) {
                if (rule.testCurrent())
                    allowed = "allowed".equals(rule.action);
            }
            return allowed;
        }
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
        /** The size of the artifact, in bytes. */
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
        /** A list of 'features' of the vanilla launcher.
         *  Currently, all known features are simple booleans, and only used for normal program arguments.
         *  They are typically the 'quick play' features, telling the game to auto-join a multi-player server
         *  or set a custom resolution. Exposed for consumers, it is on them to do anything with this value.
         */
        public @Nullable Map<String, Object> features;

        public static class OS {
            /** The OS family to check, known values: "windows", "linux" and "osx" */
            public @Nullable String name;
            /** The Architecture to check, known values: "x86" */
            public @Nullable String arch;
            /** A min/max version range to check the current OS version against, using naive numerical comparison */
            public @Nullable VersionRange versionRange;
            /** A regex applied to System.getProperty("os.version") */
            public @Nullable String version;

            public static net.minecraftforge.util.os.OS toOS(String name) {
                if (name == null)
                    return net.minecraftforge.util.os.OS.UNKNOWN;

                switch (name) {
                    case "windows":
                        return net.minecraftforge.util.os.OS.WINDOWS;
                    case "linux":
                        return net.minecraftforge.util.os.OS.LINUX;
                    case "osx":
                        return net.minecraftforge.util.os.OS.MACOS;
                    default:
                        return net.minecraftforge.util.os.OS.UNKNOWN;
                }
            }

            public net.minecraftforge.util.os.OS toOS() {
                return toOS(this.name);
            }

            /**
             * Tests the OS, if any, against the current OS, or any of its fallbacks
             * @return True if there is no OS filter, or the OS matches the current OS
             */
            public boolean testOS() {
                return testOS(net.minecraftforge.util.os.OS.current());
            }

            /**
             * Tests the OS, if any, against the target OS, or any of its fallbacks
             * @return True if there is no OS filter, or the OS matches the target OS
             */
            public boolean testOS(net.minecraftforge.util.os.OS target) {
                if (name == null)
                    return true;
                net.minecraftforge.util.os.OS fallback = target;
                net.minecraftforge.util.os.OS os = toOS();
                while (fallback != net.minecraftforge.util.os.OS.UNKNOWN) {
                    if (fallback == os)
                        return true;
                    fallback = fallback.fallback();
                }
                return false;
            }

            /**
             * Tests the architecture, if any, against the current Architecture
             * @return True if the constraint is passed or there is no filter
             */
            public boolean testArch() {
                return testArch(Arch.current());
            }

            /**
             * Tests the architecture, if any, against the specified Architecture
             * @return True if the constraint is passed or there is no filter
             */
            public boolean testArch(Arch target) {
                // Only known case is "x86", but "x64" is logical so support it as well
                if ("x86".equals(arch))
                    return target == Arch.X86 || target == Arch.ARM;
                else if ("x64".equals(arch))
                    return target == Arch.X86_64 || target == Arch.ARM64;
                return true;
            }

            /**
             * Tests the version constraints, if any, against the current OS version
             * @return true if no constraints, or if they matched
             */
            public boolean testVersion() {
                return testVersion(net.minecraftforge.util.os.OS.getCurrentVersion());
            }

            /**
             * Tests the version constraints, if any, against the specified version
             * @param target The version to test
             * @return true if no constraints, or if they matched
             */
            public boolean testVersion(String target) {
                if (versionRange != null && !versionRange.testCurrent())
                    return false;

                if (version != null) {
                    try {
                        if (!target.matches(this.version))
                            return false;
                    } catch (PatternSyntaxException e) {
                        return false;
                    }
                }

                return true;
            }
        }

        public boolean testCurrent() {
            if (os != null) {
                if (!os.testOS() || !os.testArch() || !os.testVersion())
                    return false;
            }

            // We don't have any 'features', so just assume if we were asked for it, we don't have it
            // Only thing I've seem this used for is extra command line arguments for setting the window size, or quick joining a world
            if (features != null && !features.isEmpty())
                return false;

            return true;
        }
    }

    public static class VersionRange {
        public @Nullable String min;
        public @Nullable String max;

        public boolean testCurrent() {
            return test(OS.getCurrentVersion());
        }

        public boolean test(String version) {
            if (min == null && max == null)
                return true;

            int[] current = parse(OS.getCurrentVersion());
            if (min != null) {
                int[] target = parse(min);
                if (compare(current, target) < 0)
                    return false;
            }
            if (max != null) {
                int[] target = parse(max);
                if (compare(current, target) > 1)
                    return false;
            }
            return true;
        }

        /**
         * <pre>
         * There is no format structure for version strings, and there is no explanation from Mojang on how this is actually implemented
         * So I am doing a very nieve approach:
         * Split numbers based on . or -
         * Ignore any non digit (0-9) character.
         *
         * 1.2.3.4 -> {1, 2, 3, 4}
         * 1.2-beta4 -> {1, 2, 4}
         * general-release.6 -> {0, 0, 6}
         *
         * It's possible this can cause weird interactions due to not special casing 'known' versioning and treating words as 0.
         * But as far as I know there are no cases where the value is anything but [\d+.]+ so we should be fine
         * </pre>
         */
        private static int[] parse(String version) {
            List<Integer> values = new ArrayList<Integer>();
            int value = 0;
            for (char c : version.toCharArray()) {
                if (c == '-' || c == '.') {
                    values.add(value);
                    value = 0;
                } else if (c >= '0' && c <= '9') {
                    value = (value * 10) + (c - '0');
                }
            }
            values.add(value);

            int[] ret = new int[values.size()];
            for (int x = 0; x < ret.length; x++)
                ret[x] = values.get(x);
            return ret;
        }

        private static int compare(int[] o1, int[] o2) {
            int min = Math.min(o1.length, o2.length);
            for (int x = 0; x < min; x++) {
                if (o1[x] != o2[x])
                    return o1[x] - o2[x];
            }
            if (o1.length > min) {
                for (int x = min; x < o1.length; x++) {
                    if (o1[x] != 0)
                        return o1[x];
                }
            } else if (o2.length > min) {
                for (int x = min; x < o2.length; x++) {
                    if (o2[x] != 0)
                        return -1 * o2[x];
                }
            }
            return 0;
        }
    }

    public static class ExtractRules {
        public @Nullable List<String> exclude;
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

    public static class Arguments {
        /** JVM Arguments passed to the game process */
        public @Nullable List<Argument> jvm;

        /** Default value of user customizable jvm arguments, this is typically min and max memory, and some performance related flags, appended to JVM args */
        @SerializedName("default-user-jvm")
        public @Nullable List<Argument> defaultUserJvm;

        /** Arguments that the game is meant to parse */
        public @Nullable List<Argument> game;
    }

    public static class Argument {
        public @Nullable List<Rule> rules;
        public List<String> value;

        private Argument(@Nullable List<Rule> rules, List<String> value) {
            this.rules = rules;
            this.value = value;
        }
        
        public boolean testCurrent() {
            if (rules == null || rules.isEmpty())
                return true;

            boolean allowed = false;
            for (Rule rule : rules) {
                if (rule.testCurrent())
                    allowed = "allowed".equals(rule.action);
            }
            return allowed;
        }

        public static class Serializer implements JsonSerializer<Argument>, JsonDeserializer<Argument> {
            private static final TypeToken<List<Rule>> RULES_LIST = new TypeToken<List<Rule>>(){};

            @Override
            public Argument deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString())
                    return new Argument(null, Collections.singletonList(json.getAsJsonPrimitive().getAsString()));

                if (!json.isJsonObject())
                    throw new JsonParseException("Invalid Argument object " + json);

                JsonObject obj = json.getAsJsonObject();
                List<Rule> rules = obj.has("rules") ? context.deserialize(obj.get("rules"), RULES_LIST.getType()) : null;
                List<String> values = new ArrayList<>();
                JsonElement jsonValues = obj.get("value");
                if (jsonValues.isJsonPrimitive() && jsonValues.getAsJsonPrimitive().isString())
                    values.add(jsonValues.getAsJsonPrimitive().getAsString());
                else if (jsonValues.isJsonArray()) {
                    for (JsonElement entry : jsonValues.getAsJsonArray()) {
                        if (entry.isJsonPrimitive() && entry.getAsJsonPrimitive().isString())
                            values.add(entry.getAsJsonPrimitive().getAsString());
                        else
                            throw new JsonParseException("Invalid Argument object " + json);
                    }
                }

                return new Argument(rules, values);
            }

            @Override
            public JsonElement serialize(Argument src, Type typeOfSrc, JsonSerializationContext context) {
                if (src.rules == null && src.value.size() == 1)
                    return new JsonPrimitive(src.value.get(0));

                JsonObject ret = new JsonObject();
                if (src.rules != null)
                    ret.add("rules", context.serialize(src.rules, RULES_LIST.getType()));
                if (src.value.size() == 1)
                    ret.addProperty("value", src.value.get(0));
                else
                    ret.add("value", context.serialize(src.value));
                return ret;
            }
        }
    }
}
