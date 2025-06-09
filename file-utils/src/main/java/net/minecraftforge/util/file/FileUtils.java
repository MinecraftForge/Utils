/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.file;

import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

// TODO remove Jar-related methods? some of them needs SrgUtils
public final class FileUtils {
    private FileUtils() { }

    public static final long ZIPTIME = 628041600000L;
    public static final TimeZone GMT = TimeZone.getTimeZone("GMT");

    public static ZipEntry getStableEntry(String name) {
        return getStableEntry(name, ZIPTIME);
    }

    public static ZipEntry getStableEntry(ZipEntry entry) {
        long time;

        var _default = TimeZone.getDefault();
        try {
            TimeZone.setDefault(GMT);
            time = entry.getTime();
        } finally {
            TimeZone.setDefault(_default);
        }

        return getStableEntry(entry.getName(), time);
    }

    public static ZipEntry getStableEntry(String name, long time) {
        var ret = new ZipEntry(name);

        var _default = TimeZone.getDefault();
        try {
            TimeZone.setDefault(GMT);
            ret.setTime(time);
        } finally {
            TimeZone.setDefault(_default);
        }

        return ret;
    }

    public static JarEntry getStableEntryJar(String name) {
        return getStableEntryJar(name, ZIPTIME);
    }

    public static JarEntry getStableEntryJar(JarEntry entry) {
        long time;

        var _default = TimeZone.getDefault();
        try {
            TimeZone.setDefault(GMT);
            time = entry.getTime();
        } finally {
            TimeZone.setDefault(_default);
        }

        return getStableEntryJar(entry.getName(), time);
    }

    public static JarEntry getStableEntryJar(String name, long time) {
        var ret = new JarEntry(name);

        var _default = TimeZone.getDefault();
        try {
            TimeZone.setDefault(GMT);
            ret.setTime(time);
        } finally {
            TimeZone.setDefault(_default);
        }

        return ret;
    }

    public static List<File> listFiles(File path) {
        return listFiles(path, new ArrayList<>());
    }

    private static List<File> listFiles(File dir, List<File> files) {
        if (!dir.exists())
            return files;

        if (!dir.isDirectory())
            throw new IllegalArgumentException("Path must be directory: " + dir.getAbsolutePath());

        for (var file : Objects.requireNonNull(dir.listFiles(), () -> "Failed to list files in " + dir.getAbsolutePath())) {
            if (file.isDirectory())
                files = listFiles(file, files);
            else
                files.add(file);
        }

        return files;
    }

    public static void ensure(File path) {
        if (!path.isAbsolute())
            path = path.getAbsoluteFile();

        if (!path.exists() && !path.getAbsoluteFile().mkdirs())
            throw new IllegalStateException("Failed to ensure existence of directory: " + path.getAbsolutePath());
    }

    public static void ensureParent(File path) {
        ensure(path.getAbsoluteFile().getParentFile());
    }

    public static void mergeJars(File output, boolean stripSignatures, File... files) throws IOException {
        mergeJars(output, stripSignatures, null, files);
    }

    public static void mergeJars(File output, boolean stripSignatures, @Nullable BiPredicate<File, String> filter, File... files) throws IOException {
        if (output.exists() && !output.delete())
            throw new IllegalStateException("Cannot merge jars, failed to delete existing output jar: " + output.getAbsolutePath());

        ensureParent(output);

        var zips = new ArrayList<ZipFile>();
        try {
            // Find all entries, first one wins. We also use a TreeMap so the output is sorted.
            // JarInputStream requires the manifest to be the first entry, followed by any signature
            // files, so special case them.
            Info manifest = null;
            var signatures = new TreeMap<String, Info>();
            var services = new TreeMap<String, ByteArrayOutputStream>();
            var entries = new TreeMap<String, Info>();

            for (var input : files) {
                var tmp = new ArrayList<Info>();

                if (input.isDirectory()) {
                    var prefix = input.getAbsolutePath();
                    for (var file : FileUtils.listFiles(input)) {
                        var name = file.getAbsolutePath().substring(prefix.length()).replace('\\', '/');

                        if (filter != null && !filter.test(input, name))
                            continue;

                        tmp.add(new Info(name, file));
                    }
                } else {
                    var zip = new ZipFile(input);
                    zips.add(zip);
                    for (var itr = zip.entries(); itr.hasMoreElements(); ) {
                        var entry = itr.nextElement();
                        var name = entry.getName();

                        if (entry.isDirectory() || (filter != null && !filter.test(input, name)))
                            continue;

                        tmp.add(new Info(name, () -> {
                            try {
                                return zip.getInputStream(entry);
                            } catch (IOException e) {
                                return sneak(e);
                            }
                        }));
                    }
                }

                for (var entry : tmp) {
                    if (JarFile.MANIFEST_NAME.equalsIgnoreCase(entry.name)) {
                        if (manifest == null)
                            manifest = entry;
                        // Technically services can be multi-release files, but I don't care. If this comes up in the MC world then i'll care
                    } else if (entry.name.startsWith("META-INF/services/")) {
                        var data = services.get(entry.name);
                        if (data == null) {
                            data = new ByteArrayOutputStream();
                            services.put(entry.name, data);
                        } else
                            data.write('\n');

                        try (var is = entry.stream.get()) {
                            is.transferTo(data);
                        }
                    } else if (isBlockOrSF(entry.name))
                        signatures.putIfAbsent(entry.name, entry);
                    else
                        entries.putIfAbsent(entry.name, entry);
                }
            }

            try (var zos = new ZipOutputStream(new FileOutputStream(output))) {
                if (manifest != null) {
                    if (stripSignatures)
                        writeStrippedManifest(zos, manifest);
                    else
                        writeEntry(zos, manifest);
                }
                if (!stripSignatures) {
                    for (var info : signatures.values())
                        writeEntry(zos, info);
                }
                for (var entry : services.entrySet()) {
                    zos.putNextEntry(FileUtils.getStableEntry(entry.getKey()));
                    zos.write(entry.getValue().toByteArray());
                    zos.closeEntry();
                }
                for (var info : entries.values())
                    writeEntry(zos, info);
            }
        } finally {
            for (var zip : zips) {
                try {
                    zip.close();
                } catch (IOException ignored) { }
            }
        }
    }

    public static File makeJar(File classes, File sourcesDir, List<File> sources, File jar) {
        ensureParent(jar);
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jar))) {
            var classFiles = listFiles(classes);
            classFiles.sort(Comparator.comparing(File::getAbsolutePath));
            makeJarInternal(classes, classFiles, jos);

            sources.sort(Comparator.comparing(File::getAbsolutePath));
            makeJarInternal(sourcesDir, sources, jos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return jar;
    }

    public static void makeZip(File inputDir, File zip) {
        try (var zos = new ZipOutputStream(new FileOutputStream(zip))) {
            for (var file : listFiles(inputDir)) {
                var entryName = inputDir.toPath().relativize(file.toPath()).toString().replace('\\', '/');
                writeEntry(zos, new Info(entryName, file));
            }
        } catch (IOException e) {
            sneak(e);
        }
    }

    private static void makeJarInternal(File dir, List<File> classes, JarOutputStream jos) throws IOException {
        for (var file : classes) {
            String entryName = file.getAbsoluteFile().toString().substring(dir.getAbsolutePath().length() + 1).replace('\\', '/');
            writeEntry(jos, new Info(entryName, file));
        }
    }

    private record Info(String name, Supplier<InputStream> stream) {
        private Info(String name, File file) {
            this(name, () -> {
                try {
                    return new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    return sneak(e);
                }
            });
        }
    }

    /*
    Want a whitelist of original classes from an IMappingsFile from SRGUtils?

    var whitelist = IMappingFile
        .load(mappings).getClasses().stream()
        .map(IMappingFile.IClass::getOriginal)
        .collect(Collectors.toSet());
    FileUtils.splitJar(raw, whitelist, output, slim, stable);
     */
    /**
     * @see <a
     * href="https://github.com/MinecraftForge/ForgeGradle/blob/efa70580314c88192486e4ab089f4b970d5b080c/src/common/java/net/minecraftforge/gradle/common/util/MinecraftRepo.java#L329-L364">
     * MinecraftRepo.splitJar(...) in ForgeGradle 6</a>
     */
    public static void splitJar(File raw, Set<String> whitelist, File output, boolean slim, boolean stable) throws IOException {
        try (var zin = new ZipFile(raw);
             var fos = new FileOutputStream(output);
             var out = new ZipOutputStream(fos)) {

            for (var entries = zin.entries(); entries.hasMoreElements(); ) {
                var entry = entries.nextElement();
                var name = entry.getName();
                if (name.endsWith(".class")) {
                    boolean isNotch = whitelist.contains(name.substring(0, name.length() - 6 /*.class*/));
                    if (slim != isNotch) continue;
                } else {
                    if (slim) continue;
                }

                var _new = getStableEntry(name, stable ? ZIPTIME : 0);
                out.putNextEntry(_new);
                try (InputStream ein = zin.getInputStream(entry)) {
                    ein.transferTo(out);
                }
                out.closeEntry();
            }
        }
//        updateHash(output);
    }

    private static void writeEntry(ZipOutputStream zos, Info file) throws IOException {
        zos.putNextEntry(FileUtils.getStableEntry(file.name));
        file.stream.get().transferTo(zos);
        zos.closeEntry();
    }

    private static void writeStrippedManifest(ZipOutputStream zos, Info file) throws IOException {
        try (var is = file.stream.get()) {
            var manifest = new Manifest(is);
            for (var itr = manifest.getEntries().entrySet().iterator(); itr.hasNext(); ) {
                var section = itr.next();
                for (var sItr = section.getValue().entrySet().iterator(); sItr.hasNext(); ) {
                    var attribute = sItr.next();
                    var key = attribute.getKey().toString().toLowerCase(Locale.ROOT);
                    if (key.endsWith("-digest"))
                        sItr.remove();
                }

                if (section.getValue().isEmpty())
                    itr.remove();
            }
            zos.putNextEntry(FileUtils.getStableEntry(file.name));
            manifest.write(zos);
            zos.closeEntry();
        }
    }

    /** @see sun.security.util.SignatureFileVerifier#isBlockOrSF(String) */
    @SuppressWarnings("JavadocReference")
    public static boolean isBlockOrSF(String path) {
        var s = path.toUpperCase(Locale.ENGLISH);
        if (!s.startsWith("META-INF/")) return false;
        // Copy of sun.security.util.SignatureFileVerifier.isBlockOrSF(String), hasn't really ever changed, but just in case
        return s.endsWith(".SF")
            || s.endsWith(".DSA")
            || s.endsWith(".RSA")
            || s.endsWith(".EC");
    }

    /**
     * Sneakily deletes a file or directory on JVM exit.
     *
     * @param file The file to delete
     */
    public static void deleteOnExit(File file) {
        if (!file.exists()) return;

        try (var walk = Files.walk(file.toPath())) {
            walk.sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEachOrdered(File::deleteOnExit);
        } catch (IOException e) {
            sneak(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <R, E extends Throwable> R sneak(Throwable t) throws E {
        throw (E) t;
    }
}
