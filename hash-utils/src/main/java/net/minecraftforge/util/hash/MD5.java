/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public final class MD5 extends MessageDigestHashFunction {
    private static final String PADDING = String.format(Locale.ENGLISH, "%032d", 0);
    public static final MD5 INSTANCE = new MD5();

    @Override
    protected MessageDigest getHasher() {
        try {
            return MessageDigest.getInstance("md5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String extension() {
        return "md5";
    }

    @Override
    public String pad(String hash) {
        return (PADDING + hash).substring(hash.length());
    }
}
