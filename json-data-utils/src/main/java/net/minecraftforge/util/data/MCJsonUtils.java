/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.data;

import net.minecraftforge.util.os.OS;

import java.io.File;

public class MCJsonUtils {
    @Deprecated
    public static File getMCDir() {
        return getMCDir(OS.current());
    }

    /**
     * Gets the Minecraft directory used by Minecraft Launcher. This is the directory where the launcher stores
     * libraries, which we can use instead of downloading them again.
     *
     * @return The Minecraft directory
     */
    public static File getMCDir(OS os) {
        String userHomeDir = System.getProperty("user.home", ".");
        String mcDir = ".minecraft";
        if (os == OS.WINDOWS && System.getenv("APPDATA") != null)
            return new File(System.getenv("APPDATA"), mcDir);
        else if (os == OS.MACOS)
            return new File(userHomeDir, "Library/Application Support/minecraft");
        return new File(userHomeDir, mcDir);
    }
}
