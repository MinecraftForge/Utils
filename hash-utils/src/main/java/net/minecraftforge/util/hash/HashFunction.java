/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.hash;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;

public abstract class HashFunction implements Iterable<HashFunction> {
    public abstract String extension();

    public final String hash(File file) throws IOException {
        try (FileInputStream fin = new FileInputStream(file)) {
            return hash(fin);
        }
    }

    public final String hash(File... files) throws IOException {
        return hash(Arrays.asList(files));
    }

    public abstract String hash(Iterable<File> files) throws IOException;

    public final String hash(String data) {
        return hash(data == null ? new byte[0] : data.getBytes());
    }

    public abstract String hash(InputStream inputStream) throws IOException;
    public abstract String hash(byte[] bytes);

    public abstract String pad(String hash);

    public static HashFunction alder32() {
        return Adler32.INSTANCE;
    }

    public static HashFunction crc32() {
        return CRC32.INSTANCE;
    }

    public static HashFunction md5() {
        return MD5.INSTANCE;
    }

    public static HashFunction sha1() {
        return SHA1.INSTANCE;
    }

    public static HashFunction sha256() {
        return SHA256.INSTANCE;
    }

    public static HashFunction sha512() {
        return SHA512.INSTANCE;
    }

    public static HashFunction byName(String extension) {
        switch (extension.toLowerCase(Locale.ROOT)) {
            case "adler32": return Adler32.INSTANCE;
            case "crc32": return CRC32.INSTANCE;
            case "md5": return MD5.INSTANCE;
            case "sha1": return SHA1.INSTANCE;
            case "sha256": return SHA256.INSTANCE;
            case "sha512": return SHA512.INSTANCE;
            default: throw new UnsupportedOperationException("Unknown hash extension: " + extension);
        }
    }

    public static HashFunction[] values() {
        return new HashFunction[] { Adler32.INSTANCE, CRC32.INSTANCE, MD5.INSTANCE, SHA1.INSTANCE, SHA256.INSTANCE, SHA512.INSTANCE };
    }

    @Override
    public Iterator<HashFunction> iterator() {
        return new Iterator<HashFunction>() {
            private final HashFunction[] values = values();
            private int index = 0;

            @Override
            public boolean hasNext() {
                return values.length > index;
            }

            @Override
            public HashFunction next() {
                return values[index++];
            }
        };
    }
}
