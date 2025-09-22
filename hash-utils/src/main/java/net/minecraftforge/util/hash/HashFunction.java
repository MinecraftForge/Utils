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

public abstract class HashFunction {
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

    public static HashFunction[] values() {
        return new HashFunction[] { Adler32.INSTANCE, CRC32.INSTANCE, MD5.INSTANCE, SHA1.INSTANCE, SHA256.INSTANCE, SHA512.INSTANCE };
    }
}
