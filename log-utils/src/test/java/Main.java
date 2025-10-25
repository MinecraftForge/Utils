/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
import net.minecraftforge.util.logging.Logger;

public class Main {
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        Logger log = Logger.create();

        log.info("Hello, World!");
        log.push();
        log.info("Hello, World!");
        log.push();
        log.info("Hello, World!");
        log.pop();
        log.info("Hello, World!");
        log.pop();
        log.info("Hello, World!");
        System.out.println("Time: " + (System.currentTimeMillis() - start));
    }
}
