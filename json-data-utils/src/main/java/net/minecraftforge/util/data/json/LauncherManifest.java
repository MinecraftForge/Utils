/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.data.json;

import java.net.URL;

/** Represents the launcher manifest for Minecraft versions. */
public class LauncherManifest {
    /** All Minecraft version manifest infos. */
    public VersionInfo[] versions;

    /** Represents a Minecraft version manifest info. */
    public static class VersionInfo {
        public String id;
        public URL url;
    }

    /**
     * @param version The Minecraft version
     * @return The URL for a specific version of the game
     */
    public URL getUrl(String version) {
        if (version == null || versions == null)
            return null;
        for (VersionInfo info : versions)
            if (version.equals(info.id))
                return info.url;
        return null;
    }
}
