/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.data.json;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class AssetsIndex {
    public Map<String, Asset> objects;
    public boolean virtual;
    public boolean map_to_resources;

    public static class Asset {
        public String hash;
        public long size;
    }
}
