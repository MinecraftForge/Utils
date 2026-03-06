/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.hash;

import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import static net.minecraftforge.util.hash.HashUtils.sneak;

@NotNullByDefault
public class HashStore {
    private static final HashFunction HASH = HashFunction.sha1();
    private final String root;
    private final HashMap<String, String> oldHashes = new HashMap<>();
    private final HashMap<String, String> newHashes = new HashMap<>();
    private @Nullable File target;
    private boolean saved;
    private boolean timestamps = false;

    public static HashStore fromFile(File path) {
        File parent = path.getAbsoluteFile().getParentFile();
        return new HashStore(parent)
            .load(new File(parent, path.getName() + ".cache"));
    }

    public static HashStore fromDir(File path) {
        return new HashStore(path)
            .load(new File(path, ".cache"));
    }

    public HashStore() {
        this("");
    }

    public HashStore(File root) {
        this(root.getAbsolutePath());
    }

    private HashStore(String root) {
        this.root = root;
    }

    /**
     * Use file size and last-modified timestamps instead of hashing the contents of files.
     * Note: This stores the file size and last modified time, which has a resolution of 1ms on most operating systems.
     * So it IS possible for this to not properly detect changes if the file is modified, but the timestamp is kept the same.
     */
    public HashStore timestamps() {
        return timestamps(true);
    }

    /**
     * Sets wither to use file size and last-modified timestamps or hashing the contents of files.
     * Note: This stores the file size and last modified time, which has a resolution of 1ms on most operating systems.
     * So it IS possible for this to not properly detect changes if the file is modified, but the timestamp is kept the same.
     */
    public HashStore timestamps(boolean value) {
        this.timestamps = value;
        return this;
    }

    public boolean areSame(File... files) {
        for (File file : files) {
            if (!isSame(file))
                return false;
        }
        return true;
    }

    public boolean areSame(Iterable<File> files) {
        for (File file : files) {
            if (!isSame(file))
                return false;
        }
        return true;
    }

    public boolean isSame(File file) {
        try {
            String path = getPath(file);
            String hash = oldHashes.get(path);
            if (hash == null) {
                if (file.exists()) {
                    newHashes.put(path, HASH.hash(file));
                    return false;
                }
                return true;
            }

            String fileHash = HASH.hash(file);
            newHashes.put(path, fileHash);
            return fileHash.equals(hash);
        } catch (IOException e) {
            return sneak(e);
        }
    }

    public HashStore load(File file) {
        this.target = file;
        oldHashes.clear();
        if (!file.exists())
            return this;

        try {
            for (String line : Files.readAllLines(file.toPath())) {
                String[] split = line.split("=", 2);
                oldHashes.put(split[0], split[1]);
            }
        } catch (IOException ignored) {
            // TODO [HashUtils] Make a way to properly inform consumer on cache miss
            //System.err.println("Failed to read cache file. It will be ignored. : " + e.getMessage());
        }

        return this;
    }

    public boolean isLoaded() {
        return this.target != null;
    }

    public boolean exists() {
        return this.target != null && this.target.exists();
    }

    public HashStore addKnown(String key, String data) {
        if (!data.isEmpty())
            newHashes.put(Objects.requireNonNull(key), data);
        return this;
    }

    public HashStore add(String key, String data) {
        if (!data.isEmpty())
            newHashes.put(Objects.requireNonNull(key), HASH.hash(data));
        return this;
    }

    public HashStore add(String key, byte[] data) {
        if (data.length > 0)
            this.newHashes.put(Objects.requireNonNull(key), HASH.hash(data));
        return this;
    }

    public HashStore add(@Nullable String key, File file) {
        if (!file.exists()) return this;

        try {
            if (key == null)
                key = getPath(file);

            if (file.isDirectory()) {
                String prefix = getPath(file);
                for (File f : listFiles(file)) {
                    String suffix = getPath(f).substring(prefix.length());
                    addFile(key + " - " + suffix, f);
                }
            } else {
                addFile(key, file);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    private void addFile(String key, File file) throws IOException {
        String value;
        if (this.timestamps) {
            // Use this over file.lastModified() so we get IOExceptions if the file doesn't exist
            FileTime lastModified = Files.getLastModifiedTime(file.toPath());
            value = file.length() + "-" + lastModified.toMillis();
        } else
            value = HASH.hash(file);
        this.newHashes.put(key, value);
    }

    public HashStore add(File... files) {
        for (File file : files)
            add(null, file);
        return this;
    }

    public HashStore add(Iterable<File> files) {
        for (File file : files)
            add(null, file);
        return this;
    }

    public HashStore add(File file) {
        add(null, file);
        return this;
    }

    public boolean isSame() {
        return this.oldHashes.equals(this.newHashes);
    }

    /** Clears the new hashes, does not clear the old hashes read from {@link #load(File)}. */
    public HashStore clear() {
        this.newHashes.clear();
        return this;
    }

    /** Clears the old hashes loaded from {@link #load(File) load}, {@link #fromFile(File) fromFile}, or {@link #fromDir(File) fromDir} */
    public HashStore invalidate() {
        return invalidate(true);
    }

    /**
     * Conditionally Clears the old hashes loaded from {@link #load(File) load}, {@link #fromFile(File) fromFile}, or {@link #fromDir(File) fromDir}.
     * This is meant as a builder type helper
     */
    public HashStore invalidate(boolean value) {
        if (value)
            this.oldHashes.clear();
        return this;
    }

    public void save() {
        if (this.target == null)
            throw new RuntimeException("HashStore.save() called without load(File) so we dont know where to save it! Use load(File) or save(File)");
        save(this.target);
    }

    /**
     * Returns a String representation of the old hashes, as if they were written to disc.
     * This is exposed for debugging purposes only
     */
    public String dumpOld() {
        return dump(this.oldHashes, false);
    }

    /**
     * Returns a String representation of the old hashes, as if they were written to disc.
     * This is exposed for debugging purposes only
     */
    public String dump() {
        return dump(this.newHashes, false);
    }

    private static String dump(HashMap<String, String> data, boolean newLine) {
        if (data.isEmpty())
            return "";

        ArrayList<String> keys = new ArrayList<>(data.keySet());
        keys.sort(null);
        StringBuilder buf = new StringBuilder((keys.size() + 2) * 64); // rough estimate of size

        for (String key : keys) {
            if (buf.length() > 0)
                buf.append('\n');
            buf.append(key).append('=').append(data.get(key));
        }

        if (newLine)
            buf.append('\n');
        return buf.toString();
    }

    public void save(File file) {
        try {
            Files.createDirectories(file.getParentFile().toPath());
            Files.write(file.toPath(), dump(this.newHashes, true).getBytes(StandardCharsets.UTF_8));
            this.saved = true;
        } catch (IOException e) {
            sneak(e);
        }
    }

    public boolean isSaved() {
        return this.saved;
    }

    private static ArrayList<File> listFiles(File path) {
        return listFiles(path, new ArrayList<>());
    }

    private static ArrayList<File> listFiles(File dir, ArrayList<File> files) {
        if (!dir.exists())
            return files;

        if (!dir.isDirectory())
            throw new IllegalArgumentException("Path must be directory: " + dir.getAbsolutePath());

        //noinspection DataFlowIssue - checked by File#isDirectory
        for (File file : dir.listFiles()) {
            if (file.isDirectory())
                files = listFiles(file, files);
            else
                files.add(file);
        }

        return files;
    }

    private String getPath(File file) {
        String path = file.getAbsolutePath();

        if (path.startsWith(this.root))
            path = path.substring(this.root.length());

        path = path.replace('\\', '/');

        if (file.isDirectory() && !path.endsWith("/"))
            path += '/';

        return path;
    }
}
