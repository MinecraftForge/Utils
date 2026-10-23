/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.data.json;

import java.util.Map;

public class AssetsIndex {
    public Map<String, Asset> objects;
    // Optional, only observed in legacy.json
    // If true, assets should be copied to {AssetsRoot}/virtual/{assetIndex}/{readable name}
    // And the {game_assets} game argument will always point to this virtual directory, even if the index doesn't have this flag
    public boolean virtual = false;
    // Optional, only observed in pre-1.6.json
    // If true, assets should be copied to {GameDir}/resources/{readable name}
    public boolean map_to_resources = false;

    public static class Asset {
        public String hash;
        public long size;

        @Override
        public String toString() {
        	return "[hash=" + hash + ", size=" + size + "]";
        }
    }
}
