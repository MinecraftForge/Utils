/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
import net.minecraftforge.util.logging.Log;

public class Main {
    public static void main(String[] args) {
        Log.info("Hello, World!");
        Log.push();
        Log.info("Hello, World!");
        Log.push();
        Log.info("Hello, World!");
        Log.pop();
        Log.info("Hello, World!");
        Log.pop();
        Log.info("Hello, World!");
    }
}
