/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public final class SHA1 extends MessageDigestHashFunction {
    private static final String PADDING = String.format(Locale.ENGLISH, "%040d", 0);
    public static final SHA1 INSTANCE = new SHA1();

    @Override
    protected MessageDigest getHasher() {
        try {
            return MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String extension() {
        return "sha1";
    }

    @Override
    public String pad(String hash) {
        return (PADDING + hash).substring(hash.length());
    }
}
