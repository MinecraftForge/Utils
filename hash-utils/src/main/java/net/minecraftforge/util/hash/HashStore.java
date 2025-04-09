/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.hash;

import net.minecraftforge.util.logging.Log;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static net.minecraftforge.util.hash.HashUtils.sneak;

@NotNullByDefault
public class HashStore {
    private static final HashFunction HASH = HashFunction.SHA1;

    private final String root;
    private final Map<String, String> oldHashes = new HashMap<>();
    private final Map<String, String> newHashes = new HashMap<>();
    private @Nullable File target;

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
                String[] split = line.split("=");
                oldHashes.put(split[0], split[1]);
            }
        } catch (IOException e) {
            Log.warn("Failed to read cache file. It will be ignored. : " + e.getMessage());
        }

        return this;
    }

    public boolean exists() {
        return this.target != null && this.target.exists();
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
                for (File f : HashUtils.listFiles(file)) {
                    String suffix = getPath(f).substring(prefix.length());
                    this.newHashes.put(key + " - " + suffix, HASH.hash(f));
                }
            } else {
                this.newHashes.put(key, HASH.hash(file));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
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
    public void clear() {
        this.newHashes.clear();
    }

    public void save() {
        if (this.target == null)
            throw new RuntimeException("HashStore.save() called without load(File) so we dont know where to save it! Use load(File) or save(File)");
        save(this.target);
    }

    public void save(File file) {
        StringBuilder buf = new StringBuilder();
        ArrayList<String> keys = new ArrayList<>(this.newHashes.keySet());
        Collections.sort(keys);

        for (String key : keys)
            buf.append(key).append('=').append(this.newHashes.get(key)).append('\n');

        try {
            Files.write(file.toPath(), buf.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            sneak(e);
        }
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
