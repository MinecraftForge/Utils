/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.download;

import net.minecraftforge.util.logging.Log;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Locale;
import java.util.function.Supplier;

public final class DownloadUtils {
    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final int MAX_REDIRECTS = 3;

    private static final HttpClient CLIENT = HttpClient
        .newBuilder()
        .connectTimeout(TIMEOUT)
        .followRedirects(HttpClient.Redirect.NEVER)
        .build();

    private static final HttpRequest.Builder REQUEST_BUILDER = HttpRequest
        .newBuilder()
        .timeout(TIMEOUT)
        .header("User-Agent", "MinecraftForge-Utils")
        .header("Accept", "application/json");

    private static InputStream connect(String address) throws IOException, InterruptedException {
        URI uri;
        try {
            uri = new URI(address);
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }

        // if not http, then try a URLConnection. we might be trying to make a file or jar connection.
        // see jdk.internal.net.http.HttpRequestBuilderImpl#checkUri
        var scheme = uri.getScheme();
        if (scheme == null || !(scheme.equalsIgnoreCase("https") || scheme.equalsIgnoreCase("http"))) {
            return uri.toURL().openStream();
        }

        var redirections = new ArrayList<String>();
        HttpResponse<InputStream> con;
        for (int redirects = 0; ; redirects++) {
            con = CLIENT.send(
                REQUEST_BUILDER.uri(uri).build(),
                info -> HttpResponse.BodySubscribers.ofInputStream()
            );

            int res = con.statusCode();
            if (res == HttpURLConnection.HTTP_MOVED_PERM || res == HttpURLConnection.HTTP_MOVED_TEMP) {
                var header = con.headers().firstValue("Location");
                if (header.isEmpty())
                    throw new IOException(String.format(
                        "No location header found in redirect response: %s -- previous redirections: [%s]",
                        uri, String.join(", ", redirections)
                    ));

                var location = header.get();
                redirections.add(location);

                if (redirects == MAX_REDIRECTS - 1) {
                    throw new IOException(String.format(
                        "Too many redirects: %s -- redirections: [%s]",
                        address, String.join(", ", redirections)
                    ));
                } else {
                    Log.debug("Following redirect: " + location);
                    uri = uri.resolve(location);
                }
            } else if (res == HttpURLConnection.HTTP_NOT_FOUND) {
                throw new FileNotFoundException("Returned 404: " + address);
            } else {
                break;
            }
        }

        return con.body();
    }

    /**
     * Downloads a string from the given URL, effectively acting as {@code curl}.
     *
     * @param url The URL to download from
     * @return The downloaded string
     * @throws IOException If the download failed
     */
    public static String downloadString(String url) throws IOException {
        try (InputStream stream = connect(url)) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
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
        } catch (Exception e) {
            throw new DownloadFailedException(url, e);
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
