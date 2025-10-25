/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.download;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Static utilities for downloading files.
 */
public final class DownloadUtils {
    private DownloadUtils() { }

    /**
     * Downloads raw bytes from the given URL.
     *
     * @param url The URL to download from
     * @return The downloaded bytes, stored in memory
     * @throws IOException If the download failed
     */
    public static byte[] downloadBytes(String url) throws IOException {
        try (InputStream stream = DownloadUtilsImpl.connect(url)) {
            return DownloadUtilsImpl.readInputStream(stream);
        } catch (Exception e) {
            // We catch "Exception" here since it might not be an IOException.
            throw new DownloadFailedException(url, e);
        }
    }

    /**
     * Downloads raw bytes from the given URL.
     * <p>Returns {@code null} on failure.</p>
     *
     * @param url The URL to download from
     * @return The downloaded bytes, stored in memory, or {@code null} if the download failed
     * @apiNote This method will swallow any exceptions thrown by {@link #downloadBytes(String)}. For proper debugging,
     * use that method directly and handle any exceptions yourself.
     */
    public static byte @Nullable [] tryDownloadBytes(String url) {
        try {
            return downloadBytes(url);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Downloads a string from the given URL, effectively acting as {@code curl}.
     *
     * @param url The URL to download from
     * @return The downloaded string
     * @throws IOException If the download failed
     */
    public static String downloadString(String url) throws IOException {
        return new String(downloadBytes(url), StandardCharsets.UTF_8);
    }

    /**
     * Downloads a string from the given URL, effectively acting as {@code curl}.
     * <p>Returns {@code null} on failure.</p>
     *
     * @param url The URL to download from
     * @return The downloaded string, or {@code null} if the download failed
     * @apiNote This method will swallow any exceptions thrown by {@link #downloadString(String)}. For proper debugging,
     * use that method directly and handle any exceptions yourself.
     */
    public static @Nullable String tryDownloadString(String url) {
        try {
            return downloadString(url);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Downloads a file from the given URL into the target file, effectively acting as {@code wget}.
     *
     * @param target The file to download to
     * @param url    The URL to download from
     * @throws IOException If the download failed
     */
    public static void downloadFile(File target, String url) throws IOException {
        try (InputStream stream = DownloadUtilsImpl.connect(url)) {
            Path path = target.toPath();
            Files.createDirectories(path.getParent());
            Files.copy(stream, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            // We catch "Exception" here since it might not be an IOException.
            throw new IOException(url, e);
        }
    }

    /**
     * Attempts to download a file from the given URL into the target file, effectively acting as {@code wget}.
     *
     * @param target The file to download to
     * @param url    The URL to download from
     * @return {@code true} if the download was successful
     * @apiNote This method will swallow any exceptions thrown by {@link #downloadFile(File, String)}. For proper
     * debugging, use that method directly and handle any exceptions yourself.
     */
    public static boolean tryDownloadFile(File target, String url) {
        try {
            downloadFile(target, url);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static final class DownloadFailedException extends IOException {
        public DownloadFailedException(String url, Throwable cause) {
            super("Failed to download " + url, cause);
        }
    }
}
