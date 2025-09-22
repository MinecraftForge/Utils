/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.hash;

import java.util.zip.Checksum;

public final class CRC32 extends ChecksumHashFunction {
    public static final CRC32 INSTANCE = new CRC32();

    @Override
    protected Checksum getHasher() {
        return new java.util.zip.CRC32();
    }

    @Override
    public String extension() {
        return "crc32";
    }
}
