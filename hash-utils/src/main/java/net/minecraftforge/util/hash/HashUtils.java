/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.hash;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public final class HashUtils {
    /**
     * Gets the hashes of the given file using the given hash functions.
     *
     * @param file      The file to calculate hashes for
     * @param functions The hash functions to use
     * @return The hashes of the file
     *
     * @throws IOException If an error occurs while reading the file
     */
    public static String[] bulkHash(File file, HashFunction... functions) throws IOException {
        if (file == null || !file.exists())
            return null;

        String[] ret = new String[functions.length];
        for (int x = 0; x < functions.length; x++) {
            ret[x] = functions[x].hash(file);
        }
        return ret;
    }

    /**
     * Updates the hash of the given file using all hash functions. These hashes are stored as files with the same name
     * but different extension at the folder of the target.
     *
     * @param target The file to update the hash of
     * @throws IOException If an error occurs while reading the file
     * @see #updateHash(File, HashFunction...)
     */
    public static void updateHash(File target) throws IOException {
        updateHash(target, HashFunction.values());
    }

    /**
     * Updates the hash of the given file the given hash functions. These hashes are stored as files with the same name
     * but different extension at the folder of the target.
     *
     * @param target The file to update the hash of
     * @throws IOException If an error occurs while reading the file
     */
    public static void updateHash(File target, HashFunction... functions) throws IOException {
        if (!target.exists()) {
            for (HashFunction func : functions) {
                File cache = new File(target.getAbsolutePath() + "." + func.extension());
                cache.delete();
            }
        } else {
            String[] hashes = bulkHash(target, functions);
            for (int x = 0; x < functions.length; x++) {
                HashFunction func = functions[x];
                File cache = new File(target.getAbsolutePath() + "." + func.extension());
                Files.write(cache.toPath(), hashes[x].getBytes());
            }
        }
    }

    /**
     * Allows the given {@link Throwable} to be thrown without needing to declare it in the method signature or
     * arbitrarily checked at compile time.
     *
     * @param t   The throwable
     * @param <R> The type of the fake return if used as a return statement
     * @param <E> The type of the throwable
     * @throws E Unconditionally thrown
     */
    @SuppressWarnings("unchecked")
    public static <R, E extends Throwable> R sneak(Throwable t) throws E {
        throw (E) t;
    }

    private HashUtils() { }
}
