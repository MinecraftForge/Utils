/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.data;

import java.io.File;

public class MCJsonUtils {
    /**
     * Gets the Minecraft directory used by Minecraft Launcher. This is the directory where the launcher stores
     * libraries, which we can use instead of downloading them again.
     *
     * @return The Minecraft directory
     */
    public static File getMCDir() {
        var userHomeDir = System.getProperty("user.home", ".");
        var mcDir = ".minecraft";
        if (OS.CURRENT == OS.WINDOWS && System.getenv("APPDATA") != null)
            return new File(System.getenv("APPDATA"), mcDir);
        else if (OS.CURRENT == OS.MACOS)
            return new File(userHomeDir, "Library/Application Support/minecraft");
        return new File(userHomeDir, mcDir);
    }
}
