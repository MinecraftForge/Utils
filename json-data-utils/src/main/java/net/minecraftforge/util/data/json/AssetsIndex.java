/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.data.json;

import java.util.Map;

public class AssetsIndex {
    public Map<String, Asset> objects;

    public static class Asset {
        public String hash;
        public long size;
    }
}
