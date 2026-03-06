/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.hash;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HashStoreTest {
    private static final byte[] SHORT_EMPTY = new byte[0x10];
    private static final byte[] BULK_EMPTY = new byte[0x1000];
    private static final byte[] BULK_DATA = new byte[0x1000];
    static {
        for (int x = 0; x < BULK_DATA.length; x++)
            BULK_DATA[x] = (byte)(x & 0xFF);
    }

    private static void isSame(HashStore cache, File file) throws IOException {
        if (cache.isSame())
            return;
        System.out.println("Original: " + cache.dumpOld());
        System.out.println("Modified: " + cache.dump());
        Assertions.assertTrue(cache.isSame());
    }

    private static void isNotSame(HashStore cache, File file) throws IOException {
        if (cache.isSame())
            return;
        System.out.println("Original: " + cache.dumpOld());
        System.out.println("Modified: " + cache.dump());
        Assertions.assertFalse(cache.isSame());
    }

    @Test
    public void invalidate() throws IOException {
        File file = File.createTempFile("hash-store-test", ".dat");
        // Save a cache file
        HashStore.fromFile(file)
            .add("key", "value")
            .save();

        HashStore cache = HashStore.fromFile(file)
            .invalidate()
            .add("key", "value");

        isNotSame(cache, file);
    }

    @Test
    public void loadsFile() throws IOException {
        File file = File.createTempFile("hash-store-test", ".dat");
        // Save a cache file
        HashStore.fromFile(file)
            .add("key", "value")
            .save();

        HashStore cache = HashStore.fromFile(file)
            .add("key", "value");

        isSame(cache, file);
    }

    @Test
    public void fileEntry() throws IOException {
        File file = File.createTempFile("hash-store-test", ".dat");
        Files.write(file.toPath(), BULK_DATA);

        // Save a cache file
        HashStore.fromFile(file)
            .add(file)
            .save();

        HashStore cache = HashStore.fromFile(file)
            .add(file);

        isSame(cache, file);
    }

    @Test
    public void fileEntryTimestamp() throws IOException {
        File file = File.createTempFile("hash-store-test", ".dat");
        Files.write(file.toPath(), BULK_DATA);

        // Save a cache file
        HashStore.fromFile(file)
            .timestamps()
            .add(file)
            .save();

        HashStore cache = HashStore.fromFile(file)
            .timestamps()
            .add(file);

        isSame(cache, file);
    }

    @Test
    public void detectsChange() throws IOException {
        File file = File.createTempFile("hash-store-test", ".dat");
        Files.write(file.toPath(), BULK_DATA);

        // Save a cache file
        HashStore.fromFile(file)
            .add(file)
            .save();

        Files.write(file.toPath(), BULK_EMPTY);

        HashStore cache = HashStore.fromFile(file)
            .add(file);

        isNotSame(cache, file);
    }

    @Test
    public void detectsChangeTimestamp() throws IOException, InterruptedException {
        File file = File.createTempFile("hash-store-test", ".dat");
        Files.write(file.toPath(), BULK_DATA);

        // Save a cache file
        HashStore.fromFile(file)
            .timestamps()
            .add(file)
            .save();

        // Race condition here, we can write both files in less then 1ms which means the modified time wont change
        // There is no way to actually detect this using the 'timestamp' only case. This will have to be a 'known issue'
        // It shouldn't actually show up in the real world as I can't think of a case where there isnt *some* other processing going on tha would take more then 1ms
        // So only real option is to sleep for at least 1ms
        Thread.sleep(5);
        Files.write(file.toPath(), BULK_EMPTY);

        HashStore cache = HashStore.fromFile(file)
            .timestamps()
            .add(file);

        isNotSame(cache, file);
    }

    @Test
    public void detectsSizeChange() throws IOException {
        File file = File.createTempFile("hash-store-test", ".dat");
        Files.write(file.toPath(), BULK_DATA);

        // Save a cache file
        HashStore.fromFile(file)
            .add(file)
            .save();

        Files.write(file.toPath(), SHORT_EMPTY);

        HashStore cache = HashStore.fromFile(file)
            .add(file);

        isNotSame(cache, file);
    }

    @Test
    public void detectsSizeChangeTimestamp() throws IOException, InterruptedException {
        File file = File.createTempFile("hash-store-test", ".dat");
        Files.write(file.toPath(), BULK_DATA);

        // Save a cache file
        HashStore.fromFile(file)
            .timestamps()
            .add(file)
            .save();

        Files.write(file.toPath(), SHORT_EMPTY);

        HashStore cache = HashStore.fromFile(file)
            .timestamps()
            .add(file);

        isNotSame(cache, file);
    }
}
