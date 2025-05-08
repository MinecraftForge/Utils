/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.download;

import net.minecraftforge.util.logging.Log;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public final class DownloadUtils {
    private static final int TIMEOUT = 5 * 1000;
    private static final int MAX_REDIRECTS = 3;

    private static URLConnection openConnection(URL url) throws IOException {
        URLConnection con = url.openConnection();
        con.setConnectTimeout(TIMEOUT);
        con.setReadTimeout(TIMEOUT);

        if (con instanceof HttpURLConnection) {
            HttpURLConnection hcon = (HttpURLConnection) con;
            hcon.setRequestProperty("User-Agent", "MinecraftForge-Utils");
            hcon.setRequestProperty("Accept", "application/json");
            hcon.setInstanceFollowRedirects(false);
        }

        return con;
    }

    private static InputStream connect(String address) throws IOException {
        URI uri;
        try {
            uri = new URI(address);
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }

        URL url;
        List<String> redirections = new ArrayList<>();
        URLConnection con;
        for (int redirects = 0; ; redirects++) {
            url = uri.toURL();
            con = openConnection(url);

            if (!(con instanceof HttpURLConnection)) break;
            HttpURLConnection hcon = (HttpURLConnection) con;

            int res = hcon.getResponseCode();
            if (res == HttpURLConnection.HTTP_MOVED_PERM || res == HttpURLConnection.HTTP_MOVED_TEMP) {
                String location = hcon.getHeaderField("Location");
                redirections.add(location);
                hcon.disconnect();

                if (redirects == MAX_REDIRECTS - 1) {
                    throw new IOException(String.format(
                        "Too many redirects: %s -- redirections: [%s]",
                        address, String.join(", ", redirections)
                    ));
                } else {
                    Log.debug("Following redirect: " + location);
                    uri = uri.resolve(location);
                }
            } else if (res == 404) {
                throw new FileNotFoundException("Returned 404: " + address);
            } else {
                break;
            }
        }

        final URLConnection connection = con;
        return connection.getInputStream();
    }

    @SuppressWarnings("unchecked")
    private static <R, E extends Throwable> R sneak(Throwable t) throws E {
        throw (E) t;
    }

    /**
     * Downloads a string from the given URL, effectively acting as {@code curl}.
     *
     * @param url The URL to download from
     * @return The downloaded string
     * @throws IOException If the download failed
     */
    public static String downloadString(String url) throws IOException {
        try (InputStream stream = connect(url);
             ByteArrayOutputStream out = new ByteArrayOutputStream()
        ) {
            // NOTE: main source uses InputStream#readAllBytes
            byte[] buf = new byte[1024];
            int n;
            while ((n = stream.read(buf)) > 0) {
                out.write(buf, 0, n);
            }

            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new DownloadFailedException(url, e);
        }
    }

    /**
     * Downloads a string from the given URL, effectively acting as {@code curl}.
     * <p>Returns {@code null} on failure.</p>
     *
     * @param url The URL to download from
     * @return The downloaded string, or {@code null} if the download failed
     */
    public static @Nullable String tryDownloadString(boolean silent, String url) {
        try {
            return downloadString(url);
        } catch (IOException e) {
            if (!silent) {
                e.printStackTrace(Log.WARN);
            }

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
        downloadFile(false, target, url);
    }

    /**
     * Downloads a file from the given URL into the target file, effectively acting as {@code wget}.
     *
     * @param silent If no log messages should be sent
     * @param target The file to download to
     * @param url    The URL to download from
     * @throws IOException If the download failed
     */
    public static void downloadFile(boolean silent, File target, String url) throws IOException {
        if (!silent) Log.quiet("Downloading " + url);

        try (InputStream stream = connect(url)) {
            Path path = target.toPath();
            Files.createDirectories(path.getParent());
            Files.copy(stream, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (DownloadFailedException e) {
            throw new IOException(url, e);
        }
    }

    /**
     * Attempts to download a file from the given URL into the target file, effectively acting as {@code wget}.
     *
     * @param target The file to download to
     * @param url    The URL to download from
     * @return {@code true} if the download was successful
     */
    public static boolean tryDownloadFile(boolean silent, File target, String url) {
        try {
            downloadFile(silent, target, url);
            return true;
        } catch (IOException e) {
            if (!silent) {
                e.printStackTrace(Log.WARN);
            }

            return false;
        }
    }

    private static final class DownloadFailedException extends IOException {
        public DownloadFailedException(String url, Throwable cause) {
            super("Failed to download " + url, cause);
        }
    }
}
