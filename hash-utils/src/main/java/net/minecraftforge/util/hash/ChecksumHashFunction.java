/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.hash;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.zip.Checksum;

abstract class ChecksumHashFunction extends HashFunction {
    private static final String PADDING = String.format(Locale.ENGLISH, "%032d", 0);

    protected abstract Checksum getHasher();

    @Override
    public final String hash(Iterable<File> files) throws IOException {
        Checksum hasher = getHasher();
        byte[] buffer = new byte[8192];

        for (File file : files) {
            if (!file.exists())
                continue;

            try (FileInputStream fin = new FileInputStream(file)) {
                int count = -1;
                while ((count = fin.read(buffer)) != -1)
                    hasher.update(buffer, 0, count);
            }
        }
        return pad(Long.toHexString(hasher.getValue()));
    }

    @Override
    public final String hash(InputStream inputStream) throws IOException {
        Checksum hasher = getHasher();
        byte[] buffer = new byte[8192];
        int count = -1;
        while ((count = inputStream.read(buffer)) != -1)
            hasher.update(buffer, 0, count);
        return pad(Long.toHexString(hasher.getValue()));
    }

    @Override
    public final String hash(byte[] bytes) {
        Checksum hasher = getHasher();
        hasher.update(bytes, 0, bytes.length);
        return pad(Long.toHexString(hasher.getValue()));
    }

    @Override
    public final String pad(String hash) {
        return (PADDING + hash).substring(hash.length());
    }
}
