/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.hash;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

abstract class MessageDigestHashFunction extends HashFunction {
    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    protected abstract MessageDigest getHasher();

    @Override
    public final String hash(Iterable<File> files) throws IOException {
        MessageDigest hasher = getHasher();
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
        return pad(bytesToHexString(hasher.digest()));
    }

    @Override
    public final String hash(InputStream inputStream) throws IOException {
        MessageDigest hasher = getHasher();
        byte[] buffer = new byte[8192];
        int count = -1;
        while ((count = inputStream.read(buffer)) != -1)
            hasher.update(buffer, 0, count);
        return pad(bytesToHexString(hasher.digest()));
    }

    @Override
    public final String hash(byte[] bytes) {
        return pad(bytesToHexString(getHasher().digest(bytes)));
    }

    private static String bytesToHexString(byte[] bytes) {
        char[] chars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            chars[j * 2] = HEX_CHARS[v >>> 4];
            chars[j * 2 + 1] = HEX_CHARS[v & 0x0F];
        }
        return new String(chars);
    }
}
