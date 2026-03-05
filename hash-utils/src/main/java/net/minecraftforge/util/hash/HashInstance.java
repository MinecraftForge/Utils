/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.hash;

interface HashInstance {
    /**
     * Updates the current hash using the specified array of bytes.
     *
     * @param input the array of bytes.
     */
    default void update(byte[] data) {
        update(data, 0, data.length);
    }

    /**
     * Updates the current hash with the specified array of bytes.
     * @param data the byte array to update the checksum with
     * @param offset the start offset of the data
     * @param length the number of bytes to use for the update
     */
    void update(byte[] data, int offset, int length);

    /*
     * Returns the final hash in hex-format.
     */
    String finish();
}
